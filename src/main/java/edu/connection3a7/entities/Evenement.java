package edu.connection3a7.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Evenement {
    private int idEvenement;
    private String titre;
    private String description;
    private String imageUrl;
    private Type typeEvenement; // amalthom enum Type
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalTime horaireDebut;
    private LocalTime horaireFin;
    private String lieu;
    private String adresse;
    private int capaciteMax;
    private int placesDisponibles;
    private String organisateur;
    private String contactEmail;
    private String contactTel;
    private Statutevent statut; // kif kif amaltha enum
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public Evenement() {};

    public Evenement(int idEvenement, String titre, String description, String imageUrl, Type typeEvenement, LocalDate dateDebut, LocalDate dateFin, LocalTime horaireDebut, LocalTime horaireFin, String lieu, String adresse, int capaciteMax, int placesDisponibles, String organisateur, String contactEmail, String contactTel, Statutevent statut) {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.typeEvenement = typeEvenement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.horaireDebut = horaireDebut;
        this.horaireFin = horaireFin;
        this.lieu = lieu;
        this.adresse = adresse;
        this.capaciteMax = capaciteMax;
        this.placesDisponibles = placesDisponibles;
        this.organisateur = organisateur;
        this.contactEmail = contactEmail;
        this.contactTel = contactTel;
        this.statut = statut;
    }

    public Evenement(int idEvenement, String titre, String description, String imageUrl, Type typeEvenement, LocalDate dateDebut, LocalDate dateFin, String lieu, String adresse, int capaciteMax, int placesDisponibles, String organisateur, String contactEmail, String contactTel, Statutevent statut) {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.typeEvenement = typeEvenement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.adresse = adresse;
        this.capaciteMax = capaciteMax;
        this.placesDisponibles = placesDisponibles;
        this.organisateur = organisateur;
        this.contactEmail = contactEmail;
        this.contactTel = contactTel;
        this.statut = statut;
    }





    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Type getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(Type typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalTime getHoraireDebut() {
        return horaireDebut;
    }

    public void setHoraireDebut(LocalTime horaireDebut) {
        this.horaireDebut = horaireDebut;
    }

    public LocalTime getHoraireFin() {
        return horaireFin;
    }

    public void setHoraireFin(LocalTime horaireFin) {
        this.horaireFin = horaireFin;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public int getPlacesDisponibles() {
        return placesDisponibles;
    }

    public void setPlacesDisponibles(int placesDisponibles) {
        this.placesDisponibles = placesDisponibles;
    }

    public String getOrganisateur() {
        return organisateur;
    }

    public void setOrganisateur(String organisateur) {
        this.organisateur = organisateur;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactTel() {
        return contactTel;
    }

    public void setContactTel(String contactTel) {
        this.contactTel = contactTel;
    }

    public Statutevent getStatut() {
        return statut;
    }

    public void setStatut(Statutevent statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "idEvenement=" + idEvenement +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", typeEvenement=" + typeEvenement +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", horaireDebut=" + horaireDebut +
                ", horaireFin=" + horaireFin +
                ", lieu='" + lieu + '\'' +
                ", adresse='" + adresse + '\'' +
                ", capaciteMax=" + capaciteMax +
                ", placesDisponibles=" + placesDisponibles +
                ", organisateur='" + organisateur + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", contactTel='" + contactTel + '\'' +
                ", statut=" + statut +
                ", dateCreation=" + dateCreation +
                ", dateModification=" + dateModification +
                '}';
    }
}
