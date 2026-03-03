package Firma.services.GestionMarketplace;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Firma.entities.GestionMarketplace.Commande;
import Firma.entities.GestionMarketplace.DeliveryStatus;
import Firma.entities.GestionMarketplace.PaymentStatus;
import Firma.interfaces.GestionMarketplace.IService;
import Firma.tools.GestionMarketplace.DB_connection;

/**
 * Service for Commande entity
 */
public class CommandeService implements IService<Commande> {

    @Override
    public void addEntity(Commande commande) throws SQLException {
        String requete = "INSERT INTO commandes (id_utilisateur, montant_total, statut_paiement, " +
                "statut_livraison, adresse_livraison, ville_livraison, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(
                requete, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, commande.getUtilisateurId());
        pst.setBigDecimal(2, commande.getMontantTotal());
        pst.setString(3, commande.getStatutPaiement().getValue());
        pst.setString(4, commande.getStatutLivraison().getValue());
        pst.setString(5, commande.getAdresseLivraison());
        pst.setString(6, commande.getVilleLivraison());
        pst.setString(7, commande.getNotes());

        pst.executeUpdate();

        // Retrieve and set the auto-generated primary key
        try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                commande.setId(generatedKeys.getInt(1));
            }
        }
        System.out.println("Commande added with id: " + commande.getId());

        // Fetch back DB-computed fields (numero_commande, date_commande)
        String fetchQuery = "SELECT numero_commande, date_commande FROM commandes WHERE id = ?";
        try (PreparedStatement fetchStmt = DB_connection.getInstance().getConnection()
                .prepareStatement(fetchQuery)) {
            fetchStmt.setInt(1, commande.getId());
            try (ResultSet rs2 = fetchStmt.executeQuery()) {
                if (rs2.next()) {
                    commande.setNumeroCommande(rs2.getString("numero_commande"));
                    Timestamp ts = rs2.getTimestamp("date_commande");
                    if (ts != null) {
                        commande.setDateCommande(ts.toLocalDateTime());
                    }
                }
            }
        }
    }

    @Override
    public void deleteEntity(Commande commande) throws SQLException {
        String requete = "DELETE FROM commandes WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, commande.getId());

        pst.executeUpdate();
        System.out.println("Commande deleted: " + commande.getNumeroCommande());
    }

    @Override
    public void updateEntity(Commande commande) throws SQLException {
        String requete = "UPDATE commandes SET montant_total = ?, statut_paiement = ?, " +
                "statut_livraison = ?, adresse_livraison = ?, ville_livraison = ?, " +
                "date_livraison = ?, notes = ? WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setBigDecimal(1, commande.getMontantTotal());
        pst.setString(2, commande.getStatutPaiement().getValue());
        pst.setString(3, commande.getStatutLivraison().getValue());
        pst.setString(4, commande.getAdresseLivraison());
        pst.setString(5, commande.getVilleLivraison());

        if (commande.getDateLivraison() != null) {
            pst.setDate(6, Date.valueOf(commande.getDateLivraison()));
        } else {
            pst.setNull(6, Types.DATE);
        }

        pst.setString(7, commande.getNotes());
        pst.setInt(8, commande.getId());

        pst.executeUpdate();
        System.out.println("Commande updated: " + commande.getNumeroCommande());
    }

    @Override
    public List<Commande> getEntities() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String requete = "SELECT * FROM commandes ORDER BY date_commande DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            commandes.add(mapResultSetToCommande(rs));
        }

        return commandes;
    }

    /**
     * Get orders for a specific user
     */
    public List<Commande> getCommandesByUser(int utilisateurId) throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String requete = "SELECT * FROM commandes WHERE id_utilisateur = ? ORDER BY date_commande DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, utilisateurId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            commandes.add(mapResultSetToCommande(rs));
        }

        return commandes;
    }

    /**
     * Get pending orders (not paid)
     */
    public List<Commande> getPendingCommandes() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String requete = "SELECT * FROM commandes WHERE statut_paiement = 'en_attente' ORDER BY date_commande DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            commandes.add(mapResultSetToCommande(rs));
        }

        return commandes;
    }

    /**
     * Update payment status
     * Vérifie automatiquement les stocks après paiement
     */
    public void updatePaymentStatus(int commandeId, PaymentStatus status) throws SQLException {
        String requete = "UPDATE commandes SET statut_paiement = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, status.getValue());
        pst.setInt(2, commandeId);

        pst.executeUpdate();
        System.out.println("Payment status updated to: " + status);
        
        // Si la commande est payée, vérifier les stocks des équipements
        if (status == PaymentStatus.PAYE) {
            checkStockAfterPayment(commandeId);
        }
    }
    
    /**
     * Vérifie les stocks des équipements après paiement d'une commande
     * Envoie des alertes si nécessaire
     */
    private void checkStockAfterPayment(int commandeId) {
        new Thread(() -> {
            try {
                // Récupérer les détails de la commande
                String query = "SELECT equipement_id FROM details_commandes WHERE commande_id = ?";
                PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(query);
                pst.setInt(1, commandeId);
                ResultSet rs = pst.executeQuery();
                
                EquipementService equipementService = new EquipementService();
                StockAlertService alertService = StockAlertService.getInstance();
                
                // Vérifier chaque équipement de la commande
                while (rs.next()) {
                    int equipementId = rs.getInt("equipement_id");
                    
                    // Récupérer l'équipement mis à jour
                    List<Firma.entities.GestionMarketplace.Equipement> equipements = equipementService.getEntities();
                    for (Firma.entities.GestionMarketplace.Equipement eq : equipements) {
                        if (eq.getId() == equipementId) {
                            // Vérifier et envoyer alerte si nécessaire
                            alertService.checkEquipementAndAlert(eq);
                            break;
                        }
                    }
                }
                
                System.out.println("[CommandeService] Vérification des stocks après paiement terminée");
                
            } catch (Exception e) {
                System.err.println("[CommandeService] Erreur lors de la vérification des stocks: " + e.getMessage());
                e.printStackTrace();
            }
        }, "stock-check-after-payment").start();
    }

    /**
     * Update delivery status
     */
    public void updateDeliveryStatus(int commandeId, DeliveryStatus status) throws SQLException {
        String requete = "UPDATE commandes SET statut_livraison = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, status.getValue());
        pst.setInt(2, commandeId);

        pst.executeUpdate();
        System.out.println("Delivery status updated to: " + status);
    }

    /**
     * Helper method to map ResultSet to Commande
     */
    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("date_commande");
        LocalDateTime dateCommande = timestamp != null ? timestamp.toLocalDateTime() : null;

        Date dateLivraisonSql = rs.getDate("date_livraison");
        LocalDate dateLivraison = dateLivraisonSql != null ? dateLivraisonSql.toLocalDate() : null;

        return new Commande(
                rs.getInt("id"),
                rs.getInt("id_utilisateur"),
                rs.getString("numero_commande"),
                rs.getBigDecimal("montant_total"),
                PaymentStatus.fromString(rs.getString("statut_paiement")),
                DeliveryStatus.fromString(rs.getString("statut_livraison")),
                rs.getString("adresse_livraison"),
                rs.getString("ville_livraison"),
                dateCommande,
                dateLivraison,
                rs.getString("notes"));
    }
}
