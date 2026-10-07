/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Lenovo
 */
public class Product {
    private int     id;
    private String  name;
    private String  description;
    private double price;
    private int    stock;
    private String imageUrl;
    private boolean isAvailable;
    private int    categoryId;
    private String categoryName; // để hiển thị trên bảng

    // Constructor đầy đủ
    public Product(int id, String name, String desc,
                   double price, int stock, String img,
                   boolean avail, int catId, String catName) {
        this.id = id; this.name = name; this.description = desc;
        this.price = price; this.stock = stock; this.imageUrl = img;
        this.isAvailable = avail; this.categoryId = catId;
        this.categoryName = catName;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isAvailable() { return isAvailable; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public void setName(String n) { name = n; }
    public void setPrice(double p) { price = p; }
    public void setStock(int s) { stock = s; }
    public void setAvailable(boolean a) { isAvailable = a; }
    public void setCategoryId(int c) { categoryId = c; }
}
