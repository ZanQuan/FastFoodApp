package util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

public class ImageUtil {

    // Tải ảnh ở luồng nền (không làm đơ giao diện). path có thể là http(s)://... hoặc đường dẫn file.
    // Khi xong gọi callback với ImageIcon, hoặc null nếu không tải được.
    public static void load(String path, int w, int h, Consumer<ImageIcon> callback) {
        if (path == null || path.trim().isEmpty()) { callback.accept(null); return; }
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                BufferedImage img = path.startsWith("http")
                        ? ImageIO.read(java.net.URI.create(path).toURL())
                        : ImageIO.read(new File(path));
                if (img == null) return null;
                // Co ảnh vừa khung, giữ tỉ lệ
                double scale = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
                int nw = Math.max(1, (int) (img.getWidth() * scale));
                int nh = Math.max(1, (int) (img.getHeight() * scale));
                return new ImageIcon(img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH));
            }

            @Override
            protected void done() {
                try { callback.accept(get()); }
                catch (Exception e) { callback.accept(null); }
            }
        }.execute();
    }
}
