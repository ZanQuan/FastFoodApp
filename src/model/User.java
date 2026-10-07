/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.User to edit this template
 */
package model;

/**
 *
 * @author Lenovo
 */
public class User {
    private int id;
    private String email;
    private String fullName;
    private String role; // User | NhanVien | Admin

    public User() {}

    public User(int id, String email, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin()    { return "Admin".equalsIgnoreCase(role); }
    public boolean isNhanVien() { return "NhanVien".equalsIgnoreCase(role); }
}
