package smarttourism.model;

/**
 * Model representing a Payment.
 * Corresponds to the PAYMENT table in smart_tourism_db.
 */
public class Payment {
    private int paymentId;
    private int bookingId;
    private int adminId;
    private String adminName;
    private double amount;
    private String paymentDate;   // "YYYY-MM-DD"
    private String paymentMethod; // "UPI", "Cash", "Credit Card", "Debit Card", "Net Banking"
    private String refundInfo;
    private String paymentStatus; // "Pending", "Completed", "Failed", "Refunded"

    public Payment() {}

    public Payment(int paymentId, int bookingId, int adminId, double amount,
                   String paymentDate, String paymentMethod, String refundInfo, String paymentStatus) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.adminId = adminId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.refundInfo = refundInfo;
        this.paymentStatus = paymentStatus;
    }

    public Payment(int paymentId, int bookingId, int adminId, String adminName, double amount,
                   String paymentDate, String paymentMethod, String refundInfo, String paymentStatus) {
        this(paymentId, bookingId, adminId, amount, paymentDate, paymentMethod, refundInfo, paymentStatus);
        this.adminName = adminName;
    }

    // Convenience aliases
    public int id() { return paymentId; }
    public int bookingId() { return bookingId; }
    public int adminId() { return adminId; }
    public double amount() { return amount; }
    public String date() { return paymentDate; }
    public String method() { return paymentMethod; }
    public String refundInfo() { return refundInfo; }
    public String status() { return paymentStatus; }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getRefundInfo() { return refundInfo; }
    public void setRefundInfo(String refundInfo) { this.refundInfo = refundInfo; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " - Booking #" + bookingId + " (₹" + String.format("%.2f", amount) + ", " + paymentStatus + ")";
    }
}
