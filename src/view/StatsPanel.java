package view;

import controller.AdminController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableModel;
import model.Order;
import util.Format;
import util.Style;
import util.Theme;

/**
 * Trang THỐNG KÊ của Admin: chọn khoảng ngày, xem doanh thu, biểu đồ theo ngày,
 * trạng thái đơn, phương thức thanh toán, top món, doanh thu theo danh mục và xuất CSV.
 * Viết hoàn toàn bằng code (không có form thiết kế).
 */
public class StatsPanel extends JPanel {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DM = DateTimeFormatter.ofPattern("dd/MM");
    private static final int MAX_DAYS = 366;
    private static final int MONTH_MODE_FROM = 62;   // quá số ngày này thì gộp cột theo tháng

    private final AdminController controller = new AdminController();

    private final JSpinner spFrom = dateSpinner();
    private final JSpinner spTo = dateSpinner();
    private final JLabel valRevenue = new JLabel("0");
    private final JLabel valDone = new JLabel("0");
    private final JLabel valAvg = new JLabel("0");
    private final JLabel valCancel = new JLabel("0%");
    private final BarChart chartDays = new BarChart();
    private final HBarChart chartStatus = new HBarChart();
    private final HBarChart chartPay = new HBarChart();
    private final DefaultTableModel topModel = readOnly("Món bán chạy", "Đã bán", "Doanh thu");
    private final DefaultTableModel catModel = readOnly("Danh mục", "Đã bán", "Doanh thu");
    private final JTable tblTop = new JTable(topModel);
    private final JTable tblCat = new JTable(catModel);

    // dữ liệu của lần xem gần nhất, dùng để xuất CSV
    private LocalDate curFrom, curTo;
    private double[] curSum = new double[4];
    private List<Object[]> curDays = new ArrayList<>();
    private List<Object[]> curCats = new ArrayList<>();
    private List<Object[]> curTop = new ArrayList<>();
    private List<Object[]> curStatus = new ArrayList<>();
    private List<Object[]> curPay = new ArrayList<>();

    public StatsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        add(buildTop(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        LocalDate today = LocalDate.now();
        setSpinners(today.minusDays(29), today);
    }

    // ------------------------------------------------------------------ giao diện

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        Style.page(top);
        top.setBorder(Theme.pad(8, 16));

        JLabel title = new JLabel("Thống kê");
        Style.heading(title);
        JButton btnExport = new JButton("Xuất CSV");
        Style.secondary(btnExport);
        btnExport.addActionListener(e -> exportCsv());
        JPanel head = new JPanel(new BorderLayout());
        Style.page(head);
        head.add(title, BorderLayout.WEST);
        head.add(btnExport, BorderLayout.EAST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        Style.page(filter);
        filter.add(label("Từ ngày"));
        filter.add(spFrom);
        filter.add(label("đến"));
        filter.add(spTo);
        JButton btnView = new JButton("Xem");
        Style.primary(btnView);
        btnView.addActionListener(e -> load());
        filter.add(btnView);
        filter.add(Box(12));
        filter.add(preset("7 ngày", () -> range(6)));
        filter.add(preset("30 ngày", () -> range(29)));
        filter.add(preset("Tháng này", () -> {
            LocalDate t = LocalDate.now();
            setSpinners(t.withDayOfMonth(1), t);
        }));
        filter.add(preset("Tháng trước", () -> {
            LocalDate first = LocalDate.now().withDayOfMonth(1).minusMonths(1);
            setSpinners(first, first.withDayOfMonth(first.lengthOfMonth()));
        }));
        filter.add(preset("Năm nay", () -> {
            LocalDate t = LocalDate.now();
            setSpinners(t.withDayOfYear(1), t);
        }));

        top.add(head, BorderLayout.NORTH);
        top.add(filter, BorderLayout.CENTER);
        return top;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        Style.page(body);
        body.setBorder(Theme.pad(4, 16));

        // hàng 1: 4 thẻ số liệu
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        Style.page(cards);
        cards.add(Style.statCard("Doanh thu (đơn hoàn thành)", valRevenue, Theme.PRIMARY));
        cards.add(Style.statCard("Số đơn hoàn thành", valDone, new Color(0x2B8A3E)));
        cards.add(Style.statCard("Giá trị trung bình / đơn", valAvg, new Color(0x1C7ED6)));
        cards.add(Style.statCard("Tỷ lệ đơn hủy", valCancel, Theme.DANGER));

        // hàng 2: biểu đồ
        JPanel charts = new JPanel(new GridBagLayout());
        Style.page(charts);
        Style.card(chartDays, "Doanh thu theo ngày");
        JPanel right = new JPanel(new GridBagLayout());
        Style.page(right);
        Style.card(chartStatus, "Đơn hàng theo trạng thái");
        Style.card(chartPay, "Thanh toán (số đơn không hủy)");
        right.add(chartStatus, gbc(0, 0, 1, 0.64, new Insets(0, 0, 14, 0)));
        right.add(chartPay, gbc(0, 1, 1, 0.36, new Insets(0, 0, 0, 0)));
        charts.add(chartDays, gbc(0, 0, 0.62, 1, new Insets(0, 0, 0, 14)));
        charts.add(right, gbc(1, 0, 0.38, 1, new Insets(0, 0, 0, 0)));

        // hàng 3: 2 bảng
        JPanel tables = new JPanel(new GridLayout(1, 2, 14, 0));
        Style.page(tables);
        Style.table(tblTop, 1, 2);
        Style.table(tblCat, 1, 2);
        tblTop.setRowHeight(28);
        tblCat.setRowHeight(28);
        for (JTable t : new JTable[]{tblTop, tblCat}) {
            t.getColumnModel().getColumn(0).setPreferredWidth(170);
            t.getColumnModel().getColumn(1).setPreferredWidth(70);
            t.getColumnModel().getColumn(1).setMinWidth(60);
            t.getColumnModel().getColumn(2).setPreferredWidth(150);
            t.getColumnModel().getColumn(2).setMinWidth(140);
        }
        JScrollPane spTop = new JScrollPane(tblTop);
        JScrollPane spCat = new JScrollPane(tblCat);
        Style.card(spTop, "Top 5 món bán chạy");
        Style.card(spCat, "Doanh thu theo danh mục");
        tables.add(spTop);
        tables.add(spCat);

        GridBagConstraints c = gbc(0, 0, 1, 0, new Insets(0, 0, 14, 0));
        body.add(cards, c);
        c = gbc(0, 1, 1, 0.60, new Insets(0, 0, 14, 0));
        body.add(charts, c);
        c = gbc(0, 2, 1, 0.40, new Insets(0, 0, 8, 0));
        body.add(tables, c);
        return body;
    }

    private static GridBagConstraints gbc(int x, int y, double wx, double wy, Insets in) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = wx;
        c.weighty = wy;
        c.fill = GridBagConstraints.BOTH;
        c.insets = in;
        return c;
    }

    private static JLabel label(String s) {
        JLabel l = new JLabel(s);
        Style.label(l);
        return l;
    }

    private static JComponent Box(int w) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(w, 1));
        return p;
    }

    private JButton preset(String text, Runnable r) {
        JButton b = new JButton(text);
        Style.secondary(b);
        b.setBorder(Theme.pad(6, 12));
        b.addActionListener(e -> {
            r.run();
            load();
        });
        return b;
    }

    private void range(int daysBack) {
        LocalDate t = LocalDate.now();
        setSpinners(t.minusDays(daysBack), t);
    }

    private static DefaultTableModel readOnly(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    // ------------------------------------------------------------------ ngày tháng

    private static JSpinner dateSpinner() {
        JSpinner sp = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setFont(Theme.font(Font.PLAIN, 14f));
        sp.setPreferredSize(new Dimension(120, 34));
        return sp;
    }

    private static LocalDate toLocal(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setSpinners(LocalDate from, LocalDate to) {
        spFrom.setValue(Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spTo.setValue(Date.from(to.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    // ------------------------------------------------------------------ nạp dữ liệu

    /** MainFrm gọi mỗi lần mở trang này. */
    public void reload() {
        load();
    }

    private void load() {
        LocalDate f = toLocal(spFrom), t = toLocal(spTo);
        if (f.isAfter(t)) {
            LocalDate x = f;
            f = t;
            t = x;
            setSpinners(f, t);
        }
        if (ChronoUnit.DAYS.between(f, t) + 1 > MAX_DAYS) {
            JOptionPane.showMessageDialog(this, "Chỉ xem được tối đa " + MAX_DAYS + " ngày mỗi lần.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        curFrom = f;
        curTo = t;
        curSum = controller.statSummary(f, t);
        curDays = controller.statRevenueByDay(f, t);
        curStatus = controller.statOrdersByStatus(f, t);
        curPay = controller.statByPayment(f, t);
        curCats = controller.statByCategory(f, t);
        curTop = controller.statTopProducts(f, t, 5);

        showCards();
        showDayChart();
        showStatusChart();
        showPayChart();
        fill(topModel, curTop);
        fill(catModel, curCats);
    }

    private void showCards() {
        double revenue = curSum[0], done = curSum[1], total = curSum[2], cancel = curSum[3];
        valRevenue.setText(Format.money(revenue));
        valDone.setText(String.valueOf((long) done));
        valAvg.setText(done > 0 ? Format.money(revenue / done) : "0 ₫");
        valCancel.setText(total > 0
                ? String.format("%.1f%%  (%d/%d)", cancel * 100.0 / total, (long) cancel, (long) total)
                : "0%");
    }

    private void showDayChart() {
        Map<LocalDate, Double> byDay = new LinkedHashMap<>();
        for (LocalDate d = curFrom; !d.isAfter(curTo); d = d.plusDays(1)) byDay.put(d, 0.0);
        Map<LocalDate, Integer> ordersByDay = new LinkedHashMap<>();
        for (Object[] r : curDays) {
            LocalDate d = (LocalDate) r[0];
            byDay.merge(d, (Double) r[1], Double::sum);
            ordersByDay.merge(d, (Integer) r[2], Integer::sum);
        }

        List<String> labels = new ArrayList<>();
        List<String> tips = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(curFrom, curTo) + 1;
        if (days > MONTH_MODE_FROM) {
            Map<YearMonth, double[]> byMonth = new LinkedHashMap<>();   // {doanh thu, số đơn}
            for (Map.Entry<LocalDate, Double> e : byDay.entrySet()) {
                double[] a = byMonth.computeIfAbsent(YearMonth.from(e.getKey()), k -> new double[2]);
                a[0] += e.getValue();
                a[1] += ordersByDay.getOrDefault(e.getKey(), 0);
            }
            for (Map.Entry<YearMonth, double[]> e : byMonth.entrySet()) {
                String m = String.format("%02d/%d", e.getKey().getMonthValue(), e.getKey().getYear());
                labels.add(m);
                values.add(e.getValue()[0]);
                tips.add("Tháng " + m + ": " + Format.money(e.getValue()[0]) + " (" + (long) e.getValue()[1] + " đơn)");
            }
        } else {
            for (Map.Entry<LocalDate, Double> e : byDay.entrySet()) {
                labels.add(e.getKey().format(DM));
                values.add(e.getValue());
                tips.add(e.getKey().format(DMY) + ": " + Format.money(e.getValue())
                        + " (" + ordersByDay.getOrDefault(e.getKey(), 0) + " đơn)");
            }
        }
        double[] v = new double[values.size()];
        for (int i = 0; i < v.length; i++) v[i] = values.get(i);
        chartDays.setData(labels, v, tips);
    }

    private void showStatusChart() {
        List<String> labels = new ArrayList<>();
        List<Double> vals = new ArrayList<>();
        List<Color> cols = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        // giữ đúng thứ tự quy trình, rồi tới "Đã hủy"
        String[] order = {Order.CHO_XAC_NHAN, Order.DANG_CHUAN_BI, Order.DANG_GIAO,
            Order.DA_GIAO, Order.HOAN_THANH, Order.DA_HUY};
        for (String s : order) {
            int n = 0;
            for (Object[] r : curStatus) if (s.equals(r[0])) n = (Integer) r[1];
            labels.add(s);
            vals.add((double) n);
            cols.add(statusColor(s));
            texts.add(String.valueOf(n));
        }
        chartStatus.setData(labels, vals, cols, texts);
    }

    private void showPayChart() {
        List<String> labels = new ArrayList<>();
        List<Double> vals = new ArrayList<>();
        List<Color> cols = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        for (Object[] r : curPay) {
            String m = String.valueOf(r[0]);
            int n = (Integer) r[1];
            labels.add(m);
            vals.add((double) n);
            cols.add("VNPay".equalsIgnoreCase(m) ? new Color(0x1C7ED6) : new Color(0xF59F00));
            texts.add(n + " đơn · " + Format.money((Double) r[2]));
        }
        chartPay.setData(labels, vals, cols, texts);
    }

    private static Color statusColor(String s) {
        if (Order.HOAN_THANH.equals(s)) return new Color(0x2B8A3E);
        if (Order.DA_HUY.equals(s)) return Theme.DANGER;
        if (Order.CHO_XAC_NHAN.equals(s)) return new Color(0xF59F00);
        if (Order.DANG_GIAO.equals(s) || Order.DA_GIAO.equals(s)) return new Color(0x1C7ED6);
        return Theme.PRIMARY;
    }

    private static void fill(DefaultTableModel m, List<Object[]> rows) {
        m.setRowCount(0);
        for (Object[] r : rows) m.addRow(new Object[]{r[0], r[1], Format.money((Double) r[2])});
    }

    // ------------------------------------------------------------------ xuất CSV

    private void exportCsv() {
        if (curFrom == null) load();
        if (curFrom == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("thong-ke_" + curFrom + "_" + curTo + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getPath() + ".csv");

        StringBuilder sb = new StringBuilder();
        sb.append("sep=;\r\n");
        sb.append("THỐNG KÊ FASTFOOD;").append(curFrom.format(DMY)).append(";").append(curTo.format(DMY)).append("\r\n\r\n");
        sb.append("Chỉ số;Giá trị\r\n");
        sb.append("Doanh thu (đơn hoàn thành);").append((long) curSum[0]).append("\r\n");
        sb.append("Số đơn hoàn thành;").append((long) curSum[1]).append("\r\n");
        sb.append("Tổng số đơn;").append((long) curSum[2]).append("\r\n");
        sb.append("Số đơn hủy;").append((long) curSum[3]).append("\r\n\r\n");
        sb.append("Ngày;Doanh thu;Số đơn (không hủy)\r\n");
        for (Object[] r : curDays) {
            sb.append(((LocalDate) r[0]).format(DMY)).append(';').append(((Double) r[1]).longValue())
              .append(';').append(r[2]).append("\r\n");
        }
        sb.append("\r\nTrạng thái;Số đơn\r\n");
        for (Object[] r : curStatus) sb.append(csv(r[0])).append(';').append(r[1]).append("\r\n");
        sb.append("\r\nThanh toán;Số đơn không hủy;Doanh thu\r\n");
        for (Object[] r : curPay) sb.append(csv(r[0])).append(';').append(r[1]).append(';').append(((Double) r[2]).longValue()).append("\r\n");
        sb.append("\r\nDanh mục;Đã bán;Doanh thu\r\n");
        for (Object[] r : curCats) sb.append(csv(r[0])).append(';').append(r[1]).append(';').append(((Double) r[2]).longValue()).append("\r\n");
        sb.append("\r\nMón bán chạy;Đã bán;Doanh thu\r\n");
        for (Object[] r : curTop) sb.append(csv(r[0])).append(';').append(r[1]).append(';').append(((Double) r[2]).longValue()).append("\r\n");

        try {
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
            byte[] out = new byte[body.length + 3];
            out[0] = (byte) 0xEF;   // BOM để Excel đọc đúng tiếng Việt
            out[1] = (byte) 0xBB;
            out[2] = (byte) 0xBF;
            System.arraycopy(body, 0, out, 3, body.length);
            Files.write(f.toPath(), out);
            JOptionPane.showMessageDialog(this, "Đã lưu: " + f.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(Object o) {
        String s = String.valueOf(o);
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) s = "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    // ================================================================== biểu đồ cột (doanh thu theo ngày)

    static class BarChart extends JComponent {

        private List<String> labels = new ArrayList<>();
        private List<String> tips = new ArrayList<>();
        private double[] values = new double[0];
        private int hover = -1;
        private static final int LEFT = 64, RIGHT = 8, TOP = 8, BOTTOM = 24, DIVS = 5;

        BarChart() {
            ToolTipManager.sharedInstance().registerComponent(this);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = indexAt(e.getX());
                    if (i != hover) {
                        hover = i;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    repaint();
                }
            });
        }

        void setData(List<String> labels, double[] values, List<String> tips) {
            this.labels = labels;
            this.values = values;
            this.tips = tips;
            this.hover = -1;
            repaint();
        }

        private Insets inner() {
            Insets b = getInsets();
            return b == null ? new Insets(0, 0, 0, 0) : b;
        }

        private int indexAt(int x) {
            if (values.length == 0) return -1;
            Insets in = inner();
            double plotW = getWidth() - in.left - in.right - LEFT - RIGHT;
            double slot = plotW / values.length;
            int i = (int) Math.floor((x - in.left - LEFT) / slot);
            return (i >= 0 && i < values.length) ? i : -1;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int i = indexAt(e.getX());
            return i >= 0 && i < tips.size() ? tips.get(i) : null;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            if (isOpaque()) {   // JComponent thuần không tự tô nền
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets in = inner();
            int x0 = in.left + LEFT, x1 = getWidth() - in.right - RIGHT;
            int y0 = in.top + TOP, y1 = getHeight() - in.bottom - BOTTOM;
            int plotW = x1 - x0, plotH = y1 - y0;

            double max = 0;
            for (double v : values) max = Math.max(max, v);
            g.setFont(Theme.font(Font.PLAIN, 12f));
            FontMetrics fm = g.getFontMetrics();

            if (plotW < 20 || plotH < 20) { g.dispose(); return; }
            if (values.length == 0 || max <= 0) {
                g.setColor(Theme.MUTED);
                String s = "Chưa có doanh thu trong khoảng ngày này";
                g.drawString(s, x0 + (plotW - fm.stringWidth(s)) / 2, y0 + plotH / 2);
                g.dispose();
                return;
            }
            double top = niceCeil(max);

            // đường lưới + nhãn trục Y
            for (int i = 0; i <= DIVS; i++) {
                int y = y1 - (int) Math.round(plotH * (double) i / DIVS);
                g.setColor(i == 0 ? Theme.BORDER : Style.LINE);
                g.drawLine(x0, y, x1, y);
                g.setColor(Theme.MUTED);
                String s = shortMoney(top * i / DIVS);
                g.drawString(s, x0 - 6 - fm.stringWidth(s), y + fm.getAscent() / 2 - 1);
            }

            // cột
            int n = values.length;
            double slot = (double) plotW / n;
            double bw = Math.max(1, Math.min(48, slot * 0.7));
            for (int i = 0; i < n; i++) {
                double h = values[i] / top * plotH;
                double x = x0 + i * slot + (slot - bw) / 2;
                g.setColor(i == hover ? Theme.PRIMARY_DARK : Theme.PRIMARY);
                if (values[i] > 0) g.fill(new Rectangle2D.Double(x, y1 - h, bw, Math.max(1, h)));
            }

            // nhãn trục X thưa dần để không đè nhau
            int labelW = 0;
            for (String s : labels) labelW = Math.max(labelW, fm.stringWidth(s));
            int step = (int) Math.max(1, Math.ceil((labelW + 10) / slot));
            g.setColor(Theme.MUTED);
            for (int i = 0; i < n; i += step) {
                String s = labels.get(i);
                int cx = (int) Math.round(x0 + i * slot + slot / 2);
                g.drawString(s, cx - fm.stringWidth(s) / 2, y1 + 16);
            }
            g.dispose();
        }

        private static double niceCeil(double max) {
            double exp = Math.pow(10, Math.floor(Math.log10(max)));
            double f = max / exp;
            double[] nice = {1, 2, 3, 4, 5, 6, 8, 10};
            for (double c : nice) {
                if (f <= c + 1e-9) return c * exp;
            }
            return 10 * exp;
        }

        private static String shortMoney(double v) {
            if (v >= 1e9) return trim(v / 1e9) + " tỷ";
            if (v >= 1e6) return trim(v / 1e6) + " tr";
            if (v >= 1e3) return trim(v / 1e3) + "k";
            return String.valueOf((long) v);
        }

        private static String trim(double x) {
            String s = String.format(Locale.US, "%.1f", x);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s.replace('.', ',');
        }
    }

    // ================================================================== biểu đồ thanh ngang

    static class HBarChart extends JComponent {

        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();
        private List<Color> colors = new ArrayList<>();
        private List<String> texts = new ArrayList<>();

        void setData(List<String> labels, List<Double> values, List<Color> colors, List<String> texts) {
            this.labels = labels;
            this.values = values;
            this.colors = colors;
            this.texts = texts;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            if (isOpaque()) {   // JComponent thuần không tự tô nền
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets in = getInsets();
            int x0 = in.left, x1 = getWidth() - in.right;
            int y0 = in.top, y1 = getHeight() - in.bottom;
            int n = labels.size();
            g.setFont(Theme.font(Font.PLAIN, 13f));
            FontMetrics fm = g.getFontMetrics();

            double max = 0;
            for (double v : values) max = Math.max(max, v);
            if (n == 0 || max <= 0) {
                g.setColor(Theme.MUTED);
                String s = "Chưa có đơn hàng";
                g.drawString(s, x0 + ((x1 - x0) - fm.stringWidth(s)) / 2, y0 + (y1 - y0) / 2);
                g.dispose();
                return;
            }
            int labelW = 0, textW = 0;
            for (String s : labels) labelW = Math.max(labelW, fm.stringWidth(s));
            for (String s : texts) textW = Math.max(textW, fm.stringWidth(s));
            labelW += 10;
            int barX = x0 + labelW;
            int barMax = Math.max(10, (x1 - barX) - textW - 12);
            int rowH = Math.max(14, Math.min(30, (y1 - y0) / n));
            int barH = Math.max(8, rowH - 10);
            int y = y0 + Math.max(0, ((y1 - y0) - rowH * n) / 2);
            for (int i = 0; i < n; i++) {
                int cy = y + i * rowH + rowH / 2;
                g.setColor(Theme.TEXT);
                g.drawString(labels.get(i), x0, cy + fm.getAscent() / 2 - 1);
                int len = (int) Math.round(values.get(i) / max * barMax);
                g.setColor(Style.LINE);
                g.fillRoundRect(barX, cy - barH / 2, barMax, barH, 6, 6);
                if (len > 0) {
                    g.setColor(colors.get(i));
                    g.fillRoundRect(barX, cy - barH / 2, Math.max(len, 6), barH, 6, 6);
                }
                g.setColor(Theme.MUTED);
                g.drawString(texts.get(i), barX + barMax + 8, cy + fm.getAscent() / 2 - 1);
            }
            g.dispose();
        }
    }
}
