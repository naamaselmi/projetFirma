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

    // ===================== Classe interne PositionTechnicien =====================
    public static class PositionTechnicien {
        private Technicien technicien;
        private double latitude;
        private double longitude;
        private boolean partageActif;
        private LocalDateTime derniereMaj;

        public PositionTechnicien(Technicien technicien) {
            this.technicien = technicien;
        }

        // Actif si partage activé ET coordonnées valides
        public boolean estActif() {
            return partageActif && latitude != 0.0 && longitude != 0.0;
        }

        // Statut affiché côté front
        public String getStatut() {
            if (!partageActif) return "Hors ligne";
            if (latitude == 0.0 && longitude == 0.0) return "Position inconnue";
            return "En ligne";
        }

        // ===== Getters / Setters =====
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

    // ===================== Activer / Désactiver le partage =====================
    public void activerPartage(int idTech, boolean actif) {
        String sql = "UPDATE technicien SET partage_position = ?, derniere_maj_position = ? WHERE id_tech = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, actif);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, idTech);
            ps.executeUpdate();
            System.out.println("Technicien " + idTech + " partage: " + (actif ? "ACTIVE" : "DESACTIVE"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== Mettre à jour la position =====================
    public void mettreAJourPosition(int idTech, double latitude, double longitude) {
        String sql = "UPDATE technicien SET latitude = ?, longitude = ?, derniere_maj_position = ? WHERE id_tech = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, idTech);
            ps.executeUpdate();
            System.out.println("Position mise à jour pour technicien " + idTech);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===================== Récupérer les techniciens avec position =====================
    public List<PositionTechnicien> getTechniciensAvecPosition() {
        List<PositionTechnicien> positions = new ArrayList<>();
        System.out.println("\nRECHERCHE DES TECHNICIENS EN LIGNE...");

        String sql = "SELECT * FROM technicien"; // On récupère tous les techniciens

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

                Timestamp ts = rs.getTimestamp("derniere_maj_position");
                if (ts != null) {
                    pos.setDerniereMaj(ts.toLocalDateTime());
                }

                // 🔹 Filtrage côté Java pour ne garder que les techniciens actifs avec position valide
                if (pos.estActif()) {
                    positions.add(pos);
                    System.out.println("Technicien actif: " + t.getPrenom() + " " + t.getNom()
                            + " | LAT=" + pos.getLatitude() + " | LNG=" + pos.getLongitude());
                }
            }

            System.out.println("TOTAL: " + positions.size() + " technicien(s) visibles sur la carte");

        } catch (SQLException e) {
            System.out.println("ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }

        return positions;
    }
}