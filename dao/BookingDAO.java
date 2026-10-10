package smarttourism.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.Booking;

/**
 * Tier 3 - Data Access Layer for BOOKING table.
 * Calculates estimated_amount dynamically (cost * number_of_people) as per 3NF normalized schema.
 */
public class BookingDAO {

    public List<Booking> findAll() throws SQLException {
        String sql = "SELECT b.booking_id, b.tourist_id, CONCAT(u.first_name, ' ', u.last_name) AS tourist_name, "
                   + "       b.package_id, p.package_name, b.booking_date, b.travel_date, b.number_of_people, "
                   + "       b.booking_status, (p.cost * b.number_of_people) AS estimated_amount "
                   + "FROM booking b "
                   + "JOIN tourist t ON b.tourist_id = t.user_id "
                   + "JOIN user u ON t.user_id = u.user_id "
                   + "JOIN tour_package p ON b.package_id = p.package_id "
                   + "ORDER BY b.booking_id DESC";

        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        }
        return list;
    }

    public List<Booking> findByTouristId(int touristId) throws SQLException {
        String sql = "SELECT b.booking_id, b.tourist_id, CONCAT(u.first_name, ' ', u.last_name) AS tourist_name, "
                   + "       b.package_id, p.package_name, b.booking_date, b.travel_date, b.number_of_people, "
                   + "       b.booking_status, (p.cost * b.number_of_people) AS estimated_amount "
                   + "FROM booking b "
                   + "JOIN tourist t ON b.tourist_id = t.user_id "
                   + "JOIN user u ON t.user_id = u.user_id "
                   + "JOIN tour_package p ON b.package_id = p.package_id "
                   + "WHERE b.tourist_id = ? "
                   + "ORDER BY b.booking_id DESC";

        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, touristId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        }
        return list;
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT b.booking_id, b.tourist_id, CONCAT(u.first_name, ' ', u.last_name) AS tourist_name, "
                   + "       b.package_id, p.package_name, b.booking_date, b.travel_date, b.number_of_people, "
                   + "       b.booking_status, (p.cost * b.number_of_people) AS estimated_amount "
                   + "FROM booking b "
                   + "JOIN tourist t ON b.tourist_id = t.user_id "
                   + "JOIN user u ON t.user_id = u.user_id "
                   + "JOIN tour_package p ON b.package_id = p.package_id "
                   + "WHERE b.booking_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> search(String query) throws SQLException {
        String sql = "SELECT b.booking_id, b.tourist_id, CONCAT(u.first_name, ' ', u.last_name) AS tourist_name, "
                   + "       b.package_id, p.package_name, b.booking_date, b.travel_date, b.number_of_people, "
                   + "       b.booking_status, (p.cost * b.number_of_people) AS estimated_amount "
                   + "FROM booking b "
                   + "JOIN tourist t ON b.tourist_id = t.user_id "
                   + "JOIN user u ON t.user_id = u.user_id "
                   + "JOIN tour_package p ON b.package_id = p.package_id "
                   + "WHERE CONCAT(u.first_name, ' ', u.last_name) LIKE ? "
                   + "   OR p.package_name LIKE ? "
                   + "   OR b.booking_status LIKE ? "
                   + "   OR CAST(b.booking_id AS CHAR) LIKE ? "
                   + "ORDER BY b.booking_id DESC";

        String pattern = "%" + query + "%";
        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        }
        return list;
    }

    public void insert(Booking b) throws SQLException {
        String sql = "INSERT INTO booking(booking_id, tourist_id, package_id, booking_date, travel_date, number_of_people, booking_status) "
                   + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getBookingId());
            ps.setInt(2, b.getTouristId());
            ps.setInt(3, b.getPackageId());
            ps.setDate(4, Date.valueOf(b.getBookingDate()));
            ps.setDate(5, Date.valueOf(b.getTravelDate()));
            ps.setInt(6, b.getNumberOfPeople());
            ps.setString(7, b.getBookingStatus());
            ps.executeUpdate();
        }
    }

    public void update(Booking b) throws SQLException {
        String sql = "UPDATE booking SET tourist_id=?, package_id=?, booking_date=?, travel_date=?, number_of_people=?, booking_status=? "
                   + "WHERE booking_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getTouristId());
            ps.setInt(2, b.getPackageId());
            ps.setDate(3, Date.valueOf(b.getBookingDate()));
            ps.setDate(4, Date.valueOf(b.getTravelDate()));
            ps.setInt(5, b.getNumberOfPeople());
            ps.setString(6, b.getBookingStatus());
            ps.setInt(7, b.getBookingId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Booking ID " + b.getBookingId() + " not found.");
            }
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE booking SET booking_status=? WHERE booking_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Booking ID " + id + " not found.");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM booking WHERE booking_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Booking ID " + id + " not found.");
            }
        }
    }

    public boolean hasPayments(int bookingId) throws SQLException {
        String sql = "SELECT 1 FROM payment WHERE booking_id = ? LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean exists(int bookingId) throws SQLException {
        String sql = "SELECT 1 FROM booking WHERE booking_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getNextBookingId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(booking_id), 300) + 1 AS next_id FROM booking";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 319;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getInt("booking_id"),
            rs.getInt("tourist_id"),
            rs.getString("tourist_name"),
            rs.getInt("package_id"),
            rs.getString("package_name"),
            rs.getDate("booking_date").toString(),
            rs.getDate("travel_date").toString(),
            rs.getInt("number_of_people"),
            rs.getString("booking_status"),
            rs.getDouble("estimated_amount")
        );
    }
}
