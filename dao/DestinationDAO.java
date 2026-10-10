package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.Destination;

/**
 * Tier 3 - Data Access Layer for DESTINATION table.
 */
public class DestinationDAO {

    public List<Destination> findAll() throws SQLException {
        String sql = "SELECT destination_id, name, location, description FROM destination ORDER BY destination_id";
        List<Destination> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Destination(
                    rs.getInt("destination_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("description")
                ));
            }
        }
        return list;
    }

    public Destination findById(int id) throws SQLException {
        String sql = "SELECT destination_id, name, location, description FROM destination WHERE destination_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Destination(
                        rs.getInt("destination_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public List<Destination> search(String query) throws SQLException {
        String sql = "SELECT destination_id, name, location, description FROM destination "
                   + "WHERE name LIKE ? OR location LIKE ? OR description LIKE ? OR CAST(destination_id AS CHAR) LIKE ? "
                   + "ORDER BY destination_id";
        List<Destination> list = new ArrayList<>();
        String pattern = "%" + query + "%";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Destination(
                        rs.getInt("destination_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("description")
                    ));
                }
            }
        }
        return list;
    }

    public void insert(Destination d) throws SQLException {
        String sql = "INSERT INTO destination(destination_id, name, location, description) VALUES(?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, d.getDestinationId());
            ps.setString(2, d.getName());
            ps.setString(3, d.getLocation());
            ps.setString(4, d.getDescription());
            ps.executeUpdate();
        }
    }

    public void update(Destination d) throws SQLException {
        String sql = "UPDATE destination SET name=?, location=?, description=? WHERE destination_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getLocation());
            ps.setString(3, d.getDescription());
            ps.setInt(4, d.getDestinationId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Destination ID " + d.getDestinationId() + " not found.");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM destination WHERE destination_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Destination ID " + id + " not found.");
            }
        }
    }

    public boolean exists(int id) throws SQLException {
        String sql = "SELECT 1 FROM destination WHERE destination_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getNextDestinationId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(destination_id), 100) + 1 AS next_id FROM destination";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 111;
    }
}
