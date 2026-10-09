/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import controller.AdminController;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import model.Order;
import model.OrderDetail;
import javax.swing.table.DefaultTableModel;
import util.Format;
import util.Style;
import util.Theme;
/**
 *
 * @author Lenovo
 */
public class ManageOrderPanel extends javax.swing.JPanel {

    /**
     * Creates new form ManageOrderPanel
     */
    private static final SimpleDateFormat DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final AdminController controller = new AdminController();
    private DefaultTableModel orderModel;
    private DefaultTableModel itemModel;
    private List<Order> orders = new ArrayList<>();
    private int lastPending = -1;

    public ManageOrderPanel() {
        initComponents();

        cboStatus.addItem("Tất cả");
        for (String s : Order.FLOW) cboStatus.addItem(s);
        cboStatus.addItem(Order.DA_HUY);

        orderModel = new DefaultTableModel(new Object[]{"Mã đơn", "Ngày đặt", "Người nhận",
                "SĐT", "Tổng tiền", "Thanh toán", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblOrders.setModel(orderModel);
        tblOrders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemModel = new DefaultTableModel(new Object[]{"Món", "Đơn giá", "SL", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblItems.setModel(itemModel);

        applyStyle();

        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
        cboStatus.addActionListener(e -> refresh(false));
        btnSearch.addActionListener(e -> refresh(false));
        txtSearch.addActionListener(e -> refresh(false));
        btnRefresh.addActionListener(e -> refresh(false));
        btnNext.addActionListener(e -> advance());
        btnCancel.addActionListener(e -> cancelOrder());

        // Đơn mới xuất hiện tự động: 10 giây tải lại 1 lần khi trang đang hiện
        new Timer(10000, e -> { if (isShowing()) refresh(true); }).start();
        refresh(false);
    }

    private void applyStyle() {
        setBackground(Theme.BG);
        Style.page(pnlTop, pnlMain);
        Style.transparent(pnlBottom);
        Style.bar(pnlBottom);
        Style.heading(lblTitle);
        Style.label(jLabel1);
        Style.field(txtSearch);
        txtSearch.setPreferredSize(new java.awt.Dimension(220, 38));
        cboStatus.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        Style.primary(btnSearch);
        pnlMain.setBorder(Theme.pad(4, 16));
        ((java.awt.GridLayout) pnlMain.getLayout()).setVgap(12);
        Style.card(scrollOrders, "Danh sách đơn hàng");
        Style.card(pnlDetail, "Chi tiết đơn");
        Style.table(tblOrders, 4);
        Style.table(tblItems, 1, 2, 3);
        Style.statusColumn(tblOrders, 6);
        int[] w = {80, 150, 150, 110, 110, 190, 130};   // độ rộng từng cột
        for (int i = 0; i < w.length; i++) tblOrders.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        tblItems.getColumnModel().getColumn(0).setPreferredWidth(260);
        pnlTop.setBorder(Theme.pad(8, 16));
        Style.transparent(lblInfo);
        lblInfo.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        lblInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        scrollItems.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
        lblSummary.setFont(Theme.font(java.awt.Font.BOLD, 14f));
        lblSummary.setForeground(Theme.PRIMARY_DARK);
        Style.secondary(btnRefresh);
        Style.danger(btnCancel);
        Style.primary(btnNext);
    }

    public void reload() { refresh(false); }

    private void refresh(boolean notify) {
        int keepId = selectedId();
        String status = cboStatus.getSelectedIndex() <= 0 ? "" : (String) cboStatus.getSelectedItem();
        orders = controller.orders(status, txtSearch.getText());

        orderModel.setRowCount(0);
        int select = orders.isEmpty() ? -1 : 0;
        int pending = 0;
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            if (Order.CHO_XAC_NHAN.equals(o.getStatus())) pending++;
            orderModel.addRow(new Object[]{
                "#" + o.getId(),
                o.getOrderDate() == null ? "" : DATE.format(o.getOrderDate()),
                o.getReceiverName() == null ? "(không tên)" : o.getReceiverName(),
                o.getPhone() == null ? "" : o.getPhone(),
                Format.money(o.getTotalPrice()),
                o.getPaymentMethod() + " - " + o.getPaymentStatus(),
                o.getStatus()
            });
            if (o.getId() == keepId) select = i;
        }
        lblSummary.setText(orders.size() + " đơn - " + pending + " chờ xác nhận");
        if (select >= 0) tblOrders.setRowSelectionInterval(select, select);
        else showDetail();

        // đơn mới (chỉ báo khi đang xem tất cả / chờ xác nhận và số đơn chờ tăng lên)
        if (notify && status.isEmpty() && lastPending >= 0 && pending > lastPending) {
            JOptionPane.showMessageDialog(this, "Có " + (pending - lastPending) + " đơn mới đang chờ xác nhận!",
                    "Đơn hàng mới", JOptionPane.INFORMATION_MESSAGE);
        }
        if (status.isEmpty()) lastPending = pending;
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
            lblInfo.setText(" ");
            btnNext.setText("Chuyển bước");
            btnNext.setEnabled(false);
            btnCancel.setEnabled(false);
            return;
        }
        for (OrderDetail d : controller.details(o.getId())) {
            itemModel.addRow(new Object[]{d.getProductName(), Format.money(d.getUnitPrice()),
                d.getQuantity(), Format.money(d.getSubtotal())});
        }
        lblInfo.setText("<html>Đơn <b>#" + o.getId() + "</b> - giao đến <b>"
                + Format.html(o.getReceiverName()) + "</b> - " + Format.html(o.getPhone())
                + " - " + Format.html(o.getAddress())
                + (o.getNote() == null ? "" : "<br>Ghi chú: <i>" + Format.html(o.getNote()) + "</i>")
                + "</html>");
        String next = Order.next(o.getStatus());
        btnNext.setText(next == null ? "Chuyển bước" : "→ " + next);
        btnNext.setEnabled(next != null);
        btnCancel.setEnabled(Order.staffCanCancel(o.getStatus()));
    }

    private void advance() {
        Order o = selectedOrder();
        if (o == null) return;
        String err = controller.advance(o);
        if (err != null) JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
        refresh(false);
    }

    private void cancelOrder() {
        Order o = selectedOrder();
        if (o == null) return;
        if (JOptionPane.showConfirmDialog(this, "Hủy đơn #" + o.getId() + "? Tồn kho sẽ được hoàn lại.",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String err = controller.cancel(o);
        if (err != null) JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
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
        jLabel1 = new javax.swing.JLabel();
        cboStatus = new javax.swing.JComboBox<>();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        pnlMain = new javax.swing.JPanel();
        scrollOrders = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();
        pnlDetail = new javax.swing.JPanel();
        lblInfo = new javax.swing.JLabel();
        scrollItems = new javax.swing.JScrollPane();
        tblItems = new javax.swing.JTable();
        pnlBottom = new javax.swing.JPanel();
        lblSummary = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setText("Quản lý đơn hàng");
        pnlTop.add(lblTitle);

        jLabel1.setText("Trạng thái:");
        pnlTop.add(jLabel1);

        cboStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pnlTop.add(cboStatus);
        pnlTop.add(txtSearch);

        btnSearch.setText("Tìm");
        pnlTop.add(btnSearch);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlMain.setLayout(new java.awt.GridLayout(2, 1));

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

        pnlDetail.setLayout(new java.awt.BorderLayout());

        lblInfo.setText("x");
        pnlDetail.add(lblInfo, java.awt.BorderLayout.NORTH);

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

        lblSummary.setText("x");
        pnlBottom.add(lblSummary);

        btnRefresh.setText("Làm mới");
        pnlBottom.add(btnRefresh);

        btnCancel.setText("Hủy đơn");
        pnlBottom.add(btnCancel);

        btnNext.setText("Chuyển bước");
        pnlBottom.add(btnNext);

        add(pnlBottom, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cboStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblSummary;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlDetail;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollItems;
    private javax.swing.JScrollPane scrollOrders;
    private javax.swing.JTable tblItems;
    private javax.swing.JTable tblOrders;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
