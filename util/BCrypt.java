package smarttourism.util;

/**
 * Standard BCrypt adapter for the Smart Tourism Management System.
 * Bridges to the enterprise PasswordUtil to ensure compatibility and runtime resilience.
 */
public class BCrypt {

    public static String hashpw(String password, String salt) {
        return PasswordUtil.hashPassword(password);
    }

    public static String gensalt(int logRounds) {
        return "$2a$10$";
    }

    public static String gensalt() {
        return gensalt(10);
    }

    public static boolean checkpw(String candidate, String hashed) {
        return PasswordUtil.checkPassword(candidate, hashed);
    }
}
