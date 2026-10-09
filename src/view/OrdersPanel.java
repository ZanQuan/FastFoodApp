/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import controller.OrderController;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Order;
import model.OrderDetail;
import util.Format;
import util.Style;
import util.Theme;

/**
 *
 * @author Lenovo
 */
public class OrdersPanel extends javax.swing.JPanel {
 
    /**
     * Creates new form OrdersPanel
     */
    private static final SimpleDateFormat DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm");
 
    private ShopFrm shell;
    private final OrderController controller = new OrderController();
    private DefaultTableModel orderModel;
    private DefaultTableModel itemModel;
    private List<Order> orders = new ArrayList<>();
    private final Map<Integer, String> lastStatus = new HashMap<>();
 
    public OrdersPanel(ShopFrm shell) {
        this.shell = shell;
        initComponents();
 
        orderModel = new DefaultTableModel(
                new Object[]{"Mã đơn", "Ngày đặt", "Tổng tiền", "Thanh toán", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders.setModel(orderModel);
        tblOrders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
        itemModel = new DefaultTableModel(
                new Object[]{"Món", "Đơn giá", "SL", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblItems.setModel(itemModel);
 
        applyStyle();
 
        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
 
        btnRefresh.addActionListener(e -> reload());
        btnCancel.addActionListener(e -> cancelOrder());
        btnReceived.addActionListener(e -> confirmReceived());
 
        // "Thông báo" theo kiểu polling: 10 giây tải lại 1 lần khi trang đang hiện
        new Timer(10000, e -> { if (isShowing()) refresh(true); }).start();
        refresh(false);
    }
 
    private void applyStyle() {
        setBackground(Theme.BG);
        Style.page(pnlTop, pnlMain);
        Style.transparent(pnlInfo);
        Style.bar(pnlBottom);
        Style.heading(lblTitle);
        pnlMain.setBorder(Theme.pad(4, 16));
        ((java.awt.GridLayout) pnlMain.getLayout()).setVgap(12);
        Style.card(scrollOrders, "Lịch sử đơn hàng");
        Style.card(pnlDetail, "Chi tiết & theo dõi");
        Style.table(tblOrders, 2);
        Style.table(tblItems, 1, 2, 3);
        tblOrders.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblOrders.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, false, row, col);
                setBorder(Theme.pad(0, 12));
                setFont(Theme.font(java.awt.Font.BOLD, 14f));
                String s = String.valueOf(v);
                if (Order.DA_HUY.equals(s)) setForeground(Theme.DANGER);
                else if (Order.HOAN_THANH.equals(s)) setForeground(new java.awt.Color(0x2B8A3E));
                else setForeground(Theme.PRIMARY_DARK);
                if (!sel) setBackground(row % 2 == 0 ? java.awt.Color.WHITE : Style.ZEBRA);
                return this;
            }
        });
 
        pnlInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        ((java.awt.GridLayout) pnlInfo.getLayout()).setVgap(6);
        lblTrack.setFont(Theme.font(java.awt.Font.PLAIN, 15f));
        lblShip.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        lblShip.setForeground(Theme.TEXT);
        scrollItems.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
 
        Style.primary(btnReceived);
        Style.secondary(btnRefresh);
        Style.danger(btnCancel);
    }
 
    public void reload() { refresh(false); }
 
    private void refresh(boolean notify) {
        int keepId = selectedId();
        orders = controller.myOrders();
 
        StringBuilder changes = new StringBuilder();
        for (Order o : orders) {
            String prev = lastStatus.get(o.getId());
            if (notify && prev != null && !prev.equals(o.getStatus())) {
                changes.append("Đơn #").append(o.getId()).append(" chuyển sang: ")
                       .append(o.getStatus()).append("\n");
            }
            lastStatus.put(o.getId(), o.getStatus());
        }
 
        orderModel.setRowCount(0);
        int select = orders.isEmpty() ? -1 : 0;
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            orderModel.addRow(new Object[]{
                "#" + o.getId(),
                o.getOrderDate() == null ? "" : DATE.format(o.getOrderDate()),
                Format.money(o.getTotalPrice()),
                o.getPaymentMethod() + " - " + o.getPaymentStatus(),
                o.getStatus()
            });
            if (o.getId() == keepId) select = i;
        }
        if (select >= 0) tblOrders.setRowSelectionInterval(select, select);
        else showDetail();
 
        if (changes.length() > 0) {
            JOptionPane.showMessageDialog(this, changes.toString().trim(),
                    "Cập nhật đơn hàng", JOptionPane.INFORMATION_MESSAGE);
        }
    }
 
    private Order selectedOrder() {
        int row = tblOrders.getSelectedRow();
        if (row < 0 || row >= orders.size()) return null;
        return orders.get(row);
    }
 
    private int selectedId() {
        Order o = selectedOrder();
        return o == null ? -1 : o.getId();
    }
 
    private void showDetail() {
        itemModel.setRowCount(0);
        Order o = selectedOrder();
        if (o == null) {
            lblTrack.setText("Chưa có đơn hàng nào.");
            lblShip.setText(" ");
            btnCancel.setEnabled(false);
            btnReceived.setEnabled(false);
            return;
        }
        for (OrderDetail d : controller.details(o.getId())) {
            itemModel.addRow(new Object[]{
                d.getProductName(), Format.money(d.getUnitPrice()),
                d.getQuantity(), Format.money(d.getSubtotal())
            });
        }
        lblTrack.setText(trackHtml(o));
        lblShip.setText("<html>Giao đến: <b>" + Format.html(o.getReceiverName()) + "</b> - "
                + Format.html(o.getPhone()) + " - " + Format.html(o.getAddress())
                + (o.getNote() == null ? "" : " (Ghi chú: " + Format.html(o.getNote()) + ")")
                + "</html>");
        btnCancel.setEnabled(Order.CHO_XAC_NHAN.equals(o.getStatus()));
        btnReceived.setEnabled(Order.DANG_GIAO.equals(o.getStatus())
                || Order.DA_GIAO.equals(o.getStatus()));
    }
 
    /** Thanh tiến trình:  bước đã qua / đang ở,  bước chưa tới. */
    private String trackHtml(Order o) {
        if (Order.DA_HUY.equals(o.getStatus())) {
            return "<html><font color='#C92A2A'><b>Đơn hàng đã hủy</b></font></html>";
        }
        int current = Arrays.asList(Order.FLOW).indexOf(o.getStatus());
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < Order.FLOW.length; i++) {
            if (i > 0) sb.append(" &nbsp;&rarr;&nbsp; ");
            if (i <= current) {
                sb.append("<font color='#E8590C'><b>&#9679; ").append(Order.FLOW[i]).append("</b></font>");
            } else {
                sb.append("<font color='#999999'>&#9675; ").append(Order.FLOW[i]).append("</font>");
            }
        }
        return sb.append("</html>").toString();
    }
 
    private void cancelOrder() {
        Order o = selectedOrder();
        if (o == null) return;
        if (JOptionPane.showConfirmDialog(this, "Hủy đơn #" + o.getId() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (controller.cancel(o.getId())) {
            JOptionPane.showMessageDialog(this, "Đã hủy đơn #" + o.getId() + ".");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể hủy: đơn đã được cửa hàng xác nhận.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
        }
        refresh(false);
    }
 
    private void confirmReceived() {
        Order o = selectedOrder();
        if (o == null) return;
        if (controller.confirmReceived(o.getId())) {
            JOptionPane.showMessageDialog(this, "Cảm ơn bạn! Đơn #" + o.getId() + " đã hoàn thành.");
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xác nhận đơn này.");
        }
        refresh(false);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTop = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlMain = new javax.swing.JPanel();
        scrollOrders = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();
        pnlDetail = new javax.swing.JPanel();
        pnlInfo = new javax.swing.JPanel();
        lblTrack = new javax.swing.JLabel();
        lblShip = new javax.swing.JLabel();
        scrollItems = new javax.swing.JScrollPane();
        tblItems = new javax.swing.JTable();
        pnlBottom = new javax.swing.JPanel();
        btnRefresh = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnReceived = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setText("Đơn hàng của tôi");
        pnlTop.add(lblTitle);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlMain.setLayout(new java.awt.GridLayout(2, 1, 8, 8));

        scrollOrders.setBorder(javax.swing.BorderFactory.createTitledBorder("Lịch sử đơn hàng"));

        tblOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollOrders.setViewportView(tblOrders);

        pnlMain.add(scrollOrders);

        pnlDetail.setBorder(javax.swing.BorderFactory.createTitledBorder("Chi tiết & theo dõi"));
        pnlDetail.setLayout(new java.awt.BorderLayout());

        pnlInfo.setLayout(new java.awt.GridLayout(2, 1));

        lblTrack.setText("jLabel1");
        pnlInfo.add(lblTrack);

        lblShip.setText("jLabel1");
        pnlInfo.add(lblShip);

        pnlDetail.add(pnlInfo, java.awt.BorderLayout.NORTH);

        tblItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollItems.setViewportView(tblItems);

        pnlDetail.add(scrollItems, java.awt.BorderLayout.CENTER);

        pnlMain.add(pnlDetail);

        add(pnlMain, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnRefresh.setText("Làm mới");
        pnlBottom.add(btnRefresh);

        btnCancel.setText("Hủy đơn");
        pnlBottom.add(btnCancel);

        btnReceived.setText("Đã nhận hàng");
        pnlBottom.add(btnReceived);

        add(pnlBottom, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnReceived;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JLabel lblShip;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTrack;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlDetail;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollItems;
    private javax.swing.JScrollPane scrollOrders;
    private javax.swing.JTable tblItems;
    private javax.swing.JTable tblOrders;
    // End of variables declaration//GEN-END:variables
}
