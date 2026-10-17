/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import controller.AdminController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import util.Style;
import util.Theme;
/**
 *
 * @author Lenovo
 */
public class ManageCategoryPanel extends javax.swing.JPanel {

    /**
     * Creates new form ManageCategoryPanel
     */
        private final AdminController controller = new AdminController();
    private DefaultTableModel tableModel;
    private List<Object[]> rows = new ArrayList<>();
    private int editingId = 0;          // 0 = đang thêm danh mục mới

    public ManageCategoryPanel() {
        initComponents();

        tableModel = new DefaultTableModel(new Object[]{"Mã", "Tên danh mục", "Mô tả", "Số món"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCategories.setModel(tableModel);
        tblCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        applyStyle();

        tblCategories.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblCategories.getSelectedRow();
            if (row >= 0 && row < rows.size()) fillForm(rows.get(row));
        });
        btnNew.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());

        reload();
        clearForm();
    }

    private void applyStyle() {
        setBackground(Theme.BG);
        Style.page(pnlTop, pnlMain);
        Style.transparent(pnlFields, pnlFormButtons);
        pnlTop.setBorder(Theme.pad(8, 16));
        pnlMain.setBorder(Theme.pad(4, 16));
        ((java.awt.BorderLayout) pnlMain.getLayout()).setHgap(14);
        Style.heading(lblTitle);
        Style.secondary(btnNew);

        Style.card(scrollCategories, "Danh sách danh mục");
        Style.table(tblCategories, 3);
        int[] w = {50, 180, 240, 85};
        for (int i = 0; i < w.length; i++) tblCategories.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        Style.card(pnlForm, "Thêm danh mục mới");
        pnlForm.setPreferredSize(new java.awt.Dimension(380, 0));
        java.awt.GridLayout gl = (java.awt.GridLayout) pnlFields.getLayout();
        gl.setVgap(8);
        gl.setHgap(10);
        Style.label(jLabel1);
        Style.label(jLabel2);
        Style.field(txtName);
        Style.field(txtDesc);
        pnlFormButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        Style.primary(btnSave);
        Style.danger(btnDelete);
    }

    private void setFormTitle(String t) {
        javax.swing.border.Border b = pnlForm.getBorder();
        if (b instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.Border out = ((javax.swing.border.CompoundBorder) b).getOutsideBorder();
            if (out instanceof javax.swing.border.TitledBorder) {
                ((javax.swing.border.TitledBorder) out).setTitle("  " + t + "  ");
            }
        }
        pnlForm.repaint();
    }

    public void reload() {
        int keepId = editingId;
        rows = controller.categoriesWithCount();
        tableModel.setRowCount(0);
        int select = -1;
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            tableModel.addRow(new Object[]{r[0], r[1], r[2] == null ? "" : r[2], r[3]});
            if ((int) r[0] == keepId) select = i;
        }
        if (select >= 0) tblCategories.setRowSelectionInterval(select, select);
    }

    private void fillForm(Object[] r) {
        editingId = (int) r[0];
        setFormTitle("Sửa danh mục #" + editingId);
        txtName.setText((String) r[1]);
        txtDesc.setText(r[2] == null ? "" : (String) r[2]);
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        editingId = 0;
        tblCategories.clearSelection();
        setFormTitle("Thêm danh mục mới");
        txtName.setText("");
        txtDesc.setText("");
        btnDelete.setEnabled(false);
        txtName.requestFocusInWindow();
    }

    private void save() {
        String err = controller.saveCategory(editingId, txtName.getText(), txtDesc.getText());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean adding = editingId == 0;
        reload();
        if (adding) clearForm();
        JOptionPane.showMessageDialog(this, adding ? "Đã thêm danh mục." : "Đã lưu thay đổi.");
    }

    private void delete() {
        if (editingId == 0) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa danh mục \"" + txtName.getText() + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String err = controller.deleteCategory(editingId);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        clearForm();
        reload();
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
        btnNew = new javax.swing.JButton();
        pnlMain = new javax.swing.JPanel();
        scrollCategories = new javax.swing.JScrollPane();
        tblCategories = new javax.swing.JTable();
        pnlForm = new javax.swing.JPanel();
        pnlFields = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtDesc = new javax.swing.JTextField();
        pnlFormButtons = new javax.swing.JPanel();
        btnDelete = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblTitle.setText("Quản lý danh mục");
        pnlTop.add(lblTitle);

        btnNew.setText("+ Thêm danh mục mới");
        pnlTop.add(btnNew);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlMain.setLayout(new java.awt.BorderLayout());

        tblCategories.setModel(new javax.swing.table.DefaultTableModel(
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
        scrollCategories.setViewportView(tblCategories);

        pnlMain.add(scrollCategories, java.awt.BorderLayout.CENTER);

        pnlForm.setLayout(new java.awt.BorderLayout());

        pnlFields.setLayout(new java.awt.GridLayout(2, 2));

        jLabel1.setText("Tên danh mục");
        pnlFields.add(jLabel1);
        pnlFields.add(txtName);

        jLabel2.setText("Mô tả");
        pnlFields.add(jLabel2);
        pnlFields.add(txtDesc);

        pnlForm.add(pnlFields, java.awt.BorderLayout.PAGE_START);

        pnlFormButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnDelete.setText("Xóa");
        pnlFormButtons.add(btnDelete);

        btnSave.setText("Lưu");
        pnlFormButtons.add(btnSave);

        pnlForm.add(pnlFormButtons, java.awt.BorderLayout.CENTER);

        pnlMain.add(pnlForm, java.awt.BorderLayout.EAST);

        add(pnlMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFields;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JPanel pnlFormButtons;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollCategories;
    private javax.swing.JTable tblCategories;
    private javax.swing.JTextField txtDesc;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
