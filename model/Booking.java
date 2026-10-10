package smarttourism.model;

/**
 * Model representing a Booking.
 * Corresponds to the BOOKING table in smart_tourism_db.
 * Note: total_amount is NOT stored in the database (strictly normalized 3NF);
 * it is dynamically calculated as (package_cost * number_of_people).
 */
public class Booking {
    private int bookingId;
    private int touristId;
    private String touristName;
    private int packageId;
    private String packageName;
    private String bookingDate; // "YYYY-MM-DD"
    private String travelDate;  // "YYYY-MM-DD"
    private int numberOfPeople;
    private String bookingStatus; // "Pending", "Confirmed", "Cancelled", "Completed"
    private double estimatedAmount;

    public Booking() {}

    public Booking(int bookingId, int touristId, int packageId, String bookingDate,
                   String travelDate, int numberOfPeople, String bookingStatus) {
        this.bookingId = bookingId;
        this.touristId = touristId;
        this.packageId = packageId;
        this.bookingDate = bookingDate;
        this.travelDate = travelDate;
        this.numberOfPeople = numberOfPeople;
        this.bookingStatus = bookingStatus;
    }

    public Booking(int bookingId, int touristId, int packageId, String packageName,
                   String bookingDate, String travelDate, int numberOfPeople,
                   String bookingStatus, double estimatedAmount) {
        this(bookingId, touristId, packageId, bookingDate, travelDate, numberOfPeople, bookingStatus);
        this.packageName = packageName;
        this.estimatedAmount = estimatedAmount;
    }

    public Booking(int bookingId, int touristId, String touristName, int packageId,
                   String packageName, String bookingDate, String travelDate,
                   int numberOfPeople, String bookingStatus, double estimatedAmount) {
        this(bookingId, touristId, packageId, packageName, bookingDate, travelDate, numberOfPeople, bookingStatus, estimatedAmount);
        this.touristName = touristName;
    }

    // Convenience aliases
    public int id() { return bookingId; }
    public int touristId() { return touristId; }
    public int packageId() { return packageId; }
    public String bookingDate() { return bookingDate; }
    public String travelDate() { return travelDate; }
    public int people() { return numberOfPeople; }
    public String status() { return bookingStatus; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getTouristId() { return touristId; }
    public void setTouristId(int touristId) { this.touristId = touristId; }

    public String getTouristName() { return touristName; }
    public void setTouristName(String touristName) { this.touristName = touristName; }

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getTravelDate() { return travelDate; }
    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }

    public int getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public double getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(double estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " - " + (touristName != null ? touristName : "Tourist " + touristId)
                + " (" + (packageName != null ? packageName : "Package " + packageId) + ", ₹" + String.format("%.2f", estimatedAmount) + ")";
    }
}
