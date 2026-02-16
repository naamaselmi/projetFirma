package marketplace.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Fournisseur entity - Maps to fournisseurs table
 * Represents suppliers who provide equipment
 */
public class Fournisseur {
    private int id;
    private String nomEntreprise;
    private String contactNom;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private boolean actif;
    private LocalDateTime dateCreation;

    // Constructors
    public Fournisseur() {
    }

    public Fournisseur(String nomEntreprise, String contactNom, String email, String telephone) {
        this.nomEntreprise = nomEntreprise;
        this.contactNom = contactNom;
        this.email = email;
        this.telephone = telephone;
        this.actif = true;
    }

    public Fournisseur(int id, String nomEntreprise, String contactNom, String email,
            String telephone, String adresse, String ville, boolean actif,
            LocalDateTime dateCreation) {
        this.id = id;
        this.nomEntreprise = nomEntreprise;
        this.contactNom = contactNom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ville = ville;
        this.actif = actif;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getContactNom() {
        return contactNom;
    }

    public void setContactNom(String contactNom) {
        this.contactNom = contactNom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return nomEntreprise;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Fournisseur that = (Fournisseur) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
