package marketplace.service;

import marketplace.entities.Equipement;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Equipement entity
 */
public class EquipementService implements IService<Equipement> {

    @Override
    public void addEntity(Equipement equipement) throws SQLException {
        String requete = "INSERT INTO equipements (categorie_id, fournisseur_id, nom, description, " +
                "prix_achat, prix_vente, quantite_stock, seuil_alerte, image_url, disponible) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, equipement.getCategorieId());
        pst.setInt(2, equipement.getFournisseurId());
        pst.setString(3, equipement.getNom());
        pst.setString(4, equipement.getDescription());
        pst.setBigDecimal(5, equipement.getPrixAchat());
        pst.setBigDecimal(6, equipement.getPrixVente());
        pst.setInt(7, equipement.getQuantiteStock());
        pst.setInt(8, equipement.getSeuilAlerte());
        pst.setString(9, equipement.getImageUrl());
        pst.setBoolean(10, equipement.isDisponible());

        pst.executeUpdate();
        System.out.println("Equipement added: " + equipement.getNom());
    }

    @Override
    public void deleteEntity(Equipement equipement) throws SQLException {
        String requete = "DELETE FROM equipements WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, equipement.getId());

        pst.executeUpdate();
        System.out.println("Equipement deleted: " + equipement.getNom());
    }

    @Override
    public void updateEntity(Equipement equipement) throws SQLException {
        String requete = "UPDATE equipements SET categorie_id = ?, fournisseur_id = ?, nom = ?, " +
                "description = ?, prix_achat = ?, prix_vente = ?, quantite_stock = ?, " +
                "seuil_alerte = ?, image_url = ?, disponible = ? WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, equipement.getCategorieId());
        pst.setInt(2, equipement.getFournisseurId());
        pst.setString(3, equipement.getNom());
        pst.setString(4, equipement.getDescription());
        pst.setBigDecimal(5, equipement.getPrixAchat());
        pst.setBigDecimal(6, equipement.getPrixVente());
        pst.setInt(7, equipement.getQuantiteStock());
        pst.setInt(8, equipement.getSeuilAlerte());
        pst.setString(9, equipement.getImageUrl());
        pst.setBoolean(10, equipement.isDisponible());
        pst.setInt(11, equipement.getId());

        pst.executeUpdate();
        System.out.println("Equipement updated: " + equipement.getNom());
    }

    @Override
    public List<Equipement> getEntities() throws SQLException {
        List<Equipement> equipements = new ArrayList<>();
        String requete = "SELECT * FROM equipements ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Equipement equipement = new Equipement(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getInt("fournisseur_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getBigDecimal("prix_achat"),
                    rs.getBigDecimal("prix_vente"),
                    rs.getInt("quantite_stock"),
                    rs.getInt("seuil_alerte"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            equipements.add(equipement);
        }

        return equipements;
    }

    // Additional custom methods for specific queries

    /**
     * Get all available equipment (disponible = true and stock > 0)
     */
    public List<Equipement> getAvailableEquipements() throws SQLException {
        List<Equipement> equipements = new ArrayList<>();
        String requete = "SELECT * FROM equipements WHERE disponible = true AND quantite_stock > 0 ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Equipement equipement = new Equipement(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getInt("fournisseur_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getBigDecimal("prix_achat"),
                    rs.getBigDecimal("prix_vente"),
                    rs.getInt("quantite_stock"),
                    rs.getInt("seuil_alerte"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            equipements.add(equipement);
        }

        return equipements;
    }

    /**
     * Get equipment with low stock (stock <= alert threshold)
     */
    public List<Equipement> getLowStockEquipements() throws SQLException {
        List<Equipement> equipements = new ArrayList<>();
        String requete = "SELECT * FROM equipements WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Equipement equipement = new Equipement(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getInt("fournisseur_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getBigDecimal("prix_achat"),
                    rs.getBigDecimal("prix_vente"),
                    rs.getInt("quantite_stock"),
                    rs.getInt("seuil_alerte"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            equipements.add(equipement);
        }

        return equipements;
    }

    /**
     * Search equipment by name
     */
    public List<Equipement> searchByName(String keyword) throws SQLException {
        List<Equipement> equipements = new ArrayList<>();
        String requete = "SELECT * FROM equipements WHERE nom LIKE ? OR description LIKE ? ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        String searchPattern = "%" + keyword + "%";
        pst.setString(1, searchPattern);
        pst.setString(2, searchPattern);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Equipement equipement = new Equipement(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getInt("fournisseur_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getBigDecimal("prix_achat"),
                    rs.getBigDecimal("prix_vente"),
                    rs.getInt("quantite_stock"),
                    rs.getInt("seuil_alerte"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            equipements.add(equipement);
        }

        return equipements;
    }

    /**
     * Update stock quantity for an equipment
     */
    public void updateStock(int equipementId, int newQuantity) throws SQLException {
        String requete = "UPDATE equipements SET quantite_stock = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, newQuantity);
        pst.setInt(2, equipementId);

        pst.executeUpdate();
        System.out.println("Stock updated for equipment ID: " + equipementId);
    }
}
