package marketplace.service;

import marketplace.entities.Terrain;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Terrain entity
 */
public class TerrainService implements IService<Terrain> {

    @Override
    public void addEntity(Terrain terrain) throws SQLException {
        String requete = "INSERT INTO terrains (categorie_id, titre, description, superficie_hectares, " +
                "ville, adresse, prix_mois, prix_annee, caution, image_url, disponible) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, terrain.getCategorieId());
        pst.setString(2, terrain.getTitre());
        pst.setString(3, terrain.getDescription());
        pst.setBigDecimal(4, terrain.getSuperficieHectares());
        pst.setString(5, terrain.getVille());
        pst.setString(6, terrain.getAdresse());
        pst.setBigDecimal(7, terrain.getPrixMois());
        pst.setBigDecimal(8, terrain.getPrixAnnee());
        pst.setBigDecimal(9, terrain.getCaution());
        pst.setString(10, terrain.getImageUrl());
        pst.setBoolean(11, terrain.isDisponible());

        pst.executeUpdate();
        System.out.println("Terrain added: " + terrain.getTitre());
    }

    @Override
    public void deleteEntity(Terrain terrain) throws SQLException {
        String requete = "DELETE FROM terrains WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, terrain.getId());

        pst.executeUpdate();
        System.out.println("Terrain deleted: " + terrain.getTitre());
    }

    @Override
    public void updateEntity(Terrain terrain) throws SQLException {
        String requete = "UPDATE terrains SET categorie_id = ?, titre = ?, description = ?, " +
                "superficie_hectares = ?, ville = ?, adresse = ?, prix_mois = ?, " +
                "prix_annee = ?, caution = ?, image_url = ?, disponible = ? WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, terrain.getCategorieId());
        pst.setString(2, terrain.getTitre());
        pst.setString(3, terrain.getDescription());
        pst.setBigDecimal(4, terrain.getSuperficieHectares());
        pst.setString(5, terrain.getVille());
        pst.setString(6, terrain.getAdresse());
        pst.setBigDecimal(7, terrain.getPrixMois());
        pst.setBigDecimal(8, terrain.getPrixAnnee());
        pst.setBigDecimal(9, terrain.getCaution());
        pst.setString(10, terrain.getImageUrl());
        pst.setBoolean(11, terrain.isDisponible());
        pst.setInt(12, terrain.getId());

        pst.executeUpdate();
        System.out.println("Terrain updated: " + terrain.getTitre());
    }

    @Override
    public List<Terrain> getEntities() throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String requete = "SELECT * FROM terrains ORDER BY titre";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Terrain terrain = new Terrain(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getBigDecimal("superficie_hectares"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("prix_annee"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            terrains.add(terrain);
        }

        return terrains;
    }

    /**
     * Get available terrains
     */
    public List<Terrain> getAvailableTerrains() throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String requete = "SELECT * FROM terrains WHERE disponible = true ORDER BY titre";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Terrain terrain = new Terrain(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getBigDecimal("superficie_hectares"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("prix_annee"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            terrains.add(terrain);
        }

        return terrains;
    }

    /**
     * Search terrains by title or ville
     */
    public List<Terrain> searchByTitle(String keyword) throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String requete = "SELECT * FROM terrains WHERE titre LIKE ? OR ville LIKE ? ORDER BY titre";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        String searchPattern = "%" + keyword + "%";
        pst.setString(1, searchPattern);
        pst.setString(2, searchPattern);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Terrain terrain = new Terrain(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getBigDecimal("superficie_hectares"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("prix_annee"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            terrains.add(terrain);
        }

        return terrains;
    }

    /**
     * Search terrains by location
     */
    public List<Terrain> searchByVille(String ville) throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String requete = "SELECT * FROM terrains WHERE ville LIKE ? ORDER BY titre";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, "%" + ville + "%");
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Terrain terrain = new Terrain(
                    rs.getInt("id"),
                    rs.getInt("categorie_id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getBigDecimal("superficie_hectares"),
                    rs.getString("ville"),
                    rs.getString("adresse"),
                    rs.getBigDecimal("prix_mois"),
                    rs.getBigDecimal("prix_annee"),
                    rs.getBigDecimal("caution"),
                    rs.getString("image_url"),
                    rs.getBoolean("disponible"),
                    dateCreation);
            terrains.add(terrain);
        }

        return terrains;
    }
}
