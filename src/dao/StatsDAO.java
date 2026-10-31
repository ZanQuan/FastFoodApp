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
    
    private static java.sql.Timestamp dayStart(java.time.LocalDate d) {
        return java.sql.Timestamp.valueOf(d.atStartOfDay());
    }

    /** Mốc cuối khoảng (không tính): 00:00 của ngày kế tiếp. */
    private static java.sql.Timestamp dayEnd(java.time.LocalDate d) {
        return java.sql.Timestamp.valueOf(d.plusDays(1).atStartOfDay());
    }

    /** {doanh thu, số đơn hoàn thành, tổng số đơn, số đơn hủy} trong [from, to]. */
    public double[] rangeSummary(java.time.LocalDate from, java.time.LocalDate to) {
        double[] r = new double[4];
        if (con == null) return r;
        String sql = "SELECT ISNULL(SUM(CASE WHEN Status = ? THEN TotalPrice END), 0), "
                   + "ISNULL(SUM(CASE WHEN Status = ? THEN 1 ELSE 0 END), 0), "
                   + "COUNT(*), "
                   + "ISNULL(SUM(CASE WHEN Status = ? THEN 1 ELSE 0 END), 0) "
                   + "FROM Orders WHERE OrderDate >= ? AND OrderDate < ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Order.HOAN_THANH);
            ps.setString(2, Order.HOAN_THANH);
            ps.setString(3, Order.DA_HUY);
            ps.setTimestamp(4, dayStart(from));
            ps.setTimestamp(5, dayEnd(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) for (int i = 0; i < 4; i++) r[i] = rs.getDouble(i + 1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return r;
    }

    /** Mỗi dòng: {LocalDate ngày, Double doanh thu (đơn hoàn thành), Integer số đơn không hủy}. Chỉ có các ngày có đơn. */
    public List<Object[]> revenueByDay(java.time.LocalDate from, java.time.LocalDate to) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT CAST(OrderDate AS DATE) AS D, "
                   + "ISNULL(SUM(CASE WHEN Status = ? THEN TotalPrice ELSE 0 END), 0), "
                   + "SUM(CASE WHEN Status <> ? THEN 1 ELSE 0 END) "
                   + "FROM Orders WHERE OrderDate >= ? AND OrderDate < ? "
                   + "GROUP BY CAST(OrderDate AS DATE) ORDER BY D";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Order.HOAN_THANH);
            ps.setString(2, Order.DA_HUY);
            ps.setTimestamp(3, dayStart(from));
            ps.setTimestamp(4, dayEnd(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getDate(1).toLocalDate(), rs.getDouble(2), rs.getInt(3)});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** {trạng thái, số đơn} trong khoảng ngày (mọi trạng thái). */
    public List<Object[]> ordersByStatus(java.time.LocalDate from, java.time.LocalDate to) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT Status, COUNT(*) FROM Orders "
                   + "WHERE OrderDate >= ? AND OrderDate < ? GROUP BY Status";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, dayStart(from));
            ps.setTimestamp(2, dayEnd(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Object[]{rs.getString(1), rs.getInt(2)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** {phương thức thanh toán, số đơn không hủy, doanh thu đơn hoàn thành}. */
    public List<Object[]> byPayment(java.time.LocalDate from, java.time.LocalDate to) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT PaymentMethod, "
                   + "SUM(CASE WHEN Status <> ? THEN 1 ELSE 0 END), "
                   + "ISNULL(SUM(CASE WHEN Status = ? THEN TotalPrice END), 0) "
                   + "FROM Orders WHERE OrderDate >= ? AND OrderDate < ? GROUP BY PaymentMethod";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Order.DA_HUY);
            ps.setString(2, Order.HOAN_THANH);
            ps.setTimestamp(3, dayStart(from));
            ps.setTimestamp(4, dayEnd(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** {tên danh mục, số phần đã bán, doanh thu} của các đơn hoàn thành, cao nhất trước. */
    public List<Object[]> revenueByCategory(java.time.LocalDate from, java.time.LocalDate to) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT c.Name, SUM(d.Quantity), SUM(d.Quantity * d.UnitPrice) AS Rev "
                   + "FROM OrderDetails d JOIN Orders o ON o.Id = d.OrderId "
                   + "JOIN Products p ON p.Id = d.ProductId "
                   + "JOIN Categories c ON c.Id = p.CategoryId "
                   + "WHERE o.Status = ? AND o.OrderDate >= ? AND o.OrderDate < ? "
                   + "GROUP BY c.Name ORDER BY Rev DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Order.HOAN_THANH);
            ps.setTimestamp(2, dayStart(from));
            ps.setTimestamp(3, dayEnd(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** {tên món, số phần đã bán, doanh thu} của các đơn hoàn thành, bán nhiều nhất trước. */
    public List<Object[]> topProductsRange(java.time.LocalDate from, java.time.LocalDate to, int n) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT TOP (?) p.Name, SUM(d.Quantity) AS Qty, SUM(d.Quantity * d.UnitPrice) AS Rev "
                   + "FROM OrderDetails d JOIN Orders o ON o.Id = d.OrderId "
                   + "JOIN Products p ON p.Id = d.ProductId "
                   + "WHERE o.Status = ? AND o.OrderDate >= ? AND o.OrderDate < ? "
                   + "GROUP BY p.Name ORDER BY Qty DESC, Rev DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n);
            ps.setString(2, Order.HOAN_THANH);
            ps.setTimestamp(3, dayStart(from));
            ps.setTimestamp(4, dayEnd(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getDouble(3)});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
