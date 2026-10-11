package smarttourism.test;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import smarttourism.model.User;
import smarttourism.presentation.Dashboard;
import smarttourism.presentation.DestinationForm;
import smarttourism.presentation.LoginForm;
import smarttourism.presentation.PaymentForm;
import smarttourism.presentation.TouristDashboard;
import smarttourism.service.AuthService;

/**
 * Utility to programmatically capture high-resolution screenshots of the UI forms
 * for lab assignment documentation and submission guides.
 */
public class ScreenshotCaptureUtil {

    private static final String OUTPUT_DIR = "screenshots";

    public static void main(String[] args) throws Exception {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        System.out.println("=================================================");
        System.out.println("  Capturing STMS Assignment 9 Demonstration Screens");
        System.out.println("=================================================");

        AuthService authService = new AuthService();

        // 1. Capture Login Form
        captureFrame("1_login_screen.png", () -> new LoginForm(), 580, 640);

        // 2. Admin Login & Dashboard
        User admin = authService.login("priya@example.com", "admin123");
        captureFrame("2_admin_dashboard.png", () -> new Dashboard(admin), 1100, 720);

        // 3. Admin Destination Management
        captureFrame("3_admin_destination_management.png", () -> new DestinationForm(), 1100, 700);

        // 4. Admin Payment Management
        captureFrame("4_admin_payment_management.png", () -> new PaymentForm(), 1100, 700);

        // 5. Tourist Login & Dashboard
        User tourist = authService.login("aarav@example.com", "tourist123");
        captureFrame("5_tourist_dashboard.png", () -> new TouristDashboard(tourist), 1120, 720);

        // 6. Tourist My Bookings
        captureFrame("6_tourist_my_bookings.png", () -> {
            TouristDashboard td = new TouristDashboard(tourist);
            td.getTabbedPane().setSelectedIndex(4);
            return td;
        }, 1120, 720);

        // 7. Tourist My Payments
        captureFrame("7_tourist_my_payments.png", () -> {
            TouristDashboard td = new TouristDashboard(tourist);
            td.getTabbedPane().setSelectedIndex(5);
            return td;
        }, 1120, 720);

        System.out.println("=================================================");
        System.out.println("  All demonstration screenshots saved to: /screenshots");
        System.out.println("=================================================");
    }

    private static void captureFrame(String filename, FrameSupplier supplier, int width, int height) throws Exception {
        final JFrame[] holder = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = supplier.get();
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.toFront();
            holder[0] = frame;
        });

        Thread.sleep(1200); // Allow data threads to populate tables and UI to layout

        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = holder[0];
                BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                frame.printAll(g2);
                g2.dispose();

                File out = new File(OUTPUT_DIR, filename);
                ImageIO.write(image, "PNG", out);
                System.out.println("  [CAPTURED] " + out.getAbsolutePath());
                frame.dispose();
            } catch (Exception e) {
                System.err.println("  [ERROR] Failed capturing " + filename + ": " + e.getMessage());
            }
        });
    }

    @FunctionalInterface
    interface FrameSupplier {
        JFrame get();
    }
}
