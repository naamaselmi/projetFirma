package marketplace.service;

import marketplace.entities.Commande;
import marketplace.entities.DeliveryStatus;
import marketplace.entities.PaymentStatus;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Commande entity
 */
public class CommandeService implements IService<Commande> {

    @Override
    public void addEntity(Commande commande) throws SQLException {
        String requete = "INSERT INTO commandes (utilisateur_id, montant_total, statut_paiement, " +
                "statut_livraison, adresse_livraison, ville_livraison, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, commande.getUtilisateurId());
        pst.setBigDecimal(2, commande.getMontantTotal());
        pst.setString(3, commande.getStatutPaiement().getValue());
        pst.setString(4, commande.getStatutLivraison().getValue());
        pst.setString(5, commande.getAdresseLivraison());
        pst.setString(6, commande.getVilleLivraison());
        pst.setString(7, commande.getNotes());

        pst.executeUpdate();
        System.out.println("Commande added (numero will be auto-generated)");
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
        String requete = "SELECT * FROM commandes WHERE utilisateur_id = ? ORDER BY date_commande DESC";

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
     */
    public void updatePaymentStatus(int commandeId, PaymentStatus status) throws SQLException {
        String requete = "UPDATE commandes SET statut_paiement = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, status.getValue());
        pst.setInt(2, commandeId);

        pst.executeUpdate();
        System.out.println("Payment status updated to: " + status);
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
                rs.getInt("utilisateur_id"),
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
