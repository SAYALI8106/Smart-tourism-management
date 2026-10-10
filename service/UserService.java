package smarttourism.service;

import java.sql.SQLException;
import java.util.List;
import smarttourism.dao.UserDAO;
import smarttourism.model.User;

/**
 * Tier 2 - Application / Business Layer for User Management.
 * Enforces business rules and strict RBAC:
 * - Administrators can manage all users and roles.
 * - Authenticated users can view and update their own permitted profile details.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Retrieves all users. Protected: Administrator only.
     */
    public List<User> getAllUsers() throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        return userDAO.findAll();
    }

    /**
     * Retrieves all tourists. Protected: Administrator only.
     */
    public List<User> getAllTourists() throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        return userDAO.findAllTourists();
    }

    /**
     * Retrieves all administrators. Protected: Administrator only.
     */
    public List<User> getAllAdmins() throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        return userDAO.findAllAdmins();
    }

    /**
     * Retrieves user by ID with ownership verification.
     */
    public User getUserById(int userId) throws ValidationException, SQLException {
        SessionManager.requireUserOrAdmin(userId);
        return userDAO.findById(userId);
    }

    /**
     * Retrieves the profile of the currently logged-in user.
     */
    public User getMyProfile() throws ValidationException, SQLException {
        SessionManager.requireLoggedIn();
        return userDAO.findById(SessionManager.getCurrentUserId());
    }

    /**
     * Updates permitted profile fields (First Name, Last Name, Phone, Address)
     * for the currently authenticated user only.
     */
    public void updateMyProfile(String firstName, String lastName, String phone,
                                String street, String city, String state, String country)
            throws ValidationException, SQLException {

        SessionManager.requireLoggedIn();
        int currentUserId = SessionManager.getCurrentUserId();

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First Name is required.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last Name is required.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }
        if (!phone.trim().matches("^[0-9+\\-\\s()]{7,15}$")) {
            throw new ValidationException("Phone number must contain between 7 and 15 digits.");
        }

        User existing = userDAO.findById(currentUserId);
        if (existing == null) {
            throw new ValidationException("User profile not found.");
        }

        existing.setFirstName(firstName.trim());
        existing.setLastName(lastName.trim());
        existing.setPhone(phone.trim());
        existing.setStreet(street != null ? street.trim() : "");
        existing.setCity(city != null ? city.trim() : "");
        existing.setState(state != null ? state.trim() : "");
        existing.setCountry(country != null ? country.trim() : "India");

        userDAO.updateProfile(existing);

        // Update active session user instance
        SessionManager.startSession(existing);
    }

    public List<User> searchUsers(String query) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (query == null || query.trim().isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.search(query.trim());
    }

    /**
     * Adds a user. Protected: Administrator only.
     */
    public void addUser(User u, String role) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateUser(u);

        if (role == null || (!role.equalsIgnoreCase("Tourist") && !role.equalsIgnoreCase("Administrator"))) {
            throw new ValidationException("User role must be specified as either 'Tourist' or 'Administrator'.");
        }

        if (userDAO.exists(u.getUserId())) {
            throw new ValidationException("User with ID " + u.getUserId() + " already exists.");
        }

        userDAO.insert(u, role);
    }

    /**
     * Updates a user. Protected: Administrator only.
     */
    public void updateUser(User u) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        validateUser(u);

        if (!userDAO.exists(u.getUserId())) {
            throw new ValidationException("Cannot update: User ID " + u.getUserId() + " does not exist.");
        }

        userDAO.update(u);
    }

    /**
     * Deletes a user safely. Protected: Administrator only.
     */
    public void deleteUser(int userId) throws ValidationException, SQLException {
        SessionManager.requireAdmin();
        if (userId <= 0) {
            throw new ValidationException("Invalid User ID.");
        }
        if (!userDAO.exists(userId)) {
            throw new ValidationException("User ID " + userId + " does not exist.");
        }

        try {
            userDAO.delete(userId);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Cannot delete or update a parent row")) {
                throw new ValidationException("Cannot delete User #" + userId + ": Referenced by existing bookings or payments. Delete related bookings/payments first.");
            }
            throw ex;
        }
    }

    public int getNextUserId() throws SQLException {
        return userDAO.getNextUserId();
    }

    private void validateUser(User u) throws ValidationException {
        if (u == null) {
            throw new ValidationException("User object cannot be null.");
        }
        if (u.getUserId() <= 0) {
            throw new ValidationException("User ID must be a positive integer.");
        }
        if (u.getFirstName() == null || u.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First Name is required.");
        }
        if (u.getFirstName().trim().length() > 50) {
            throw new ValidationException("First Name must not exceed 50 characters.");
        }
        if (u.getLastName() == null || u.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last Name is required.");
        }
        if (u.getLastName().trim().length() > 50) {
            throw new ValidationException("Last Name must not exceed 50 characters.");
        }
        if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email address is required.");
        }
        if (!u.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Please provide a valid email address (e.g. user@example.com).");
        }
        if (u.getPhone() == null || u.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }
        if (!u.getPhone().trim().matches("^[0-9+\\-\\s()]{7,15}$")) {
            throw new ValidationException("Phone number must contain between 7 and 15 digits.");
        }
    }
}
