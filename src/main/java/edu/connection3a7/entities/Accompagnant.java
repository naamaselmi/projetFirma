package edu.connection3a7.entities;

public class Accompagnant {
    private int idAccompagnant;
    private int idParticipation;
    private String nom;
    private String prenom;

    public Accompagnant() {}

    public Accompagnant(int idAccompagnant, int idParticipation, String nom, String prenom) {
        this.idAccompagnant = idAccompagnant;
        this.idParticipation = idParticipation;
        this.nom = nom;
        this.prenom = prenom;
    }

    public Accompagnant(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getIdAccompagnant() {
        return idAccompagnant;
    }

    public void setIdAccompagnant(int idAccompagnant) {
        this.idAccompagnant = idAccompagnant;
    }

    public int getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public String toString() {
        return "Accompagnant{" +
                "idAccompagnant=" + idAccompagnant +
                ", idParticipation=" + idParticipation +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
}
