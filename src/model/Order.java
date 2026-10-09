package model;

import java.util.Date;

public class Order {
    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DANG_CHUAN_BI = "Đang chuẩn bị";
    public static final String DANG_GIAO = "Đang giao";
    public static final String DA_GIAO = "Đã giao";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String DA_HUY = "Đã hủy";

    /** Thứ tự các bước của một đơn bình thường (dùng cho thanh tiến trình). */
    public static final String[] FLOW = {CHO_XAC_NHAN, DANG_CHUAN_BI, DANG_GIAO, DA_GIAO, HOAN_THANH};

    private int id;
    private int userId;
    private String receiverName;
    private String phone;
    private String address;
    private String note;
    private Date orderDate;
    private double totalPrice;
    private String status;
    private String paymentMethod;
    private String paymentStatus;

    /** Bước tiếp theo nhân viên được phép chuyển. null = không còn bước nào cho nhân viên. */
    public static String next(String status) {
        if (CHO_XAC_NHAN.equals(status)) return DANG_CHUAN_BI;
        if (DANG_CHUAN_BI.equals(status)) return DANG_GIAO;
        if (DANG_GIAO.equals(status)) return DA_GIAO;
        return null;   // "Đã giao" -> khách bấm "Đã nhận hàng" để Hoàn thành
    }

    /** Nhân viên chỉ được hủy khi đơn chưa giao đi. */
    public static boolean staffCanCancel(String status) {
        return CHO_XAC_NHAN.equals(status) || DANG_CHUAN_BI.equals(status);
    }

    public Order() {}

    public Order(int id, int userId, String receiverName, String phone, String address,
                 String note, Date orderDate, double totalPrice, String status,
                 String paymentMethod, String paymentStatus) {
        this.id = id; this.userId = userId; this.receiverName = receiverName;
        this.phone = phone; this.address = address; this.note = note;
        this.orderDate = orderDate; this.totalPrice = totalPrice; this.status = status;
        this.paymentMethod = paymentMethod; this.paymentStatus = paymentStatus;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getReceiverName() { return receiverName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNote() { return note; }
    public Date getOrderDate() { return orderDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setReceiverName(String v) { this.receiverName = v; }
    public void setPhone(String v) { this.phone = v; }
    public void setAddress(String v) { this.address = v; }
    public void setNote(String v) { this.note = v; }
    public void setTotalPrice(double v) { this.totalPrice = v; }
    public void setStatus(String s) { this.status = s; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public void setPaymentStatus(String v) { this.paymentStatus = v; }
}
