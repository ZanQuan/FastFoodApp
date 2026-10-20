package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.User;
import util.PasswordUtil;

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

    // Trả về User nếu đúng email + mật khẩu và tài khoản KHÔNG bị khóa, ngược lại null
    public User login(String email, String password) {
        if (con == null) return null;
        String sql = "SELECT Id, Email, FullName, Role, PasswordHash, IsLocked FROM Users WHERE Email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && !rs.getBoolean("IsLocked")
                        && PasswordUtil.verify(password, rs.getString("PasswordHash"))) {
                    return new User(rs.getInt("Id"), rs.getString("Email"),
                                    rs.getString("FullName"), rs.getString("Role"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Tài khoản này có đang bị khóa không (dùng để báo đúng lý do ở màn hình đăng nhập). */
    public boolean isLocked(String email) {
        if (con == null) return false;
        String sql = "SELECT IsLocked FROM Users WHERE Email = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- Dành cho Admin ----------

    /** Mỗi dòng: {id (Integer), email, fullName, role, locked (Boolean)} */
    public List<Object[]> getAll(String keyword) {
        List<Object[]> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT Id, Email, FullName, Role, IsLocked FROM Users "
                   + "WHERE (? = N'' OR Email LIKE ? OR FullName LIKE ?) ORDER BY Id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String k = keyword == null ? "" : keyword.trim();
            ps.setString(1, k);
            ps.setString(2, "%" + k + "%");
            ps.setString(3, "%" + k + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getInt("Id"), rs.getString("Email"),
                        rs.getString("FullName"), rs.getString("Role"), rs.getBoolean("IsLocked")});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateRole(int id, String role) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("UPDATE Users SET Role = ? WHERE Id = ?")) {
            ps.setString(1, role);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setLocked(int id, boolean locked) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("UPDATE Users SET IsLocked = ? WHERE Id = ?")) {
            ps.setBoolean(1, locked);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(int id, String newPassword) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("UPDATE Users SET PasswordHash = ? WHERE Id = ?")) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
