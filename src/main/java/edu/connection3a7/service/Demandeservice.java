package edu.connection3a7.service;

import edu.connection3a7.entities.Demande;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Demandeservice implements IService<Demande> {

    private Connection cnx;

    public Demandeservice() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ================= ADD =================
    @Override
    public void addentitiy(Demande d) throws SQLException {
        String sql = "INSERT INTO demande (id_utilisateur, type_probleme, description, date_demande, statut, id_tech) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getIdUtilisateur());
            ps.setString(2, d.getTypeProbleme());
            ps.setString(3, d.getDescription());
            ps.setDate(4, d.getDateDemande());
            ps.setString(5, d.getStatut());
            ps.setInt(6, d.getIdTech());

            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    d.setIdDemande(rs.getInt(1));
                }
            }
        }
    }

    // ================= DELETE =================
    @Override
    public void delet(Demande d) throws SQLException {
        String sql = "DELETE FROM demande WHERE id_demande = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, d.getIdDemande());
            ps.executeUpdate();
        }
    }

    // ================= UPDATE =================
    @Override
    public void update(Demande d) throws SQLException {
        String sql = "UPDATE demande SET id_utilisateur=?, type_probleme=?, description=?, date_demande=?, statut=?, id_tech=? " +
                "WHERE id_demande=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, d.getIdUtilisateur());
            ps.setString(2, d.getTypeProbleme());
            ps.setString(3, d.getDescription());
            ps.setDate(4, d.getDateDemande());
            ps.setString(5, d.getStatut());
            ps.setInt(6, d.getIdTech());
            ps.setInt(7, d.getIdDemande());

            ps.executeUpdate();
        }
    }

    // ================= GET ALL =================
    @Override
    public List<Demande> getdata() throws SQLException {
        List<Demande> list = new ArrayList<>();
        String sql = "SELECT * FROM demande ORDER BY date_demande DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Demande d = new Demande(
                        rs.getInt("id_demande"),
                        rs.getInt("id_utilisateur"),
                        rs.getString("type_probleme"),
                        rs.getString("description"),
                        rs.getDate("date_demande"),
                        rs.getString("statut"),
                        rs.getInt("id_tech")
                );
                list.add(d);
            }
        }
        return list;
    }

    // ================= MÉTHODE FORCÉE PAR IService =================
    @Override
    public List<Technicien> getData() throws SQLException {
        return new ArrayList<>();
    }

    // ================= MÉTHODES MÉTIER EXISTANTES =================

    /**
     * Récupère TOUTES les demandes d'un utilisateur
     */
    public List<Demande> getDemandesByUtilisateur(int idUtilisateur) throws SQLException {
        List<Demande> list = new ArrayList<>();
        String sql = "SELECT * FROM demande WHERE id_utilisateur = ? ORDER BY date_demande DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Demande d = new Demande(
                            rs.getInt("id_demande"),
                            rs.getInt("id_utilisateur"),
                            rs.getString("type_probleme"),
                            rs.getString("description"),
                            rs.getDate("date_demande"),
                            rs.getString("statut"),
                            rs.getInt("id_tech")
                    );
                    list.add(d);
                    System.out.println("📌 Demande trouvée: ID=" + d.getIdDemande() +
                            ", Statut=" + d.getStatut());
                }
            }
        }
        System.out.println("📊 Total demandes pour utilisateur " + idUtilisateur + ": " + list.size());
        return list;
    }

    /**
     * Récupère TOUTES les demandes d'un technicien
     */
    public List<Demande> getDemandesByTechnicien(int idTech) throws SQLException {
        List<Demande> list = new ArrayList<>();
        String sql = "SELECT * FROM demande WHERE id_tech = ? ORDER BY date_demande DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idTech);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Demande d = new Demande(
                            rs.getInt("id_demande"),
                            rs.getInt("id_utilisateur"),
                            rs.getString("type_probleme"),
                            rs.getString("description"),
                            rs.getDate("date_demande"),
                            rs.getString("statut"),
                            rs.getInt("id_tech")
                    );
                    list.add(d);
                }
            }
        }
        return list;
    }

    /**
     * Change le statut d'une demande
     */
    public void changerStatut(int idDemande, String nouveauStatut) throws SQLException {
        String sql = "UPDATE demande SET statut = ? WHERE id_demande = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idDemande);
            ps.executeUpdate();
            System.out.println("✅ Statut changé: Demande " + idDemande + " → " + nouveauStatut);
        }
    }

    // ================= NOUVELLES MÉTHODES POUR AUTO-ASSIGNATION =================

    /**
     * Récupère les demandes par statut
     * @param statut Le statut recherché ("En attente", "Acceptée", "Terminée", etc.)
     * @return Liste des demandes avec ce statut
     */
    public List<Demande> getDemandesByStatut(String statut) throws SQLException {
        List<Demande> list = new ArrayList<>();
        String sql = "SELECT * FROM demande WHERE statut = ? ORDER BY date_demande DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Demande d = new Demande(
                            rs.getInt("id_demande"),
                            rs.getInt("id_utilisateur"),
                            rs.getString("type_probleme"),
                            rs.getString("description"),
                            rs.getDate("date_demande"),
                            rs.getString("statut"),
                            rs.getInt("id_tech")
                    );
                    list.add(d);
                }
            }
        }
        return list;
    }

    /**
     * Compte le nombre de demandes pour un technicien à une date donnée
     * @param idTech L'ID du technicien
     * @param date La date (java.sql.Date)
     * @return Le nombre de demandes ce jour-là
     */
    public int compterDemandesParJour(int idTech, Date date) throws SQLException {
        String sql = "SELECT COUNT(*) as nb FROM demande " +
                "WHERE id_tech = ? AND date_demande = ? " +
                "AND statut IN ('Acceptée', 'En attente')";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idTech);
            ps.setDate(2, date);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nb");
                }
            }
        }
        return 0;
    }

    /**
     * Compte le nombre de demandes terminées pour un technicien
     * @param idTech L'ID du technicien
     * @return Le nombre de demandes terminées
     */
    public int compterDemandesTerminees(int idTech) throws SQLException {
        String sql = "SELECT COUNT(*) as nb FROM demande " +
                "WHERE id_tech = ? AND statut = 'Terminée'";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idTech);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nb");
                }
            }
        }
        return 0;
    }

    /**
     * Récupère une demande par son ID
     * @param idDemande L'ID de la demande
     * @return La demande trouvée ou null
     */
    public Demande getDemandeById(int idDemande) throws SQLException {
        String sql = "SELECT * FROM demande WHERE id_demande = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idDemande);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Demande(
                            rs.getInt("id_demande"),
                            rs.getInt("id_utilisateur"),
                            rs.getString("type_probleme"),
                            rs.getString("description"),
                            rs.getDate("date_demande"),
                            rs.getString("statut"),
                            rs.getInt("id_tech")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Vérifie si un technicien est disponible pour une date donnée
     * @param idTech L'ID du technicien
     * @param date La date
     * @param capaciteMax Capacité maximale par jour
     * @return true si disponible
     */
    public boolean estDisponible(int idTech, Date date, int capaciteMax) throws SQLException {
        int nbDemandes = compterDemandesParJour(idTech, date);
        return nbDemandes < capaciteMax;
    }

    /**
     * Récupère les statistiques des demandes par statut
     * @return Map avec les compteurs par statut
     */
    public java.util.Map<String, Integer> getStatistiquesParStatut() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        String sql = "SELECT statut, COUNT(*) as nb FROM demande GROUP BY statut";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("statut"), rs.getInt("nb"));
            }
        }
        return stats;
    }

    /**
     * Récupère le nombre total de demandes
     */
    public int getTotalDemandes() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM demande";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
}