package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.User;

/**
 * Tier 3 - Data Access Layer for USER, TOURIST, ADMINISTRATOR, and USER_CREDENTIAL tables.
 * Employs transactions for multi-table inserts/deletions to maintain referential integrity.
 * Strictly uses prepared statements to prevent SQL injection.
 */
public class UserDAO {

    public List<User> findAll() throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, "
                   + "       u.street, u.city, u.state, u.country, "
                   + "       CASE "
                   + "         WHEN a.user_id IS NOT NULL THEN 'Administrator' "
                   + "         WHEN t.user_id IS NOT NULL THEN 'Tourist' "
                   + "         ELSE 'User' "
                   + "       END AS role "
                   + "FROM user u "
                   + "LEFT JOIN tourist t ON u.user_id = t.user_id "
                   + "LEFT JOIN administrator a ON u.user_id = a.user_id "
                   + "ORDER BY u.user_id";

        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        }
        return list;
    }

    public List<User> findAllTourists() throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, "
                   + "       u.street, u.city, u.state, u.country, 'Tourist' AS role "
                   + "FROM user u "
                   + "JOIN tourist t ON u.user_id = t.user_id "
                   + "ORDER BY u.user_id";

        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        }
        return list;
    }

    public List<User> findAllAdmins() throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, "
                   + "       u.street, u.city, u.state, u.country, 'Administrator' AS role "
                   + "FROM user u "
                   + "JOIN administrator a ON u.user_id = a.user_id "
                   + "ORDER BY u.user_id";

        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        }
        return list;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, "
                   + "       u.street, u.city, u.state, u.country, "
                   + "       CASE "
                   + "         WHEN a.user_id IS NOT NULL THEN 'Administrator' "
                   + "         WHEN t.user_id IS NOT NULL THEN 'Tourist' "
                   + "         ELSE 'User' "
                   + "       END AS role "
                   + "FROM user u "
                   + "LEFT JOIN tourist t ON u.user_id = t.user_id "
                   + "LEFT JOIN administrator a ON u.user_id = a.user_id "
                   + "WHERE u.user_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, "
                   + "       u.street, u.city, u.state, u.country, "
                   + "       CASE "
                   + "         WHEN a.user_id IS NOT NULL THEN 'Administrator' "
                   + "         WHEN t.user_id IS NOT NULL THEN 'Tourist' "
                   + "         ELSE 'User' "
                   + "       END AS role "
                   + "FROM user u "
                   + "LEFT JOIN tourist t ON u.user_id = t.user_id "
                   + "LEFT JOIN administrator a ON u.user_id = a.user_id "
                   + "WHERE LOWER(u.email) = LOWER(?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findByEmailOrId(String identifier) throws SQLException {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String clean = identifier.trim();
        // If numeric, try finding by ID first
        if (clean.matches("^\\d+$")) {
            try {
                int id = Integer.parseInt(clean);
                User u = findById(id);
                if (u != null) return u;
            } catch (NumberFormatException ignored) {}
        }
        return findByEmail(clean);
    }

    public List<User> search(String query) throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, "
                   + "       u.street, u.city, u.state, u.country, "
                   + "       CASE "
                   + "         WHEN a.user_id IS NOT NULL THEN 'Administrator' "
                   + "         WHEN t.user_id IS NOT NULL THEN 'Tourist' "
                   + "         ELSE 'User' "
                   + "       END AS role "
                   + "FROM user u "
                   + "LEFT JOIN tourist t ON u.user_id = t.user_id "
                   + "LEFT JOIN administrator a ON u.user_id = a.user_id "
                   + "WHERE u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ? "
                   + "   OR u.city LIKE ? OR CAST(u.user_id AS CHAR) LIKE ? "
                   + "ORDER BY u.user_id";

        String pattern = "%" + query + "%";
        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        }
        return list;
    }

    // --- Credential Access Methods ---

    public String getPasswordHash(int userId) throws SQLException {
        String sql = "SELECT password_hash FROM user_credential WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        }
        return null;
    }

    public boolean hasCredential(int userId) throws SQLException {
        String sql = "SELECT 1 FROM user_credential WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void saveCredential(int userId, String passwordHash) throws SQLException {
        String sql = "INSERT INTO user_credential (user_id, password_hash) VALUES (?, ?) "
                   + "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), updated_at = CURRENT_TIMESTAMP";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        }
    }

    public void updatePassword(int userId, String passwordHash) throws SQLException {
        String sql = "UPDATE user_credential SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0) {
                // If row didn't exist, insert it
                saveCredential(userId, passwordHash);
            }
        }
    }

    // --- Registration & Transactional Methods ---

    /**
     * Registers a new Tourist in a single ACID transaction across USER, TOURIST, and USER_CREDENTIAL.
     */
    public void registerTourist(User u, String passwordHash) throws SQLException {
        String insertUserSql = "INSERT INTO user(user_id, first_name, last_name, email, phone, street, city, state, country) "
                             + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertTouristSql = "INSERT INTO tourist(user_id) VALUES(?)";
        String insertCredSql = "INSERT INTO user_credential(user_id, password_hash) VALUES(?, ?)";

        try (Connection c = DBConnection.getConnection()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(insertUserSql)) {
                    ps.setInt(1, u.getUserId());
                    ps.setString(2, u.getFirstName());
                    ps.setString(3, u.getLastName());
                    ps.setString(4, u.getEmail());
                    ps.setString(5, u.getPhone());
                    ps.setString(6, u.getStreet());
                    ps.setString(7, u.getCity());
                    ps.setString(8, u.getState());
                    ps.setString(9, u.getCountry());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(insertTouristSql)) {
                    ps.setInt(1, u.getUserId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(insertCredSql)) {
                    ps.setInt(1, u.getUserId());
                    ps.setString(2, passwordHash);
                    ps.executeUpdate();
                }

                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Inserts user and corresponding role record in a transaction (Administrative provision).
     */
    public void insert(User u, String role) throws SQLException {
        String insertUserSql = "INSERT INTO user(user_id, first_name, last_name, email, phone, street, city, state, country) "
                             + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertRoleSql = "Administrator".equalsIgnoreCase(role)
                             ? "INSERT INTO administrator(user_id) VALUES(?)"
                             : "INSERT INTO tourist(user_id) VALUES(?)";

        try (Connection c = DBConnection.getConnection()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement psUser = c.prepareStatement(insertUserSql)) {
                    psUser.setInt(1, u.getUserId());
                    psUser.setString(2, u.getFirstName());
                    psUser.setString(3, u.getLastName());
                    psUser.setString(4, u.getEmail());
                    psUser.setString(5, u.getPhone());
                    psUser.setString(6, u.getStreet());
                    psUser.setString(7, u.getCity());
                    psUser.setString(8, u.getState());
                    psUser.setString(9, u.getCountry());
                    psUser.executeUpdate();
                }

                try (PreparedStatement psRole = c.prepareStatement(insertRoleSql)) {
                    psRole.setInt(1, u.getUserId());
                    psRole.executeUpdate();
                }

                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public void update(User u) throws SQLException {
        String sql = "UPDATE user SET first_name=?, last_name=?, email=?, phone=?, street=?, city=?, state=?, country=? "
                   + "WHERE user_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFirstName());
            ps.setString(2, u.getLastName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getStreet());
            ps.setString(6, u.getCity());
            ps.setString(7, u.getState());
            ps.setString(8, u.getCountry());
            ps.setInt(9, u.getUserId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("User ID " + u.getUserId() + " not found.");
            }
        }
    }

    /**
     * Updates only permitted profile fields for a user (cannot alter role or ID).
     */
    public void updateProfile(User u) throws SQLException {
        String sql = "UPDATE user SET first_name=?, last_name=?, phone=?, street=?, city=?, state=?, country=? "
                   + "WHERE user_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFirstName());
            ps.setString(2, u.getLastName());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getStreet());
            ps.setString(5, u.getCity());
            ps.setString(6, u.getState());
            ps.setString(7, u.getCountry());
            ps.setInt(8, u.getUserId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("User ID " + u.getUserId() + " not found.");
            }
        }
    }

    public void delete(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // Delete from user_credential first if exists
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM user_credential WHERE user_id=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                // Delete from role tables
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM tourist WHERE user_id=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM administrator WHERE user_id=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                // Delete from user table
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM user WHERE user_id=?")) {
                    ps.setInt(1, userId);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("User ID " + userId + " not found.");
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public boolean exists(int userId) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE LOWER(email) = LOWER(?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean phoneExists(String phone) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE phone = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getNextUserId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(user_id), 9000) + 1 AS next_id FROM user";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 9013;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("street"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("country"),
            rs.getString("role")
        );
    }
}
