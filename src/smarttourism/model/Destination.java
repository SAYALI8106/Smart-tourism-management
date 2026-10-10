package smarttourism.model;

/**
 * Model representing a tourist Destination.
 * Corresponds to the DESTINATION table in smart_tourism_db.
 */
public class Destination {
    private int destinationId;
    private String name;
    private String location;
    private String description;

    public Destination() {}

    public Destination(int destinationId, String name, String location, String description) {
        this.destinationId = destinationId;
        this.name = name;
        this.location = location;
        this.description = description;
    }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    // Convenience alias matching previous records if referenced
    public int id() { return destinationId; }
    public String name() { return name; }
    public String location() { return location; }
    public String description() { return description; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return destinationId + " - " + name + " (" + location + ")";
    }
}
