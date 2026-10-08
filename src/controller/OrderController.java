package controller;

import dao.OrderDAO;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import model.CartItem;
import model.Order;
import model.OrderDetail;
import model.User;
import util.AppSession;

public class OrderController {
    private final OrderDAO dao = new OrderDAO();

    public int placeOrder(String name, String phone, String address, String note,
                          String paymentMethod, List<CartItem> items) {
        User u = AppSession.getCurrentUser();
        if (u == null) throw new IllegalStateException("Bạn cần đăng nhập để đặt hàng.");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");

        name = name == null ? "" : name.trim();
        phone = phone == null ? "" : phone.trim();
        address = address == null ? "" : address.trim();
        note = note == null ? "" : note.trim();

        if (name.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên người nhận.");
        if (!phone.matches("0\\d{9,10}")) throw new IllegalArgumentException("Số điện thoại không hợp lệ (ví dụ 0901234567).");
        if (address.length() < 5) throw new IllegalArgumentException("Vui lòng nhập địa chỉ giao hàng đầy đủ.");

        Order o = new Order();
        o.setUserId(u.getId());
        o.setReceiverName(name);
        o.setPhone(phone);
        o.setAddress(address);
        o.setNote(note.isEmpty() ? null : note);
        o.setPaymentMethod(paymentMethod);
        try {
            return dao.create(o, items);
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<Order> myOrders() {
        User u = AppSession.getCurrentUser();
        if (u == null) return Collections.emptyList();
        return dao.getByUser(u.getId());
    }

    public List<OrderDetail> details(int orderId) { return dao.getDetails(orderId); }

    public boolean cancel(int orderId) {
        User u = AppSession.getCurrentUser();
        if (u == null) return false;
        try { return dao.cancel(orderId, u.getId()); }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean confirmReceived(int orderId) {
        User u = AppSession.getCurrentUser();
        return u != null && dao.confirmReceived(orderId, u.getId());
    }
}
