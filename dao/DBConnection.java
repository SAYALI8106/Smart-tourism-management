package smarttourism.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Driver wrapper allowing dynamic runtime loading of MySQL Connector/J
 * even when the application is launched without classpath flags (e.g. 'java Main.java').
 */
class DriverShim implements Driver {
    private final Driver driver;

    public DriverShim(Driver driver) {
        this.driver = driver;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return driver.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return driver.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return driver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }
}

/**
 * Tier 3 - Data Access Layer
 * Manages database connectivity with flexible configuration support.
 * Loads connection properties from db.properties, environment variables,
 * or fallback defaults. Supports dynamic driver discovery so that
 * 'java Main.java' works out of the box.
 */
public final class DBConnection {

    private static String url = "jdbc:mysql://localhost:3306/smart_tourism_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "root";
    private static boolean initialized = false;

    private DBConnection() {
        // Utility class
    }

    /**
     * Initializes and reloads configuration from db.properties or environment.
     */
    public static synchronized void init() {
        Properties props = new Properties();

        // 1. Try loading from working directory or parent directories
        File[] candidateFiles = new File[] {
            new File("db.properties"),
            new File("../db.properties"),
            new File("../../db.properties"),
            new File("c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/db.properties")
        };
        for (File f : candidateFiles) {
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                    if (!props.isEmpty()) break;
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Try loading from classpath if not found on disk
        if (props.isEmpty()) {
            try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
                if (in != null) props.load(in);
            } catch (Exception ignored) {
            }
        }
        if (props.isEmpty()) {
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) props.load(in);
            } catch (Exception ignored) {
            }
        }

        // 3. Read properties with fallback
        String propUrl = props.getProperty("db.url");
        String propUser = props.getProperty("db.user");
        String propPass = props.getProperty("db.password");

        if (propUrl != null && !propUrl.trim().isEmpty()) {
            url = propUrl.trim();
        }
        if (propUser != null && !propUser.trim().isEmpty()) {
            user = propUser.trim();
        }
        if (propPass != null) {
            password = propPass.trim();
        }

        // 4. Override with Environment Variables if set
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        if (envUrl != null && !envUrl.trim().isEmpty()) url = envUrl.trim();
        if (envUser != null && !envUser.trim().isEmpty()) user = envUser.trim();
        if (envPass != null) password = envPass.trim();

        // 5. Ensure MySQL Driver is registered
        ensureDriverRegistered();

        initialized = true;
    }

    /**
     * Attempts standard driver loading; if not found on classpath,
     * dynamically locates mysql-connector-j-8.3.0.jar and registers a DriverShim.
     */
    private static void ensureDriverRegistered() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return;
        } catch (ClassNotFoundException ignored) {
        }

        // Search for the jar on disk
        File[] candidateJars = new File[] {
            new File("lib/mysql-connector-j-8.3.0.jar"),
            new File("../lib/mysql-connector-j-8.3.0.jar"),
            new File("../../lib/mysql-connector-j-8.3.0.jar"),
            new File("c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/lib/mysql-connector-j-8.3.0.jar")
        };

        for (File jar : candidateJars) {
            if (jar.exists()) {
                try {
                    URLClassLoader loader = new URLClassLoader(
                        new URL[] { jar.toURI().toURL() },
                        DBConnection.class.getClassLoader()
                    );
                    Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver", true, loader);
                    Driver realDriver = (Driver) driverClass.getDeclaredConstructor().newInstance();
                    DriverManager.registerDriver(new DriverShim(realDriver));
                    return;
                } catch (Exception ex) {
                    System.err.println("Driver auto-load failed from " + jar + ": " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Obtains a new database connection from the driver manager.
     * Callers must close the connection using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            init();
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            String diagnostic = diagnose(ex);
            throw new SQLException("Database Connection Failed: " + diagnostic, ex.getSQLState(), ex.getErrorCode(), ex);
        }
    }

    /**
     * Helper to diagnose common connection issues.
     */
    public static String diagnose(SQLException ex) {
        String msg = ex.getMessage();
        if (msg == null) return "Unknown SQL error";
        if (msg.contains("Communications link failure") || msg.contains("Connection refused")) {
            return "MySQL Server is not running on localhost:3306 or network port is unreachable.\n"
                 + "Please ensure MySQL Server is started.";
        }
        if (msg.contains("Access denied for user")) {
            return "Invalid MySQL credentials for user '" + user + "'.\n"
                 + "Please verify db.properties or your MySQL root password.";
        }
        if (msg.contains("Unknown database 'smart_tourism_db'")) {
            return "Database 'smart_tourism_db' does not exist.\n"
                 + "Please verify that the database was created.";
        }
        return msg;
    }

    public static String getUrl() {
        if (!initialized) init();
        return url;
    }

    public static String getUser() {
        if (!initialized) init();
        return user;
    }
}
