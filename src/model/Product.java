package model;

public class Product {
    private int     id;
    private String  name;
    private String  description;
    private double  price;
    private int     stock;
    private String  imageUrl;
    private boolean isAvailable;
    private boolean isFeatured;
    private int     categoryId;
    private String  categoryName;

    public Product(int id, String name, String desc,
                   double price, int stock, String img,
                   boolean avail, int catId, String catName) {
        this.id = id; this.name = name; this.description = desc;
        this.price = price; this.stock = stock; this.imageUrl = img;
        this.isAvailable = avail; this.categoryId = catId;
        this.categoryName = catName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return isAvailable; }
    public boolean isFeatured() { return isFeatured; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    public void setName(String n) { name = n; }
    public void setDescription(String d) { description = d; }
    public void setPrice(double p) { price = p; }
    public void setStock(int s) { stock = s; }
    public void setImageUrl(String u) { imageUrl = u; }
    public void setAvailable(boolean a) { isAvailable = a; }
    public void setFeatured(boolean f) { isFeatured = f; }
    public void setCategoryId(int c) { categoryId = c; }
}
