package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Terrain entity - Maps to terrains table
 * Represents land available for rental
 */
public class Terrain {
    private int id;
    private int categorieId;
    private String titre;
    private String description;
    private BigDecimal superficieHectares;
    private String ville;
    private String adresse;
    private BigDecimal prixMois;
    private BigDecimal prixAnnee;
    private BigDecimal caution;
    private String imageUrl;
    private boolean disponible;
    private LocalDateTime dateCreation;

    // Constructors
    public Terrain() {
    }

    public Terrain(int categorieId, String titre, BigDecimal superficieHectares,
            String ville, BigDecimal prixAnnee) {
        this.categorieId = categorieId;
        this.titre = titre;
        this.superficieHectares = superficieHectares;
        this.ville = ville;
        this.prixAnnee = prixAnnee;
        this.disponible = true;
        this.caution = BigDecimal.ZERO;
    }

    public Terrain(int id, int categorieId, String titre, String description,
            BigDecimal superficieHectares, String ville, String adresse,
            BigDecimal prixMois, BigDecimal prixAnnee, BigDecimal caution,
            String imageUrl, boolean disponible, LocalDateTime dateCreation) {
        this.id = id;
        this.categorieId = categorieId;
        this.titre = titre;
        this.description = description;
        this.superficieHectares = superficieHectares;
        this.ville = ville;
        this.adresse = adresse;
        this.prixMois = prixMois;
        this.prixAnnee = prixAnnee;
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

    public BigDecimal getSuperficieHectares() {
        return superficieHectares;
    }

    public void setSuperficieHectares(BigDecimal superficieHectares) {
        this.superficieHectares = superficieHectares;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public BigDecimal getPrixMois() {
        return prixMois;
    }

    public void setPrixMois(BigDecimal prixMois) {
        this.prixMois = prixMois;
    }

    public BigDecimal getPrixAnnee() {
        return prixAnnee;
    }

    public void setPrixAnnee(BigDecimal prixAnnee) {
        this.prixAnnee = prixAnnee;
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
        return "Terrain{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", superficieHectares=" + superficieHectares +
                ", ville='" + ville + '\'' +
                ", prixAnnee=" + prixAnnee +
                ", disponible=" + disponible +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Terrain terrain = (Terrain) o;
        return id == terrain.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
