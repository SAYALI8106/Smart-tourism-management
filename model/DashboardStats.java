package smarttourism.model;

/**
 * Model representing aggregated dashboard statistics.
 */
public class DashboardStats {
    private int totalDestinations;
    private int totalAttractions;
    private int totalPackages;
    private int totalBookings;
    private int totalPayments;
    private double totalRevenue;
    private int pendingBookings;
    private int confirmedBookings;
    private int completedBookings;
    private int totalTourists;
    private int totalAdmins;

    public DashboardStats() {}

    public int getTotalDestinations() { return totalDestinations; }
    public void setTotalDestinations(int totalDestinations) { this.totalDestinations = totalDestinations; }

    public int getTotalAttractions() { return totalAttractions; }
    public void setTotalAttractions(int totalAttractions) { this.totalAttractions = totalAttractions; }

    public int getTotalPackages() { return totalPackages; }
    public void setTotalPackages(int totalPackages) { this.totalPackages = totalPackages; }

    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }

    public int getTotalPayments() { return totalPayments; }
    public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(int pendingBookings) { this.pendingBookings = pendingBookings; }

    public int getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(int confirmedBookings) { this.confirmedBookings = confirmedBookings; }

    public int getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(int completedBookings) { this.completedBookings = completedBookings; }

    public int getTotalTourists() { return totalTourists; }
    public void setTotalTourists(int totalTourists) { this.totalTourists = totalTourists; }

    public int getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(int totalAdmins) { this.totalAdmins = totalAdmins; }
}
