package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.*;
import model.Product;
import util.Format;
import util.ImageUtil;
import util.Theme;

// Thẻ sản phẩm hiển thị trong lưới
public class ProductCard extends JPanel {

    public ProductCard(Product p, Consumer<Product> onOpen) {
        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ảnh
        JLabel img = new JLabel("Không có ảnh", SwingConstants.CENTER);
        img.setForeground(Theme.MUTED);
        img.setOpaque(true);
        img.setBackground(new Color(0xF3EFE8));
        img.setPreferredSize(new Dimension(190, 140));
        ImageUtil.load(p.getImageUrl(), 190, 140, icon -> {
            if (icon != null) { img.setText(null); img.setIcon(icon); }
        });
        add(img, BorderLayout.NORTH);

        // Thông tin
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("<html><body style='width:165px'>" + Format.html(p.getName()) + "</body></html>");
        name.setFont(Theme.font(Font.BOLD, 15f));
        name.setForeground(Theme.TEXT);

        JLabel cat = Theme.muted(p.getCategoryName() == null ? "" : p.getCategoryName());

        JLabel price = new JLabel(Format.money(p.getPrice()));
        price.setFont(Theme.font(Font.BOLD, 16f));
        price.setForeground(Theme.PRIMARY);

        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(cat);
        info.add(Box.createVerticalStrut(6));
        info.add(price);
        if (p.getStock() <= 0) {
            JLabel out = new JLabel("Hết hàng");
            out.setForeground(Theme.DANGER);
            out.setFont(Theme.font(Font.BOLD, 12f));
            info.add(out);
        }
        add(info, BorderLayout.CENTER);

        // Nút
        JButton btn = Theme.primaryButton("Xem chi tiết");
        btn.addActionListener(e -> onOpen.accept(p));
        add(btn, BorderLayout.SOUTH);

        // Bấm vào thẻ cũng mở chi tiết
        MouseAdapter open = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onOpen.accept(p); }
        };
        addMouseListener(open);
        img.addMouseListener(open);
        info.addMouseListener(open);
    }
}
