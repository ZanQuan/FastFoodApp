package view;

import controller.AccountController;
import java.awt.*;
import javax.swing.*;
import model.User;
import util.Theme;

// Cửa sổ chính của khách: thanh menu + các trang 
public class ShopFrm extends JFrame {
    private static final String HOME = "home", LIST = "list", DETAIL = "detail", CART = "cart";

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final HomePanel homePanel;
    private final ProductListPanel listPanel;
    private final ProductDetailPanel detailPanel;
    private String currentCard = HOME;
    private String previousCard = HOME;
    private CartPanel cart;
    
    public ShopFrm(User user) {
        super("FastFood");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        homePanel = new HomePanel(this);
        listPanel = new ProductListPanel(this);
        detailPanel = new ProductDetailPanel(this);
        cart = new CartPanel(this);
        content.add(homePanel, HOME);
        content.add(listPanel, LIST);
        content.add(detailPanel, DETAIL);
        content.add(cart, CART);

        add(buildNavBar(user), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        showHome();
    }

    private JPanel buildNavBar(User user) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PRIMARY);
        bar.setBorder(Theme.pad(8, 16));

        JLabel brand = new JLabel("FastFood");
        brand.setFont(Theme.font(Font.BOLD, 22f));
        brand.setForeground(Color.WHITE);
        bar.add(brand, BorderLayout.WEST);

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        menu.setOpaque(false);
        JButton btnHome = Theme.navButton("Trang chủ");
        JButton btnProducts = Theme.navButton("Sản phẩm");
        JButton btnCart = Theme.navButton("Giỏ hàng");
        btnHome.addActionListener(e -> showHome());
        btnProducts.addActionListener(e -> showProducts(0, ""));
        btnCart.addActionListener(e -> showCart());
        menu.add(btnHome);
        menu.add(btnProducts);
        menu.add(btnCart);
        bar.add(menu, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        String who = user == null ? "" : (user.getFullName() != null && !user.getFullName().isEmpty()
                ? user.getFullName() : user.getEmail());
        JLabel hello = new JLabel("Xin chào, " + who);
        hello.setForeground(Color.WHITE);
        hello.setFont(Theme.font(Font.PLAIN, 14f));
        JButton btnLogout = Theme.navButton("Đăng xuất");
        btnLogout.addActionListener(e -> logout());
        right.add(hello);
        right.add(btnLogout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void show(String card) {
        previousCard = currentCard;
        currentCard = card;
        cards.show(content, card);
    }

    public void showHome() {
        homePanel.reload();
        show(HOME);
    }

    public void showProducts(int categoryId, String keyword) {
        listPanel.showWith(categoryId, keyword);
        show(LIST);
    }

    public void showProductDetail(model.Product p) {
        detailPanel.load(p.getId());
        if (!DETAIL.equals(currentCard)) show(DETAIL);   // xem sp liên quan: vẫn giữ trang trước đó
    }

    public void goBack() {
        cards.show(content, previousCard);
        currentCard = previousCard;
    }

    private void logout() {
        new AccountController().logout();
        new LoginFrm().setVisible(true);
        dispose();
    }
    
    public void showCart() {
    cart.reload();
    show(CART);
    }
}
