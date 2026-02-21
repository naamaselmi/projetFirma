package edu.connection3a7.service;

import edu.connection3a7.entities.Technicien;
import edu.connection3a7.tools.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LocalisationTechnicienService {

    private Connection cnx;

    public LocalisationTechnicienService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    // ========== CLASSE POUR LA POSITION ==========

    public static class PositionTechnicien {
        private Technicien technicien;
        private double latitude;
        private double longitude;
        private boolean partageActif;
        private LocalDateTime derniereMaj;

        public PositionTechnicien(Technicien technicien) {
            this.technicien = technicien;
        }

        public boolean estActif() {
            if (!partageActif) return false;
            if (derniereMaj == null) return false;
            return derniereMaj.isAfter(LocalDateTime.now().minusMinutes(5));
        }

        public String getStatut() {
            if (!partageActif) return "🔴 Hors ligne";
            if (!estActif()) return "🟡 Inactif";
            return "🟢 En ligne";
        }

        // Getters et setters
        public Technicien getTechnicien() { return technicien; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public boolean isPartageActif() { return partageActif; }
        public void setPartageActif(boolean partageActif) { this.partageActif = partageActif; }
        public LocalDateTime getDerniereMaj() { return derniereMaj; }
        public void setDerniereMaj(LocalDateTime derniereMaj) { this.derniereMaj = derniereMaj; }
    }

    // ========== MÉTHODES ==========

    /**
     * Met à jour les colonnes dans la table technicien
     * Ajoutez ces colonnes si elles n'existent pas :
     * ALTER TABLE technicien ADD COLUMN latitude DOUBLE;
     * ALTER TABLE technicien ADD COLUMN longitude DOUBLE;
     * ALTER TABLE technicien ADD COLUMN partage_position BOOLEAN DEFAULT FALSE;
     * ALTER TABLE technicien ADD COLUMN derniere_maj_position TIMESTAMP;
     */

    public void activerPartage(int idTech, boolean actif) {
        String sql = "UPDATE technicien SET partage_position = ?, derniere_maj_position = ? WHERE id_tech = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, actif);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, idTech);
            ps.executeUpdate();
            System.out.println("📍 Technicien " + idTech + " partage: " + (actif ? "ACTIVÉ" : "DÉSACTIVÉ"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void mettreAJourPosition(int idTech, double latitude, double longitude) {
        String sql = "UPDATE technicien SET latitude = ?, longitude = ?, derniere_maj_position = ? WHERE id_tech = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, idTech);
            ps.executeUpdate();
            System.out.println("📍 Position mise à jour pour technicien " + idTech);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PositionTechnicien> getTechniciensAvecPosition() throws SQLException {
        List<PositionTechnicien> positions = new ArrayList<>();

        String sql = "SELECT * FROM technicien WHERE partage_position = TRUE " +
                "AND derniere_maj_position > DATE_SUB(NOW(), INTERVAL 5 MINUTE)";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Technicien t = new Technicien();
                t.setId_tech(rs.getInt("id_tech"));
                t.setNom(rs.getString("nom"));
                t.setPrenom(rs.getString("prenom"));
                t.setSpecialite(rs.getString("specialite"));

                PositionTechnicien pos = new PositionTechnicien(t);
                pos.setLatitude(rs.getDouble("latitude"));
                pos.setLongitude(rs.getDouble("longitude"));
                pos.setPartageActif(rs.getBoolean("partage_position"));
                pos.setDerniereMaj(rs.getTimestamp("derniere_maj_position").toLocalDateTime());

                positions.add(pos);
            }
        }

        return positions;
    }

    public void nettoyerPositionsExpirees() {
        String sql = "UPDATE technicien SET partage_position = FALSE " +
                "WHERE derniere_maj_position < DATE_SUB(NOW(), INTERVAL 10 MINUTE)";

        try (Statement st = cnx.createStatement()) {
            int rows = st.executeUpdate(sql);
            if (rows > 0) {
                System.out.println("🧹 " + rows + " position(s) expirée(s) nettoyée(s)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}