/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package view;
import controller.PasswordResetController;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import util.Style;
import util.Theme;
/**
 *
 * @author Lenovo
 */
public class ForgotPasswordDlg extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ForgotPasswordDlg.class.getName());

    /**
     * Creates new form ForgotPasswordDlg
     */
    private final PasswordResetController controller = new PasswordResetController();
    private javax.swing.Timer cooldown;
    private int secondsLeft = 0;

    public ForgotPasswordDlg(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("FastFood - Quên mật khẩu");
        setResizable(false);
        applyStyle();
        setSteps(false);

        btnSend.addActionListener(e -> sendCode());
        btnReset.addActionListener(e -> resetPassword());
        btnCancel.addActionListener(e -> dispose());
        txtEmail.addActionListener(e -> sendCode());

        cooldown = new javax.swing.Timer(1000, e -> tick());
        pack();
        setSize(620, getHeight());
        setLocationRelativeTo(parent);
    }

    /** LoginFrm gọi để điền sẵn email người dùng đã gõ. */
    public void setEmail(String email) {
        txtEmail.setText(email == null ? "" : email);
    }

    private void applyStyle() {
        getContentPane().setBackground(Theme.BG);
        Style.page(pnlTop, pnlFields, pnlBottom, pnlEmail, pnlButtons);
        pnlTop.setBorder(Theme.pad(14, 20));
        pnlFields.setBorder(Theme.pad(4, 20));
        pnlBottom.setBorder(Theme.pad(10, 20));

        Style.heading(lblTitle);
        lblHint.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
        lblHint.setForeground(Theme.MUTED);
        lblHint.setText("Nhập email đã đăng ký, chúng tôi sẽ gửi mã 6 số để đặt lại mật khẩu.");

        java.awt.GridLayout gl = (java.awt.GridLayout) pnlFields.getLayout();
        gl.setVgap(12);
        gl.setHgap(12);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{jLabel1, jLabel2, jLabel3, jLabel4}) Style.label(l);
        for (javax.swing.JTextField f : new javax.swing.JTextField[]{txtEmail, txtCode, txtNew, txtConfirm}) Style.field(f);
        ((java.awt.BorderLayout) pnlEmail.getLayout()).setHgap(8);
        Style.primary(btnSend);
        btnSend.setPreferredSize(new java.awt.Dimension(145, 34));

        Style.secondary(btnCancel);
        Style.primary(btnReset);
        lblMsg.setFont(Theme.font(java.awt.Font.PLAIN, 13f));
        lblMsg.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblMsg.setPreferredSize(new java.awt.Dimension(300, 64));
        showMsg("", false);
    }

    private void showMsg(String text, boolean error) {
        lblMsg.setForeground(error ? Theme.DANGER : new java.awt.Color(0x2B8A3E));
        lblMsg.setText("<html>" + text + "</html>");
    }

    /** false: mới chỉ nhập email; true: đã gửi mã, cho nhập mã + mật khẩu mới. */
    private void setSteps(boolean codeSent) {
        txtCode.setEnabled(codeSent);
        txtNew.setEnabled(codeSent);
        txtConfirm.setEnabled(codeSent);
        btnReset.setEnabled(codeSent);
    }

    private void sendCode() {
        if (!btnSend.isEnabled()) return;
        final String email = txtEmail.getText().trim();
        btnSend.setEnabled(false);
        btnSend.setText("Đang gửi...");
        showMsg("", false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return controller.requestCode(email);
                } catch (RuntimeException ex) {
                    return ex.getMessage();
                }
            }
            @Override
            protected void done() {
                String err;
                try { err = get(); } catch (Exception ex) { err = "Có lỗi khi gửi mã."; }
                if (err != null) {
                    showMsg(err, true);
                    btnSend.setText("Gửi mã");
                    btnSend.setEnabled(true);
                    return;
                }
                showMsg("Nếu email đã đăng ký, mã 6 số đã được gửi (hiệu lực 10 phút). Hãy xem cả mục Spam.", false);
                setSteps(true);
                secondsLeft = 60;
                tick();
                cooldown.start();
                txtCode.requestFocusInWindow();
            }
        }.execute();
    }

    private void tick() {
        if (secondsLeft > 0) {
            btnSend.setText("Gửi lại (" + secondsLeft + "s)");
            secondsLeft--;
        } else {
            cooldown.stop();
            btnSend.setText("Gửi lại mã");
            btnSend.setEnabled(true);
        }
    }

    private void resetPassword() {
        String err = controller.resetPassword(txtEmail.getText(), txtCode.getText(),
                new String(txtNew.getPassword()), new String(txtConfirm.getPassword()));
        if (err != null) {
            showMsg(err, true);
            return;
        }
        JOptionPane.showMessageDialog(this, "Đã đổi mật khẩu. Bạn có thể đăng nhập bằng mật khẩu mới.");
        dispose();
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
        lblHint = new javax.swing.JLabel();
        pnlFields = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pnlEmail = new javax.swing.JPanel();
        txtEmail = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtNew = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        txtConfirm = new javax.swing.JPasswordField();
        pnlBottom = new javax.swing.JPanel();
        lblMsg = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlTop.setLayout(new java.awt.GridLayout(2, 1));

        lblTitle.setText("Quên mật khẩu");
        pnlTop.add(lblTitle);
        pnlTop.add(lblHint);

        getContentPane().add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(4, 2));

        jLabel1.setText("Email");
        pnlFields.add(jLabel1);

        pnlEmail.setLayout(new java.awt.BorderLayout());
        pnlEmail.add(txtEmail, java.awt.BorderLayout.CENTER);

        btnSend.setText("Gửi mã");
        pnlEmail.add(btnSend, java.awt.BorderLayout.EAST);

        pnlFields.add(pnlEmail);

        jLabel2.setText("Mã xác nhận");
        pnlFields.add(jLabel2);
        pnlFields.add(txtCode);

        jLabel3.setText("Mật khẩu mới");
        pnlFields.add(jLabel3);

        txtNew.setText("jPasswordField1");
        pnlFields.add(txtNew);

        jLabel4.setText("Nhập lại mật khẩu");
        pnlFields.add(jLabel4);

        txtConfirm.setText("jPasswordField1");
        pnlFields.add(txtConfirm);

        getContentPane().add(pnlFields, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.BorderLayout());
        pnlBottom.add(lblMsg, java.awt.BorderLayout.CENTER);

        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnCancel.setText("Đóng");
        pnlButtons.add(btnCancel);

        btnReset.setText("Đổi mật khẩu");
        pnlButtons.add(btnReset);

        pnlBottom.add(pnlButtons, java.awt.BorderLayout.EAST);

        getContentPane().add(pnlBottom, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ForgotPasswordDlg dialog = new ForgotPasswordDlg(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblMsg;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlEmail;
    private javax.swing.JPanel pnlFields;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JTextField txtCode;
    private javax.swing.JPasswordField txtConfirm;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JPasswordField txtNew;
    // End of variables declaration//GEN-END:variables
}
