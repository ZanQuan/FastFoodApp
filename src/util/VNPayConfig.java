package util;

import java.io.FileInputStream;
import java.util.Properties;

/** Đọc cấu hình VNPay từ vnpay.properties (cùng thư mục với db.properties). */
public class VNPayConfig {
    public final String tmnCode;
    public final String hashSecret;
    public final String payUrl;
    public final int returnPort;

    private VNPayConfig(String tmnCode, String hashSecret, String payUrl, int returnPort) {
        this.tmnCode = tmnCode;
        this.hashSecret = hashSecret;
        this.payUrl = payUrl;
        this.returnPort = returnPort;
    }

    public static VNPayConfig load() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("vnpay.properties")) {
            p.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Chưa có file vnpay.properties (sao chép từ vnpay.properties.example rồi điền TmnCode và HashSecret).");
        }
        String tmn = p.getProperty("tmnCode", "").trim();
        String secret = p.getProperty("hashSecret", "").trim();
        if (tmn.isEmpty() || secret.isEmpty() || tmn.startsWith("DAN_") || secret.startsWith("DAN_")) {
            throw new IllegalStateException("vnpay.properties chưa điền tmnCode / hashSecret.");
        }
        String url = p.getProperty("payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html").trim();
        int port = 8765;
        try { port = Integer.parseInt(p.getProperty("returnPort", "8765").trim()); } catch (NumberFormatException ignored) { }
        return new VNPayConfig(tmn, secret, url, port);
    }
}
