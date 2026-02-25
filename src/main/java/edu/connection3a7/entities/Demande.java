package edu.connection3a7.entities;

import java.sql.Date;

public class Demande {
    private int idDemande;
    private int idUtilisateur;
    private String typeProbleme;
    private String description;
    private Date dateDemande;
    private String statut;
    private Integer idTech;
    private String adresseClient;  // 🔥 NOUVEAU CHAMP

    // Constructeurs
    public Demande() {}

    public Demande(int idDemande, int idUtilisateur, String typeProbleme,
                   String description, Date dateDemande, String statut,
                   Integer idTech, String adresseClient) {
        this.idDemande = idDemande;
        this.idUtilisateur = idUtilisateur;
        this.typeProbleme = typeProbleme;
        this.description = description;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.idTech = idTech;
        this.adresseClient = adresseClient;
    }

    // Getters et Setters
    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getTypeProbleme() { return typeProbleme; }
    public void setTypeProbleme(String typeProbleme) { this.typeProbleme = typeProbleme; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDateDemande() { return dateDemande; }
    public void setDateDemande(Date dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getIdTech() { return idTech; }
    public void setIdTech(Integer idTech) { this.idTech = idTech; }

    // 🔥 NOUVEAU GETTER/SETTER
    public String getAdresseClient() { return adresseClient; }
    public void setAdresseClient(String adresseClient) { this.adresseClient = adresseClient; }

    @Override
    public String toString() {
        return "Demande{" +
                "idDemande=" + idDemande +
                ", typeProbleme='" + typeProbleme + '\'' +
                ", statut='" + statut + '\'' +
                ", adresseClient='" + adresseClient + '\'' +
                '}';
    }
}