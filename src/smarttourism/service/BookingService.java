package smarttourism.service;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import smarttourism.dao.BookingDAO;
import smarttourism.dao.PackageDAO;
import smarttourism.dao.UserDAO;
import smarttourism.model.Booking;
import smarttourism.model.TourPackage;
import smarttourism.model.User;

/**
 * Tier 2 - Application / Business Layer for Booking Management.
 * Enforces business rules and strict RBAC:
 * - Admin can view all bookings and update statuses.
 * - Tourists can only view and cancel their own bookings.
 * - Dynamic cost calculation (package cost * people) adheres to 3NF schema.
 * - Travel date cannot precede booking date.
 */
public class BookingService {

    public static final List<String> ALLOWED_STATUSES = Arrays.asList("Pending", "Confirmed", "Cancelled", "Completed");

    private final BookingDAO bookingDAO = new BookingDAO();
    private final PackageDAO packageDAO = new PackageDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Retrieves all bookings. Protected: Administrator only.
     */
    public List<Booking> getAllBookings() throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        return bookingDAO.findAll();
    }

    /**
     * Retrieves bookings belonging strictly to the currently authenticated tourist.
     */
    public List<Booking> getMyBookings() throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        return bookingDAO.findByTouristId(SessionManager.getCurrentUserId());
    }

    /**
     * Retrieves a booking by ID. Verifies ownership if caller is a tourist.
     */
    public Booking getBookingById(int id) throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        Booking b = bookingDAO.findById(id);
        if (b == null) {
            return null;
        }
        SessionManager.requireUserOrAdmin(b.getTouristId());
        return b;
    }

    /**
     * Stored Function: CountTouristBookings from Assignment 7.
     */
    public int getTouristBookingCount(int touristId) throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        return bookingDAO.countTouristBookings(touristId);
    }

    public List<Booking> searchBookings(String query) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (query == null || query.trim().isEmpty()) {
            return bookingDAO.findAll();
        }
        return bookingDAO.search(query.trim());
    }

    public double calculateEstimatedCost(int packageId, int people) throws SQLException {
        if (people <= 0) return 0.0;
        TourPackage pkg = packageDAO.findById(packageId);
        if (pkg == null) return 0.0;
        return pkg.getCost() * people;
    }

    /**
     * Books a tour package for the currently authenticated tourist.
     * Prevents tourist ID forgery by pulling identity strictly from active session.
     */
    public Booking bookForCurrentTourist(int packageId, String travelDate, int numberOfPeople)
            throws ValidationException, SQLException {

        SessionManager.requireLoggedIn();
        int touristId = SessionManager.getCurrentUserId();

        int nextBookingId = bookingDAO.getNextBookingId();
        String today = LocalDate.now().toString();

        Booking b = new Booking(
            nextBookingId,
            touristId,
            SessionManager.getCurrentUser().getFullName(),
            packageId,
            "",
            today,
            travelDate,
            numberOfPeople,
            "Confirmed",
            0.0
        );

        addBooking(b);
        return bookingDAO.findById(nextBookingId);
    }

    /**
     * Adds a booking. If caller is Tourist, enforces tourist_id = session.user_id.
     */
    public void addBooking(Booking b) throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();

        // Enforce ownership: tourists cannot submit someone else's ID
        if (SessionManager.isTourist()) {
            b.setTouristId(SessionManager.getCurrentUserId());
        }

        validateBooking(b);

        if (bookingDAO.exists(b.getBookingId())) {
            throw new ValidationException("Booking ID " + b.getBookingId() + " already exists.");
        }

        bookingDAO.insert(b);
    }

    /**
     * Cancels a booking. Tourists can only cancel their own eligible bookings.
     */
    public void cancelBooking(int bookingId) throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        Booking b = bookingDAO.findById(bookingId);
        if (b == null) {
            throw new ValidationException("Booking #" + bookingId + " not found.");
        }

        // Enforce ownership
        SessionManager.requireUserOrAdmin(b.getTouristId());

        if ("Cancelled".equalsIgnoreCase(b.getBookingStatus())) {
            throw new ValidationException("Booking #" + bookingId + " is already cancelled.");
        }
        if ("Completed".equalsIgnoreCase(b.getBookingStatus())) {
            throw new ValidationException("Cannot cancel Booking #" + bookingId + ": Trip has already been completed.");
        }

        bookingDAO.updateStatus(bookingId, "Cancelled");
    }

    /**
     * Updates an existing booking. Protected: Administrator only.
     */
    public void updateBooking(Booking b) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateBooking(b);

        if (!bookingDAO.exists(b.getBookingId())) {
            throw new ValidationException("Cannot update: Booking ID " + b.getBookingId() + " does not exist.");
        }

        bookingDAO.update(b);
    }

    /**
     * Updates booking status. Protected: Administrator only.
     */
    public void updateStatus(int bookingId, String newStatus) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (!ALLOWED_STATUSES.contains(newStatus)) {
            throw new ValidationException("Invalid status: '" + newStatus + "'. Allowed: " + ALLOWED_STATUSES);
        }
        if (!bookingDAO.exists(bookingId)) {
            throw new ValidationException("Booking ID " + bookingId + " not found.");
        }
        bookingDAO.updateStatus(bookingId, newStatus);
    }

    /**
     * Deletes a booking. Protected: Administrator only.
     */
    public void deleteBooking(int id) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (id <= 0) {
            throw new ValidationException("Invalid Booking ID.");
        }
        if (!bookingDAO.exists(id)) {
            throw new ValidationException("Booking ID " + id + " does not exist.");
        }
        if (bookingDAO.hasPayments(id)) {
            throw new ValidationException("Cannot delete Booking #" + id + ": Payments are associated with this booking. Remove or refund payments first.");
        }

        bookingDAO.delete(id);
    }

    public int getNextBookingId() throws SQLException {
        return bookingDAO.getNextBookingId();
    }

    public List<User> getTourists() throws SQLException {
        return userDAO.findAllTourists();
    }

    public List<TourPackage> getPackages() throws SQLException {
        return packageDAO.findAll();
    }

    private void validateBooking(Booking b) throws ValidationException, SQLException {
        if (b == null) {
            throw new ValidationException("Booking object cannot be null.");
        }
        if (b.getBookingId() <= 0) {
            throw new ValidationException("Booking ID must be a positive integer.");
        }
        if (b.getTouristId() <= 0) {
            throw new ValidationException("Please select a valid Tourist.");
        }
        if (!userDAO.exists(b.getTouristId())) {
            throw new ValidationException("Tourist ID " + b.getTouristId() + " does not exist in the database.");
        }
        if (b.getPackageId() <= 0) {
            throw new ValidationException("Please select a valid Tour Package.");
        }
        if (!packageDAO.exists(b.getPackageId())) {
            throw new ValidationException("Tour Package ID " + b.getPackageId() + " does not exist in the database.");
        }
        if (b.getNumberOfPeople() <= 0) {
            throw new ValidationException("Number of people must be at least 1.");
        }
        if (b.getBookingDate() == null || b.getBookingDate().trim().isEmpty()) {
            throw new ValidationException("Booking Date is required (YYYY-MM-DD).");
        }
        if (b.getTravelDate() == null || b.getTravelDate().trim().isEmpty()) {
            throw new ValidationException("Travel Date is required (YYYY-MM-DD).");
        }

        Date bookDate;
        Date travDate;
        try {
            bookDate = Date.valueOf(b.getBookingDate().trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid Booking Date format. Use YYYY-MM-DD (e.g. 2026-10-15).");
        }
        try {
            travDate = Date.valueOf(b.getTravelDate().trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid Travel Date format. Use YYYY-MM-DD (e.g. 2026-11-01).");
        }

        if (travDate.before(bookDate)) {
            throw new ValidationException("Travel date (" + travDate + ") cannot be earlier than booking date (" + bookDate + ").");
        }

        if (b.getBookingStatus() == null || !ALLOWED_STATUSES.contains(b.getBookingStatus())) {
            throw new ValidationException("Invalid booking status: '" + b.getBookingStatus() + "'. Allowed: " + ALLOWED_STATUSES);
        }
    }
}
