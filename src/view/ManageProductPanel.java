/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;

import controller.AdminController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import model.Category;
import model.Product;
import util.Format;
import util.Style;
import util.Theme;
/**
 *
 * @author Lenovo
 */
public class ManageProductPanel extends javax.swing.JPanel {

    /**
     * Creates new form ManageProductPanel
     */
    private final AdminController controller = new AdminController();
    private DefaultTableModel tableModel;
    private List<Product> products = new ArrayList<>();
    private int editingId = 0;          // 0 = đang thêm món mới
    private int previewToken = 0;       // để bỏ qua ảnh tải chậm của lần chọn cũ
    private javax.swing.Timer previewTimer;

    public ManageProductPanel() {
        initComponents();

        tableModel = new DefaultTableModel(new Object[]{"Tên món", "Danh mục", "Giá",
                "Tồn", "Bán"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblProducts.setModel(tableModel);
        tblProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        spnStock.setModel(new SpinnerNumberModel(0, 0, 100000, 1));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        applyStyle();

        tblProducts.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblProducts.getSelectedRow();
            if (row >= 0 && row < products.size()) fillForm(products.get(row));
        });
        btnSearch.addActionListener(e -> reload());
        txtSearch.addActionListener(e -> reload());
        btnNew.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnBrowse.addActionListener(e -> browse());

        // Xem trước ảnh: tự cập nhật khi ô "Ảnh" đổi (chọn dòng, bấm "...", hoặc gõ đường dẫn)
        previewTimer = new javax.swing.Timer(250, e -> updatePreview());
        previewTimer.setRepeats(false);
        txtImage.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { previewTimer.restart(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { previewTimer.restart(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { previewTimer.restart(); }
        });

        reload();
        clearForm();
    }

    private void applyStyle() {
        setBackground(Theme.BG);
        Style.page(pnlTop, pnlMain);
        Style.transparent(pnlFields, pnlImage, pnlFlags, pnlDesc, pnlFormButtons);
        pnlTop.setBorder(Theme.pad(8, 16));
        pnlMain.setBorder(Theme.pad(4, 16));
        ((java.awt.BorderLayout) pnlMain.getLayout()).setHgap(14);
        Style.heading(lblTitle);
        Style.field(txtSearch);
        txtSearch.setPreferredSize(new java.awt.Dimension(220, 38));
        Style.primary(btnSearch);
        Style.secondary(btnNew);

        Style.card(scrollProducts, "Danh sách món");
        Style.table(tblProducts, 2, 3);
        int[] w = {190, 100, 95, 50, 55};
        for (int i = 0; i < w.length; i++) tblProducts.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        Style.card(pnlForm, "Thêm món mới");
        pnlForm.setPreferredSize(new java.awt.Dimension(400, 0));
        java.awt.GridLayout gl = (java.awt.GridLayout) pnlFields.getLayout();
        gl.setVgap(8);
        gl.setHgap(10);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7}) {
            Style.label(l);
        }
        Style.field(txtName);
        Style.field(txtPrice);
        Style.field(txtImage);
        cboCategory.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        spnStock.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        Style.secondary(btnBrowse);
        btnBrowse.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Theme.PRIMARY, 1), Theme.pad(8, 10)));
        for (javax.swing.JCheckBox c : new javax.swing.JCheckBox[]{chkAvailable, chkFeatured}) {
            c.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
            c.setOpaque(false);
            c.setFocusPainted(false);
        }
        txtDesc.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
        txtDesc.setMargin(new java.awt.Insets(6, 8, 6, 8));
        scrollDesc.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
        pnlDesc.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 8, 0));
        Style.primary(btnSave);
        Style.danger(btnDelete);

        // khung xem trước ảnh
        Style.transparent(pnlPreview);
        Style.label(jLabel8);
        pnlPreview.setPreferredSize(new java.awt.Dimension(0, 210));
        pnlPreview.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        lblPreview.setOpaque(true);
        lblPreview.setBackground(new java.awt.Color(0xFFFBF7));
        lblPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
        lblPreview.setForeground(Theme.MUTED);
        lblPreview.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER));
        lblPreview.setText("Chưa chọn ảnh");
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

    /** MainFrm gọi mỗi lần mở trang này. */
    public void reload() {
        // danh mục cho ComboBox (giữ lại mục đang chọn)
        Category keep = (Category) cboCategory.getSelectedItem();
        cboCategory.removeAllItems();
        for (Category c : controller.categoryList()) cboCategory.addItem(c);
        if (keep != null) selectCategory(keep.getId());

        int keepId = editingId;
        products = controller.products(txtSearch.getText());
        tableModel.setRowCount(0);
        int select = -1;
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            tableModel.addRow(new Object[]{
                (p.isFeatured() ? "★ " : "") + p.getName(), p.getCategoryName(),
                Format.money(p.getPrice()), p.getStock(), p.isAvailable() ? "Bán" : "Ẩn"
            });
            if (p.getId() == keepId) select = i;
        }
        if (select >= 0) tblProducts.setRowSelectionInterval(select, select);
    }

    private void selectCategory(int id) {
        for (int i = 0; i < cboCategory.getItemCount(); i++) {
            if (cboCategory.getItemAt(i).getId() == id) {
                cboCategory.setSelectedIndex(i);
                return;
            }
        }
    }

    private void fillForm(Product p) {
        editingId = p.getId();
        setFormTitle("Sửa món #" + p.getId());
        txtName.setText(p.getName());
        selectCategory(p.getCategoryId());
        txtPrice.setText(String.valueOf((long) p.getPrice()));
        spnStock.setValue(p.getStock());
        txtImage.setText(p.getImageUrl() == null ? "" : p.getImageUrl());
        chkAvailable.setSelected(p.isAvailable());
        chkFeatured.setSelected(p.isFeatured());
        txtDesc.setText(p.getDescription() == null ? "" : p.getDescription());
        txtDesc.setCaretPosition(0);
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        editingId = 0;
        tblProducts.clearSelection();
        setFormTitle("Thêm món mới");
        txtName.setText("");
        txtPrice.setText("");
        spnStock.setValue(0);
        txtImage.setText("");
        chkAvailable.setSelected(true);
        chkFeatured.setSelected(false);
        txtDesc.setText("");
        if (cboCategory.getItemCount() > 0) cboCategory.setSelectedIndex(0);
        btnDelete.setEnabled(false);
        txtName.requestFocusInWindow();
    }

    private void save() {
        String digits = txtPrice.getText().replaceAll("[^0-9]", "");   // cho phép gõ 49.000
        if (digits.isEmpty() || digits.length() > 10) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ (chỉ nhập số, ví dụ 49000).",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Category cat = (Category) cboCategory.getSelectedItem();
        Product p = new Product(editingId, txtName.getText(), txtDesc.getText().trim(),
                Double.parseDouble(digits), (int) spnStock.getValue(), txtImage.getText().trim(),
                chkAvailable.isSelected(), cat == null ? 0 : cat.getId(),
                cat == null ? "" : cat.getName());
        p.setFeatured(chkFeatured.isSelected());

        String err = controller.saveProduct(p);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean adding = editingId == 0;
        reload();
        if (adding) clearForm();
        JOptionPane.showMessageDialog(this, adding ? "Đã thêm món mới." : "Đã lưu thay đổi.");
    }

    private void delete() {
        if (editingId == 0) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa món \"" + txtName.getText() + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String msg = controller.deleteProduct(editingId);
        clearForm();
        reload();
        JOptionPane.showMessageDialog(this, msg);
    }

    /** Tải ảnh ở ô "Ảnh" và hiện vào khung xem trước (giữ đúng tỉ lệ, không làm đơ giao diện). */
    private void updatePreview() {
        String path = txtImage.getText().trim();
        int token = ++previewToken;
        lblPreview.setIcon(null);
        if (path.isEmpty()) {
            lblPreview.setText("Chưa chọn ảnh");
            return;
        }
        lblPreview.setText("Đang tải ảnh...");
        int w = Math.max(100, lblPreview.getWidth() - 12);
        int h = Math.max(100, lblPreview.getHeight() - 12);
        util.ImageUtil.load(path, w, h, icon -> {
            if (token != previewToken) return;       // đã chọn ảnh khác trong lúc chờ
            if (icon == null) {
                lblPreview.setIcon(null);
                lblPreview.setText("Không đọc được ảnh này - kiểm tra lại đường dẫn");
            } else {
                lblPreview.setText(null);
                lblPreview.setIcon(icon);
            }
        });
    }

    private void browse() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Ảnh (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtImage.setText(fc.getSelectedFile().getAbsolutePath());
        }
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
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnNew = new javax.swing.JButton();
        pnlMain = new javax.swing.JPanel();
        scrollProducts = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        pnlForm = new javax.swing.JPanel();
        pnlFields = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cboCategory = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        spnStock = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        pnlImage = new javax.swing.JPanel();
        txtImage = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        pnlFlags = new javax.swing.JPanel();
        chkAvailable = new javax.swing.JCheckBox();
        chkFeatured = new javax.swing.JCheckBox();
        pnlDesc = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        scrollDesc = new javax.swing.JScrollPane();
        txtDesc = new javax.swing.JTextArea();
        pnlPreview = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        lblPreview = new javax.swing.JLabel();
        pnlFormButtons = new javax.swing.JPanel();
        btnDelete = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblTitle.setText("Quản lý sản phẩm");
        pnlTop.add(lblTitle);
        pnlTop.add(txtSearch);

        btnSearch.setText("Tìm");
        pnlTop.add(btnSearch);

        btnNew.setText("+ Thêm món mới");
        pnlTop.add(btnNew);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlMain.setLayout(new java.awt.BorderLayout());

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
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
        scrollProducts.setViewportView(tblProducts);

        pnlMain.add(scrollProducts, java.awt.BorderLayout.CENTER);

        pnlForm.setLayout(new java.awt.BorderLayout());

        pnlFields.setLayout(new java.awt.GridLayout(6, 2));

        jLabel1.setText("Tên món");
        pnlFields.add(jLabel1);
        pnlFields.add(txtName);

        jLabel2.setText("Danh mục");
        pnlFields.add(jLabel2);

        pnlFields.add(cboCategory);

        jLabel3.setText("Giá (₫)");
        pnlFields.add(jLabel3);
        pnlFields.add(txtPrice);

        jLabel4.setText("Tồn kho");
        pnlFields.add(jLabel4);
        pnlFields.add(spnStock);

        jLabel5.setText("Ảnh");
        pnlFields.add(jLabel5);

        pnlImage.setLayout(new java.awt.BorderLayout());
        pnlImage.add(txtImage, java.awt.BorderLayout.CENTER);

        btnBrowse.setText("...");
        pnlImage.add(btnBrowse, java.awt.BorderLayout.EAST);

        pnlFields.add(pnlImage);

        jLabel6.setText("Hiển thị");
        pnlFields.add(jLabel6);

        pnlFlags.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        chkAvailable.setText("Đang bán");
        pnlFlags.add(chkAvailable);

        chkFeatured.setText("Nổi bật");
        pnlFlags.add(chkFeatured);

        pnlFields.add(pnlFlags);

        pnlForm.add(pnlFields, java.awt.BorderLayout.NORTH);

        pnlDesc.setLayout(new java.awt.BorderLayout());

        jLabel7.setText("Mô tả");
        pnlDesc.add(jLabel7, java.awt.BorderLayout.NORTH);

        txtDesc.setColumns(20);
        txtDesc.setRows(5);
        scrollDesc.setViewportView(txtDesc);

        pnlDesc.add(scrollDesc, java.awt.BorderLayout.CENTER);

        pnlPreview.setLayout(new java.awt.BorderLayout());

        jLabel8.setText("Xem trước ảnh");
        pnlPreview.add(jLabel8, java.awt.BorderLayout.NORTH);
        pnlPreview.add(lblPreview, java.awt.BorderLayout.CENTER);

        pnlDesc.add(pnlPreview, java.awt.BorderLayout.SOUTH);

        pnlForm.add(pnlDesc, java.awt.BorderLayout.CENTER);

        pnlFormButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnDelete.setText("Xóa");
        pnlFormButtons.add(btnDelete);

        btnSave.setText("Lưu");
        pnlFormButtons.add(btnSave);

        pnlForm.add(pnlFormButtons, java.awt.BorderLayout.SOUTH);

        pnlMain.add(pnlForm, java.awt.BorderLayout.EAST);

        add(pnlMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<model.Category> cboCategory;
    private javax.swing.JCheckBox chkAvailable;
    private javax.swing.JCheckBox chkFeatured;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlDesc;
    private javax.swing.JPanel pnlFields;
    private javax.swing.JPanel pnlFlags;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JPanel pnlFormButtons;
    private javax.swing.JPanel pnlImage;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlPreview;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollDesc;
    private javax.swing.JScrollPane scrollProducts;
    private javax.swing.JSpinner spnStock;
    private javax.swing.JTable tblProducts;
    private javax.swing.JTextArea txtDesc;
    private javax.swing.JTextField txtImage;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
