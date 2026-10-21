package controller;

import dao.PasswordResetDAO;
import dao.UserDAO;
import java.security.SecureRandom;
import util.Mailer;
import util.PasswordUtil;

/** Quên mật khẩu: gửi mã 6 số qua email -> nhập mã + mật khẩu mới. */
public class PasswordResetController {
    private static final int CODE_MINUTES = 10;
    private static final int RESEND_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;
    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    private final UserDAO userDAO = new UserDAO();
    private final PasswordResetDAO resetDAO = new PasswordResetDAO();

    /** Gửi mã. Trả về null nếu OK, ngược lại là thông báo lỗi. (Có thể mất vài giây: gọi ở luồng nền.) */
    public String requestCode(String email) {
        email = email == null ? "" : email.trim();
        if (email.isEmpty()) return "Vui lòng nhập email.";
        if (!email.matches(EMAIL_REGEX)) return "Email không hợp lệ.";

        int userId = userDAO.findIdByEmail(email);
        if (userId < 0) return null;     // không tiết lộ email có tồn tại hay không

        int wait = RESEND_SECONDS - resetDAO.secondsSinceLast(userId);
        if (wait > 0) return "Vui lòng đợi " + wait + " giây rồi gửi lại mã.";

        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        if (!resetDAO.create(userId, PasswordUtil.hash(code), CODE_MINUTES)) {
            return "Không lưu được mã (kiểm tra kết nối cơ sở dữ liệu).";
        }
        try {
            Mailer.send(email, "Mã đặt lại mật khẩu FastFood",
                "Xin chào,\n\n"
                + "Mã xác nhận đặt lại mật khẩu FastFood của bạn là:\n\n    " + code + "\n\n"
                + "Mã có hiệu lực trong " + CODE_MINUTES + " phút. Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
                + "FastFood");
        } catch (RuntimeException e) {
            return e.getMessage();
        }
        return null;
    }

    /** Đổi mật khẩu bằng mã. Trả về null nếu OK, ngược lại là thông báo lỗi. */
    public String resetPassword(String email, String code, String newPass, String confirm) {
        email = email == null ? "" : email.trim();
        code = code == null ? "" : code.trim();
        if (email.isEmpty()) return "Vui lòng nhập email.";
        if (!code.matches("\\d{6}")) return "Mã xác nhận gồm 6 chữ số.";
        if (newPass == null || newPass.length() < 6) return "Mật khẩu mới tối thiểu 6 ký tự.";
        if (!newPass.equals(confirm)) return "Mật khẩu nhập lại không khớp.";

        int userId = userDAO.findIdByEmail(email);
        Object[] active = userId < 0 ? null : resetDAO.findActive(userId);
        if (active == null) return "Mã không đúng hoặc đã hết hạn. Hãy gửi mã mới.";

        int resetId = (int) active[0];
        if ((int) active[2] >= MAX_ATTEMPTS) {
            resetDAO.markUsed(resetId);
            return "Bạn nhập sai quá nhiều lần. Hãy gửi mã mới.";
        }
        if (!PasswordUtil.verify(code, (String) active[1])) {
            resetDAO.addAttempt(resetId);
            int left = MAX_ATTEMPTS - ((int) active[2] + 1);
            return left > 0 ? "Mã không đúng (còn " + left + " lần thử)." : "Bạn nhập sai quá nhiều lần. Hãy gửi mã mới.";
        }
        if (!userDAO.updatePassword(userId, newPass)) return "Không đổi được mật khẩu (kiểm tra kết nối cơ sở dữ liệu).";
        resetDAO.markUsed(resetId);
        return null;
    }
}
