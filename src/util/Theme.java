package util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Theme {
    public static final Color PRIMARY = new Color(0xE8590C);
    public static final Color PRIMARY_DARK = new Color(0xC2410C);
    public static final Color BG = new Color(0xFAF7F2);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x2B2B2B);
    public static final Color MUTED = new Color(0x777777);
    public static final Color BORDER = new Color(0xE5E0D8);
    public static final Color DANGER = new Color(0xC92A2A);

    public static Font font(int style, float size) {
        return new Font("Segoe UI", style, 14).deriveFont(style, size);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(Font.BOLD, 14f));
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Nút phẳng chữ trắng cho thanh menu
    public static JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(Font.BOLD, 14f));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel title(String text, float size) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, size));
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.PLAIN, 13f));
        l.setForeground(MUTED);
        return l;
    }

    public static javax.swing.border.Border pad(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }
}
