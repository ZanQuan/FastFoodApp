package util;

import java.text.NumberFormat;
import java.util.Locale;

public class Format {
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    static { NF.setMaximumFractionDigits(0); }

    public static String money(double v) {
        return NF.format(v) + " ₫";
    }

    public static String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
