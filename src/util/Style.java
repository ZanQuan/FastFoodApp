/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Lenovo
 */
public class Style {

    public static final Color SOFT = new Color(0xFFF1E6);
    public static final Color SELECT = new Color(0xFFE0CC);
    public static final Color ZEBRA = new Color(0xFFFBF7);
    public static final Color LINE = new Color(0xF0EAE0);

    public static void page(JComponent... cs) {
        for (JComponent c : cs) {
            c.setOpaque(true);
            c.setBackground(Theme.BG);
        }
    }

    public static void transparent(JComponent... cs) {
        for (JComponent c : cs) {
            c.setOpaque(false);
        }
    }

    public static void card(JComponent c, String title) {
        c.setOpaque(true);
        c.setBackground(Theme.CARD);
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1), "  " + title + "  ");
        tb.setTitleFont(Theme.font(Font.BOLD, 15f));
        tb.setTitleColor(Theme.PRIMARY_DARK);
        c.setBorder(BorderFactory.createCompoundBorder(tb, Theme.pad(8, 12)));
        if (c instanceof JScrollPane) {
            ((JScrollPane) c).getViewport().setBackground(Theme.CARD);
        }
    }

    public static void bar(JPanel p) {
        p.setOpaque(true);
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER), Theme.pad(8, 16)));
    }

    public static void label(JLabel l) {
        l.setFont(Theme.font(Font.BOLD, 14f));
        l.setForeground(Theme.TEXT);
    }

    public static void heading(JLabel l) {
        l.setFont(Theme.font(Font.BOLD, 24f));
        l.setForeground(Theme.PRIMARY_DARK);
    }

    public static void money(JLabel l) {
        l.setFont(Theme.font(Font.BOLD, 20f));
        l.setForeground(Theme.PRIMARY_DARK);
    }

    public static void field(JTextField f) {
        f.setFont(Theme.font(Font.PLAIN, 14f));
        f.setForeground(Theme.TEXT);
        f.setBackground(Color.WHITE);
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1), Theme.pad(5, 10));
        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 2), Theme.pad(4, 9));
        f.setBorder(normal);
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 38));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(focus);
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(normal);
            }
        });
    }

    public static void radio(JRadioButton r) {
        r.setFont(Theme.font(Font.PLAIN, 14f));
        r.setForeground(Theme.TEXT);
        r.setOpaque(false);
        r.setFocusPainted(false);
    }

    public static void primary(JButton b) {
        base(b);
        Color normal = Theme.PRIMARY, hover = Theme.PRIMARY_DARK, off = new Color(0xF2C4A8);
        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setBorder(Theme.pad(9, 20));
        hover(b, normal, hover);
        b.addPropertyChangeListener("enabled", e -> b.setBackground(b.isEnabled() ? normal : off));
        if (!b.isEnabled()) {
            b.setBackground(off);
        }
    }

    public static void secondary(JButton b) {
        outline(b, Theme.PRIMARY, new Color(0xFFF1E6));
    }

    public static void danger(JButton b) {
        outline(b, Theme.DANGER, new Color(0xFFE3E3));
    }

    private static void outline(JButton b, Color c, Color hoverBg) {
        base(b);
        Color grey = new Color(0xBBBBBB);
        b.setBackground(Color.WHITE);
        Runnable paint = () -> {
            Color col = b.isEnabled() ? c : grey;
            b.setForeground(col);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(col, 1), Theme.pad(8, 18)));
            if (!b.isEnabled()) {
                b.setBackground(Color.WHITE);
            }
        };
        paint.run();
        hover(b, Color.WHITE, hoverBg);
        b.addPropertyChangeListener("enabled", e -> paint.run());
    }

    private static void base(JButton b) {
        b.setFont(Theme.font(Font.BOLD, 14f));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void hover(JButton b, Color normal, Color over) {
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(over);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(normal);
                }
            }
        });
    }

    public static void table(JTable t, int... rightCols) {
        t.setFont(Theme.font(Font.PLAIN, 14f));
        t.setForeground(Theme.TEXT);
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(LINE);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(Theme.TEXT);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);

        for (int c = 0; c < t.getColumnCount(); c++) {
            boolean right = false;
            for (int rc : rightCols) {
                if (rc == c) {
                    right = true;
                }
            }
            t.getColumnModel().getColumn(c).setCellRenderer(new Cell(right));
        }

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s,
                    boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, false, false, r, c);
                setFont(Theme.font(Font.BOLD, 13f));
                setBackground(SOFT);
                setForeground(Theme.PRIMARY_DARK);
                boolean right = false;
                for (int rc : rightCols) {
                    if (rc == c) {
                        right = true;
                    }
                }
                setHorizontalAlignment(right ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.PRIMARY), Theme.pad(0, 12)));
                return this;
            }
        });
    }

    public static void statusColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                    boolean foc, int row, int c) {
                super.getTableCellRendererComponent(tb, v, sel, false, row, c);
                setBorder(Theme.pad(0, 12));
                setFont(Theme.font(Font.BOLD, 14f));
                String s = String.valueOf(v);
                if (model.Order.DA_HUY.equals(s)) {
                    setForeground(Theme.DANGER);
                } else if (model.Order.HOAN_THANH.equals(s)) {
                    setForeground(new Color(0x2B8A3E));
                } else {
                    setForeground(Theme.PRIMARY_DARK);
                }
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
                }
                return this;
            }
        });
    }

    public static JPanel statCard(String title, JLabel value, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER, 1), Theme.pad(12, 16))));
        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.PLAIN, 13f));
        t.setForeground(Theme.MUTED);
        value.setFont(Theme.font(Font.BOLD, 26f));
        value.setForeground(Theme.TEXT);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private static class Cell extends DefaultTableCellRenderer {

        private final boolean right;

        Cell(boolean right) {
            this.right = right;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setHorizontalAlignment(right ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(Theme.pad(0, 12));
            if (!sel) {
                setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
            }
            return this;
        }
    }
}
