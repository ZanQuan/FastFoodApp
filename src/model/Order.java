/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.Date;

/**
 *
 * @author Lenovo
 */
public class Order {
    private int    id;
    private int    userId;
    private String address;
    private String note;
    private Date   orderDate;
    private double totalPrice;
    private String status;
    private String paymentMethod;

    public Order(int id, int userId, String address, String note,
                  Date orderDate, double total, String status, String payment) {
        this.id = id; this.userId = userId; this.address = address;
        this.note = note; this.orderDate = orderDate;
        this.totalPrice = total; this.status = status;
        this.paymentMethod = payment;
    }

    public int getId() { return id; }
    public String getAddress() { return address; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }
    public Date getOrderDate() { return orderDate; }
    public String getPaymentMethod() { return paymentMethod; }
}
