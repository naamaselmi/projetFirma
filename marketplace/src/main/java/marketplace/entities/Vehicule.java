package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Vehicule entity - Maps to vehicules table
 * Represents vehicles available for rental
 */
public class Vehicule {
    private int id;
    private int categorieId;
    private String nom;
    private String description;
    private String marque;
    private String modele;
    private String immatriculation;
    private BigDecimal prixJour;
    private BigDecimal prixSemaine;
    private BigDecimal prixMois;
    private BigDecimal caution;
    private String imageUrl;
    private boolean disponible;
    private LocalDateTime dateCreation;

    // Constructors
    public Vehicule() {
    }

    public Vehicule(int categorieId, String nom, String marque, String modele,
            String immatriculation, BigDecimal prixJour) {
        this.categorieId = categorieId;
        this.nom = nom;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.prixJour = prixJour;
        this.disponible = true;
        this.caution = BigDecimal.ZERO;
    }

    public Vehicule(int id, int categorieId, String nom, String description, String marque,
            String modele, String immatriculation, BigDecimal prixJour, BigDecimal prixSemaine,
            BigDecimal prixMois, BigDecimal caution, String imageUrl, boolean disponible,
            LocalDateTime dateCreation) {
        this.id = id;
        this.categorieId = categorieId;
        this.nom = nom;
        this.description = description;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.prixJour = prixJour;
        this.prixSemaine = prixSemaine;
        this.prixMois = prixMois;
        this.caution = caution;
        this.imageUrl = imageUrl;
        this.disponible = disponible;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public BigDecimal getPrixJour() {
        return prixJour;
    }

    public void setPrixJour(BigDecimal prixJour) {
        this.prixJour = prixJour;
    }

    public BigDecimal getPrixSemaine() {
        return prixSemaine;
    }

    public void setPrixSemaine(BigDecimal prixSemaine) {
        this.prixSemaine = prixSemaine;
    }

    public BigDecimal getPrixMois() {
        return prixMois;
    }

    public void setPrixMois(BigDecimal prixMois) {
        this.prixMois = prixMois;
    }

    public BigDecimal getCaution() {
        return caution;
    }

    public void setCaution(BigDecimal caution) {
        this.caution = caution;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", immatriculation='" + immatriculation + '\'' +
                ", prixJour=" + prixJour +
                ", disponible=" + disponible +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vehicule vehicule = (Vehicule) o;
        return id == vehicule.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
