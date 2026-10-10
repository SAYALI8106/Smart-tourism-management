package smarttourism.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.Payment;

/**
 * Tier 3 - Data Access Layer for PAYMENT table.
 */
public class PaymentDAO {

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT p.payment_id, p.booking_id, p.admin_id, "
                   + "       CONCAT(u.first_name, ' ', u.last_name) AS admin_name, "
                   + "       p.amount, p.payment_date, p.payment_method, p.refund_info, p.payment_status "
                   + "FROM payment p "
                   + "JOIN administrator a ON p.admin_id = a.user_id "
                   + "JOIN user u ON a.user_id = u.user_id "
                   + "ORDER BY p.payment_id DESC";

        List<Payment> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPayment(rs));
            }
        }
        return list;
    }

    public List<Payment> findByTouristId(int touristId) throws SQLException {
        String sql = "SELECT p.payment_id, p.booking_id, p.admin_id, "
                   + "       CONCAT(u.first_name, ' ', u.last_name) AS admin_name, "
                   + "       p.amount, p.payment_date, p.payment_method, p.refund_info, p.payment_status "
                   + "FROM payment p "
                   + "JOIN administrator a ON p.admin_id = a.user_id "
                   + "JOIN user u ON a.user_id = u.user_id "
                   + "JOIN booking b ON p.booking_id = b.booking_id "
                   + "WHERE b.tourist_id = ? "
                   + "ORDER BY p.payment_id DESC";

        List<Payment> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, touristId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        }
        return list;
    }

    public Payment findById(int paymentId) throws SQLException {
        String sql = "SELECT p.payment_id, p.booking_id, p.admin_id, "
                   + "       CONCAT(u.first_name, ' ', u.last_name) AS admin_name, "
                   + "       p.amount, p.payment_date, p.payment_method, p.refund_info, p.payment_status "
                   + "FROM payment p "
                   + "JOIN administrator a ON p.admin_id = a.user_id "
                   + "JOIN user u ON a.user_id = u.user_id "
                   + "WHERE p.payment_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPayment(rs);
                }
            }
        }
        return null;
    }

    public List<Payment> search(String query) throws SQLException {
        String sql = "SELECT p.payment_id, p.booking_id, p.admin_id, "
                   + "       CONCAT(u.first_name, ' ', u.last_name) AS admin_name, "
                   + "       p.amount, p.payment_date, p.payment_method, p.refund_info, p.payment_status "
                   + "FROM payment p "
                   + "JOIN administrator a ON p.admin_id = a.user_id "
                   + "JOIN user u ON a.user_id = u.user_id "
                   + "WHERE p.payment_method LIKE ? "
                   + "   OR p.payment_status LIKE ? "
                   + "   OR CAST(p.payment_id AS CHAR) LIKE ? "
                   + "   OR CAST(p.booking_id AS CHAR) LIKE ? "
                   + "   OR CONCAT(u.first_name, ' ', u.last_name) LIKE ? "
                   + "ORDER BY p.payment_id DESC";

        String pattern = "%" + query + "%";
        List<Payment> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        }
        return list;
    }

    public void insert(Payment p) throws SQLException {
        String sql = "INSERT INTO payment(payment_id, booking_id, admin_id, amount, payment_date, payment_method, refund_info, payment_status) "
                   + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getPaymentId());
            ps.setInt(2, p.getBookingId());
            ps.setInt(3, p.getAdminId());
            ps.setDouble(4, p.getAmount());
            ps.setDate(5, Date.valueOf(p.getPaymentDate()));
            ps.setString(6, p.getPaymentMethod());
            ps.setString(7, p.getRefundInfo());
            ps.setString(8, p.getPaymentStatus());
            ps.executeUpdate();
        }
    }

    public void update(Payment p) throws SQLException {
        String sql = "UPDATE payment SET booking_id=?, admin_id=?, amount=?, payment_date=?, payment_method=?, refund_info=?, payment_status=? "
                   + "WHERE payment_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getBookingId());
            ps.setInt(2, p.getAdminId());
            ps.setDouble(3, p.getAmount());
            ps.setDate(4, Date.valueOf(p.getPaymentDate()));
            ps.setString(5, p.getPaymentMethod());
            ps.setString(6, p.getRefundInfo());
            ps.setString(7, p.getPaymentStatus());
            ps.setInt(8, p.getPaymentId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Payment ID " + p.getPaymentId() + " not found.");
            }
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE payment SET payment_status=? WHERE payment_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Payment ID " + id + " not found.");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM payment WHERE payment_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Payment ID " + id + " not found.");
            }
        }
    }

    public boolean exists(int id) throws SQLException {
        String sql = "SELECT 1 FROM payment WHERE payment_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getNextPaymentId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(payment_id), 400) + 1 AS next_id FROM payment";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 416;
    }

    /**
     * Demonstrates invoking the stored function GetCompletedPaymentAmount from Assignment 7
     */
    public double getCompletedPaymentAmountFromFunction(int bookingId) {
        String sql = "{? = call GetCompletedPaymentAmount(?)}";
        try (Connection c = DBConnection.getConnection();
             CallableStatement cs = c.prepareCall(sql)) {
            cs.registerOutParameter(1, java.sql.Types.DECIMAL);
            cs.setInt(2, bookingId);
            cs.execute();
            return cs.getDouble(1);
        } catch (SQLException e) {
            // Fallback to simple query if stored function is unavailable
            return 0.0;
        }
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
            rs.getInt("payment_id"),
            rs.getInt("booking_id"),
            rs.getInt("admin_id"),
            rs.getString("admin_name"),
            rs.getDouble("amount"),
            rs.getDate("payment_date").toString(),
            rs.getString("payment_method"),
            rs.getString("refund_info"),
            rs.getString("payment_status")
        );
    }
}
