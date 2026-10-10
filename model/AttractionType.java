package smarttourism.model;

/**
 * Model representing an Attraction Type category.
 * Corresponds to the ATTRACTION_TYPE table in smart_tourism_db.
 */
public class AttractionType {
    private String type;
    private String typeDescription;

    public AttractionType() {}

    public AttractionType(String type, String typeDescription) {
        this.type = type;
        this.typeDescription = typeDescription;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }

    @Override
    public String toString() {
        return type;
    }
}
