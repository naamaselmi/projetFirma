package edu.connection3a7.entities;

import java.sql.Date;
import java.util.Objects;

public class Avis {

    private Integer idAvis;
    private Integer idUtilisateur;
    private Integer note; // Note de 1 à 10
    private String commentaire;
    private Date dateAvis;
    private Integer idTech;
    private Integer idDemande;

    // ===== Constructeurs =====
    public Avis() {
    }

    public Avis(Integer idAvis, Integer idUtilisateur, Integer note, String commentaire,
                Date dateAvis, Integer idTech, Integer idDemande) {
        this.idAvis = idAvis;
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.idTech = idTech;
        this.idDemande = idDemande;
    }

    // ===== Getters et Setters =====
    public Integer getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(Integer idAvis) {
        this.idAvis = idAvis;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        if (note != null && (note < 1 || note > 10)) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 10");
        }
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Date getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(Date dateAvis) {
        this.dateAvis = dateAvis;
    }

    public Integer getIdTech() {
        return idTech;
    }

    public void setIdTech(Integer idTech) {
        this.idTech = idTech;
    }

    public Integer getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(Integer idDemande) {
        this.idDemande = idDemande;
    }

    // ===== Equals et HashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avis avis = (Avis) o;
        return Objects.equals(idAvis, avis.idAvis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAvis);
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "Avis{" +
                "idAvis=" + idAvis +
                ", idUtilisateur=" + idUtilisateur +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateAvis=" + dateAvis +
                ", idTech=" + idTech +
                ", idDemande=" + idDemande +
                '}';
    }
}