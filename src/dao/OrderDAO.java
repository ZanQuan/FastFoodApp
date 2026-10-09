package dao;

import java.sql.*;
import java.util.*;
import model.CartItem;
import model.Order;
import model.OrderDetail;

public class OrderDAO extends DAO {

    private Order map(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt("Id"), rs.getInt("UserId"),
            rs.getString("ReceiverName"), rs.getString("Phone"),
            rs.getString("Address"), rs.getString("Note"),
            rs.getTimestamp("OrderDate"), rs.getDouble("TotalPrice"),
            rs.getString("Status"), rs.getString("PaymentMethod"),
            rs.getString("PaymentStatus"));
    }

    public int create(Order o, List<CartItem> items) throws SQLException {
        if (con == null) throw new SQLException("Không kết nối được cơ sở dữ liệu.");
        double total = 0;
        for (CartItem it : items) total += it.getSubtotal();

        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int orderId;
            String sql = "INSERT INTO Orders(UserId, ReceiverName, Phone, Address, Note, "
                       + "TotalPrice, Status, PaymentMethod, PaymentStatus) VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, o.getUserId());
                ps.setString(2, o.getReceiverName());
                ps.setString(3, o.getPhone());
                ps.setString(4, o.getAddress());
                ps.setString(5, o.getNote());
                ps.setDouble(6, total);
                ps.setString(7, Order.CHO_XAC_NHAN);
                ps.setString(8, o.getPaymentMethod());
                ps.setString(9, "Chưa thanh toán");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được mã đơn hàng.");
                    orderId = keys.getInt(1);
                }
            }

            String sqlDetail = "INSERT INTO OrderDetails(OrderId, ProductId, Quantity, UnitPrice) VALUES (?,?,?,?)";
            // chỉ trừ kho khi đủ hàng và món còn bán
            String sqlStock = "UPDATE Products SET Stock = Stock - ? "
                            + "WHERE Id = ? AND IsAvailable = 1 AND Stock >= ?";
            try (PreparedStatement psD = con.prepareStatement(sqlDetail);
                 PreparedStatement psS = con.prepareStatement(sqlStock)) {
                for (CartItem it : items) {
                    psS.setInt(1, it.getQuantity());
                    psS.setInt(2, it.getProductId());
                    psS.setInt(3, it.getQuantity());
                    if (psS.executeUpdate() == 0) {
                        throw new SQLException("Món \"" + it.getName() + "\" không đủ hàng hoặc đã ngừng bán.");
                    }
                    psD.setInt(1, orderId);
                    psD.setInt(2, it.getProductId());
                    psD.setInt(3, it.getQuantity());
                    psD.setDouble(4, it.getPrice());
                    psD.executeUpdate();
                }
            }
            con.commit();
            return orderId;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

    public List<Order> getByUser(int userId) {
        List<Order> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT * FROM Orders WHERE UserId = ? ORDER BY OrderDate DESC, Id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<OrderDetail> getDetails(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT d.Quantity, d.UnitPrice, p.Name FROM OrderDetails d "
                   + "LEFT JOIN Products p ON p.Id = d.ProductId WHERE d.OrderId = ? ORDER BY d.Id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    list.add(new OrderDetail(name == null ? "(món đã xóa)" : name,
                            rs.getInt("Quantity"), rs.getDouble("UnitPrice")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Khách hủy đơn: chỉ được khi đơn còn "Chờ xác nhận". Hoàn lại tồn kho. */
    public boolean cancel(int orderId, int userId) throws SQLException {
        if (con == null) return false;
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            String sql = "UPDATE Orders SET Status = ? WHERE Id = ? AND UserId = ? AND Status = ?";
            int rows;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, Order.DA_HUY);
                ps.setInt(2, orderId);
                ps.setInt(3, userId);
                ps.setString(4, Order.CHO_XAC_NHAN);
                rows = ps.executeUpdate();
            }
            if (rows == 0) { con.rollback(); return false; }
            String restore = "UPDATE p SET p.Stock = p.Stock + d.Quantity "
                           + "FROM Products p JOIN OrderDetails d ON d.ProductId = p.Id "
                           + "WHERE d.OrderId = ?";
            try (PreparedStatement ps = con.prepareStatement(restore)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }

    /** Khách xác nhận đã nhận hàng: "Đang giao"/"Đã giao" -> "Hoàn thành". */
    public boolean confirmReceived(int orderId, int userId) {
        if (con == null) return false;
        String sql = "UPDATE Orders SET Status = ?, "
                   + "PaymentStatus = CASE WHEN PaymentMethod = 'COD' THEN ? ELSE PaymentStatus END "
                   + "WHERE Id = ? AND UserId = ? AND Status IN (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Order.HOAN_THANH);
            ps.setString(2, "Đã thanh toán");
            ps.setInt(3, orderId);
            ps.setInt(4, userId);
            ps.setString(5, Order.DANG_GIAO);
            ps.setString(6, Order.DA_GIAO);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Tất cả đơn hàng. status rỗng = mọi trạng thái; keyword khớp tên, SĐT hoặc mã đơn. */
    public List<Order> getAll(String status, String keyword) {
        List<Order> list = new ArrayList<>();
        if (con == null) return list;
        boolean hasStatus = status != null && !status.trim().isEmpty();
        String kw = keyword == null ? "" : keyword.trim().replace("#", "");
        boolean hasKw = !kw.isEmpty();

        StringBuilder sql = new StringBuilder("SELECT * FROM Orders WHERE 1 = 1 ");
        if (hasStatus) sql.append("AND Status = ? ");
        if (hasKw) sql.append("AND (ReceiverName LIKE ? OR Phone LIKE ? OR CAST(Id AS NVARCHAR(20)) = ?) ");
        sql.append("ORDER BY OrderDate DESC, Id DESC");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasStatus) ps.setString(i++, status);
            if (hasKw) {
                ps.setString(i++, "%" + kw + "%");
                ps.setString(i++, "%" + kw + "%");
                ps.setString(i++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Chuyển trạng thái, chỉ thành công nếu đơn vẫn đang ở trạng thái "from" (tránh 2 người bấm cùng lúc). */
    public boolean updateStatus(int orderId, String from, String to) {
        if (con == null) return false;
        String sql = "UPDATE Orders SET Status = ? WHERE Id = ? AND Status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, to);
            ps.setInt(2, orderId);
            ps.setString(3, from);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Nhân viên hủy đơn (chưa giao): đổi trạng thái + hoàn lại tồn kho. */
    public boolean staffCancel(int orderId) throws SQLException {
        if (con == null) return false;
        boolean oldAuto = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            String sql = "UPDATE Orders SET Status = ? WHERE Id = ? AND Status IN (?, ?)";
            int rows;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, Order.DA_HUY);
                ps.setInt(2, orderId);
                ps.setString(3, Order.CHO_XAC_NHAN);
                ps.setString(4, Order.DANG_CHUAN_BI);
                rows = ps.executeUpdate();
            }
            if (rows == 0) { con.rollback(); return false; }
            String restore = "UPDATE p SET p.Stock = p.Stock + d.Quantity "
                           + "FROM Products p JOIN OrderDetails d ON d.ProductId = p.Id "
                           + "WHERE d.OrderId = ?";
            try (PreparedStatement ps = con.prepareStatement(restore)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAuto);
        }
    }
}
