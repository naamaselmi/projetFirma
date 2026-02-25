package edu.connection3a7.service;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.entities.Coordonnees;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.*;

public class AutoAssignationService {

    private Connection cnx;
    private Technicienserv technicienService;
    private Avisservice avisService;
    private Demandeservice demandeService;
    private GeocodageService geocodageService;

    // Poids des critères (total = 100)
    private static final int POIDS_COMPETENCE = 30;      // -5 (car distance prend 10)
    private static final int POIDS_DISPONIBILITE = 25;   // -5
    private static final int POIDS_NOTE = 15;            // -5
    private static final int POIDS_EXPERIENCE = 10;      // -5
    private static final int POIDS_DISTANCE = 20;        // NOUVEAU : 20 points

    private static final int CAPACITE_MAX_JOUR = 6;
    private static final double DISTANCE_MAX_KM = 50;    // Distance maximale considérée

    public AutoAssignationService() {
        this.cnx = MyConnection.getInstance().getCnx();
        this.technicienService = new Technicienserv();
        this.avisService = new Avisservice();
        this.demandeService = new Demandeservice();
        this.geocodageService = new GeocodageService();
    }

    // ================= CLASSE INTERNE =================
    public static class TechnicienScore {
        private Technicien technicien;
        private double score;
        private double distanceKm;
        private Map<String, Double> details;

        public TechnicienScore(Technicien technicien, double score) {
            this.technicien = technicien;
            this.score = score;
            this.distanceKm = 0;
            this.details = new HashMap<>();
        }

        public Technicien getTechnicien() { return technicien; }
        public double getScore() { return score; }
        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        public Map<String, Double> getDetails() { return details; }
        public void addDetail(String critere, double valeur) { details.put(critere, valeur); }

        @Override
        public String toString() {
            return String.format("%s (%.1f/100, dist: %.1f km) - %s",
                    technicien.getNom(), score, distanceKm, technicien.getSpecialite());
        }

        public String getDetailsString() {
            StringBuilder sb = new StringBuilder();
            sb.append("👤 ").append(technicien.getNom()).append(" (").append(technicien.getSpecialite()).append(")\n");
            for (Map.Entry<String, Double> entry : details.entrySet()) {
                sb.append(String.format("  • %s: +%.1f\n", entry.getKey(), entry.getValue()));
            }
            sb.append(String.format("  • 📍 Distance: %.1f km\n", distanceKm));
            sb.append(String.format("  TOTAL: %.1f/100", score));
            return sb.toString();
        }
    }

    // ================= MÉTHODE PRINCIPALE =================
    public TechnicienScore trouverMeilleurTechnicien(Demande demande) throws SQLException {
        List<Technicien> tousTechniciens = technicienService.getdata();
        List<TechnicienScore> scores = new ArrayList<>();

        // 1. Obtenir les coordonnées du client (depuis la demande)
        Coordonnees coordsClient = null;
        if (demande.getAdresseClient() != null && !demande.getAdresseClient().isEmpty()) {
            Optional<Coordonnees> result = geocodageService.geocoderAdresse(demande.getAdresseClient());
            if (result.isPresent()) {
                coordsClient = result.get();
                System.out.println("📍 Adresse client géocodée: " + demande.getAdresseClient());
            } else {
                System.out.println("⚠️ Impossible de géocoder l'adresse client");
            }
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔍 ANALYSE POUR DEMANDE");
        System.out.println("📋 Type: " + demande.getTypeProbleme());
        System.out.println("📅 Date: " + demande.getDateDemande());
        if (coordsClient != null) {
            System.out.println(String.format("📍 Client: %.6f, %.6f",
                    coordsClient.getLatitude(), coordsClient.getLongitude()));
        }
        System.out.println("=".repeat(70));

        for (Technicien tech : tousTechniciens) {
            TechnicienScore techScore = calculerScore(tech, demande, coordsClient);
            scores.add(techScore);
        }

        scores.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        if (!scores.isEmpty()) {
            System.out.println("\n🏆 MEILLEUR TECHNICIEN: " + scores.get(0));
            System.out.println("=".repeat(70));
        }

        return scores.isEmpty() ? null : scores.get(0);
    }

    private TechnicienScore calculerScore(Technicien tech, Demande demande, Coordonnees coordsClient) throws SQLException {
        double score = 0;
        TechnicienScore techScore = new TechnicienScore(tech, 0);

        System.out.println("\n👤 " + tech.getNom() + " (" + tech.getSpecialite() + "):");

        // 1. COMPÉTENCE (30 points)
        if (tech.getSpecialite().equalsIgnoreCase(demande.getTypeProbleme())) {
            score += POIDS_COMPETENCE;
            techScore.addDetail("Compétence", (double) POIDS_COMPETENCE);
            System.out.println("  ✓ Compétence: +" + POIDS_COMPETENCE);
        } else {
            techScore.addDetail("Compétence", 0.0);
            System.out.println("  ✗ Compétence: 0");
        }

        // 2. DISPONIBILITÉ (25 points)
        int nbDemandesJour = demandeService.compterDemandesParJour(
                tech.getId_tech(), demande.getDateDemande()
        );
        double scoreDispo = POIDS_DISPONIBILITE * (1 - ((double)nbDemandesJour / CAPACITE_MAX_JOUR));
        scoreDispo = Math.max(0, Math.min(POIDS_DISPONIBILITE, scoreDispo));
        score += scoreDispo;
        techScore.addDetail("Disponibilité", scoreDispo);
        System.out.println("  📊 Disponibilité (" + nbDemandesJour + "/" + CAPACITE_MAX_JOUR + "): +" +
                String.format("%.1f", scoreDispo));

        // 3. NOTE MOYENNE (15 points)
        double noteMoyenne = avisService.getNoteMoyenneTechnicien(tech.getId_tech());
        double scoreNote = (noteMoyenne / 5.0) * POIDS_NOTE;
        score += scoreNote;
        techScore.addDetail("Note moyenne", scoreNote);
        System.out.println("  ⭐ Note moyenne (" + String.format("%.1f", noteMoyenne) + "/5): +" +
                String.format("%.1f", scoreNote));

        // 4. EXPÉRIENCE (10 points)
        int nbTerminees = demandeService.compterDemandesTerminees(tech.getId_tech());
        double scoreExp = Math.min(POIDS_EXPERIENCE, nbTerminees / 10.0);
        score += scoreExp;
        techScore.addDetail("Expérience", scoreExp);
        System.out.println("  💼 Expérience (" + nbTerminees + " interventions): +" +
                String.format("%.1f", scoreExp));

        // 5. DISTANCE (20 points) - NOUVEAU
        double scoreDistance = 0;
        double distanceKm = 0;

        if (coordsClient != null) {
            // Récupérer les coordonnées du technicien
            Coordonnees coordsTech = getCoordonneesTechnicien(tech);

            if (coordsTech != null) {
                distanceKm = calculerDistanceHaversine(
                        coordsTech.getLatitude(), coordsTech.getLongitude(),
                        coordsClient.getLatitude(), coordsClient.getLongitude()
                );

                // Calcul du score de distance (plus on est proche, plus le score est élevé)
                // Si distance = 0 → 20 points, si distance >= DISTANCE_MAX_KM → 0 points
                scoreDistance = POIDS_DISTANCE * (1 - Math.min(1, distanceKm / DISTANCE_MAX_KM));

                System.out.println(String.format("  📍 Distance client: %.1f km → +%.1f",
                        distanceKm, scoreDistance));
            } else {
                System.out.println("  ⚠️ Position du technicien inconnue → 0 point");
            }
        } else {
            System.out.println("  ⚠️ Position client inconnue → score distance = 10 (moyen)");
            scoreDistance = POIDS_DISTANCE / 2; // Score par défaut
        }

        techScore.addDetail("Proximité", scoreDistance);
        techScore.setDistanceKm(distanceKm);
        score += scoreDistance;

        System.out.println("  TOTAL: " + String.format("%.1f", score) + "/100");

        techScore.score = score;
        return techScore;
    }

    // ========== MÉTHODES UTILITAIRES POUR LA GÉOLOCALISATION ==========

    /**
     * Récupère les coordonnées d'un technicien
     */
    private Coordonnees getCoordonneesTechnicien(Technicien tech) {
        // Essayer d'abord de récupérer depuis la base
        try {
            String sql = "SELECT latitude, longitude FROM technicien WHERE id_tech = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, tech.getId_tech());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double lat = rs.getDouble("latitude");
                    double lng = rs.getDouble("longitude");
                    if (lat != 0 && lng != 0) {
                        return new Coordonnees(lat, lng, tech.getLocalisation());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sinon, géocoder à partir de l'adresse
        if (tech.getLocalisation() != null && !tech.getLocalisation().isEmpty()) {
            Optional<Coordonnees> result = geocodageService.geocoderAdresse(tech.getLocalisation());
            if (result.isPresent()) {
                return result.get();
            }
        }

        return null;
    }

    /**
     * Calcule la distance entre deux points GPS (formule de Haversine)
     */
    private double calculerDistanceHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ========== MÉTHODE D'ASSIGNATION ==========

    public boolean assignerDemande(Demande demande) throws SQLException {
        TechnicienScore meilleur = trouverMeilleurTechnicien(demande);

        if (meilleur == null) {
            System.out.println("❌ Aucun technicien disponible");
            return false;
        }

        if (meilleur.getScore() < 50) {
            System.out.println("⚠️ Score trop faible (" + String.format("%.1f", meilleur.getScore()) + ")");
            return false;
        }

        demande.setIdTech(meilleur.getTechnicien().getId_tech());
        demande.setStatut("Acceptée");
        demandeService.update(demande);

        System.out.println("\n✅ Demande assignée à " + meilleur.getTechnicien().getNom());
        System.out.println(String.format("   Distance: %.1f km", meilleur.getDistanceKm()));
        return true;
    }

    /**
     * Version alternative qui donne plus de poids à la distance
     */
    public TechnicienScore trouverMeilleurTechnicienProximite(Demande demande) throws SQLException {
        List<Technicien> tousTechniciens = technicienService.getdata();
        List<TechnicienScore> scores = new ArrayList<>();

        Coordonnees coordsClient = null;
        if (demande.getAdresseClient() != null && !demande.getAdresseClient().isEmpty()) {
            Optional<Coordonnees> result = geocodageService.geocoderAdresse(demande.getAdresseClient());
            if (result.isPresent()) {
                coordsClient = result.get();
            }
        }

        for (Technicien tech : tousTechniciens) {
            double scoreDistance = 0;
            double distanceKm = 0;

            Coordonnees coordsTech = getCoordonneesTechnicien(tech);

            if (coordsClient != null && coordsTech != null) {
                distanceKm = calculerDistanceHaversine(
                        coordsTech.getLatitude(), coordsTech.getLongitude(),
                        coordsClient.getLatitude(), coordsClient.getLongitude()
                );
                // Score basé uniquement sur la distance (plus petite = meilleur)
                scoreDistance = 100 * (1 - Math.min(1, distanceKm / DISTANCE_MAX_KM));
            }

            TechnicienScore techScore = new TechnicienScore(tech, scoreDistance);
            techScore.setDistanceKm(distanceKm);
            techScore.addDetail("Proximité", scoreDistance);
            scores.add(techScore);
        }

        scores.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return scores.isEmpty() ? null : scores.get(0);
    }
}