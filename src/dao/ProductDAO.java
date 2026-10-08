package dao;

import java.sql.*;
import java.util.*;
import model.Product;

public class ProductDAO extends DAO {

    private static final String SELECT_BASE =
        "SELECT p.*, c.Name AS CatName FROM Products p "
      + "LEFT JOIN Categories c ON p.CategoryId = c.Id ";

    // Chuyển 1 dòng kết quả thành đối tượng Product
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

    // ===== PHẦN DÀNH CHO KHÁCH (Giai đoạn 2) =====

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

    // ===== PHẦN QUẢN TRỊ (dùng ở Giai đoạn 6) =====

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        if (con == null) return list;
        try (PreparedStatement ps = con.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addProduct(Product p) {
        String sql = "INSERT INTO Products(Name,Description,Price,Stock,ImageUrl,IsAvailable,CategoryId) "
                   + "VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getImageUrl());
            ps.setBoolean(6, p.isAvailable());
            ps.setInt(7, p.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateProduct(Product p) {
        String sql = "UPDATE Products SET Name=?,Description=?,Price=?,Stock=?,IsAvailable=?,CategoryId=? WHERE Id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setBoolean(5, p.isAvailable());
            ps.setInt(6, p.getCategoryId());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteProduct(int id) {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE Id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
