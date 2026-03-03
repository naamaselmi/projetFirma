package Firma.services.GestionMarketplace;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Firma.entities.GestionMarketplace.AchatFournisseur;
import Firma.entities.GestionMarketplace.PaymentStatus;
import Firma.interfaces.GestionMarketplace.IService;
import Firma.tools.GestionMarketplace.DB_connection;

/**
 * Service for AchatFournisseur entity
 */
public class AchatFournisseurService implements IService<AchatFournisseur> {

    @Override
    public void addEntity(AchatFournisseur achat) throws SQLException {
        String requete = "INSERT INTO achats_fournisseurs (fournisseur_id, equipement_id, quantite, " +
                "prix_unitaire, montant_total, date_achat, numero_facture, statut_paiement) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, achat.getFournisseurId());
        pst.setInt(2, achat.getEquipementId());
        pst.setInt(3, achat.getQuantite());
        pst.setBigDecimal(4, achat.getPrixUnitaire());
        pst.setBigDecimal(5, achat.getMontantTotal());
        pst.setDate(6, Date.valueOf(achat.getDateAchat()));
        pst.setString(7, achat.getNumeroFacture());
        pst.setString(8, achat.getStatutPaiement().getValue());

        pst.executeUpdate();
        System.out.println("Achat fournisseur added");
    }

    @Override
    public void deleteEntity(AchatFournisseur achat) throws SQLException {
        String requete = "DELETE FROM achats_fournisseurs WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, achat.getId());

        pst.executeUpdate();
        System.out.println("Achat fournisseur deleted");
    }

    @Override
    public void updateEntity(AchatFournisseur achat) throws SQLException {
        String requete = "UPDATE achats_fournisseurs SET quantite = ?, prix_unitaire = ?, " +
                "montant_total = ?, date_achat = ?, numero_facture = ?, statut_paiement = ? " +
                "WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, achat.getQuantite());
        pst.setBigDecimal(2, achat.getPrixUnitaire());
        pst.setBigDecimal(3, achat.getMontantTotal());
        pst.setDate(4, Date.valueOf(achat.getDateAchat()));
        pst.setString(5, achat.getNumeroFacture());
        pst.setString(6, achat.getStatutPaiement().getValue());
        pst.setInt(7, achat.getId());

        pst.executeUpdate();
        System.out.println("Achat fournisseur updated");
    }

    @Override
    public List<AchatFournisseur> getEntities() throws SQLException {
        List<AchatFournisseur> achats = new ArrayList<>();
        String requete = "SELECT * FROM achats_fournisseurs ORDER BY date_achat DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            AchatFournisseur achat = new AchatFournisseur(
                    rs.getInt("id"),
                    rs.getInt("fournisseur_id"),
                    rs.getInt("equipement_id"),
                    rs.getInt("quantite"),
                    rs.getBigDecimal("prix_unitaire"),
                    rs.getBigDecimal("montant_total"),
                    rs.getDate("date_achat").toLocalDate(),
                    rs.getString("numero_facture"),
                    PaymentStatus.fromString(rs.getString("statut_paiement")),
                    dateCreation);
            achats.add(achat);
        }

        return achats;
    }

    /**
     * Get purchases by supplier
     */
    public List<AchatFournisseur> getAchatsByFournisseur(int fournisseurId) throws SQLException {
        List<AchatFournisseur> achats = new ArrayList<>();
        String requete = "SELECT * FROM achats_fournisseurs WHERE fournisseur_id = ? ORDER BY date_achat DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, fournisseurId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            AchatFournisseur achat = new AchatFournisseur(
                    rs.getInt("id"),
                    rs.getInt("fournisseur_id"),
                    rs.getInt("equipement_id"),
                    rs.getInt("quantite"),
                    rs.getBigDecimal("prix_unitaire"),
                    rs.getBigDecimal("montant_total"),
                    rs.getDate("date_achat").toLocalDate(),
                    rs.getString("numero_facture"),
                    PaymentStatus.fromString(rs.getString("statut_paiement")),
                    dateCreation);
            achats.add(achat);
        }

        return achats;
    }
}
