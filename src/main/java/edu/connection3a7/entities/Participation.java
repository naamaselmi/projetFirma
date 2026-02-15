package edu.connection3a7.entities;

import java.time.LocalDateTime;

public class Participation {
    private int idParticipation;
    private int idEvenement;
    private int idUtilisateur;
    private Statut statut;
    private LocalDateTime dateInscription;
    private LocalDateTime dateAnnulation;
    private int nombreAccompagnants;
    private String commentaire;

    public Participation() {};

    public Participation(int idParticipation, int idEvenement, int idUtilisateur, Statut statut, LocalDateTime dateInscription, LocalDateTime dateAnnulation, int nombreAccompagnants, String commentaire) {
        this.idParticipation = idParticipation;
        this.idEvenement = idEvenement;
        this.idUtilisateur = idUtilisateur;
        this.statut = statut;
        this.dateInscription = dateInscription;
        this.dateAnnulation = dateAnnulation;
        this.nombreAccompagnants = nombreAccompagnants;
        this.commentaire = commentaire;
    }

    public int getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
    }

    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public LocalDateTime getDateAnnulation() {
        return dateAnnulation;
    }

    public void setDateAnnulation(LocalDateTime dateAnnulation) {
        this.dateAnnulation = dateAnnulation;
    }

    public int getNombreAccompagnants() {
        return nombreAccompagnants;
    }

    public void setNombreAccompagnants(int nombreAccompagnants) {
        this.nombreAccompagnants = nombreAccompagnants;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "idParticipation=" + idParticipation +
                ", idEvenement=" + idEvenement +
                ", idUtilisateur=" + idUtilisateur +
                ", statut='" + statut + '\'' +
                ", dateInscription=" + dateInscription +
                ", dateAnnulation=" + dateAnnulation +
                ", nombreAccompagnants=" + nombreAccompagnants +
                ", commentaire='" + commentaire + '\'' +
                '}';
    }
}
