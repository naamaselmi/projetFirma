package marketplace.service;

import marketplace.entities.DetailCommande;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for DetailCommande entity
 */
public class DetailCommandeService implements IService<DetailCommande> {

    @Override
    public void addEntity(DetailCommande detail) throws SQLException {
        String requete = "INSERT INTO details_commandes (commande_id, equipement_id, quantite, prix_unitaire) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, detail.getCommandeId());
        pst.setInt(2, detail.getEquipementId());
        pst.setInt(3, detail.getQuantite());
        pst.setBigDecimal(4, detail.getPrixUnitaire());

        pst.executeUpdate();
        System.out.println("Detail commande added");
    }

    @Override
    public void deleteEntity(DetailCommande detail) throws SQLException {
        String requete = "DELETE FROM details_commandes WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, detail.getId());

        pst.executeUpdate();
        System.out.println("Detail commande deleted");
    }

    @Override
    public void updateEntity(DetailCommande detail) throws SQLException {
        String requete = "UPDATE details_commandes SET quantite = ?, prix_unitaire = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, detail.getQuantite());
        pst.setBigDecimal(2, detail.getPrixUnitaire());
        pst.setInt(3, detail.getId());

        pst.executeUpdate();
        System.out.println("Detail commande updated");
    }

    @Override
    public List<DetailCommande> getEntities() throws SQLException {
        List<DetailCommande> details = new ArrayList<>();
        String requete = "SELECT * FROM details_commandes";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            DetailCommande detail = new DetailCommande(
                    rs.getInt("id"),
                    rs.getInt("commande_id"),
                    rs.getInt("equipement_id"),
                    rs.getInt("quantite"),
                    rs.getBigDecimal("prix_unitaire"),
                    rs.getBigDecimal("sous_total"));
            details.add(detail);
        }

        return details;
    }

    /**
     * Get all details for a specific order
     */
    public List<DetailCommande> getDetailsByCommande(int commandeId) throws SQLException {
        List<DetailCommande> details = new ArrayList<>();
        String requete = "SELECT * FROM details_commandes WHERE commande_id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, commandeId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            DetailCommande detail = new DetailCommande(
                    rs.getInt("id"),
                    rs.getInt("commande_id"),
                    rs.getInt("equipement_id"),
                    rs.getInt("quantite"),
                    rs.getBigDecimal("prix_unitaire"),
                    rs.getBigDecimal("sous_total"));
            details.add(detail);
        }

        return details;
    }
}
