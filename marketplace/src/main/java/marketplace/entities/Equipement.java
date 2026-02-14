package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Equipement entity - Maps to equipements table
 * Represents agricultural equipment for sale
 */
public class Equipement {
    private int id;
    private int categorieId;
    private int fournisseurId;
    private String nom;
    private String description;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private int quantiteStock;
    private int seuilAlerte;
    private String imageUrl;
    private boolean disponible;
    private LocalDateTime dateCreation;

    // Constructors
    public Equipement() {
    }

    public Equipement(int categorieId, int fournisseurId, String nom, String description,
            BigDecimal prixAchat, BigDecimal prixVente, int quantiteStock) {
        this.categorieId = categorieId;
        this.fournisseurId = fournisseurId;
        this.nom = nom;
        this.description = description;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = 5; // Default value
        this.disponible = true;
    }

    public Equipement(int id, int categorieId, int fournisseurId, String nom, String description,
            BigDecimal prixAchat, BigDecimal prixVente, int quantiteStock, int seuilAlerte,
            String imageUrl, boolean disponible, LocalDateTime dateCreation) {
        this.id = id;
        this.categorieId = categorieId;
        this.fournisseurId = fournisseurId;
        this.nom = nom;
        this.description = description;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.quantiteStock = quantiteStock;
        this.seuilAlerte = seuilAlerte;
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

    public int getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(int fournisseurId) {
        this.fournisseurId = fournisseurId;
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

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(BigDecimal prixAchat) {
        this.prixAchat = prixAchat;
    }

    public BigDecimal getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(BigDecimal prixVente) {
        this.prixVente = prixVente;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
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

    // Business methods
    public boolean isStockFaible() {
        return quantiteStock <= seuilAlerte;
    }

    public BigDecimal getMargeBeneficiaire() {
        return prixVente.subtract(prixAchat);
    }

    public double getPourcentageMarge() {
        if (prixAchat.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return getMargeBeneficiaire().divide(prixAchat, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prixVente=" + prixVente +
                ", quantiteStock=" + quantiteStock +
                ", disponible=" + disponible +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Equipement that = (Equipement) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
