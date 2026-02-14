package marketplace.entities;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DetailCommande entity - Maps to details_commandes table
 * Represents individual line items in an order
 */
public class DetailCommande {
    private int id;
    private int commandeId;
    private int equipementId;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;

    // Constructors
    public DetailCommande() {
    }

    public DetailCommande(int commandeId, int equipementId, int quantite, BigDecimal prixUnitaire) {
        this.commandeId = commandeId;
        this.equipementId = equipementId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }

    public DetailCommande(int id, int commandeId, int equipementId, int quantite,
            BigDecimal prixUnitaire, BigDecimal sousTotal) {
        this.id = id;
        this.commandeId = commandeId;
        this.equipementId = equipementId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }

    public int getEquipementId() {
        return equipementId;
    }

    public void setEquipementId(int equipementId) {
        this.equipementId = equipementId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        // Recalculate subtotal when quantity changes
        if (this.prixUnitaire != null) {
            this.sousTotal = this.prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        // Recalculate subtotal when price changes
        if (this.quantite > 0) {
            this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(this.quantite));
        }
    }

    public BigDecimal getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(BigDecimal sousTotal) {
        this.sousTotal = sousTotal;
    }

    // Business method
    public void recalculerSousTotal() {
        if (prixUnitaire != null && quantite > 0) {
            this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

    @Override
    public String toString() {
        return "DetailCommande{" +
                "id=" + id +
                ", commandeId=" + commandeId +
                ", equipementId=" + equipementId +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", sousTotal=" + sousTotal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DetailCommande that = (DetailCommande) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
