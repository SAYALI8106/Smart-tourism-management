package smarttourism.service;

import smarttourism.model.User;

/**
 * Centralized Session and Authorization Manager.
 * Stores the authenticated user identity and role, and enforces
 * strict Role-Based Access Control (RBAC) across the service layer.
 */
public class SessionManager {

    private static User activeUser = null;

    private SessionManager() {
        // Prevent instantiation
    }

    public static synchronized void startSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot start session with null user.");
        }
        activeUser = user;
    }

    public static synchronized User getCurrentUser() {
        return activeUser;
    }

    public static synchronized int getCurrentUserId() {
        if (activeUser == null) {
            throw new IllegalStateException("No active session.");
        }
        return activeUser.getUserId();
    }

    public static synchronized String getCurrentRole() {
        return activeUser != null ? activeUser.getRole() : null;
    }

    public static synchronized boolean isLoggedIn() {
        return activeUser != null;
    }

    public static synchronized boolean isAdmin() {
        return activeUser != null && "Administrator".equalsIgnoreCase(activeUser.getRole());
    }

    public static synchronized boolean isTourist() {
        return activeUser != null && "Tourist".equalsIgnoreCase(activeUser.getRole());
    }

    public static synchronized void invalidate() {
        activeUser = null;
    }

    // --- Strict RBAC Guard Methods ---

    /**
     * Enforces that a user is currently authenticated.
     */
    public static synchronized void requireLoggedIn() throws ValidationException {
        if (!isLoggedIn()) {
            throw new ValidationException("Access Denied: Authentication required. Please log in.");
        }
    }

    /**
     * Enforces that the authenticated user holds the Administrator role.
     */
    public static synchronized void requireAdmin() throws ValidationException {
        requireLoggedIn();
        if (!isAdmin()) {
            throw new ValidationException("Access Denied: Administrator privileges are required to perform this operation.");
        }
    }

    /**
     * Enforces that the authenticated user holds the Tourist role.
     */
    public static synchronized void requireTourist() throws ValidationException {
        requireLoggedIn();
        if (!isTourist()) {
            throw new ValidationException("Access Denied: Only registered tourists can perform this action.");
        }
    }

    /**
     * Enforces that the authenticated user is either an Administrator or the specified user.
     */
    public static synchronized void requireUserOrAdmin(int targetUserId) throws ValidationException {
        requireLoggedIn();
        if (isAdmin()) {
            return;
        }
        if (getCurrentUserId() != targetUserId) {
            throw new ValidationException("Access Denied: You cannot view or modify another user's records.");
        }
    }
}
