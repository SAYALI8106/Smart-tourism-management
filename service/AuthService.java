package smarttourism.service;

import java.sql.SQLException;
import java.util.List;
import smarttourism.dao.UserDAO;
import smarttourism.model.User;
import smarttourism.util.PasswordUtil;

/**
 * Tier 2 - Application / Business Layer: Authentication and Session Service.
 * Implements secure login with BCrypt hashing, tourist self-registration,
 * password change, and centralized session control.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticates a user by email or user ID and verifies password with BCrypt.
     */
    public User login(String emailOrId, String password) throws ValidationException, SQLException {
        if (emailOrId == null || emailOrId.trim().isEmpty()) {
            throw new ValidationException("Please enter your Email or User ID.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Please enter your password.");
        }

        User user = userDAO.findByEmailOrId(emailOrId.trim());
        if (user == null) {
            throw new ValidationException("Invalid credentials. Please verify your email/ID and password.");
        }

        String storedHash = userDAO.getPasswordHash(user.getUserId());
        if (storedHash == null || storedHash.isEmpty()) {
            throw new ValidationException("No credentials found for this account. Please use First-Time Password Setup to activate your account.");
        }

        boolean matched = PasswordUtil.checkPassword(password, storedHash);
        if (!matched) {
            throw new ValidationException("Invalid credentials. Please verify your email/ID and password.");
        }

        // Start active authenticated session
        SessionManager.startSession(user);
        return user;
    }

    /**
     * Tourist self-registration with full validation, unique email enforcement,
     * and secure BCrypt credential storage in a single transaction.
     */
    public User registerTourist(String firstName, String lastName, String email, String phone,
                                String street, String city, String state, String country,
                                String password, String confirmPassword)
            throws ValidationException, SQLException {

        // Validate basic names
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First Name is required.");
        }
        if (firstName.trim().length() > 50) {
            throw new ValidationException("First Name must not exceed 50 characters.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last Name is required.");
        }
        if (lastName.trim().length() > 50) {
            throw new ValidationException("Last Name must not exceed 50 characters.");
        }

        // Validate email format and uniqueness
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email address is required.");
        }
        String cleanEmail = email.trim();
        if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Please enter a valid email address (e.g. name@example.com).");
        }
        if (userDAO.emailExists(cleanEmail)) {
            throw new ValidationException("An account with email '" + cleanEmail + "' is already registered. Please login.");
        }

        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }
        String cleanPhone = phone.trim();
        if (!cleanPhone.matches("^[0-9+\\-\\s()]{7,15}$")) {
            throw new ValidationException("Please provide a valid phone number (7-15 digits).");
        }
        if (userDAO.phoneExists(cleanPhone)) {
            throw new ValidationException("Phone number '" + cleanPhone + "' is already registered with another account.");
        }

        // Validate password & confirmation
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Password confirmation does not match. Please re-enter.");
        }

        int nextId = userDAO.getNextUserId();
        User newUser = new User(
            nextId,
            firstName.trim(),
            lastName.trim(),
            cleanEmail,
            cleanPhone,
            street != null ? street.trim() : "",
            city != null ? city.trim() : "",
            state != null ? state.trim() : "",
            country != null ? country.trim() : "India",
            "Tourist"
        );

        String passwordHash = PasswordUtil.hashPassword(password);
        userDAO.registerTourist(newUser, passwordHash);

        return newUser;
    }

    /**
     * Changes password for the currently logged-in user. Requires verification of the current password.
     */
    public void changePassword(String currentPassword, String newPassword, String confirmPassword)
            throws ValidationException, SQLException {

        SessionManager.requireLoggedIn();
        int currentUserId = SessionManager.getCurrentUserId();

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new ValidationException("Please enter your current password.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("New password confirmation does not match.");
        }

        String storedHash = userDAO.getPasswordHash(currentUserId);
        if (storedHash != null && !PasswordUtil.checkPassword(currentPassword, storedHash)) {
            throw new ValidationException("Current password is incorrect.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        userDAO.updatePassword(currentUserId, newHash);
    }

    /**
     * First-time password setup for existing database accounts that have no credential record.
     */
    public void setupFirstTimePassword(String emailOrId, String newPassword, String confirmPassword)
            throws ValidationException, SQLException {

        if (emailOrId == null || emailOrId.trim().isEmpty()) {
            throw new ValidationException("Please enter your Email or User ID.");
        }
        User user = userDAO.findByEmailOrId(emailOrId.trim());
        if (user == null) {
            throw new ValidationException("User not found in system.");
        }
        if (userDAO.hasCredential(user.getUserId())) {
            throw new ValidationException("This account already has credentials configured. Please use regular login.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Password confirmation does not match.");
        }

        String hash = PasswordUtil.hashPassword(newPassword);
        userDAO.saveCredential(user.getUserId(), hash);
    }

    public static User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return SessionManager.isLoggedIn();
    }

    public static boolean isAdmin() {
        return SessionManager.isAdmin();
    }

    public static boolean isTourist() {
        return SessionManager.isTourist();
    }

    public static void logout() {
        SessionManager.invalidate();
    }

    public List<User> getAvailableAdmins() throws SQLException {
        return userDAO.findAllAdmins();
    }

    public List<User> getAvailableTourists() throws SQLException {
        return userDAO.findAllTourists();
    }
}
