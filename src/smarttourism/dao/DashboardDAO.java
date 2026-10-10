package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.Booking;
import smarttourism.model.DashboardStats;

/**
 * Tier 3 - Data Access Layer for Dashboard aggregations and KPIs.
 */
public class DashboardDAO {

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        String sql = "SELECT "
                   + "  (SELECT COUNT(*) FROM destination) AS total_destinations, "
                   + "  (SELECT COUNT(*) FROM attraction) AS total_attractions, "
                   + "  (SELECT COUNT(*) FROM tour_package) AS total_packages, "
                   + "  (SELECT COUNT(*) FROM booking) AS total_bookings, "
                   + "  (SELECT COUNT(*) FROM payment) AS total_payments, "
                   + "  (SELECT COALESCE(SUM(amount), 0) FROM payment WHERE payment_status = 'Completed') AS total_revenue, "
                   + "  (SELECT COUNT(*) FROM booking WHERE booking_status = 'Pending') AS pending_bookings, "
                   + "  (SELECT COUNT(*) FROM booking WHERE booking_status = 'Confirmed') AS confirmed_bookings, "
                   + "  (SELECT COUNT(*) FROM booking WHERE booking_status = 'Completed') AS completed_bookings, "
                   + "  (SELECT COUNT(*) FROM tourist) AS total_tourists, "
                   + "  (SELECT COUNT(*) FROM administrator) AS total_admins";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setTotalDestinations(rs.getInt("total_destinations"));
                stats.setTotalAttractions(rs.getInt("total_attractions"));
                stats.setTotalPackages(rs.getInt("total_packages"));
                stats.setTotalBookings(rs.getInt("total_bookings"));
                stats.setTotalPayments(rs.getInt("total_payments"));
                stats.setTotalRevenue(rs.getDouble("total_revenue"));
                stats.setPendingBookings(rs.getInt("pending_bookings"));
                stats.setConfirmedBookings(rs.getInt("confirmed_bookings"));
                stats.setCompletedBookings(rs.getInt("completed_bookings"));
                stats.setTotalTourists(rs.getInt("total_tourists"));
                stats.setTotalAdmins(rs.getInt("total_admins"));
            }
        }
        return stats;
    }

    public List<Booking> getRecentBookings(int limit) throws SQLException {
        String sql = "SELECT b.booking_id, b.tourist_id, CONCAT(u.first_name, ' ', u.last_name) AS tourist_name, "
                   + "       b.package_id, p.package_name, b.booking_date, b.travel_date, b.number_of_people, "
                   + "       b.booking_status, (p.cost * b.number_of_people) AS estimated_amount "
                   + "FROM booking b "
                   + "JOIN tourist t ON b.tourist_id = t.user_id "
                   + "JOIN user u ON t.user_id = u.user_id "
                   + "JOIN tour_package p ON b.package_id = p.package_id "
                   + "ORDER BY b.booking_id DESC LIMIT ?";

        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Booking(
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
                    ));
                }
            }
        }
        return list;
    }
}
