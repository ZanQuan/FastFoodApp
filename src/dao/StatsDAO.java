package dao;

import java.sql.*;
import java.util.*;
import model.Order;

/** Số liệu cho trang Tổng quan của quản trị. */
public class StatsDAO extends DAO {

    private double scalarDouble(String sql, String... params) {
        if (con == null) return 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Doanh thu = tổng các đơn đã Hoàn thành. */
    public double revenue() {
        return scalarDouble("SELECT ISNULL(SUM(TotalPrice), 0) FROM Orders WHERE Status = ?", Order.HOAN_THANH);
    }

    public int totalOrders() {
        return (int) scalarDouble("SELECT COUNT(*) FROM Orders");
    }

    public int pendingOrders() {
        return (int) scalarDouble("SELECT COUNT(*) FROM Orders WHERE Status = ?", Order.CHO_XAC_NHAN);
    }

    public int activeProducts() {
        return (int) scalarDouble("SELECT COUNT(*) FROM Products WHERE IsAvailable = 1");
    }

    /** Món bán chạy: {tên, số lượng, doanh thu}, không tính đơn đã hủy. */
    public List<Object[]> topProducts(int n) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT TOP (?) p.Name, SUM(d.Quantity) AS Qty, SUM(d.Quantity * d.UnitPrice) AS Rev "
                   + "FROM OrderDetails d JOIN Orders o ON o.Id = d.OrderId "
                   + "JOIN Products p ON p.Id = d.ProductId "
                   + "WHERE o.Status <> ? GROUP BY p.Name ORDER BY Qty DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n);
            ps.setString(2, Order.DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Doanh số theo ngày trong N ngày gần nhất: {ngày, số đơn, tổng tiền}, không tính đơn đã hủy. */
    public List<Object[]> lastDays(int days) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT CAST(OrderDate AS DATE) AS D, COUNT(*) AS C, SUM(TotalPrice) AS T "
                   + "FROM Orders WHERE Status <> ? "
                   + "AND OrderDate >= DATEADD(DAY, -(?) + 1, CAST(GETDATE() AS DATE)) "
                   + "GROUP BY CAST(OrderDate AS DATE) ORDER BY D DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Order.DA_HUY);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getDate(1), rs.getInt(2), rs.getDouble(3)});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
