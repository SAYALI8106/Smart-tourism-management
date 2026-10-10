package smarttourism.model;

/**
 * Model representing the many-to-many relationship between Destination and Tour Package.
 * Corresponds to DESTINATION_PACKAGE table in smart_tourism_db.
 */
public class DestinationPackage {
    private int destinationId;
    private String destinationName;
    private int packageId;
    private String packageName;

    public DestinationPackage() {}

    public DestinationPackage(int destinationId, int packageId) {
        this.destinationId = destinationId;
        this.packageId = packageId;
    }

    public DestinationPackage(int destinationId, String destinationName, int packageId, String packageName) {
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.packageId = packageId;
        this.packageName = packageName;
    }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
}
