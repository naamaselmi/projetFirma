package Firma.services.GestionMarketplace;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Firma.entities.GestionMarketplace.Location;
import Firma.entities.GestionMarketplace.RentalStatus;
import Firma.interfaces.GestionMarketplace.IService;
import Firma.tools.GestionMarketplace.DB_connection;

/**
 * Service for Location entity
 */
public class LocationService implements IService<Location> {

    @Override
    public void addEntity(Location location) throws SQLException {
        // Generate numero_location if not set
        if (location.getNumeroLocation() == null || location.getNumeroLocation().isEmpty()) {
            String numeroLocation = generateNumeroLocation();
            location.setNumeroLocation(numeroLocation);
        }
        
        String requete;
        if ("vehicule".equalsIgnoreCase(location.getTypeLocation())) {
            requete = "INSERT INTO locations (id_utilisateur, type_location, vehicule_id, " +
                    "numero_location, date_debut, date_fin, duree_jours, prix_total, caution, statut, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            requete = "INSERT INTO locations (id_utilisateur, type_location, terrain_id, " +
                    "numero_location, date_debut, date_fin, duree_jours, prix_total, caution, statut, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, location.getUtilisateurId());
        pst.setString(2, location.getTypeLocation());
        pst.setInt(3, location.getElementId());
        pst.setString(4, location.getNumeroLocation());
        pst.setDate(5, Date.valueOf(location.getDateDebut()));
        pst.setDate(6, Date.valueOf(location.getDateFin()));
        pst.setInt(7, location.getDureeJours());
        pst.setBigDecimal(8, location.getPrixTotal());
        pst.setBigDecimal(9, location.getCaution());
        pst.setString(10, location.getStatut().getValue());
        pst.setString(11, location.getNotes());

        pst.executeUpdate();
        System.out.println("Location added: " + location.getTypeLocation() + " #" + location.getElementId() + " (" + location.getNumeroLocation() + ")");
    }
    
    /**
     * Generate unique location number
     */
    private String generateNumeroLocation() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = now.format(formatter);
        int randomPart = (int) (Math.random() * 9999);
        return String.format("LOC-%s-%04d", datePart, randomPart);
    }

    @Override
    public void deleteEntity(Location location) throws SQLException {
        String requete = "DELETE FROM locations WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, location.getId());

        pst.executeUpdate();
        System.out.println("Location deleted: " + location.getNumeroLocation());
    }

    @Override
    public void updateEntity(Location location) throws SQLException {
        String requete = "UPDATE locations SET date_debut = ?, date_fin = ?, duree_jours = ?, " +
                "prix_total = ?, caution = ?, statut = ?, notes = ? WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setDate(1, Date.valueOf(location.getDateDebut()));
        pst.setDate(2, Date.valueOf(location.getDateFin()));
        pst.setInt(3, location.getDureeJours());
        pst.setBigDecimal(4, location.getPrixTotal());
        pst.setBigDecimal(5, location.getCaution());
        pst.setString(6, location.getStatut().getValue());
        pst.setString(7, location.getNotes());
        pst.setInt(8, location.getId());

        pst.executeUpdate();
        System.out.println("Location updated: " + location.getNumeroLocation());
    }

    @Override
    public List<Location> getEntities() throws SQLException {
        List<Location> locations = new ArrayList<>();
        String requete = "SELECT * FROM locations ORDER BY date_debut DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_reservation");
            LocalDateTime dateReservation = timestamp != null ? timestamp.toLocalDateTime() : null;

            String typeLocation = rs.getString("type_location");
            int elementId = "vehicule".equalsIgnoreCase(typeLocation) 
                    ? rs.getInt("vehicule_id") 
                    : rs.getInt("terrain_id");

            Location location = new Location(
                    rs.getInt("id"),
                    rs.getInt("id_utilisateur"),
                    typeLocation,
                    elementId,
                    rs.getString("numero_location"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getInt("duree_jours"),
                    rs.getBigDecimal("prix_total"),
                    rs.getBigDecimal("caution"),
                    RentalStatus.fromString(rs.getString("statut")),
                    dateReservation,
                    rs.getString("notes"));
            locations.add(location);
        }

        return locations;
    }

    /**
     * Get locations for a user
     */
    public List<Location> getLocationsByUser(int utilisateurId) throws SQLException {
        List<Location> locations = new ArrayList<>();
        String requete = "SELECT * FROM locations WHERE id_utilisateur = ? ORDER BY date_debut DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, utilisateurId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_reservation");
            LocalDateTime dateReservation = timestamp != null ? timestamp.toLocalDateTime() : null;

            String typeLocation = rs.getString("type_location");
            int elementId = "vehicule".equalsIgnoreCase(typeLocation) 
                    ? rs.getInt("vehicule_id") 
                    : rs.getInt("terrain_id");

            Location location = new Location(
                    rs.getInt("id"),
                    rs.getInt("id_utilisateur"),
                    typeLocation,
                    elementId,
                    rs.getString("numero_location"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getInt("duree_jours"),
                    rs.getBigDecimal("prix_total"),
                    rs.getBigDecimal("caution"),
                    RentalStatus.fromString(rs.getString("statut")),
                    dateReservation,
                    rs.getString("notes"));
            locations.add(location);
        }

        return locations;
    }

    /**
     * Update location status
     */
    public void updateStatus(int locationId, RentalStatus status) throws SQLException {
        String requete = "UPDATE locations SET statut = ? WHERE id = ?";
        try (PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete)) {
            pst.setString(1, status.getValue());
            pst.setInt(2, locationId);
            int rowsUpdated = pst.executeUpdate();
            System.out.println("Location " + locationId + " status updated to: " + status + " (rows affected: " + rowsUpdated + ")");
        }
    }
}
