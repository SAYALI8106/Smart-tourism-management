package smarttourism.service;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import smarttourism.dao.BookingDAO;
import smarttourism.dao.PaymentDAO;
import smarttourism.dao.UserDAO;
import smarttourism.model.Booking;
import smarttourism.model.Payment;
import smarttourism.model.User;

/**
 * Tier 2 - Application / Business Layer for Payment and Refund Management.
 * Strictly enforces Role-Based Access Control (RBAC):
 * - Administrators can review all payments, process refunds, and record/update payment statuses.
 * - Tourists can only view payments associated with their own bookings.
 * - Enforces business rules: amount > 0, valid payment methods, and referential integrity.
 */
public class PaymentService {

    public static final List<String> ALLOWED_METHODS = Arrays.asList("UPI", "Cash", "Credit Card", "Debit Card", "Net Banking");
    public static final List<String> ALLOWED_STATUSES = Arrays.asList("Pending", "Completed", "Failed", "Refunded");

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Retrieves all payments. Protected: Administrator only.
     */
    public List<Payment> getAllPayments() throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        return paymentDAO.findAll();
    }

    /**
     * Retrieves payments associated only with the currently authenticated tourist's bookings.
     */
    public List<Payment> getMyPayments() throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        return paymentDAO.findByTouristId(SessionManager.getCurrentUserId());
    }

    /**
     * Retrieves a payment by ID with ownership verification for tourists.
     */
    public Payment getPaymentById(int id) throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        Payment p = paymentDAO.findById(id);
        if (p == null) {
            return null;
        }

        if (SessionManager.isTourist()) {
            Booking b = bookingDAO.findById(p.getBookingId());
            if (b == null || b.getTouristId() != SessionManager.getCurrentUserId()) {
                throw new ValidationException("Access Denied: You cannot view another tourist's payment details.");
            }
        }
        return p;
    }

    public List<Payment> searchPayments(String query) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (query == null || query.trim().isEmpty()) {
            return paymentDAO.findAll();
        }
        return paymentDAO.search(query.trim());
    }

    /**
     * Records a new payment. Protected: Administrator only.
     */
    public void addPayment(Payment p) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validatePayment(p);

        if (paymentDAO.exists(p.getPaymentId())) {
            throw new ValidationException("Payment ID " + p.getPaymentId() + " already exists.");
        }

        paymentDAO.insert(p);
    }

    /**
     * Updates an existing payment. Protected: Administrator only.
     */
    public void updatePayment(Payment p) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validatePayment(p);

        if (!paymentDAO.exists(p.getPaymentId())) {
            throw new ValidationException("Cannot update: Payment ID " + p.getPaymentId() + " does not exist.");
        }

        paymentDAO.update(p);
    }

    /**
     * Updates payment status. Protected: Administrator only.
     */
    public void updateStatus(int paymentId, String status) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new ValidationException("Invalid payment status: '" + status + "'. Allowed: " + ALLOWED_STATUSES);
        }
        if (!paymentDAO.exists(paymentId)) {
            throw new ValidationException("Payment ID " + paymentId + " does not exist.");
        }
        paymentDAO.updateStatus(paymentId, status);
    }

    /**
     * Processes and records a refund for a payment. Protected: Administrator only.
     */
    public void processRefund(int paymentId, String refundInfo) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        Payment p = paymentDAO.findById(paymentId);
        if (p == null) {
            throw new ValidationException("Payment ID " + paymentId + " does not exist.");
        }
        if ("Refunded".equalsIgnoreCase(p.getPaymentStatus())) {
            throw new ValidationException("Payment #" + paymentId + " has already been refunded.");
        }

        p.setPaymentStatus("Refunded");
        p.setRefundInfo(refundInfo != null && !refundInfo.trim().isEmpty() ? refundInfo.trim() : "Refund processed by administrator");
        paymentDAO.update(p);
    }

    /**
     * Deletes a payment record. Protected: Administrator only.
     */
    public void deletePayment(int id) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (id <= 0) {
            throw new ValidationException("Invalid Payment ID.");
        }
        if (!paymentDAO.exists(id)) {
            throw new ValidationException("Payment ID " + id + " does not exist.");
        }
        paymentDAO.delete(id);
    }

    public int getNextPaymentId() throws SQLException {
        return paymentDAO.getNextPaymentId();
    }

    public List<Booking> getBookings() throws SQLException {
        return bookingDAO.findAll();
    }

    public List<User> getAdministrators() throws SQLException {
        return userDAO.findAllAdmins();
    }

    public double getCompletedPaymentAmountForBooking(int bookingId) {
        return paymentDAO.getCompletedPaymentAmountFromFunction(bookingId);
    }

    private void validatePayment(Payment p) throws ValidationException, SQLException {
        if (p == null) {
            throw new ValidationException("Payment object cannot be null.");
        }
        if (p.getPaymentId() <= 0) {
            throw new ValidationException("Payment ID must be a positive integer.");
        }
        if (p.getBookingId() <= 0) {
            throw new ValidationException("Please select a valid Booking.");
        }
        if (!bookingDAO.exists(p.getBookingId())) {
            throw new ValidationException("Booking ID " + p.getBookingId() + " does not exist.");
        }
        if (p.getAdminId() <= 0) {
            throw new ValidationException("Please select a valid Administrator.");
        }
        if (!userDAO.exists(p.getAdminId())) {
            throw new ValidationException("Administrator ID " + p.getAdminId() + " does not exist.");
        }
        if (p.getAmount() <= 0) {
            throw new ValidationException("Payment amount must be greater than zero.");
        }
        if (p.getPaymentDate() == null || p.getPaymentDate().trim().isEmpty()) {
            throw new ValidationException("Payment Date is required (YYYY-MM-DD).");
        }
        try {
            Date.valueOf(p.getPaymentDate().trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid Payment Date format. Use YYYY-MM-DD (e.g. 2026-10-15).");
        }
        if (p.getPaymentMethod() == null || !ALLOWED_METHODS.contains(p.getPaymentMethod())) {
            throw new ValidationException("Invalid Payment Method: '" + p.getPaymentMethod() + "'. Allowed: " + ALLOWED_METHODS);
        }
        if (p.getPaymentStatus() == null || !ALLOWED_STATUSES.contains(p.getPaymentStatus())) {
            throw new ValidationException("Invalid Payment Status: '" + p.getPaymentStatus() + "'. Allowed: " + ALLOWED_STATUSES);
        }
    }
}
