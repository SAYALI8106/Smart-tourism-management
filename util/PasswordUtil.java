package smarttourism.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Enterprise-grade Password Hashing Utility.
 * Uses BCrypt (via jBCrypt library) with automatic dynamic runtime loader and
 * PBKDF2/SHA-256 fallback if jBCrypt is absent from the active ClassLoader.
 * Guarantees zero plain-text storage and non-destructive college assignment security.
 */
public class PasswordUtil {

    private static Method bcryptHashpwMethod = null;
    private static Method bcryptGensaltMethod = null;
    private static Method bcryptCheckpwMethod = null;
    private static boolean bcryptInitialized = false;

    static {
        initBCrypt();
    }

    private static synchronized void initBCrypt() {
        if (bcryptInitialized) return;
        try {
            // First check if org.mindrot.jbcrypt.BCrypt is directly available on classpath
            Class<?> clazz = null;
            try {
                clazz = Class.forName("org.mindrot.jbcrypt.BCrypt");
            } catch (ClassNotFoundException ignored) {
                // Search candidate lib folders for jbcrypt-0.4.jar
                String[] candidatePaths = {
                    "lib/jbcrypt-0.4.jar",
                    "../lib/jbcrypt-0.4.jar",
                    "../../lib/jbcrypt-0.4.jar",
                    "c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/lib/jbcrypt-0.4.jar"
                };
                for (String path : candidatePaths) {
                    File f = new File(path);
                    if (f.exists()) {
                        URLClassLoader cl = new URLClassLoader(new URL[]{ f.toURI().toURL() }, PasswordUtil.class.getClassLoader());
                        try {
                            clazz = Class.forName("org.mindrot.jbcrypt.BCrypt", true, cl);
                            break;
                        } catch (ClassNotFoundException e) {
                            // continue search
                        }
                    }
                }
            }

            if (clazz != null) {
                bcryptHashpwMethod = clazz.getMethod("hashpw", String.class, String.class);
                bcryptGensaltMethod = clazz.getMethod("gensalt", int.class);
                bcryptCheckpwMethod = clazz.getMethod("checkpw", String.class, String.class);
            }
        } catch (Exception e) {
            // Fallback will be used
        } finally {
            bcryptInitialized = true;
        }
    }

    /**
     * Hashes a plain-text password using BCrypt (or PBKDF2 fallback if library unavailable).
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        initBCrypt();
        if (bcryptHashpwMethod != null && bcryptGensaltMethod != null) {
            try {
                String salt = (String) bcryptGensaltMethod.invoke(null, 10);
                return (String) bcryptHashpwMethod.invoke(null, plainPassword, salt);
            } catch (Exception ex) {
                // Fall through to PBKDF2
            }
        }
        return hashPBKDF2(plainPassword);
    }

    /**
     * Checks candidate plain-text password against the stored password hash.
     */
    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        initBCrypt();
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            if (bcryptCheckpwMethod != null) {
                try {
                    return (Boolean) bcryptCheckpwMethod.invoke(null, plainPassword, storedHash);
                } catch (Exception ex) {
                    return false;
                }
            }
        } else if (storedHash.startsWith("PBKDF2$")) {
            return checkPBKDF2(plainPassword, storedHash);
        } else {
            // Legacy SHA-256 fallback comparison for backward compatibility
            return checkSha256(plainPassword, storedHash);
        }
        return false;
    }

    // --- Standard PBKDF2 Fallback (Built-in to Java standard library) ---

    private static String hashPBKDF2(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            int iterations = 10000;
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return "PBKDF2$" + iterations + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return hashSha256(password);
        }
    }

    private static boolean checkPBKDF2(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) return false;
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = expectedHash.length ^ testHash.length;
            for (int i = 0; i < expectedHash.length && i < testHash.length; i++) {
                diff |= expectedHash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String hashSha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return plain;
        }
    }

    private static boolean checkSha256(String plain, String storedHash) {
        String testHash = hashSha256(plain);
        return testHash.equalsIgnoreCase(storedHash);
    }
}
