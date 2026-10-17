package dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Category;

public class CategoryDAO extends DAO {

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT Id, Name, Description FROM Categories ORDER BY Name";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("Id"), rs.getString("Name"),
                                      rs.getString("Description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Category getById(int id) {
        if (con == null) return null;
        String sql = "SELECT Id, Name, Description FROM Categories WHERE Id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("Id"), rs.getString("Name"),
                                        rs.getString("Description"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== PHẦN DÀNH CHO ADMIN (Giai đoạn 6b) =====

    /** Danh mục kèm số món: {id, tên, mô tả, số món}. */
    public List<Object[]> getAllWithCount() {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT c.Id, c.Name, c.Description, COUNT(p.Id) AS Cnt "
                   + "FROM Categories c LEFT JOIN Products p ON p.CategoryId = c.Id "
                   + "GROUP BY c.Id, c.Name, c.Description ORDER BY c.Name";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countProducts(int categoryId) {
        if (con == null) return 0;
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM Products WHERE CategoryId = ?")) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean nameExists(String name, int exceptId) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM Categories WHERE Name = ? AND Id <> ?")) {
            ps.setString(1, name);
            ps.setInt(2, exceptId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean add(String name, String description) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO Categories(Name, Description) VALUES(?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(int id, String name, String description) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("UPDATE Categories SET Name = ?, Description = ? WHERE Id = ?")) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM Categories WHERE Id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
