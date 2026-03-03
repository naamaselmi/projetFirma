package Firma.services.GestionTechnicien;

import Firma.entities.GestionTechnicien.Technicien;

public class TechnicienScore {
    private Technicien technicien;
    private double score;

    public TechnicienScore(Technicien technicien, double score) {
        this.technicien = technicien;
        this.score = score;
    }

    public Technicien getTechnicien() { return technicien; }
    public double getScore() { return score; }

    @Override
    public String toString() {
        return String.format("%s (%.1f/100) - %s",
                technicien.getNom(), score, technicien.getSpecialite());
    }
}