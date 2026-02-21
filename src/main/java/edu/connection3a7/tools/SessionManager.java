package edu.connection3a7.tools;

import edu.connection3a7.entities.Technicien;

public class SessionManager {

    private static SessionManager instance;
    private Integer idUtilisateurConnecte;
    private Technicien technicienConnecte;
    private String role; // "client" ou "technicien"

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Pour un client
    public void setUtilisateurConnecte(int id) {
        this.idUtilisateurConnecte = id;
        this.role = "client";
    }

    // Pour un technicien
    public void setTechnicienConnecte(Technicien tech) {
        this.technicienConnecte = tech;
        this.idUtilisateurConnecte = tech.getId_utilisateur();
        this.role = "technicien";
    }

    public Integer getIdUtilisateur() {
        return idUtilisateurConnecte;
    }

    public Technicien getTechnicienConnecte() {
        return technicienConnecte;
    }

    public String getRole() {
        return role;
    }

    public boolean isClient() {
        return "client".equals(role);
    }

    public boolean isTechnicien() {
        return "technicien".equals(role);
    }

    public void logout() {
        idUtilisateurConnecte = null;
        technicienConnecte = null;
        role = null;
    }
    public int getIdTechnicien() {
        if (technicienConnecte != null) {
            return technicienConnecte.getId_tech(); // ou getIdTechnicien() selon ta classe
        }
        return 0; // ou -1 si tu préfères
    }
}