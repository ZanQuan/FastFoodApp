package controller;

import dao.CategoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.StatsDAO;
import dao.UserDAO;
import java.sql.SQLException;
import java.util.List;
import model.Category;
import model.Order;
import model.OrderDetail;
import model.Product;
import model.User;
import util.AppSession;

/** Dùng cho Admin và Nhân viên. Mọi hàm đều kiểm tra quyền trước. */
public class AdminController {
    private final OrderDAO orderDAO = new OrderDAO();
    private final StatsDAO statsDAO = new StatsDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();

    private void requireStaff() {
        User u = AppSession.getCurrentUser();
        if (u == null || !(u.isAdmin() || u.isNhanVien())) {
            throw new SecurityException("Bạn không có quyền thực hiện thao tác này.");
        }
    }

    private void requireAdmin() {
        User u = AppSession.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            throw new SecurityException("Chỉ Admin mới được thực hiện thao tác này.");
        }
    }

    // ---------- Đơn hàng ----------
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

    // ---------- Tổng quan ----------
    public double revenue() { requireStaff(); return statsDAO.revenue(); }
    public int totalOrders() { requireStaff(); return statsDAO.totalOrders(); }
    public int pendingOrders() { requireStaff(); return statsDAO.pendingOrders(); }
    public int activeProducts() { requireStaff(); return statsDAO.activeProducts(); }
    public List<Object[]> topProducts(int n) { requireStaff(); return statsDAO.topProducts(n); }
    public List<Object[]> lastDays(int days) { requireStaff(); return statsDAO.lastDays(days); }

    // ---------- Sản phẩm (chỉ Admin) ----------
    public List<Product> products(String keyword) {
        requireAdmin();
        return productDAO.getAllProducts(keyword);
    }

    public List<Category> categoryList() {
        requireAdmin();
        return categoryDAO.getAll();
    }

    /** Thêm (id = 0) hoặc sửa món. Trả về null nếu OK, ngược lại là thông báo lỗi. */
    public String saveProduct(Product p) {
        requireAdmin();
        if (p.getName() == null || p.getName().trim().isEmpty()) return "Vui lòng nhập tên món.";
        if (p.getName().trim().length() > 100) return "Tên món tối đa 100 ký tự.";
        if (p.getPrice() <= 0) return "Giá phải lớn hơn 0.";
        if (p.getPrice() > 100000000) return "Giá quá lớn.";
        if (p.getStock() < 0) return "Tồn kho không được âm.";
        if (p.getCategoryId() <= 0) return "Vui lòng chọn danh mục.";
        p.setName(p.getName().trim());
        boolean ok = p.getId() == 0 ? productDAO.addProduct(p) : productDAO.updateProduct(p);
        return ok ? null : "Không lưu được vào cơ sở dữ liệu (xem cửa sổ Output).";
    }

    /** Xóa món; trả về thông báo để hiển thị cho người dùng. */
    public String deleteProduct(int id) {
        requireAdmin();
        switch (productDAO.deleteProduct(id)) {
            case 1: return "Đã xóa món.";
            case 2: return "Món này đã có trong đơn hàng nên không xóa hẳn được.\nMón đã được chuyển sang trạng thái ẨN.";
            default: return "Không xóa được món (xem cửa sổ Output).";
        }
    }

    // ---------- Danh mục (chỉ Admin) ----------
    public List<Object[]> categoriesWithCount() {
        requireAdmin();
        return categoryDAO.getAllWithCount();
    }

    /** Thêm (id = 0) hoặc sửa danh mục. Trả về null nếu OK. */
    public String saveCategory(int id, String name, String description) {
        requireAdmin();
        name = name == null ? "" : name.trim();
        description = description == null ? "" : description.trim();
        if (name.isEmpty()) return "Vui lòng nhập tên danh mục.";
        if (name.length() > 100) return "Tên danh mục tối đa 100 ký tự.";
        if (categoryDAO.nameExists(name, id)) return "Đã có danh mục tên \"" + name + "\".";
        boolean ok = id == 0 ? categoryDAO.add(name, description) : categoryDAO.update(id, name, description);
        return ok ? null : "Không lưu được vào cơ sở dữ liệu (xem cửa sổ Output).";
    }

    /** Xóa danh mục: chỉ khi chưa có món nào. Trả về null nếu OK. */
    public String deleteCategory(int id) {
        requireAdmin();
        int n = categoryDAO.countProducts(id);
        if (n > 0) return "Danh mục còn " + n + " món, hãy chuyển hoặc xóa các món đó trước.";
        return categoryDAO.delete(id) ? null : "Không xóa được danh mục (xem cửa sổ Output).";
    }

    // ---------- Người dùng (chỉ Admin) ----------
    private static final java.util.List<String> ROLES = java.util.Arrays.asList("User", "NhanVien", "Admin");

    private boolean isSelf(int userId) {
        User me = AppSession.getCurrentUser();
        return me != null && me.getId() == userId;
    }

    /** Mỗi dòng: {id, email, fullName, role, locked}. */
    public List<Object[]> users(String keyword) {
        requireAdmin();
        return userDAO.getAll(keyword);
    }

    /** Đổi vai trò. Trả về null nếu OK. */
    public String changeRole(int userId, String role) {
        requireAdmin();
        if (!ROLES.contains(role)) return "Vai trò không hợp lệ.";
        if (isSelf(userId)) return "Bạn không thể tự đổi vai trò của chính mình.";
        return userDAO.updateRole(userId, role) ? null : "Không lưu được (xem cửa sổ Output).";
    }

    /** Khóa / mở khóa. Trả về null nếu OK. */
    public String setLocked(int userId, boolean locked) {
        requireAdmin();
        if (isSelf(userId)) return "Bạn không thể tự khóa tài khoản của chính mình.";
        return userDAO.setLocked(userId, locked) ? null : "Không lưu được (xem cửa sổ Output).";
    }

    /** Đặt lại mật khẩu ngẫu nhiên. Trả về mật khẩu mới, hoặc null nếu thất bại. */
    public String resetPassword(int userId) {
        requireAdmin();
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"; // bỏ ký tự dễ nhầm
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        String pw = sb.toString();
        return userDAO.resetPassword(userId, pw) ? pw : null;
    }
}
