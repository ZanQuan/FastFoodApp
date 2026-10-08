/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Lenovo
 */
public class CartItem {
    private final int productId;
    private final String name;
    private final double price;
    private final String imageUrl;
    private final int stock;
    private int quantity;

    public CartItem(Product p, int quantity) {
        this.productId = p.getId();
        this.name = p.getName();
        this.price = p.getPrice();
        this.imageUrl = p.getImageUrl();
        this.stock = p.getStock();
        this.quantity = quantity;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getStock() { return stock; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getSubtotal() { return price * quantity; }
}