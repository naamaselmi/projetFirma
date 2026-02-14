package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AchatFournisseur entity - Maps to achats_fournisseurs table
 * Represents purchases from suppliers
 */
public class AchatFournisseur {
    private int id;
    private int fournisseurId;
    private int equipementId;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    private LocalDate dateAchat;
    private String numeroFacture;
    private PaymentStatus statutPaiement;
    private LocalDateTime dateCreation;

    // Constructors
    public AchatFournisseur() {
    }

    public AchatFournisseur(int fournisseurId, int equipementId, int quantite,
            BigDecimal prixUnitaire, LocalDate dateAchat, String numeroFacture) {
        this.fournisseurId = fournisseurId;
        this.equipementId = equipementId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        this.dateAchat = dateAchat;
        this.numeroFacture = numeroFacture;
        this.statutPaiement = PaymentStatus.EN_ATTENTE;
    }

    public AchatFournisseur(int id, int fournisseurId, int equipementId, int quantite,
            BigDecimal prixUnitaire, BigDecimal montantTotal, LocalDate dateAchat,
            String numeroFacture, PaymentStatus statutPaiement, LocalDateTime dateCreation) {
        this.id = id;
        this.fournisseurId = fournisseurId;
        this.equipementId = equipementId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.montantTotal = montantTotal;
        this.dateAchat = dateAchat;
        this.numeroFacture = numeroFacture;
        this.statutPaiement = statutPaiement;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(int fournisseurId) {
        this.fournisseurId = fournisseurId;
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
        recalculerMontantTotal();
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        recalculerMontantTotal();
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDate getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(LocalDate dateAchat) {
        this.dateAchat = dateAchat;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public PaymentStatus getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(PaymentStatus statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Business method
    private void recalculerMontantTotal() {
        if (prixUnitaire != null && quantite > 0) {
            this.montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

    public boolean isPaye() {
        return statutPaiement == PaymentStatus.PAYE;
    }

    @Override
    public String toString() {
        return "AchatFournisseur{" +
                "id=" + id +
                ", fournisseurId=" + fournisseurId +
                ", equipementId=" + equipementId +
                ", quantite=" + quantite +
                ", montantTotal=" + montantTotal +
                ", dateAchat=" + dateAchat +
                ", statutPaiement=" + statutPaiement +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AchatFournisseur that = (AchatFournisseur) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
