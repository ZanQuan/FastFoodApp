/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view;
import controller.AdminController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import util.Style;
import util.Theme;

/**
 *
 * @author Lenovo
 */
public class ManageUserPanel extends javax.swing.JPanel {

    /**
     * Creates new form ManageUserPanel
     */
    private final AdminController controller = new AdminController();
    private DefaultTableModel tableModel;
    private List<Object[]> rows = new ArrayList<>();   // {id, email, fullName, role, locked}
    private int selectedId = 0;                        // 0 = chưa chọn ai

    public ManageUserPanel() {
        initComponents();

        tableModel = new DefaultTableModel(new Object[]{"Email", "Họ tên", "Vai trò", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUsers.setModel(tableModel);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cboRole.setModel(new DefaultComboBoxModel<>(new String[]{"User", "NhanVien", "Admin"}));

        applyStyle();

        tblUsers.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblUsers.getSelectedRow();
            if (row >= 0 && row < rows.size()) fillForm(rows.get(row));
            else clearForm();
        });
        btnSearch.addActionListener(e -> { selectedId = 0; reload(); });
        txtSearch.addActionListener(e -> { selectedId = 0; reload(); });
        btnSaveRole.addActionListener(e -> saveRole());
        btnToggleLock.addActionListener(e -> toggleLock());
        btnResetPass.addActionListener(e -> resetPassword());

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
        Style.field(txtSearch);
        txtSearch.setPreferredSize(new java.awt.Dimension(220, 34));
        Style.primary(btnSearch);

        // bảng
        Style.card(scrollUsers, "Danh sách tài khoản");
        Style.table(tblUsers);
        int[] w = {180, 120, 100, 90};
        for (int i = 0; i < w.length; i++) tblUsers.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        tblUsers.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable tb, Object v,
                    boolean sel, boolean foc, int row, int c) {
                super.getTableCellRendererComponent(tb, v, sel, false, row, c);
                setBorder(Theme.pad(0, 12));
                setFont(Theme.font(java.awt.Font.BOLD, 13f));
                boolean locked = "Đã khóa".equals(String.valueOf(v));
                setForeground(locked ? Theme.DANGER : new java.awt.Color(0x2B8A3E));
                if (!sel) setBackground(row % 2 == 0 ? java.awt.Color.WHITE : Style.ZEBRA);
                return this;
            }
        });

        // form bên phải
        Style.card(pnlForm, "Tài khoản");
        pnlForm.setPreferredSize(new java.awt.Dimension(380, 0));
        java.awt.GridLayout gl = (java.awt.GridLayout) pnlFields.getLayout();
        gl.setVgap(10);
        gl.setHgap(10);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{jLabel1, jLabel2, jLabel3, jLabel4}) Style.label(l);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblEmail, lblName}) {
            l.setFont(Theme.font(java.awt.Font.PLAIN, 14f));
            l.setForeground(Theme.TEXT);
        }
        lblStatus.setFont(Theme.font(java.awt.Font.BOLD, 14f));
        cboRole.setFont(Theme.font(java.awt.Font.PLAIN, 14f));

        lblNote.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
        lblNote.setForeground(Theme.MUTED);
        lblNote.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblNote.setBorder(Theme.pad(10, 0));

        java.awt.GridLayout bl = (java.awt.GridLayout) pnlFormButtons.getLayout();
        bl.setVgap(8);
        pnlFormButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        Style.primary(btnSaveRole);
        Style.danger(btnToggleLock);
        Style.secondary(btnResetPass);
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

    private boolean isMe(int id) {
        model.User me = util.AppSession.getCurrentUser();
        return me != null && me.getId() == id;
    }

    /** MainFrm gọi mỗi lần mở trang này. */
    public void reload() {
        rows = controller.users(txtSearch.getText());
        tableModel.setRowCount(0);
        int select = -1;
        for (int i = 0; i < rows.size(); i++) {
            Object[] r = rows.get(i);
            boolean locked = (boolean) r[4];
            tableModel.addRow(new Object[]{r[1], r[2] == null ? "" : r[2], r[3], locked ? "Đã khóa" : "Hoạt động"});
            if ((int) r[0] == selectedId) select = i;
        }
        if (select >= 0) tblUsers.setRowSelectionInterval(select, select);
        else clearForm();
    }

    private Object[] current() {
        for (Object[] r : rows) if ((int) r[0] == selectedId) return r;
        return null;
    }

    private void fillForm(Object[] r) {
        selectedId = (int) r[0];
        boolean me = isMe(selectedId);
        boolean locked = (boolean) r[4];
        setFormTitle("Tài khoản #" + selectedId + (me ? " (bạn)" : ""));
        lblEmail.setText((String) r[1]);
        lblName.setText(r[2] == null ? "" : (String) r[2]);
        cboRole.setSelectedItem(r[3]);
        lblStatus.setText(locked ? "Đã khóa" : "Hoạt động");
        lblStatus.setForeground(locked ? Theme.DANGER : new java.awt.Color(0x2B8A3E));
        btnToggleLock.setText(locked ? "Mở khóa tài khoản" : "Khóa tài khoản");
        cboRole.setEnabled(!me);
        btnSaveRole.setEnabled(!me);
        btnToggleLock.setEnabled(!me);
        btnResetPass.setEnabled(true);
        lblNote.setText(me
            ? "<html>Đây là tài khoản của bạn nên không thể tự đổi vai trò hoặc tự khóa.</html>"
            : "<html>Khóa: tài khoản không đăng nhập được nữa nhưng đơn hàng vẫn được giữ.<br>"
              + "Đặt lại mật khẩu: tạo mật khẩu tạm, bạn gửi cho người dùng.</html>");
    }

    private void clearForm() {
        selectedId = 0;
        setFormTitle("Tài khoản");
        lblEmail.setText("-");
        lblName.setText("-");
        lblStatus.setText("-");
        lblStatus.setForeground(Theme.MUTED);
        btnToggleLock.setText("Khóa tài khoản");
        cboRole.setEnabled(false);
        btnSaveRole.setEnabled(false);
        btnToggleLock.setEnabled(false);
        btnResetPass.setEnabled(false);
        lblNote.setText("<html>Chọn một tài khoản trong bảng bên trái để xem và thao tác.</html>");
    }

    private void saveRole() {
        Object[] r = current();
        if (r == null) return;
        String role = (String) cboRole.getSelectedItem();
        if (role.equals(r[3])) {
            JOptionPane.showMessageDialog(this, "Vai trò chưa thay đổi.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Đổi vai trò của " + r[1] + " từ " + r[3] + " sang " + role + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String err = controller.changeRole(selectedId, role);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        reload();
        JOptionPane.showMessageDialog(this, "Đã đổi vai trò. Người này cần đăng nhập lại để có hiệu lực.");
    }

    private void toggleLock() {
        Object[] r = current();
        if (r == null) return;
        boolean lock = !(boolean) r[4];
        if (JOptionPane.showConfirmDialog(this,
                (lock ? "Khóa" : "Mở khóa") + " tài khoản " + r[1] + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String err = controller.setLocked(selectedId, lock);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        reload();
    }

    private void resetPassword() {
        Object[] r = current();
        if (r == null) return;
        if (JOptionPane.showConfirmDialog(this,
                "Đặt lại mật khẩu của " + r[1] + "?\nMật khẩu cũ sẽ không dùng được nữa.",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String pw = controller.resetPassword(selectedId);
        if (pw == null) {
            JOptionPane.showMessageDialog(this, "Không đặt lại được mật khẩu (xem cửa sổ Output).",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        javax.swing.JTextField tf = new javax.swing.JTextField(pw);
        tf.setEditable(false);
        tf.setFont(Theme.font(java.awt.Font.BOLD, 20f));
        tf.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        JOptionPane.showMessageDialog(this,
                new Object[]{"Mật khẩu tạm của " + r[1] + " (bôi đen để sao chép):", tf,
                             "Hãy gửi cho người dùng. Mật khẩu này chỉ hiện một lần."},
                "Mật khẩu mới", JOptionPane.INFORMATION_MESSAGE);
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
        pnlMain = new javax.swing.JPanel();
        scrollUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        pnlForm = new javax.swing.JPanel();
        pnlFields = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cboRole = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblNote = new javax.swing.JLabel();
        pnlFormButtons = new javax.swing.JPanel();
        btnSaveRole = new javax.swing.JButton();
        btnToggleLock = new javax.swing.JButton();
        btnResetPass = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblTitle.setText("Quản lý người dùng");
        pnlTop.add(lblTitle);
        pnlTop.add(txtSearch);

        btnSearch.setText("Tìm");
        pnlTop.add(btnSearch);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlMain.setLayout(new java.awt.BorderLayout());

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
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
        scrollUsers.setViewportView(tblUsers);

        pnlMain.add(scrollUsers, java.awt.BorderLayout.CENTER);

        pnlForm.setLayout(new java.awt.BorderLayout());

        pnlFields.setLayout(new java.awt.GridLayout(4, 2));

        jLabel1.setText("Email");
        pnlFields.add(jLabel1);
        pnlFields.add(lblEmail);

        jLabel2.setText("Họ tên");
        pnlFields.add(jLabel2);
        pnlFields.add(lblName);

        jLabel3.setText("Vai trò");
        pnlFields.add(jLabel3);

        cboRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pnlFields.add(cboRole);

        jLabel4.setText("Trạng thái");
        pnlFields.add(jLabel4);
        pnlFields.add(lblStatus);

        pnlForm.add(pnlFields, java.awt.BorderLayout.NORTH);
        pnlForm.add(lblNote, java.awt.BorderLayout.CENTER);

        pnlFormButtons.setLayout(new java.awt.GridLayout(3, 1));

        btnSaveRole.setText("Lưu vai trò");
        pnlFormButtons.add(btnSaveRole);

        btnToggleLock.setText("Khóa tài khoản");
        pnlFormButtons.add(btnToggleLock);

        btnResetPass.setText("Đặt lại mật khẩu");
        pnlFormButtons.add(btnResetPass);

        pnlForm.add(pnlFormButtons, java.awt.BorderLayout.SOUTH);

        pnlMain.add(pnlForm, java.awt.BorderLayout.EAST);

        add(pnlMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnResetPass;
    private javax.swing.JButton btnSaveRole;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnToggleLock;
    private javax.swing.JComboBox<String> cboRole;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNote;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFields;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JPanel pnlFormButtons;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrollUsers;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
