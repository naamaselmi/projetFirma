package com.examen.firmapi.utils;

import com.examen.firmapi.entities.Utilisateur;

public class UserSession {

    private static Utilisateur currentUser;

    // Set logged user
    public static void setUser(Utilisateur user) {
        currentUser = user;

        System.out.println("=================================");
        System.out.println("🔐 SESSION CREATED");
        System.out.println("User ID: " + user.getId_utilisateur());
        System.out.println("Name: " + user.getPrenom() + " " + user.getNom());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("=================================");
    }

    // Get logged user
    public static Utilisateur getUser() {

        if (currentUser == null) {
            System.out.println("⚠️ No active session.");
        } else {
            System.out.println("ℹ️ Session active for: " + currentUser.getEmail());
        }

        return currentUser;
    }

    // Clear session (logout)
    public static void clear() {

        if (currentUser != null) {
            System.out.println("=================================");
            System.out.println("🚪 LOGOUT");
            System.out.println("User logged out: " + currentUser.getEmail());
            System.out.println("=================================");
        }

        currentUser = null;
    }

    public static boolean isLoggedIn() {
        boolean status = currentUser != null;
        System.out.println("Session status: " + status);
        return status;
    }
}