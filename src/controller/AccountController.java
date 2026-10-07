/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dao.UserDAO;
import model.User;
import util.AppSession;
/**
 *
 * @author Lenovo
 */
public class AccountController {
    private final UserDAO userDAO = new UserDAO();

    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    // Trả về null nếu đăng ký thành công, ngược lại trả về thông báo lỗi
    public String register(String email, String fullName, String password, String confirm) {
        if (email.isEmpty() || password.isEmpty())  return "Vui lòng nhập email và mật khẩu.";
        if (!email.matches(EMAIL_REGEX))            return "Email không hợp lệ.";
        if (password.length() < 6)                  return "Mật khẩu tối thiểu 6 ký tự.";
        if (!password.equals(confirm))              return "Mật khẩu nhập lại không khớp.";
        if (userDAO.emailExists(email))             return "Email đã được đăng ký.";
        if (!userDAO.create(email, fullName, password)) return "Không thể tạo tài khoản (kiểm tra kết nối DB).";

        // Theo sơ đồ: đăng ký xong tự đăng nhập
        User u = userDAO.login(email, password);
        AppSession.setCurrentUser(u);
        return null;
    }

    // Trả về null nếu đăng nhập sai
    public User login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) return null;
        User u = userDAO.login(email, password);
        if (u != null) AppSession.setCurrentUser(u);
        return u;
    }

    public void logout() {
        AppSession.clear();
    }
}
