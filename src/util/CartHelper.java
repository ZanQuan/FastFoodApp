/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.util.ArrayList;
import java.util.List;
import model.CartItem;
import model.Product;
/**
 *
 * @author Lenovo
 */
public class CartHelper {
    private static final List<CartItem> items = new ArrayList<>();

    public static List<CartItem> getItems() { return items; }

    private static CartItem find(int productId) {
        for (CartItem i : items) {
            if (i.getProductId() == productId) return i;
        }
        return null;
    }

    public static String add(Product p, int qty) {
        if (p == null) return "Không tìm thấy sản phẩm.";
        if (qty <= 0) return "Số lượng phải lớn hơn 0.";
        if (!p.isAvailable()) return "Món này hiện không bán.";
        CartItem existing = find(p.getId());
        int newQty = (existing == null ? 0 : existing.getQuantity()) + qty;
        if (newQty > p.getStock()) {
            return "Chỉ còn " + p.getStock() + " phần trong kho.";
        }
        if (existing == null) items.add(new CartItem(p, qty));
        else existing.setQuantity(newQty);
        return null;
    }

    public static String update(int productId, int qty) {
        CartItem it = find(productId);
        if (it == null) return "Món không còn trong giỏ.";
        if (qty <= 0) { items.remove(it); return null; }
        if (qty > it.getStock()) return "Chỉ còn " + it.getStock() + " phần trong kho.";
        it.setQuantity(qty);
        return null;
    }

    public static void remove(int productId) {
        items.removeIf(i -> i.getProductId() == productId);
    }

    public static void clear() { items.clear(); }

    public static double total() {
        double t = 0;
        for (CartItem i : items) t += i.getSubtotal();
        return t;
    }

    public static int count() {
        int c = 0;
        for (CartItem i : items) c += i.getQuantity();
        return c;
    }
}
