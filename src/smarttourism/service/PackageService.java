package smarttourism.service;

import java.sql.SQLException;
import java.util.List;
import smarttourism.dao.DestinationPackageDAO;
import smarttourism.dao.PackageDAO;
import smarttourism.model.Destination;
import smarttourism.model.TourPackage;

/**
 * Tier 2 - Application / Business Layer for Tour Package Management.
 * Enforces Administrator privileges for package creation, updates, and destination associations.
 */
public class PackageService {

    private final PackageDAO packageDAO = new PackageDAO();
    private final DestinationPackageDAO destPackageDAO = new DestinationPackageDAO();

    public List<TourPackage> getAllPackages() throws SQLException {
        return packageDAO.findAll();
    }

    public TourPackage getPackageById(int id) throws SQLException {
        return packageDAO.findById(id);
    }

    public List<TourPackage> searchPackages(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return packageDAO.findAll();
        }
        return packageDAO.search(query.trim());
    }

    /**
     * Adds a new tour package. Protected: Administrator only.
     */
    public void addPackage(TourPackage p) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validatePackage(p);

        if (packageDAO.exists(p.getPackageId())) {
            throw new ValidationException("Tour Package ID " + p.getPackageId() + " already exists.");
        }

        packageDAO.insert(p);
    }

    /**
     * Updates an existing tour package. Protected: Administrator only.
     */
    public void updatePackage(TourPackage p) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validatePackage(p);

        if (!packageDAO.exists(p.getPackageId())) {
            throw new ValidationException("Cannot update: Tour Package ID " + p.getPackageId() + " does not exist.");
        }

        packageDAO.update(p);
    }

    /**
     * Deletes a tour package. Protected: Administrator only.
     */
    public void deletePackage(int id) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (id <= 0) {
            throw new ValidationException("Invalid Package ID.");
        }
        if (!packageDAO.exists(id)) {
            throw new ValidationException("Package ID " + id + " does not exist.");
        }

        try {
            packageDAO.delete(id);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Cannot delete or update a parent row")) {
                throw new ValidationException("Cannot delete Tour Package #" + id + ": Active bookings or destination links reference this package.");
            }
            throw ex;
        }
    }

    public List<Destination> getDestinationsForPackage(int packageId) throws SQLException {
        return destPackageDAO.getDestinationsForPackage(packageId);
    }

    /**
     * Links a destination to a package. Protected: Administrator only.
     */
    public void linkDestination(int destinationId, int packageId) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (destPackageDAO.mappingExists(destinationId, packageId)) {
            throw new ValidationException("Destination is already linked to this package.");
        }
        destPackageDAO.addMapping(destinationId, packageId);
    }

    /**
     * Unlinks a destination from a package. Protected: Administrator only.
     */
    public void unlinkDestination(int destinationId, int packageId) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        destPackageDAO.removeMapping(destinationId, packageId);
    }

    public int getNextPackageId() throws SQLException {
        return packageDAO.getNextPackageId();
    }

    private void validatePackage(TourPackage p) throws ValidationException {
        if (p == null) {
            throw new ValidationException("Tour Package object cannot be null.");
        }
        if (p.getPackageId() <= 0) {
            throw new ValidationException("Package ID must be a positive integer.");
        }
        if (p.getPackageName() == null || p.getPackageName().trim().isEmpty()) {
            throw new ValidationException("Package Name is required.");
        }
        if (p.getDuration() <= 0) {
            throw new ValidationException("Duration must be at least 1 day.");
        }
        if (p.getCost() < 0) {
            throw new ValidationException("Package cost cannot be negative.");
        }
    }
}
