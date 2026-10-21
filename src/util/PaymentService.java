package util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.OrderDAO;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Thanh toán VNPay cho ứng dụng desktop:
 *  1. mở trình duyệt tới trang thanh toán VNPay,
 *  2. VNPay chuyển trình duyệt về http://localhost:PORT/vnpay-return (máy chủ nhỏ chạy ngay trong app),
 *  3. kiểm tra chữ ký, cập nhật đơn hàng, báo kết quả cho giao diện.
 */
public class PaymentService {

    public static class Result {
        public final int orderId;
        public final boolean success;
        public final String message;
        public Result(int orderId, boolean success, String message) {
            this.orderId = orderId; this.success = success; this.message = message;
        }
    }

    private static final Map<Integer, Consumer<Result>> pending = new ConcurrentHashMap<>();
    private static HttpServer server;
    private static int port;
    private static VNPayConfig config;

    /** Cho phép thay thế khi kiểm thử; mặc định ghi vào cơ sở dữ liệu. */
    static BiPredicate<Integer, Double> markPaid = (id, amount) -> new OrderDAO().markPaid(id, amount);

    /** Mở trang thanh toán. Ném lỗi (IllegalStateException) với thông báo tiếng Việt nếu không làm được. */
    public static synchronized void pay(int orderId, double amount, Consumer<Result> onDone) {
        VNPayConfig cfg = VNPayConfig.load();
        config = cfg;
        ensureServer(cfg.returnPort);
        String txnRef = orderId + "A" + (System.currentTimeMillis() / 1000);   // mỗi lần thanh toán một mã riêng
        String returnUrl = "http://localhost:" + port + "/vnpay-return";
        String url = VNPayUtil.buildPaymentUrl(cfg, txnRef, amount, "Thanh toan don hang " + orderId, returnUrl);
        pending.put(orderId, onDone);
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new IllegalStateException("Máy này không mở được trình duyệt từ ứng dụng.");
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            pending.remove(orderId);
            throw new IllegalStateException("Không mở được trình duyệt: " + e.getMessage(), e);
        }
    }

    private static void ensureServer(int basePort) {
        if (server != null) return;
        IOException last = null;
        for (int p = basePort; p < basePort + 10; p++) {
            try {
                HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), p), 0);
                s.createContext("/vnpay-return", PaymentService::handleReturn);
                s.setExecutor(Executors.newCachedThreadPool(r -> {
                    Thread t = new Thread(r, "vnpay-return");
                    t.setDaemon(true);
                    return t;
                }));
                s.start();
                server = s;
                port = p;
                return;
            } catch (IOException e) {
                last = e;
            }
        }
        throw new IllegalStateException("Không mở được cổng nhận kết quả VNPay: " + (last == null ? "" : last.getMessage()));
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null) return m;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            if (i <= 0) continue;
            m.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8),
                  URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }

    private static void handleReturn(HttpExchange ex) throws IOException {
        Result result;
        try {
            result = process(parseQuery(ex.getRequestURI().getRawQuery()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            result = new Result(0, false, "Có lỗi khi xử lý kết quả thanh toán.");
        }
        Consumer<Result> cb = result.orderId > 0 ? pending.remove(result.orderId) : null;
        if (cb != null) cb.accept(result);

        String color = result.success ? "#2B8A3E" : "#C92A2A";
        String html = "<!doctype html><html><head><meta charset='utf-8'><title>FastFood</title></head>"
                + "<body style='font-family:Segoe UI,Arial,sans-serif;background:#FAF7F2;text-align:center;padding-top:90px'>"
                + "<h1 style='color:" + color + "'>" + (result.success ? "Thanh toán thành công" : "Chưa thanh toán được") + "</h1>"
                + "<p style='font-size:18px'>" + result.message + "</p>"
                + "<p style='color:#777'>Bạn có thể đóng trang này và quay lại ứng dụng FastFood.</p></body></html>";
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }

        if (pending.isEmpty()) stopLater();
    }

    /** Tách riêng để dễ kiểm thử. */
    static Result process(Map<String, String> q) {
        VNPayConfig cfg = config != null ? config : VNPayConfig.load();
        String ref = q.getOrDefault("vnp_TxnRef", "");
        int a = ref.indexOf('A');
        int orderId = 0;
        try { orderId = Integer.parseInt(a > 0 ? ref.substring(0, a) : ref); } catch (NumberFormatException ignored) { }

        if (orderId <= 0 || !VNPayUtil.verify(cfg, q)) {
            return new Result(0, false, "Dữ liệu trả về không hợp lệ (sai chữ ký).");
        }
        String code = q.getOrDefault("vnp_ResponseCode", "");
        String status = q.getOrDefault("vnp_TransactionStatus", "");
        if (!"00".equals(code) || !"00".equals(status)) {
            String why = "24".equals(code) ? "Bạn đã hủy thanh toán." : "Giao dịch không thành công (mã " + code + ").";
            return new Result(orderId, false, why + " Đơn #" + orderId + " vẫn được giữ, bạn có thể thanh toán lại trong mục Đơn hàng.");
        }
        double amount;
        try { amount = Long.parseLong(q.getOrDefault("vnp_Amount", "0")) / 100.0; }
        catch (NumberFormatException e) { amount = 0; }
        if (markPaid.test(orderId, amount)) {
            return new Result(orderId, true, "Đơn #" + orderId + " đã được thanh toán. Cảm ơn bạn!");
        }
        return new Result(orderId, false, "Đã nhận tiền nhưng không cập nhật được đơn #" + orderId
                + " (đơn đã hủy hoặc số tiền không khớp). Vui lòng liên hệ cửa hàng.");
    }

    private static void stopLater() {
        Thread t = new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
            synchronized (PaymentService.class) {
                if (pending.isEmpty() && server != null) {
                    server.stop(0);
                    server = null;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
