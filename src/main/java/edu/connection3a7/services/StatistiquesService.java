package edu.connection3a7.services;

import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service de statistiques pour le dashboard analytique.
 * Fournit des données agrégées depuis la base de données.
 */
public class StatistiquesService {

    // ================================================================
    //  COMPTEURS GLOBAUX (KPIs)
    // ================================================================

    /** Nombre total d'événements */
    public int countEvenements() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM evenements");
    }

    /** Nombre d'événements actifs */
    public int countEvenementsActifs() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM evenements WHERE LOWER(statut) = 'actif'");
    }

    /** Nombre total de participations confirmées */
    public int countParticipationsConfirmees() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM participations WHERE statut = 'CONFIRME'");
    }

    /** Nombre de participations en attente */
    public int countParticipationsEnAttente() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM participations WHERE statut = 'EN_ATTENTE'");
    }

    /** Nombre total de participants (incluant accompagnants) */
    public int countTotalParticipants() throws SQLException {
        return countQuery("SELECT COALESCE(SUM(1 + nombre_accompagnants), 0) FROM participations WHERE statut = 'CONFIRME'");
    }

    /** Nombre total d'accompagnants */
    public int countAccompagnants() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM accompagnants");
    }

    /** Taux de remplissage moyen (%) */
    public double tauxRemplissageMoyen() throws SQLException {
        String sql = "SELECT AVG( (capacite_max - places_disponibles) * 100.0 / NULLIF(capacite_max, 0) ) " +
                     "FROM evenements WHERE LOWER(statut) = 'actif' AND capacite_max > 0";
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return Math.round(rs.getDouble(1) * 10.0) / 10.0;
        }
        return 0;
    }

    // ================================================================
    //  RÉPARTITIONS (pour PieChart)
    // ================================================================

    /** Répartition des événements par type */
    public Map<String, Integer> repartitionParType() throws SQLException {
        return groupQuery("SELECT LOWER(type_evenement), COUNT(*) FROM evenements GROUP BY LOWER(type_evenement) ORDER BY COUNT(*) DESC");
    }

    /** Répartition des événements par statut */
    public Map<String, Integer> repartitionParStatut() throws SQLException {
        return groupQuery("SELECT LOWER(statut), COUNT(*) FROM evenements GROUP BY LOWER(statut) ORDER BY COUNT(*) DESC");
    }

    /** Répartition des participations par statut */
    public Map<String, Integer> repartitionParticipationsParStatut() throws SQLException {
        return groupQuery("SELECT statut, COUNT(*) FROM participations GROUP BY statut ORDER BY COUNT(*) DESC");
    }

    // ================================================================
    //  SÉRIES TEMPORELLES (pour LineChart / BarChart)
    // ================================================================

    /** Nombre d'événements par mois (12 derniers mois) */
    public Map<String, Integer> evenementsParMois() throws SQLException {
        String sql = "SELECT DATE_FORMAT(date_debut, '%Y-%m') AS mois, COUNT(*) " +
                     "FROM evenements " +
                     "WHERE date_debut >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                     "GROUP BY mois ORDER BY mois";
        return groupQuery(sql);
    }

    /** Nombre de participations par mois (12 derniers mois) */
    public Map<String, Integer> participationsParMois() throws SQLException {
        String sql = "SELECT DATE_FORMAT(date_inscription, '%Y-%m') AS mois, COUNT(*) " +
                     "FROM participations " +
                     "WHERE date_inscription >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                     "GROUP BY mois ORDER BY mois";
        return groupQuery(sql);
    }

    // ================================================================
    //  TOP / CLASSEMENTS
    // ================================================================

    /** Top 5 événements les plus populaires (par nb de participations) */
    public Map<String, Integer> topEvenements() throws SQLException {
        String sql = "SELECT e.titre, COUNT(p.id_participation) AS nb " +
                     "FROM evenements e " +
                     "LEFT JOIN participations p ON e.id_evenement = p.id_evenement AND p.statut = 'CONFIRME' " +
                     "GROUP BY e.id_evenement, e.titre " +
                     "ORDER BY nb DESC LIMIT 5";
        return groupQuery(sql);
    }

    /** Événements avec le plus de places restantes (opportunités) */
    public Map<String, Integer> evenementsPlacesDisponibles() throws SQLException {
        String sql = "SELECT titre, places_disponibles FROM evenements " +
                     "WHERE LOWER(statut) = 'actif' AND places_disponibles > 0 " +
                     "ORDER BY places_disponibles DESC LIMIT 5";
        return groupQuery(sql);
    }

    // ================================================================
    //  ÉVÉNEMENTS À VENIR
    // ================================================================

    /** Nombre d'événements dans les 7 prochains jours */
    public int evenementsCetteSemaine() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM evenements WHERE date_debut BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)");
    }

    /** Nombre d'événements dans les 30 prochains jours */
    public int evenementsCeMois() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM evenements WHERE date_debut BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)");
    }

    // ================================================================
    //  UTILITAIRES
    // ================================================================

    private int countQuery(String sql) throws SQLException {
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Map<String, Integer> groupQuery(String sql) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String key = rs.getString(1);
                if (key == null || key.isBlank()) key = "Inconnu";
                map.put(capitaliser(key), rs.getInt(2));
            }
        }
        return map;
    }

    private String capitaliser(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
