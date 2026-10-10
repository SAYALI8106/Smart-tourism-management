package smarttourism.model;

/**
 * Model representing an Attraction with composite primary key (destination_id, attraction_id).
 * Corresponds to the ATTRACTION table in smart_tourism_db.
 */
public class Attraction {
    private int destinationId;
    private String destinationName;
    private int attractionId;
    private String name;
    private String type;
    private double entryFee;
    private String description;
    private String closingTime;   // e.g. "17:00:00"
    private String operatingTime; // e.g. "09:00 - 17:00"

    public Attraction() {}

    public Attraction(int destinationId, int attractionId, String name, String type,
                      double entryFee, String description, String closingTime, String operatingTime) {
        this.destinationId = destinationId;
        this.attractionId = attractionId;
        this.name = name;
        this.type = type;
        this.entryFee = entryFee;
        this.description = description;
        this.closingTime = closingTime;
        this.operatingTime = operatingTime;
    }

    public Attraction(int destinationId, String destinationName, int attractionId, String name,
                      String type, double entryFee, String description, String closingTime, String operatingTime) {
        this(destinationId, attractionId, name, type, entryFee, description, closingTime, operatingTime);
        this.destinationName = destinationName;
    }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public int getAttractionId() { return attractionId; }
    public void setAttractionId(int attractionId) { this.attractionId = attractionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getEntryFee() { return entryFee; }
    public void setEntryFee(double entryFee) { this.entryFee = entryFee; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }

    public String getOperatingTime() { return operatingTime; }
    public void setOperatingTime(String operatingTime) { this.operatingTime = operatingTime; }

    @Override
    public String toString() {
        return name + " (" + (destinationName != null ? destinationName : "Dest " + destinationId) + ")";
    }
}
