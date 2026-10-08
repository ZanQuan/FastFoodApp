/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import controller.CartController;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import model.CartItem;
import util.Format;
import util.Theme;

/**
 *
 * @author Lenovo
 */
public class CartPanel extends javax.swing.JPanel {

    /**
     * Creates new form CartPanel
     */
    private ShopFrm shell;
    private final CartController controller = new CartController();
    private DefaultTableModel tableModel;

    public CartPanel(ShopFrm shell) {
        this.shell = shell;
        initComponents();

        tableModel = new DefaultTableModel(
                new Object[]{"Món", "Đơn giá", "Số lượng", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblCartItems.setModel(tableModel);
        tblCartItems.setRowHeight(28);
        javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
        right.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        right.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        javax.swing.table.DefaultTableCellRenderer left = new javax.swing.table.DefaultTableCellRenderer();
        left.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        tblCartItems.getColumnModel().getColumn(0).setCellRenderer(left);
        for (int c = 1; c <= 3; c++) {
            tblCartItems.getColumnModel().getColumn(c).setCellRenderer(right);
        }
        tblCartItems.getTableHeader().setReorderingAllowed(false);
        tblCartItems.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        tblCartItems.getTableHeader().setFont(Theme.font(java.awt.Font.BOLD, 14f));
        tblCartItems.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblCartItems.getColumnModel().getColumn(0).setPreferredWidth(300);
        tblCartItems.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        tblCartItems.getTableHeader().setFont(Theme.font(java.awt.Font.BOLD, 14f));
        spnQty.setModel(new SpinnerNumberModel(1, 1, 99, 1));

        // Khi chọn một dòng thì spinner hiện số lượng hiện tại
        tblCartItems.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            CartItem it = selectedItem();
            if (it != null) {
                int max = Math.max(1, it.getStock());
                spnQty.setModel(new SpinnerNumberModel(
                        Math.min(it.getQuantity(), max), 1, max, 1));
            }
        });

        btnRemove.addActionListener(e -> removeSelected());
        btnClear.addActionListener(e -> clearAll());
        btnCheckout.addActionListener(e -> checkout());

        reload();
    }

    public void reload() {
        tableModel.setRowCount(0);
        for (CartItem it : controller.index()) {
            tableModel.addRow(new Object[]{
                it.getName(),
                Format.money(it.getPrice()),
                it.getQuantity(),
                Format.money(it.getSubtotal())
            });
        }
        lblTotal.setText("Tổng: " + Format.money(controller.total()));
        boolean has = !controller.index().isEmpty();
        btnUpdate.setEnabled(has);
        btnRemove.setEnabled(has);
        btnClear.setEnabled(has);
        btnCheckout.setEnabled(has);
    }

    private CartItem selectedItem() {
        int row = tblCartItems.getSelectedRow();
        if (row < 0 || row >= controller.index().size()) {
            return null;
        }
        return controller.index().get(row);
    }

    private void updateQuantity() {
        CartItem it = selectedItem();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một món trong bảng.");
            return;
        }
        String err = controller.updateQuantity(it.getProductId(), (int) spnQty.getValue());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err);
        }
        reload();
    }

    private void removeSelected() {
        CartItem it = selectedItem();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một món để xóa.");
            return;
        }
        controller.remove(it.getProductId());
        reload();
    }

    private void clearAll() {
        if (JOptionPane.showConfirmDialog(this, "Xóa toàn bộ giỏ hàng?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.clear();
            reload();
        }
    }

    private void checkout() {
        if (controller.index().isEmpty()) {
            return;
        }
        shell.showCheckout();
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
        scrollCart = new javax.swing.JScrollPane();
        tblCartItems = new javax.swing.JTable();
        pnlBottom = new javax.swing.JPanel();
        pnlActions = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        spnQty = new javax.swing.JSpinner();
        btnUpdate = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        pnlPay = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        btnCheckout = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setText("Giỏ hàng của bạn");
        pnlTop.add(lblTitle);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        tblCartItems.setModel(new javax.swing.table.DefaultTableModel(
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
        scrollCart.setViewportView(tblCartItems);

        add(scrollCart, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.BorderLayout());

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Số lượng:");
        pnlActions.add(jLabel1);
        pnlActions.add(spnQty);

        btnUpdate.setText("Cập nhật");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);
        pnlActions.add(btnUpdate);

        btnRemove.setText("Xóa món");
        pnlActions.add(btnRemove);

        btnClear.setText("Xóa tất cả");
        pnlActions.add(btnClear);

        pnlBottom.add(pnlActions, java.awt.BorderLayout.WEST);

        pnlPay.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotal.setText("Tổng: 0 ₫");
        pnlPay.add(lblTotal);

        btnCheckout.setText("Thanh toán");
        pnlPay.add(btnCheckout);

        pnlBottom.add(pnlPay, java.awt.BorderLayout.EAST);

        add(pnlBottom, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateQuantity();
    }//GEN-LAST:event_btnUpdateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckout;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlActions;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlPay;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollCart;
    private javax.swing.JSpinner spnQty;
    private javax.swing.JTable tblCartItems;
    // End of variables declaration//GEN-END:variables
}
