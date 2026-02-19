package edu.connection3a7.service;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.*;

public class AutoAssignationService {

    private Connection cnx;
    private Technicienserv technicienService;
    private Avisservice avisService;
    private Demandeservice demandeService;

    // Poids des critères
    private static final int POIDS_COMPETENCE = 35;
    private static final int POIDS_DISPONIBILITE = 30;
    private static final int POIDS_NOTE = 20;
    private static final int POIDS_EXPERIENCE = 15;
    private static final int CAPACITE_MAX_JOUR = 6;

    public AutoAssignationService() {
        this.cnx = MyConnection.getInstance().getCnx();
        this.technicienService = new Technicienserv();
        this.avisService = new Avisservice();
        this.demandeService = new Demandeservice();
    }

    // ================= CLASSE INTERNE =================
    public static class TechnicienScore {
        private Technicien technicien;
        private double score;
        private Map<String, Double> details;

        public TechnicienScore(Technicien technicien, double score) {
            this.technicien = technicien;
            this.score = score;
            this.details = new HashMap<>();
        }

        public Technicien getTechnicien() { return technicien; }
        public double getScore() { return score; }
        public Map<String, Double> getDetails() { return details; }
        public void addDetail(String critere, double valeur) { details.put(critere, valeur); }

        @Override
        public String toString() {
            return String.format("%s (%.1f/100) - %s",
                    technicien.getNom(), score, technicien.getSpecialite());
        }

        public String getDetailsString() {
            StringBuilder sb = new StringBuilder();
            sb.append("👤 ").append(technicien.getNom()).append(" (").append(technicien.getSpecialite()).append(")\n");
            for (Map.Entry<String, Double> entry : details.entrySet()) {
                sb.append(String.format("  • %s: +%.1f\n", entry.getKey(), entry.getValue()));
            }
            sb.append(String.format("  TOTAL: %.1f/100", score));
            return sb.toString();
        }
    }

    // ================= MÉTHODE PRINCIPALE =================
    public TechnicienScore trouverMeilleurTechnicien(Demande demande) throws SQLException {
        List<Technicien> tousTechniciens = technicienService.getdata();
        List<TechnicienScore> scores = new ArrayList<>();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔍 ANALYSE POUR DEMANDE");
        System.out.println("📋 Type: " + demande.getTypeProbleme());
        System.out.println("📅 Date: " + demande.getDateDemande());
        System.out.println("=".repeat(60));

        for (Technicien tech : tousTechniciens) {
            TechnicienScore techScore = calculerScore(tech, demande);
            scores.add(techScore);
        }

        scores.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        if (!scores.isEmpty()) {
            System.out.println("\n🏆 MEILLEUR TECHNICIEN: " + scores.get(0));
            System.out.println("=".repeat(60));
        }

        return scores.isEmpty() ? null : scores.get(0);
    }

    private TechnicienScore calculerScore(Technicien tech, Demande demande) throws SQLException {
        double score = 0;
        TechnicienScore techScore = new TechnicienScore(tech, 0);

        System.out.println("\n👤 " + tech.getNom() + " (" + tech.getSpecialite() + "):");

        // 1. COMPÉTENCE
        if (tech.getSpecialite().equalsIgnoreCase(demande.getTypeProbleme())) {
            score += POIDS_COMPETENCE;
            techScore.addDetail("Compétence", (double) POIDS_COMPETENCE);
            System.out.println("  ✓ Compétence: +" + POIDS_COMPETENCE);
        } else {
            techScore.addDetail("Compétence", 0.0);
            System.out.println("  ✗ Compétence: 0");
        }

        // 2. DISPONIBILITÉ
        int nbDemandesJour = demandeService.compterDemandesParJour(
                tech.getId_tech(), demande.getDateDemande()
        );
        double scoreDispo = POIDS_DISPONIBILITE * (1 - ((double)nbDemandesJour / CAPACITE_MAX_JOUR));
        scoreDispo = Math.max(0, Math.min(POIDS_DISPONIBILITE, scoreDispo));
        score += scoreDispo;
        techScore.addDetail("Disponibilité", scoreDispo);
        System.out.println("  📊 Disponibilité (" + nbDemandesJour + "/" + CAPACITE_MAX_JOUR + "): +" +
                String.format("%.1f", scoreDispo));

        // 3. NOTE MOYENNE
        double noteMoyenne = avisService.getNoteMoyenneTechnicien(tech.getId_tech());
        double scoreNote = (noteMoyenne / 5.0) * POIDS_NOTE;
        score += scoreNote;
        techScore.addDetail("Note moyenne", scoreNote);
        System.out.println("  ⭐ Note moyenne (" + String.format("%.1f", noteMoyenne) + "/5): +" +
                String.format("%.1f", scoreNote));

        // 4. EXPÉRIENCE
        int nbTerminees = demandeService.compterDemandesTerminees(tech.getId_tech());
        double scoreExp = Math.min(POIDS_EXPERIENCE, nbTerminees / 10.0);
        score += scoreExp;
        techScore.addDetail("Expérience", scoreExp);
        System.out.println("  💼 Expérience (" + nbTerminees + " interventions): +" +
                String.format("%.1f", scoreExp));

        System.out.println("  TOTAL: " + String.format("%.1f", score) + "/100");

        techScore.score = score;
        return techScore;
    }

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
        return true;
    }
}