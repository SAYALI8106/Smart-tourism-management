package smarttourism.service;

import java.sql.SQLException;
import java.util.List;
import smarttourism.dao.DestinationDAO;
import smarttourism.model.Destination;

/**
 * Tier 2 - Application / Business Layer for Destination Management.
 * Strictly enforces Role-Based Access Control (RBAC):
 * - Read operations are accessible to all authenticated users (Tourists & Administrators).
 * - Mutation operations (Add, Update, Delete) require Administrator privileges.
 */
public class DestinationService {

    private final DestinationDAO destinationDAO = new DestinationDAO();

    public List<Destination> getAllDestinations() throws SQLException {
        return destinationDAO.findAll();
    }

    public Destination getDestinationById(int id) throws SQLException {
        return destinationDAO.findById(id);
    }

    public List<Destination> searchDestinations(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return destinationDAO.findAll();
        }
        return destinationDAO.search(query.trim());
    }

    /**
     * Creates a new destination. Protected: Administrator only.
     */
    public void addDestination(Destination d) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateDestination(d);

        if (destinationDAO.exists(d.getDestinationId())) {
            throw new ValidationException("Destination with ID " + d.getDestinationId() + " already exists.");
        }

        destinationDAO.insert(d);
    }

    /**
     * Updates an existing destination. Protected: Administrator only.
     */
    public void updateDestination(Destination d) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateDestination(d);

        if (!destinationDAO.exists(d.getDestinationId())) {
            throw new ValidationException("Cannot update: Destination ID " + d.getDestinationId() + " does not exist.");
        }

        destinationDAO.update(d);
    }

    /**
     * Deletes a destination safely. Protected: Administrator only.
     */
    public void deleteDestination(int id) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (id <= 0) {
            throw new ValidationException("Invalid Destination ID.");
        }
        if (!destinationDAO.exists(id)) {
            throw new ValidationException("Destination ID " + id + " does not exist.");
        }

        try {
            destinationDAO.delete(id);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Cannot delete or update a parent row")) {
                throw new ValidationException("Cannot delete Destination #" + id + ": It is currently referenced by attractions or tour packages. Delete associated records first.");
            }
            throw ex;
        }
    }

    public int getNextDestinationId() throws SQLException {
        return destinationDAO.getNextDestinationId();
    }

    private void validateDestination(Destination d) throws ValidationException {
        if (d == null) {
            throw new ValidationException("Destination object cannot be null.");
        }
        if (d.getDestinationId() <= 0) {
            throw new ValidationException("Destination ID must be a positive integer.");
        }
        if (d.getName() == null || d.getName().trim().isEmpty()) {
            throw new ValidationException("Destination Name is required.");
        }
        if (d.getName().trim().length() > 100) {
            throw new ValidationException("Destination Name must not exceed 100 characters.");
        }
        if (d.getLocation() == null || d.getLocation().trim().isEmpty()) {
            throw new ValidationException("Destination Location is required.");
        }
        if (d.getLocation().trim().length() > 150) {
            throw new ValidationException("Location must not exceed 150 characters.");
        }
    }
}
