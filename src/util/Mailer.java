package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.SSLSocketFactory;

/** Gửi email qua SMTP (SSL, ví dụ Gmail cổng 465) bằng JDK thuần, không cần thư viện ngoài. */
public class Mailer {

    private static Properties load() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("mail.properties")) {
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Chưa có file mail.properties (sao chép từ mail.properties.example rồi điền).");
        }
        String u = p.getProperty("username", "").trim();
        String pw = p.getProperty("password", "").trim();
        if (u.isEmpty() || pw.isEmpty() || u.startsWith("DIA_CHI") || pw.startsWith("DAN_")) {
            throw new IllegalStateException("mail.properties chưa điền username / password.");
        }
        return p;
    }

    private static String b64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String encodeHeader(String s) {
        return "=?UTF-8?B?" + b64(s) + "?=";
    }

    /** Ném IllegalStateException (thông báo tiếng Việt) khi không gửi được. */
    public static void send(String to, String subject, String body) {
        if (to == null || !to.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("Địa chỉ email người nhận không hợp lệ.");
        }
        Properties cfg = load();
        String host = cfg.getProperty("host", "smtp.gmail.com").trim();
        int port = Integer.parseInt(cfg.getProperty("port", "465").trim());
        boolean ssl = !"false".equalsIgnoreCase(cfg.getProperty("ssl", "true").trim());
        String user = cfg.getProperty("username").trim();
        String pass = cfg.getProperty("password").trim();
        String fromName = cfg.getProperty("fromName", "FastFood").trim();

        try (Socket socket = ssl ? SSLSocketFactory.getDefault().createSocket(host, port) : new Socket(host, port)) {
            socket.setSoTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            expect(in, "220");
            cmd(out, in, "EHLO fastfood-app", "250");
            cmd(out, in, "AUTH LOGIN", "334");
            cmd(out, in, b64(user), "334");
            String r = cmd(out, in, b64(pass), "235");
            cmd(out, in, "MAIL FROM:<" + user + ">", "250");
            cmd(out, in, "RCPT TO:<" + to + ">", "250");
            cmd(out, in, "DATA", "354");

            StringBuilder msg = new StringBuilder();
            msg.append("From: ").append(encodeHeader(fromName)).append(" <").append(user).append(">\r\n");
            msg.append("To: <").append(to).append(">\r\n");
            msg.append("Subject: ").append(encodeHeader(subject)).append("\r\n");
            msg.append("MIME-Version: 1.0\r\n");
            msg.append("Content-Type: text/plain; charset=UTF-8\r\n");
            msg.append("Content-Transfer-Encoding: base64\r\n\r\n");
            String enc = Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII))
                    .encodeToString(body.getBytes(StandardCharsets.UTF_8));
            msg.append(enc).append("\r\n.\r\n");
            out.write(msg.toString());
            out.flush();
            expect(in, "250");
            try { cmd(out, in, "QUIT", "221"); } catch (Exception ignored) { }
        } catch (IOException e) {
            throw new IllegalStateException("Không kết nối được máy chủ email: " + e.getMessage(), e);
        }
    }

    private static String cmd(BufferedWriter out, BufferedReader in, String line, String okCode) throws IOException {
        out.write(line + "\r\n");
        out.flush();
        return expect(in, okCode);
    }

    /** Đọc hết một phản hồi (có thể nhiều dòng "250-...") và kiểm tra mã. */
    private static String expect(BufferedReader in, String okCode) throws IOException {
        String line;
        String last = "";
        do {
            line = in.readLine();
            if (line == null) throw new IOException("Máy chủ email đóng kết nối.");
            last = line;
        } while (line.length() > 3 && line.charAt(3) == '-');
        if (!last.startsWith(okCode)) {
            if (last.startsWith("535")) {
                throw new IllegalStateException("Gmail từ chối đăng nhập: kiểm tra lại username và MẬT KHẨU ỨNG DỤNG trong mail.properties.");
            }
            throw new IOException("Máy chủ email trả lỗi: " + last);
        }
        return last;
    }
}
