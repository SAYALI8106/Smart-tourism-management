package smarttourism.service;

import java.sql.SQLException;
import java.util.List;
import smarttourism.dao.AttractionDAO;
import smarttourism.dao.AttractionTypeDAO;
import smarttourism.dao.DestinationDAO;
import smarttourism.model.Attraction;
import smarttourism.model.AttractionType;

/**
 * Tier 2 - Application / Business Layer for Attraction Management.
 * Accurately validates and operates on composite key (destination_id, attraction_id).
 * Enforces Administrator privileges on mutation operations.
 */
public class AttractionService {

    private final AttractionDAO attractionDAO = new AttractionDAO();
    private final DestinationDAO destinationDAO = new DestinationDAO();
    private final AttractionTypeDAO attractionTypeDAO = new AttractionTypeDAO();

    public List<Attraction> getAllAttractions() throws SQLException {
        return attractionDAO.findAll();
    }

    public List<Attraction> getAttractionsByDestination(int destinationId) throws SQLException {
        return attractionDAO.findByDestination(destinationId);
    }

    public Attraction getAttractionByKey(int destinationId, int attractionId) throws SQLException {
        return attractionDAO.findByCompositeKey(destinationId, attractionId);
    }

    public List<Attraction> searchAttractions(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return attractionDAO.findAll();
        }
        return attractionDAO.search(query.trim());
    }

    public List<AttractionType> getAllAttractionTypes() throws SQLException {
        return attractionTypeDAO.findAll();
    }

    /**
     * Adds a new attraction. Protected: Administrator only.
     */
    public void addAttraction(Attraction a) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateAttraction(a);

        if (!destinationDAO.exists(a.getDestinationId())) {
            throw new ValidationException("Destination ID " + a.getDestinationId() + " does not exist.");
        }

        Attraction existing = attractionDAO.findByCompositeKey(a.getDestinationId(), a.getAttractionId());
        if (existing != null) {
            throw new ValidationException("Attraction ID " + a.getAttractionId() + " already exists for Destination ID " + a.getDestinationId() + ".");
        }

        attractionDAO.insert(a);
    }

    /**
     * Updates an attraction. Protected: Administrator only.
     */
    public void updateAttraction(Attraction a) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateAttraction(a);

        Attraction existing = attractionDAO.findByCompositeKey(a.getDestinationId(), a.getAttractionId());
        if (existing == null) {
            throw new ValidationException("Cannot update: Attraction ID " + a.getAttractionId() + " not found under Destination ID " + a.getDestinationId() + ".");
        }

        attractionDAO.update(a);
    }

    /**
     * Deletes an attraction. Protected: Administrator only.
     */
    public void deleteAttraction(int destinationId, int attractionId) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        Attraction existing = attractionDAO.findByCompositeKey(destinationId, attractionId);
        if (existing == null) {
            throw new ValidationException("Attraction not found for Destination " + destinationId + ", Attraction " + attractionId);
        }

        attractionDAO.delete(destinationId, attractionId);
    }

    public int getNextAttractionId(int destinationId) throws SQLException {
        return attractionDAO.getNextAttractionId(destinationId);
    }

    private void validateAttraction(Attraction a) throws ValidationException {
        if (a == null) {
            throw new ValidationException("Attraction object cannot be null.");
        }
        if (a.getDestinationId() <= 0) {
            throw new ValidationException("Destination ID must be a valid positive number.");
        }
        if (a.getAttractionId() <= 0) {
            throw new ValidationException("Attraction ID must be a valid positive number.");
        }
        if (a.getName() == null || a.getName().trim().isEmpty()) {
            throw new ValidationException("Attraction Name is required.");
        }
        if (a.getEntryFee() < 0) {
            throw new ValidationException("Entry fee cannot be negative (must be >= 0).");
        }
        if (a.getClosingTime() != null && !a.getClosingTime().trim().isEmpty()) {
            String ct = a.getClosingTime().trim();
            if (!ct.matches("^([01]?\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$")) {
                throw new ValidationException("Closing time must be in HH:mm or HH:mm:ss format (e.g. 18:30:00).");
            }
        }
    }
}
