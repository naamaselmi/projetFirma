package marketplace.service;

import marketplace.entities.Vehicule;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Vehicule entity
 */
public class VehiculeService implements IService<Vehicule> {

    @Override
    public void addEntity(Vehicule vehicule) throws SQLException {
        String requete = "INSERT INTO vehicules (categorie_id, nom, description, marque, modele, " +
                "immatriculation, prix_jour, prix_semaine, prix_mois, caution, " +
                "image_url, disponible) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, vehicule.getCategorieId());
        pst.setString(2, vehicule.getNom());
        pst.setString(3, vehicule.getDescription());
        pst.setString(4, vehicule.getMarque());
        pst.setString(5, vehicule.getModele());
        pst.setString(6, vehicule.getImmatriculation());
        pst.setBigDecimal(7, vehicule.getPrixJour());
        pst.setBigDecimal(8, vehicule.getPrixSemaine());
        pst.setBigDecimal(9, vehicule.getPrixMois());
        pst.setBigDecimal(10, vehicule.getCaution());
        pst.setString(11, vehicule.getImageUrl());
        pst.setBoolean(12, vehicule.isDisponible());

        pst.executeUpdate();
        System.out.println("Vehicule added: " + vehicule.getNom());
    }

    @Override
    public void deleteEntity(Vehicule vehicule) throws SQLException {
        String requete = "DELETE FROM vehicules WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, vehicule.getId());

        pst.executeUpdate();
        System.out.println("Vehicule deleted: " + vehicule.getNom());
    }

    @Override
    public void updateEntity(Vehicule vehicule) throws SQLException {
        String requete = "UPDATE vehicules SET categorie_id = ?, nom = ?, description = ?, " +
                "marque = ?, modele = ?, immatriculation = ?, prix_jour = ?, " +
                "prix_semaine = ?, prix_mois = ?, caution = ?, image_url = ?, " +
                "disponible = ? WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, vehicule.getCategorieId());
        pst.setString(2, vehicule.getNom());
        pst.setString(3, vehicule.getDescription());
        pst.setString(4, vehicule.getMarque());
        pst.setString(5, vehicule.getModele());
        pst.setString(6, vehicule.getImmatriculation());
        pst.setBigDecimal(7, vehicule.getPrixJour());
        pst.setBigDecimal(8, vehicule.getPrixSemaine());
        pst.setBigDecimal(9, vehicule.getPrixMois());
        pst.setBigDecimal(10, vehicule.getCaution());
        pst.setString(11, vehicule.getImageUrl());
        pst.setBoolean(12, vehicule.isDisponible());
        pst.setInt(13, vehicule.getId());

        pst.executeUpdate();
        System.out.println("Vehicule updated: " + vehicule.getNom());
    }

    @Override
    public List<Vehicule> getEntities() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String requete = "SELECT * FROM vehicules ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Vehicule vehicule = new Vehicule(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("marque"),
                    rs.getString("modele"),
                    rs.getString("immatriculation"),
                    rs.getBigDecimal("prix_jour"),
                    rs.getBigDecimal("prix_semaine"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            vehicules.add(vehicule);
        }

        return vehicules;
    }

    /**
     * Get available vehicles
     */
    public List<Vehicule> getAvailableVehicules() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String requete = "SELECT * FROM vehicules WHERE disponible = true ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Vehicule vehicule = new Vehicule(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("marque"),
                    rs.getString("modele"),
                    rs.getString("immatriculation"),
                    rs.getBigDecimal("prix_jour"),
                    rs.getBigDecimal("prix_semaine"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            vehicules.add(vehicule);
        }

        return vehicules;
    }

    /**
     * Search vehicles by name or brand/model
     */
    public List<Vehicule> search(String keyword) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String requete = "SELECT * FROM vehicules WHERE nom LIKE ? OR marque LIKE ? OR modele LIKE ? ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        String searchPattern = "%" + keyword + "%";
        pst.setString(1, searchPattern);
        pst.setString(2, searchPattern);
        pst.setString(3, searchPattern);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Vehicule vehicule = new Vehicule(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("marque"),
                    rs.getString("modele"),
                    rs.getString("immatriculation"),
                    rs.getBigDecimal("prix_jour"),
                    rs.getBigDecimal("prix_semaine"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            vehicules.add(vehicule);
        }

        return vehicules;
    }
}
