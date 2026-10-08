/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.util.List;
import model.CartItem;
import model.Product;
import util.CartHelper;
/**
 *
 * @author Lenovo
 */
public class CartController {
    public List<CartItem> index() { return CartHelper.getItems(); }
    public String add(Product p, int qty) { return CartHelper.add(p, qty); }
    public String updateQuantity(int productId, int qty) { return CartHelper.update(productId, qty); }
    public void remove(int productId) { CartHelper.remove(productId); }
    public void clear() { CartHelper.clear(); }
    public double total() { return CartHelper.total(); }
    public int count() { return CartHelper.count(); }
}
