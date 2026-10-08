package view;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import model.Product;
import util.Theme;

// Lưới thẻ sản phẩm, dùng chung cho Trang chủ, Danh sách, Sản phẩm liên quan
public class ProductGridPanel extends JPanel {
    private final int columns;

    public ProductGridPanel(int columns) {
        super(new BorderLayout());
        this.columns = columns;
        setOpaque(false);
    }

    public void setProducts(List<Product> list, Consumer<Product> onOpen) {
        removeAll();
        if (list == null || list.isEmpty()) {
            JLabel empty = Theme.muted("Không có sản phẩm phù hợp.");
            empty.setBorder(Theme.pad(20, 0));
            add(empty, BorderLayout.NORTH);
        } else {
            JPanel grid = new JPanel(new GridLayout(0, columns, 16, 16));
            grid.setOpaque(false);
            for (Product p : list) grid.add(new ProductCard(p, onOpen));
            // Bù ô trống cho hàng cuối để thẻ không bị kéo giãn
            int rest = (columns - list.size() % columns) % columns;
            for (int i = 0; i < rest; i++) {
                JPanel blank = new JPanel();
                blank.setOpaque(false);
                grid.add(blank);
            }
            add(grid, BorderLayout.NORTH);
        }
        revalidate();
        repaint();
    }
}
