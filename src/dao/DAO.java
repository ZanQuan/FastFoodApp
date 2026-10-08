package dao;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DAO {
    private static final Properties CFG = loadConfig();
    private static Connection shared;      // 1 kết nối dùng chung cho mọi DAO

    protected Connection con;

    public DAO() {
        con = getSharedConnection();
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (Exception e) {
            System.err.println("❌ Không đọc được db.properties: " + e.getMessage());
        }
        return p;
    }

    private static synchronized Connection getSharedConnection() {
        try {
            if (shared == null || shared.isClosed()) {
                String url = "jdbc:sqlserver://" + CFG.getProperty("server", "localhost")
                    + ";databaseName=" + CFG.getProperty("database", "FastFood")
                    + ";user=" + CFG.getProperty("user", "sa")
                    + ";password=" + CFG.getProperty("password", "")
                    + ";encrypt=false;trustServerCertificate=true";

                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                shared = DriverManager.getConnection(url);
                System.out.println("✅ Kết nối SQL Server thành công!");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
            shared = null;
        }
        return shared;
    }

    // Kết nối dùng chung nên không đóng ở đây (đóng khi tắt app)
    public void closeConnection() { }
}
