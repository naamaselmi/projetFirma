package com.examen.firmapi.entities;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Profile {

    private int id_profile;
    private int id_utilisateur;

    private String photo_profil;
    private String bio;
    private LocalDate date_naissance;
    private Genre genre;

    private String pays;
    private String ville;

    private Timestamp derniere_mise_a_jour;

    // 🔹 Empty constructor
    public Profile() {
    }

    // 🔹 Full constructor
    public Profile(int id_profile, int id_utilisateur,
                   String photo_profil, String bio,
                   LocalDate date_naissance, Genre genre,
                   String pays, String ville,
                   Timestamp derniere_mise_a_jour) {

        this.id_profile = id_profile;
        this.id_utilisateur = id_utilisateur;
        this.photo_profil = photo_profil;
        this.bio = bio;
        this.date_naissance = date_naissance;
        this.genre = genre;
        this.pays = pays;
        this.ville = ville;
        this.derniere_mise_a_jour = derniere_mise_a_jour;
    }

    // 🔹 Getters & Setters
    public int getId_profile() {
        return id_profile;
    }

    public void setId_profile(int id_profile) {
        this.id_profile = id_profile;
    }

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    public String getPhoto_profil() {
        return photo_profil;
    }

    public void setPhoto_profil(String photo_profil) {
        this.photo_profil = photo_profil;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDate getDate_naissance() {
        return date_naissance;
    }

    public void setDate_naissance(LocalDate date_naissance) {
        this.date_naissance = date_naissance;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Timestamp getDerniere_mise_a_jour() {
        return derniere_mise_a_jour;
    }

    public void setDerniere_mise_a_jour(Timestamp derniere_mise_a_jour) {
        this.derniere_mise_a_jour = derniere_mise_a_jour;
    }
}
