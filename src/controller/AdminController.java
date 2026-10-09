package controller;

import dao.OrderDAO;
import dao.StatsDAO;
import java.sql.SQLException;
import java.util.List;
import model.Order;
import model.OrderDetail;
import model.User;
import util.AppSession;

/** Dùng cho Admin và Nhân viên. Mọi hàm đều kiểm tra quyền trước. */
public class AdminController {
    private final OrderDAO orderDAO = new OrderDAO();
    private final StatsDAO statsDAO = new StatsDAO();

    private void requireStaff() {
        User u = AppSession.getCurrentUser();
        if (u == null || !(u.isAdmin() || u.isNhanVien())) {
            throw new SecurityException("Bạn không có quyền thực hiện thao tác này.");
        }
    }

    // Đơn hàng
    public List<Order> orders(String status, String keyword) {
        requireStaff();
        return orderDAO.getAll(status, keyword);
    }

    public List<OrderDetail> details(int orderId) {
        requireStaff();
        return orderDAO.getDetails(orderId);
    }

    /** Chuyển đơn sang bước tiếp theo. Trả về null nếu OK, ngược lại là thông báo lỗi. */
    public String advance(Order o) {
        requireStaff();
        String next = Order.next(o.getStatus());
        if (next == null) return "Đơn này không còn bước nào để chuyển.";
        if (!orderDAO.updateStatus(o.getId(), o.getStatus(), next)) {
            return "Trạng thái đơn đã thay đổi. Hãy bấm Làm mới rồi thử lại.";
        }
        return null;
    }

    /** Hủy đơn. Trả về null nếu OK. */
    public String cancel(Order o) {
        requireStaff();
        if (!Order.staffCanCancel(o.getStatus())) return "Đơn đã giao đi, không thể hủy.";
        try {
            return orderDAO.staffCancel(o.getId()) ? null
                    : "Trạng thái đơn đã thay đổi. Hãy bấm Làm mới rồi thử lại.";
        } catch (SQLException e) {
            return "Lỗi cơ sở dữ liệu: " + e.getMessage();
        }
    }

    //Tổng quan 
    public double revenue() { requireStaff(); return statsDAO.revenue(); }
    public int totalOrders() { requireStaff(); return statsDAO.totalOrders(); }
    public int pendingOrders() { requireStaff(); return statsDAO.pendingOrders(); }
    public int activeProducts() { requireStaff(); return statsDAO.activeProducts(); }
    public List<Object[]> topProducts(int n) { requireStaff(); return statsDAO.topProducts(n); }
    public List<Object[]> lastDays(int days) { requireStaff(); return statsDAO.lastDays(days); }
}
