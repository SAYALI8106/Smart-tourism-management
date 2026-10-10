package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.Attraction;

/**
 * Tier 3 - Data Access Layer for ATTRACTION table.
 * Accurately models and respects composite primary key (destination_id, attraction_id).
 */
public class AttractionDAO {

    public List<Attraction> findAll() throws SQLException {
        String sql = "SELECT a.destination_id, d.name AS destination_name, a.attraction_id, "
                   + "       a.name, a.type, a.entry_fee, a.description, a.closing_time, a.operating_time "
                   + "FROM attraction a "
                   + "JOIN destination d ON a.destination_id = d.destination_id "
                   + "ORDER BY a.destination_id, a.attraction_id";

        List<Attraction> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAttraction(rs));
            }
        }
        return list;
    }

    public List<Attraction> findByDestination(int destinationId) throws SQLException {
        String sql = "SELECT a.destination_id, d.name AS destination_name, a.attraction_id, "
                   + "       a.name, a.type, a.entry_fee, a.description, a.closing_time, a.operating_time "
                   + "FROM attraction a "
                   + "JOIN destination d ON a.destination_id = d.destination_id "
                   + "WHERE a.destination_id = ? "
                   + "ORDER BY a.attraction_id";

        List<Attraction> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAttraction(rs));
                }
            }
        }
        return list;
    }

    public Attraction findByCompositeKey(int destinationId, int attractionId) throws SQLException {
        String sql = "SELECT a.destination_id, d.name AS destination_name, a.attraction_id, "
                   + "       a.name, a.type, a.entry_fee, a.description, a.closing_time, a.operating_time "
                   + "FROM attraction a "
                   + "JOIN destination d ON a.destination_id = d.destination_id "
                   + "WHERE a.destination_id = ? AND a.attraction_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setInt(2, attractionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAttraction(rs);
                }
            }
        }
        return null;
    }

    public List<Attraction> search(String query) throws SQLException {
        String sql = "SELECT a.destination_id, d.name AS destination_name, a.attraction_id, "
                   + "       a.name, a.type, a.entry_fee, a.description, a.closing_time, a.operating_time "
                   + "FROM attraction a "
                   + "JOIN destination d ON a.destination_id = d.destination_id "
                   + "WHERE a.name LIKE ? OR d.name LIKE ? OR a.type LIKE ? OR a.description LIKE ? "
                   + "ORDER BY a.destination_id, a.attraction_id";

        String pattern = "%" + query + "%";
        List<Attraction> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAttraction(rs));
                }
            }
        }
        return list;
    }

    public void insert(Attraction a) throws SQLException {
        String sql = "INSERT INTO attraction(destination_id, attraction_id, name, type, entry_fee, description, closing_time, operating_time) "
                   + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getDestinationId());
            ps.setInt(2, a.getAttractionId());
            ps.setString(3, a.getName());
            ps.setString(4, a.getType());
            ps.setDouble(5, a.getEntryFee());
            ps.setString(6, a.getDescription());
            if (a.getClosingTime() != null && !a.getClosingTime().trim().isEmpty()) {
                String ct = a.getClosingTime().trim();
                if (ct.length() == 5) ct += ":00"; // convert HH:mm to HH:mm:ss
                ps.setTime(7, Time.valueOf(ct));
            } else {
                ps.setNull(7, java.sql.Types.TIME);
            }
            ps.setString(8, a.getOperatingTime());
            ps.executeUpdate();
        }
    }

    public void update(Attraction a) throws SQLException {
        String sql = "UPDATE attraction SET name=?, type=?, entry_fee=?, description=?, closing_time=?, operating_time=? "
                   + "WHERE destination_id=? AND attraction_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getType());
            ps.setDouble(3, a.getEntryFee());
            ps.setString(4, a.getDescription());
            if (a.getClosingTime() != null && !a.getClosingTime().trim().isEmpty()) {
                String ct = a.getClosingTime().trim();
                if (ct.length() == 5) ct += ":00";
                ps.setTime(5, Time.valueOf(ct));
            } else {
                ps.setNull(5, java.sql.Types.TIME);
            }
            ps.setString(6, a.getOperatingTime());
            ps.setInt(7, a.getDestinationId());
            ps.setInt(8, a.getAttractionId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Attraction not found for Destination " + a.getDestinationId() + ", Attraction " + a.getAttractionId());
            }
        }
    }

    public void delete(int destinationId, int attractionId) throws SQLException {
        String sql = "DELETE FROM attraction WHERE destination_id=? AND attraction_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setInt(2, attractionId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Attraction not found for Destination " + destinationId + ", Attraction " + attractionId);
            }
        }
    }

    public int getNextAttractionId(int destinationId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(attraction_id), 0) + 1 AS next_id FROM attraction WHERE destination_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("next_id");
            }
        }
        return 1;
    }

    private Attraction mapAttraction(ResultSet rs) throws SQLException {
        Time ct = rs.getTime("closing_time");
        return new Attraction(
            rs.getInt("destination_id"),
            rs.getString("destination_name"),
            rs.getInt("attraction_id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getDouble("entry_fee"),
            rs.getString("description"),
            ct != null ? ct.toString() : "",
            rs.getString("operating_time")
        );
    }
}
