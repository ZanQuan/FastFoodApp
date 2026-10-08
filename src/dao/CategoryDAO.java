package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Category;

public class CategoryDAO extends DAO {

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        if (con == null) return list;
        String sql = "SELECT Id, Name, Description FROM Categories ORDER BY Name";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("Id"), rs.getString("Name"),
                                      rs.getString("Description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Category getById(int id) {
        if (con == null) return null;
        String sql = "SELECT Id, Name, Description FROM Categories WHERE Id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("Id"), rs.getString("Name"),
                                        rs.getString("Description"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
