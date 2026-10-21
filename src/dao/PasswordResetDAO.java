package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordResetDAO extends DAO {

    /** Hủy mã cũ của người này rồi lưu mã mới (hết hạn sau `minutes` phút). */
    public boolean create(int userId, String codeHash, int minutes) {
        if (con == null) return false;
        try (PreparedStatement off = con.prepareStatement("UPDATE PasswordResets SET Used = 1 WHERE UserId = ? AND Used = 0");
             PreparedStatement ins = con.prepareStatement(
                 "INSERT INTO PasswordResets(UserId, CodeHash, ExpiresAt) VALUES (?, ?, DATEADD(MINUTE, ?, SYSDATETIME()))")) {
            off.setInt(1, userId);
            off.executeUpdate();
            ins.setInt(1, userId);
            ins.setString(2, codeHash);
            ins.setInt(3, minutes);
            return ins.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Số giây kể từ lần gửi mã gần nhất (Integer.MAX_VALUE nếu chưa từng gửi). */
    public int secondsSinceLast(int userId) {
        if (con == null) return Integer.MAX_VALUE;
        String sql = "SELECT DATEDIFF(SECOND, MAX(CreatedAt), SYSDATETIME()) FROM PasswordResets WHERE UserId = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt(1);
                    return rs.wasNull() ? Integer.MAX_VALUE : v;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    /** Mã còn hiệu lực gần nhất: {id (Integer), codeHash, attempts (Integer)} hoặc null. */
    public Object[] findActive(int userId) {
        if (con == null) return null;
        String sql = "SELECT TOP 1 Id, CodeHash, Attempts FROM PasswordResets "
                   + "WHERE UserId = ? AND Used = 0 AND ExpiresAt > SYSDATETIME() ORDER BY Id DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3)};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addAttempt(int id) { exec("UPDATE PasswordResets SET Attempts = Attempts + 1 WHERE Id = ?", id); }

    public void markUsed(int id) { exec("UPDATE PasswordResets SET Used = 1 WHERE Id = ?", id); }

    private void exec(String sql, int id) {
        if (con == null) return;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
