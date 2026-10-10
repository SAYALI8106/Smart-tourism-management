package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.Destination;
import smarttourism.model.DestinationPackage;

/**
 * Tier 3 - Data Access Layer for DESTINATION_PACKAGE many-to-many relationship.
 */
public class DestinationPackageDAO {

    public List<DestinationPackage> findAll() throws SQLException {
        String sql = "SELECT dp.destination_id, d.name AS destination_name, dp.package_id, p.package_name "
                   + "FROM destination_package dp "
                   + "JOIN destination d ON dp.destination_id = d.destination_id "
                   + "JOIN tour_package p ON dp.package_id = p.package_id "
                   + "ORDER BY dp.package_id, dp.destination_id";

        List<DestinationPackage> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DestinationPackage(
                    rs.getInt("destination_id"),
                    rs.getString("destination_name"),
                    rs.getInt("package_id"),
                    rs.getString("package_name")
                ));
            }
        }
        return list;
    }

    public List<Destination> getDestinationsForPackage(int packageId) throws SQLException {
        String sql = "SELECT d.destination_id, d.name, d.location, d.description "
                   + "FROM destination d "
                   + "JOIN destination_package dp ON d.destination_id = dp.destination_id "
                   + "WHERE dp.package_id = ? "
                   + "ORDER BY d.destination_id";

        List<Destination> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, packageId);
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

    public void addMapping(int destinationId, int packageId) throws SQLException {
        String sql = "INSERT INTO destination_package(destination_id, package_id) VALUES(?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setInt(2, packageId);
            ps.executeUpdate();
        }
    }

    public void removeMapping(int destinationId, int packageId) throws SQLException {
        String sql = "DELETE FROM destination_package WHERE destination_id = ? AND package_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setInt(2, packageId);
            ps.executeUpdate();
        }
    }

    public boolean mappingExists(int destinationId, int packageId) throws SQLException {
        String sql = "SELECT 1 FROM destination_package WHERE destination_id = ? AND package_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, destinationId);
            ps.setInt(2, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
