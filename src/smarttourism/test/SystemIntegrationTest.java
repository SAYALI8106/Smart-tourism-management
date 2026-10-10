package smarttourism.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import smarttourism.dao.DBConnection;
import smarttourism.model.Booking;
import smarttourism.model.Destination;
import smarttourism.model.Payment;
import smarttourism.model.TourPackage;
import smarttourism.model.User;
import smarttourism.service.AttractionService;
import smarttourism.service.AuthService;
import smarttourism.service.BookingService;
import smarttourism.service.DashboardService;
import smarttourism.service.DestinationService;
import smarttourism.service.PackageService;
import smarttourism.service.PaymentService;
import smarttourism.service.SessionManager;
import smarttourism.service.UserService;
import smarttourism.service.ValidationException;

/**
 * Automated End-to-End System Integration Test Suite.
 * Comprehensively verifies:
 * 1. Database Connection & Schema Health
 * 2. BCrypt Authentication & Session Management
 * 3. Role-Based Access Control (RBAC) Service-Layer Guards
 * 4. Tourist Self-Registration & Duplicate Checks
 * 5. Dynamic 3NF Cost Calculation & Booking Ownership
 * 6. Payment Scoping & Administrative Capabilities
 * 7. Password Management & Session Invalidation
 */
public class SystemIntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  Smart Tourism Management System — RBAC & Integration Test Suite");
        System.out.println("==================================================================");

        AuthService authService = new AuthService();
        DestinationService destService = new DestinationService();
        AttractionService attrService = new AttractionService();
        PackageService pkgService = new PackageService();
        BookingService bookingService = new BookingService();
        PaymentService paymentService = new PaymentService();
        UserService userService = new UserService();
        DashboardService dashService = new DashboardService();

        // 1. Database Connectivity
        test("1. Database Connection to MySQL smart_tourism_db", () -> {
            try (Connection c = DBConnection.getConnection()) {
                if (c == null || c.isClosed()) throw new RuntimeException("Connection is null or closed.");
            }
        });

        // 2. Administrator Login
        test("2. Administrator Login with valid BCrypt credentials", () -> {
            User admin = authService.login("priya@example.com", "admin123");
            if (admin == null || !SessionManager.isAdmin()) {
                throw new RuntimeException("Admin login failed or role mismatch.");
            }
        });

        // 3. Invalid Password Rejection
        test("3. Invalid password rejected cleanly", () -> {
            try {
                authService.login("priya@example.com", "wrongpassword");
                throw new RuntimeException("Login should have failed with invalid password.");
            } catch (ValidationException ve) {
                // Expected
            }
        });

        // 3b. Account without credentials rejected with proper guidance
        test("3b. Account without credentials receives clear administrator contact message", () -> {
            // Temporarily insert a user without credential
            smarttourism.dao.UserDAO uDao = new smarttourism.dao.UserDAO();
            int tempId = 9999;
            try {
                uDao.insert(new User(tempId, "Unconfigured", "User", "unconfigured@example.com", "9900112233", "", "", "", "", "Tourist"), "Tourist");
                try {
                    authService.login("unconfigured@example.com", "anyPassword");
                    throw new RuntimeException("Unconfigured account should not be allowed to log in.");
                } catch (ValidationException ve) {
                    if (!ve.getMessage().contains("This account has not been configured for login")) {
                        throw new RuntimeException("Expected unconfigured account message, got: " + ve.getMessage());
                    }
                }
            } finally {
                try { uDao.delete(tempId); } catch (Exception ignored) {}
            }
        });

        // 4. Tourist Login
        test("4. Tourist Login with valid BCrypt credentials", () -> {
            User tourist = authService.login("aarav@example.com", "tourist123");
            if (tourist == null || !SessionManager.isTourist()) {
                throw new RuntimeException("Tourist login failed or role mismatch.");
            }
        });

        // 5. Tourist Self-Registration with Transaction
        final String testEmail = "testtourist" + System.currentTimeMillis() + "@example.com";
        final String testPhone = "98" + (System.currentTimeMillis() % 100000000L);
        test("5. Tourist Self-Registration creates USER, TOURIST, & USER_CREDENTIAL", () -> {
            User reg = authService.registerTourist(
                "Kavita", "Desai", testEmail, testPhone,
                "123 Sea View", "Mumbai", "Maharashtra", "India",
                "kavita123", "kavita123"
            );
            if (reg == null || reg.getUserId() <= 0) {
                throw new RuntimeException("Registration failed to create valid user.");
            }
            // Verify new user can immediately log in
            User logged = authService.login(testEmail, "kavita123");
            if (logged == null || !SessionManager.isTourist()) {
                throw new RuntimeException("Newly registered tourist could not log in.");
            }
        });

        // 6. Duplicate Email Registration Protection
        test("6. Duplicate email registration rejected", () -> {
            try {
                authService.registerTourist(
                    "Duplicate", "User", testEmail, "9999999999",
                    "Street", "City", "State", "India",
                    "pass123", "pass123"
                );
                throw new RuntimeException("Duplicate email should have been rejected.");
            } catch (ValidationException ve) {
                // Expected
            }
        });

        // 7. Tourist Browsing Permitted
        test("7. Tourist can browse destinations and packages", () -> {
            List<Destination> dests = destService.getAllDestinations();
            List<TourPackage> pkgs = pkgService.getAllPackages();
            if (dests.isEmpty() || pkgs.isEmpty()) {
                throw new RuntimeException("Destinations or packages returned empty.");
            }
        });

        // 8. RBAC Guard: Tourist CANNOT add destination
        test("8. RBAC Guard: Tourist denied from creating destination", () -> {
            try {
                Destination d = new Destination(999, "Restricted Isle", "Pacific", "Test");
                destService.addDestination(d);
                throw new RuntimeException("Tourist should NOT be allowed to add destinations!");
            } catch (ValidationException ve) {
                if (!ve.getMessage().contains("Access Denied")) {
                    throw new RuntimeException("Expected Access Denied error, got: " + ve.getMessage());
                }
            }
        });

        // 9. RBAC Guard: Tourist CANNOT add package
        test("9. RBAC Guard: Tourist denied from creating tour package", () -> {
            try {
                TourPackage p = new TourPackage(999, "Restricted Safari", 5, 25000.0);
                pkgService.addPackage(p);
                throw new RuntimeException("Tourist should NOT be allowed to create packages!");
            } catch (ValidationException ve) {
                if (!ve.getMessage().contains("Access Denied")) {
                    throw new RuntimeException("Expected Access Denied error, got: " + ve.getMessage());
                }
            }
        });

        // 10. Tourist Self-Booking & Dynamic 3NF Cost Calculation
        final int[] createdBookingId = new int[1];
        test("10. Tourist can create booking for themselves with dynamic 3NF cost", () -> {
            TourPackage pkg = pkgService.getAllPackages().get(0);
            int people = 3;
            String travelDate = LocalDate.now().plusDays(10).toString();

            Booking b = bookingService.bookForCurrentTourist(pkg.getPackageId(), travelDate, people);
            if (b == null) throw new RuntimeException("Booking creation failed.");

            createdBookingId[0] = b.getBookingId();
            double expectedCost = pkg.getCost() * people;
            if (Math.abs(b.getEstimatedAmount() - expectedCost) > 0.01) {
                throw new RuntimeException("Expected dynamic cost " + expectedCost + " but got " + b.getEstimatedAmount());
            }

            // Verify booking appears in tourist's own bookings
            List<Booking> myBookings = bookingService.getMyBookings();
            boolean found = myBookings.stream().anyMatch(bk -> bk.getBookingId() == createdBookingId[0]);
            if (!found) throw new RuntimeException("Created booking did not appear in tourist's bookings.");
        });

        // 11. Ownership Scoping: Tourist cannot access other tourist's bookings
        test("11. Ownership Scoping: Tourist cannot view another tourist's booking", () -> {
            try {
                // Booking 301 belongs to Aarav (9001). Our active session is Kavita.
                Booking b = bookingService.getBookingById(301);
                // If it returned, verify it actually belongs to current tourist or throws
                if (b.getTouristId() != SessionManager.getCurrentUserId()) {
                    throw new RuntimeException("Tourist was able to view another tourist's booking!");
                }
            } catch (ValidationException ve) {
                // Expected Access Denied
            }
        });

        // 12. Tourist Payment Scoping & Status Protection
        test("12. Tourist cannot update payment status", () -> {
            try {
                paymentService.updateStatus(401, "Refunded");
                throw new RuntimeException("Tourist should NOT be allowed to update payment status!");
            } catch (ValidationException ve) {
                if (!ve.getMessage().contains("Access Denied")) {
                    throw new RuntimeException("Expected Access Denied error, got: " + ve.getMessage());
                }
            }
        });

        // 13. Tourist Booking Cancellation
        test("13. Tourist can cancel their own eligible booking", () -> {
            bookingService.cancelBooking(createdBookingId[0]);
            Booking b = bookingService.getBookingById(createdBookingId[0]);
            if (!"Cancelled".equalsIgnoreCase(b.getBookingStatus())) {
                throw new RuntimeException("Booking status was not updated to Cancelled.");
            }
        });

        // 14. Password Change Flow
        test("14. Password change requires current password and works with BCrypt", () -> {
            // Wrong current password fails
            try {
                authService.changePassword("wrongpass", "newpass123", "newpass123");
                throw new RuntimeException("Wrong current password should be rejected.");
            } catch (ValidationException ve) {
                // Expected
            }
            // Correct current password succeeds
            authService.changePassword("kavita123", "newpass123", "newpass123");
            // Verify new password works
            User relogged = authService.login(testEmail, "newpass123");
            if (relogged == null) throw new RuntimeException("Login with new password failed.");
        });

        // 15. Administrator Capabilities
        test("15. Administrator can view all bookings and statistics", () -> {
            authService.login("priya@example.com", "admin123");
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Payment> allPayments = paymentService.getAllPayments();
            var stats = dashService.getStats();
            if (allBookings.isEmpty() || allPayments.isEmpty() || stats.getTotalRevenue() <= 0) {
                throw new RuntimeException("Admin queries returned incomplete data.");
            }
        });

        // 16. Logout & Session Invalidation
        test("16. Logout clears session and blocks protected operations", () -> {
            AuthService.logout();
            if (SessionManager.isLoggedIn()) {
                throw new RuntimeException("Session should be null after logout.");
            }
            try {
                destService.addDestination(new Destination(998, "Ghost", "Nowhere", ""));
                throw new RuntimeException("Protected operation should fail after logout.");
            } catch (ValidationException ve) {
                // Expected
            }
        });

        System.out.println("==================================================================");
        System.out.printf("  Test Summary: %d / %d Tests Passed Successfully!%n", passed, (passed + failed));
        if (failed > 0) {
            System.err.printf("  WARNING: %d tests failed!%n", failed);
        }
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, TestAction action) {
        try {
            action.run();
            System.out.println("  [PASS] " + name);
            passed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] " + name + ": " + t.getMessage());
            failed++;
        }
    }

    @FunctionalInterface
    interface TestAction {
        void run() throws Throwable;
    }
}
