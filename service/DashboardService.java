package smarttourism.service;

import java.sql.SQLException;
import java.util.List;
import smarttourism.dao.DashboardDAO;
import smarttourism.model.Booking;
import smarttourism.model.DashboardStats;

/**
 * Tier 2 - Application / Business Layer for Dashboard Metrics.
 */
public class DashboardService {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardStats getStats() throws SQLException {
        return dashboardDAO.getDashboardStats();
    }

    public List<Booking> getRecentBookings(int limit) throws SQLException {
        if (limit <= 0) limit = 10;
        return dashboardDAO.getRecentBookings(limit);
    }
}
