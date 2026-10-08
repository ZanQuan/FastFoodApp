package controller;

import dao.CategoryDAO;
import dao.ProductDAO;
import java.util.List;
import model.Category;
import model.Product;

public class HomeController {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public List<Product> index() { return productDAO.getFeatured(); }

    public List<Category> categories() { return categoryDAO.getAll(); }
}
