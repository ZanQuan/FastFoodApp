package controller;

import dao.CategoryDAO;
import dao.ProductDAO;
import java.util.List;
import model.Category;
import model.Product;

public class ProductController {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // Index(categoryId, keyword): lọc / tìm kiếm
    public List<Product> index(int categoryId, String keyword) {
        return productDAO.filter(categoryId, keyword);
    }

    // Details(id)
    public Product details(int id) { return productDAO.getById(id); }

    public List<Product> related(Product p, int limit) {
        return productDAO.getRelated(p.getCategoryId(), p.getId(), limit);
    }

    public List<Category> categories() { return categoryDAO.getAll(); }
}
