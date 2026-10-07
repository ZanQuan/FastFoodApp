/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Product;
import java.sql.*; import java.util.*;
/**
 *
 * @author Lenovo
 */
public class ProductDAO extends DAO {

    // Lấy tất cả sản phẩm (kèm tên danh mục)
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.Name AS CatName "
                   + "FROM Products p "
                   + "LEFT JOIN Categories c ON p.CategoryId = c.Id";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                    rs.getInt("Id"), rs.getString("Name"),
                    rs.getString("Description"), rs.getDouble("Price"),
                    rs.getInt("Stock"), rs.getString("ImageUrl"),
                    rs.getBoolean("IsAvailable"), rs.getInt("CategoryId"),
                    rs.getString("CatName")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Thêm sản phẩm mới
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

    // Cập nhật sản phẩm
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

    // Xóa sản phẩm
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE Id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Tìm kiếm sản phẩm theo tên
    public List<Product> searchProduct(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.Name AS CatName FROM Products p "
                   + "LEFT JOIN Categories c ON p.CategoryId = c.Id "
                   + "WHERE p.Name LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Product(
                    rs.getInt("Id"), rs.getString("Name"),
                    rs.getString("Description"), rs.getDouble("Price"),
                    rs.getInt("Stock"), rs.getString("ImageUrl"),
                    rs.getBoolean("IsAvailable"), rs.getInt("CategoryId"),
                    rs.getString("CatName")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
