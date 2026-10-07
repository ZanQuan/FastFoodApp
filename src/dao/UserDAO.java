/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.User;
import util.PasswordUtil;
/**
 *
 * @author Lenovo
 */
public class UserDAO extends DAO {

    public boolean emailExists(String email) {
        if (con == null) return false;
        String sql = "SELECT 1 FROM Users WHERE Email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tạo tài khoản mới, role mặc định "User"
    public boolean create(String email, String fullName, String password) {
        if (con == null) return false;
        String sql = "INSERT INTO Users(Username, Email, PasswordHash, FullName, Role) "
                   + "VALUES (?, ?, ?, ?, N'User')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);                       // Username = Email
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, fullName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Trả về User nếu đúng email + mật khẩu, ngược lại null
    public User login(String email, String password) {
        if (con == null) return null;
        String sql = "SELECT Id, Email, FullName, Role, PasswordHash FROM Users WHERE Email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("PasswordHash"))) {
                    return new User(rs.getInt("Id"), rs.getString("Email"),
                                    rs.getString("FullName"), rs.getString("Role"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
