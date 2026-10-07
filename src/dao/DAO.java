/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Lenovo
 */
public class DAO {
    private static final java.util.Properties CFG = loadConfig();

    private static java.util.Properties loadConfig() {
        java.util.Properties p = new java.util.Properties();
        try (java.io.FileInputStream in = new java.io.FileInputStream("db.properties")) {
        p.load(in);
        } catch (Exception e) {
        System.err.println("❌ Không đọc được db.properties: " + e.getMessage());
        }
        return p;
    }

    private static final String SERVER   = CFG.getProperty("server", "localhost");
    private static final String DATABASE = CFG.getProperty("database", "FastFood");
    private static final String USER     = CFG.getProperty("user", "sa");
    private static final String PASSWORD = CFG.getProperty("password", "");

    protected Connection con;

    public DAO() {
        try {
            String url = "jdbc:sqlserver://" + SERVER
                + ";databaseName=" + DATABASE
                + ";user=" + USER
                + ";password=" + PASSWORD
                + ";encrypt=false;trustServerCertificate=true";

            // Load driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(url);
            System.out.println("✅ Kết nối SQL Server thành công!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
        }
    }

    // Đóng kết nối
    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
