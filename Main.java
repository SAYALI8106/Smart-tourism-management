package smarttourism;

import java.awt.Font;
import java.sql.Connection;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import smarttourism.dao.DBConnection;
import smarttourism.presentation.LoginForm;

/**
 * Smart Tourism Management System (STMS)
 * Entry point for Assignment 9 desktop application.
 */
public class Main {

    public static void main(String[] args) {
        // Configure modern UI look & feel and anti-aliasing
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 13));
        } catch (Exception ignored) {
        }

        // Test database connectivity
        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("=================================================");
            System.out.println("  Smart Tourism Management System (Assignment 9)");
            System.out.println("  Successfully connected to MySQL database: " + connection.getCatalog());
            System.out.println("=================================================");
        } catch (Exception ex) {
            String msg = "Could not connect to MySQL.\n\n"
                       + "Diagnostic: " + ex.getMessage() + "\n\n"
                       + "Please verify:\n"
                       + "1. MySQL Server is running on localhost:3306\n"
                       + "2. 'db.properties' has correct username and password\n"
                       + "3. The database 'smart_tourism_db' exists.";
            System.err.println(msg);
            JOptionPane.showMessageDialog(null, msg, "Database Connection Warning", JOptionPane.WARNING_MESSAGE);
        }

        // Launch presentation layer (LoginForm)
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
}
