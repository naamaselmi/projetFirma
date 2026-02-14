package marketplace.service;

import marketplace.entities.Location;
import marketplace.entities.RentalStatus;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Location entity
 */
public class LocationService implements IService<Location> {

    @Override
    public void addEntity(Location location) throws SQLException {
        String requete = "INSERT INTO locations (utilisateur_id, type_location, element_id, " +
                "date_debut, date_fin, duree_jours, prix_total, caution, statut, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, location.getUtilisateurId());
        pst.setString(2, location.getTypeLocation());
        pst.setInt(3, location.getElementId());
        pst.setDate(4, Date.valueOf(location.getDateDebut()));
        pst.setDate(5, Date.valueOf(location.getDateFin()));
        pst.setInt(6, location.getDureeJours());
        pst.setBigDecimal(7, location.getPrixTotal());
        pst.setBigDecimal(8, location.getCaution());
        pst.setString(9, location.getStatut().getValue());
        pst.setString(10, location.getNotes());

        pst.executeUpdate();
        System.out.println("Location added: " + location.getTypeLocation() + " #" + location.getElementId());
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

            Location location = new Location(
                    rs.getInt("id"),
                    rs.getInt("utilisateur_id"),
                    rs.getString("type_location"),
                    rs.getInt("element_id"),
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
        String requete = "SELECT * FROM locations WHERE utilisateur_id = ? ORDER BY date_debut DESC";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, utilisateurId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_reservation");
            LocalDateTime dateReservation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Location location = new Location(
                    rs.getInt("id"),
                    rs.getInt("utilisateur_id"),
                    rs.getString("type_location"),
                    rs.getInt("element_id"),
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
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, status.getValue());
        pst.setInt(2, locationId);

        pst.executeUpdate();
        System.out.println("Location status updated to: " + status);
    }
}
