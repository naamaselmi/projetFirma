package marketplace.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Commande entity - Maps to commandes table
 * Represents customer orders for equipment
 */
public class Commande {
    private int id;
    private int utilisateurId;
    private String numeroCommande;
    private BigDecimal montantTotal;
    private PaymentStatus statutPaiement;
    private DeliveryStatus statutLivraison;
    private String adresseLivraison;
    private String villeLivraison;
    private LocalDateTime dateCommande;
    private LocalDate dateLivraison;
    private String notes;

    // Constructors
    public Commande() {
    }

    public Commande(int utilisateurId, String adresseLivraison, String villeLivraison) {
        this.utilisateurId = utilisateurId;
        this.adresseLivraison = adresseLivraison;
        this.villeLivraison = villeLivraison;
        this.montantTotal = BigDecimal.ZERO;
        this.statutPaiement = PaymentStatus.EN_ATTENTE;
        this.statutLivraison = DeliveryStatus.EN_ATTENTE;
    }

    public Commande(int id, int utilisateurId, String numeroCommande, BigDecimal montantTotal,
            PaymentStatus statutPaiement, DeliveryStatus statutLivraison,
            String adresseLivraison, String villeLivraison, LocalDateTime dateCommande,
            LocalDate dateLivraison, String notes) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.numeroCommande = numeroCommande;
        this.montantTotal = montantTotal;
        this.statutPaiement = statutPaiement;
        this.statutLivraison = statutLivraison;
        this.adresseLivraison = adresseLivraison;
        this.villeLivraison = villeLivraison;
        this.dateCommande = dateCommande;
        this.dateLivraison = dateLivraison;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getNumeroCommande() {
        return numeroCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public PaymentStatus getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(PaymentStatus statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public DeliveryStatus getStatutLivraison() {
        return statutLivraison;
    }

    public void setStatutLivraison(DeliveryStatus statutLivraison) {
        this.statutLivraison = statutLivraison;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String getVilleLivraison() {
        return villeLivraison;
    }

    public void setVilleLivraison(String villeLivraison) {
        this.villeLivraison = villeLivraison;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Business methods
    public boolean isPaye() {
        return statutPaiement == PaymentStatus.PAYE;
    }

    public boolean isLivre() {
        return statutLivraison == DeliveryStatus.LIVRE;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", numeroCommande='" + numeroCommande + '\'' +
                ", montantTotal=" + montantTotal +
                ", statutPaiement=" + statutPaiement +
                ", statutLivraison=" + statutLivraison +
                ", dateCommande=" + dateCommande +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Commande commande = (Commande) o;
        return id == commande.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
