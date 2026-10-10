package smarttourism.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Tour Package.
 * Corresponds to the TOUR_PACKAGE table in smart_tourism_db.
 */
public class TourPackage {
    private int packageId;
    private String packageName;
    private int duration; // in days
    private double cost;
    private List<String> destinationNames = new ArrayList<>();

    public TourPackage() {}

    public TourPackage(int packageId, String packageName, int duration, double cost) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.duration = duration;
        this.cost = cost;
    }

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }

    // Convenience aliases
    public int id() { return packageId; }
    public String name() { return packageName; }
    public int duration() { return duration; }
    public double cost() { return cost; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public List<String> getDestinationNames() { return destinationNames; }
    public void setDestinationNames(List<String> destinationNames) { this.destinationNames = destinationNames; }

    @Override
    public String toString() {
        return packageId + " - " + packageName + " (" + duration + " Days, ₹" + String.format("%.2f", cost) + ")";
    }
}
