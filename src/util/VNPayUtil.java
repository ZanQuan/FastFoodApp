package util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Tạo link thanh toán và kiểm tra chữ ký VNPay (phiên bản 2.1.0, HMAC-SHA512). */
public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được chữ ký: " + e.getMessage(), e);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.US_ASCII);
    }

    /** Ghép các tham số đã sắp xếp theo tên thành chuỗi k=v&k=v (giá trị đã mã hóa). */
    static String joinSorted(Map<String, String> params) {
        List<String> names = new ArrayList<>(params.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (String n : names) {
            String v = params.get(n);
            if (v == null || v.isEmpty()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(enc(n)).append('=').append(enc(v));
        }
        return sb.toString();
    }

    public static String buildPaymentUrl(VNPayConfig cfg, String txnRef, double amountVnd,
                                         String orderInfo, String returnUrl) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        Map<String, String> p = new TreeMap<>();
        p.put("vnp_Version", "2.1.0");
        p.put("vnp_Command", "pay");
        p.put("vnp_TmnCode", cfg.tmnCode);
        p.put("vnp_Amount", String.valueOf(Math.round(amountVnd) * 100L));   // VNPay yêu cầu nhân 100
        p.put("vnp_CurrCode", "VND");
        p.put("vnp_TxnRef", txnRef);
        p.put("vnp_OrderInfo", orderInfo);
        p.put("vnp_OrderType", "other");
        p.put("vnp_Locale", "vn");
        p.put("vnp_ReturnUrl", returnUrl);
        p.put("vnp_IpAddr", "127.0.0.1");
        p.put("vnp_CreateDate", f.format(now));
        p.put("vnp_ExpireDate", f.format(now.plusMinutes(15)));
        String query = joinSorted(p);
        return cfg.payUrl + "?" + query + "&vnp_SecureHash=" + hmacSHA512(cfg.hashSecret, query);
    }

    /** Kiểm tra chữ ký của tham số VNPay trả về (đã giải mã). */
    public static boolean verify(VNPayConfig cfg, Map<String, String> returned) {
        String received = returned.get("vnp_SecureHash");
        if (received == null) return false;
        Map<String, String> p = new TreeMap<>(returned);
        p.remove("vnp_SecureHash");
        p.remove("vnp_SecureHashType");
        p.keySet().removeIf(k -> !k.startsWith("vnp_"));
        String expected = hmacSHA512(cfg.hashSecret, joinSorted(p));
        return expected.equalsIgnoreCase(received);
    }
}
