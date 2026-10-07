/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import model.User;
/**
 *
 * @author Lenovo
 */
public class AppSession {
    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void clear() { currentUser = null; }
}
