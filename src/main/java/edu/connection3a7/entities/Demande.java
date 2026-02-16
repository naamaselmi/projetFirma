package edu.connection3a7.entities;

import java.sql.Date;
import java.util.Objects;

public class Demande {

    private Integer idDemande;        // id_demande (PK)
    private Integer idUtilisateur;    // id_utilisateur (FK)
    private String typeProbleme;      // type_probleme
    private String description;       // description
    private Date dateDemande;         // date_demande
    private String statut;            // statut
    private Integer idTech;           // id_tech (FK)

    // ===== Constructors =====
    public Demande() {
        // constructeur vide
    }

    public Demande(Integer idDemande, Integer idUtilisateur, String typeProbleme,
                   String description, Date dateDemande,
                   String statut, Integer idTech) {

        this.idDemande = idDemande;
        this.idUtilisateur = idUtilisateur;
        this.typeProbleme = typeProbleme;
        this.description = description;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.idTech = idTech;
    }

    // ===== Getters & Setters =====
    public Integer getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(Integer idDemande) {
        this.idDemande = idDemande;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getTypeProbleme() {
        return typeProbleme;
    }

    public void setTypeProbleme(String typeProbleme) {
        this.typeProbleme = typeProbleme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getIdTech() {
        return idTech;
    }

    public void setIdTech(Integer idTech) {
        this.idTech = idTech;
    }

    // ===== EQUALS and HASHCODE =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Demande demande = (Demande) o;
        return Objects.equals(idDemande, demande.idDemande);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDemande);
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "Demande{" +
                "idDemande=" + idDemande +
                ", idUtilisateur=" + idUtilisateur +
                ", typeProbleme='" + typeProbleme + '\'' +
                ", description='" + description + '\'' +
                ", dateDemande=" + dateDemande +
                ", statut='" + statut + '\'' +
                ", idTech=" + idTech +
                '}';
    }
}