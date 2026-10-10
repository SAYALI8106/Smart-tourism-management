package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.AttractionType;

/**
 * Tier 3 - Data Access Layer for ATTRACTION_TYPE table.
 */
public class AttractionTypeDAO {

    public List<AttractionType> findAll() throws SQLException {
        String sql = "SELECT type, type_description FROM attraction_type ORDER BY type";
        List<AttractionType> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AttractionType(
                    rs.getString("type"),
                    rs.getString("type_description")
                ));
            }
        }
        return list;
    }

    public void insert(AttractionType at) throws SQLException {
        String sql = "INSERT INTO attraction_type(type, type_description) VALUES(?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, at.getType());
            ps.setString(2, at.getTypeDescription());
            ps.executeUpdate();
        }
    }

    public boolean exists(String type) throws SQLException {
        String sql = "SELECT 1 FROM attraction_type WHERE type = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
