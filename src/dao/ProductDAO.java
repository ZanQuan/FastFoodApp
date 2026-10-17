package dao;

import java.sql.*;
import java.util.*;
import model.Product;

public class ProductDAO extends DAO {

    private static final String SELECT_BASE =
        "SELECT p.*, c.Name AS CatName FROM Products p "
      + "LEFT JOIN Categories c ON p.CategoryId = c.Id ";

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product(
            rs.getInt("Id"), rs.getString("Name"),
            rs.getString("Description"), rs.getDouble("Price"),
            rs.getInt("Stock"), rs.getString("ImageUrl"),
            rs.getBoolean("IsAvailable"), rs.getInt("CategoryId"),
            rs.getString("CatName"));
        p.setFeatured(rs.getBoolean("IsFeatured"));
        return p;
    }

    // Sản phẩm nổi bật cho trang chủ
    public List<Product> getFeatured() {
        List<Product> list = new ArrayList<>();
        if (con == null) return list;
        String sql = SELECT_BASE + "WHERE p.IsFeatured = 1 AND p.IsAvailable = 1 ORDER BY p.Name";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Lọc theo danh mục (categoryId <= 0 là tất cả) và từ khóa tên
    public List<Product> filter(int categoryId, String keyword) {
        List<Product> list = new ArrayList<>();
        if (con == null) return list;
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE p.IsAvailable = 1 ");
        if (categoryId > 0) sql.append("AND p.CategoryId = ? ");
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        if (hasKw) sql.append("AND p.Name LIKE ? ");
        sql.append("ORDER BY p.Name");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (categoryId > 0) ps.setInt(i++, categoryId);
            if (hasKw) ps.setString(i++, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Product getById(int id) {
        if (con == null) return null;
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE p.Id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Sản phẩm liên quan: cùng danh mục, trừ chính nó
    public List<Product> getRelated(int categoryId, int excludeId, int limit) {
        List<Product> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT TOP (?) p.*, c.Name AS CatName FROM Products p "
                   + "LEFT JOIN Categories c ON p.CategoryId = c.Id "
                   + "WHERE p.CategoryId = ? AND p.Id <> ? AND p.IsAvailable = 1 ORDER BY p.Name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, categoryId);
            ps.setInt(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ===== PHẦN DÀNH CHO ADMIN (Giai đoạn 6b) =====

    /** Tất cả món (kể cả món đang ẩn). keyword rỗng = không lọc. */
    public List<Product> getAllProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        if (con == null) return list;
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = SELECT_BASE + (hasKw ? "WHERE p.Name LIKE ? " : "") + "ORDER BY p.Id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (hasKw) ps.setString(1, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Product> getAllProducts() { return getAllProducts(""); }

    public boolean addProduct(Product p) {
        if (con == null) return false;
        String sql = "INSERT INTO Products(Name,Description,Price,Stock,ImageUrl,IsAvailable,IsFeatured,CategoryId) "
                   + "VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImageUrl());
            ps.setBoolean(6, p.isAvailable());
            ps.setBoolean(7, p.isFeatured());
            ps.setInt(8, p.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateProduct(Product p) {
        if (con == null) return false;
        String sql = "UPDATE Products SET Name=?,Description=?,Price=?,Stock=?,ImageUrl=?,"
                   + "IsAvailable=?,IsFeatured=?,CategoryId=? WHERE Id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImageUrl());
            ps.setBoolean(6, p.isAvailable());
            ps.setBoolean(7, p.isFeatured());
            ps.setInt(8, p.getCategoryId());
            ps.setInt(9, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * Xóa món. Trả về: 1 = đã xóa hẳn, 2 = món đã có trong đơn hàng nên chỉ ẨN (không xóa được),
     * 0 = lỗi.
     */
    public int deleteProduct(int id) {
        if (con == null) return 0;
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE Id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {      // vướng khóa ngoại: món đã nằm trong OrderDetails
                String hide = "UPDATE Products SET IsAvailable = 0, IsFeatured = 0 WHERE Id = ?";
                try (PreparedStatement ps = con.prepareStatement(hide)) {
                    ps.setInt(1, id);
                    return ps.executeUpdate() > 0 ? 2 : 0;
                } catch (SQLException e2) { e2.printStackTrace(); return 0; }
            }
            e.printStackTrace();
            return 0;
        }
    }
}
