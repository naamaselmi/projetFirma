package edu.connection3a7.tools;

import edu.connection3a7.entities.Utilisateur;

/**
 * Singleton qui conserve l'utilisateur connecte pendant toute la session.
 * Appeler SessionManager.getInstance().setUtilisateur(u) apres le login.
 * Appeler SessionManager.getInstance().getUtilisateur() n'importe ou.
 */
public class SessionManager {

    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void clearSession() {
        utilisateurConnecte = null;
    }

    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }
}