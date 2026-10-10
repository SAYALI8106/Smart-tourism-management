package smarttourism.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import smarttourism.model.TourPackage;

/**
 * Tier 3 - Data Access Layer for TOUR_PACKAGE table.
 */
public class PackageDAO {

    public List<TourPackage> findAll() throws SQLException {
        String sql = "SELECT package_id, package_name, duration, cost FROM tour_package ORDER BY package_id";
        List<TourPackage> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TourPackage(
                    rs.getInt("package_id"),
                    rs.getString("package_name"),
                    rs.getInt("duration"),
                    rs.getDouble("cost")
                ));
            }
        }
        return list;
    }

    public TourPackage findById(int id) throws SQLException {
        String sql = "SELECT package_id, package_name, duration, cost FROM tour_package WHERE package_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TourPackage(
                        rs.getInt("package_id"),
                        rs.getString("package_name"),
                        rs.getInt("duration"),
                        rs.getDouble("cost")
                    );
                }
            }
        }
        return null;
    }

    public List<TourPackage> search(String query) throws SQLException {
        String sql = "SELECT package_id, package_name, duration, cost FROM tour_package "
                   + "WHERE package_name LIKE ? OR CAST(package_id AS CHAR) LIKE ? OR CAST(cost AS CHAR) LIKE ? "
                   + "ORDER BY package_id";
        String pattern = "%" + query + "%";
        List<TourPackage> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TourPackage(
                        rs.getInt("package_id"),
                        rs.getString("package_name"),
                        rs.getInt("duration"),
                        rs.getDouble("cost")
                    ));
                }
            }
        }
        return list;
    }

    public void insert(TourPackage p) throws SQLException {
        String sql = "INSERT INTO tour_package(package_id, package_name, duration, cost) VALUES(?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getPackageId());
            ps.setString(2, p.getPackageName());
            ps.setInt(3, p.getDuration());
            ps.setDouble(4, p.getCost());
            ps.executeUpdate();
        }
    }

    public void update(TourPackage p) throws SQLException {
        String sql = "UPDATE tour_package SET package_name=?, duration=?, cost=? WHERE package_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getPackageName());
            ps.setInt(2, p.getDuration());
            ps.setDouble(3, p.getCost());
            ps.setInt(4, p.getPackageId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Package ID " + p.getPackageId() + " not found.");
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tour_package WHERE package_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Package ID " + id + " not found.");
            }
        }
    }

    public boolean exists(int id) throws SQLException {
        String sql = "SELECT 1 FROM tour_package WHERE package_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int getNextPackageId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(package_id), 200) + 1 AS next_id FROM tour_package";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 211;
    }
}
