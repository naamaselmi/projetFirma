package edu.connection3a7.service;

import edu.connection3a7.entities.Avis;
import edu.connection3a7.entities.Technicien;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Avisservice implements IService<Avis> {

    private Connection cnx;

    public Avisservice() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ================= CREATE =================
    @Override
    public void addentitiy(Avis avis) throws SQLException {
        String sql = "INSERT INTO avis (id_utilisateur, note, commentaire, date_avis, id_tech, id_demande) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, avis.getIdUtilisateur());

            // Gérer note NULL
            if (avis.getNote() != null) {
                ps.setInt(2, avis.getNote());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            // Gérer commentaire NULL
            if (avis.getCommentaire() != null) {
                ps.setString(3, avis.getCommentaire());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }

            // Gérer date NULL
            if (avis.getDateAvis() != null) {
                ps.setDate(4, avis.getDateAvis());
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }

            // Gérer id_tech NULL
            if (avis.getIdTech() != null) {
                ps.setInt(5, avis.getIdTech());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            // Gérer id_demande NULL
            if (avis.getIdDemande() != null) {
                ps.setInt(6, avis.getIdDemande());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    avis.setIdAvis(rs.getInt(1));
                }
            }
        }
    }

    // ================= READ ALL =================
    @Override
    public List<Avis> getdata() throws SQLException {
        List<Avis> list = new ArrayList<>();
        String sql = "SELECT * FROM avis ORDER BY date_avis DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Avis avis = new Avis();
                avis.setIdAvis(rs.getInt("id_avis"));
                avis.setIdUtilisateur(rs.getInt("id_utilisateur"));
                avis.setNote(rs.getInt("note"));
                avis.setCommentaire(rs.getString("commentaire"));
                avis.setDateAvis(rs.getDate("date_avis"));
                avis.setIdTech(rs.getInt("id_tech"));
                avis.setIdDemande(rs.getInt("id_demande"));
                list.add(avis);
            }
        }
        return list;
    }

    // ================= UPDATE =================
    @Override
    public void update(Avis avis) throws SQLException {
        String sql = "UPDATE avis SET note = ?, commentaire = ? WHERE id_avis = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (avis.getNote() != null) {
                ps.setInt(1, avis.getNote());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            if (avis.getCommentaire() != null) {
                ps.setString(2, avis.getCommentaire());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }

            ps.setInt(3, avis.getIdAvis());

            ps.executeUpdate();
        }
    }

    // ================= DELETE =================
    @Override
    public void delet(Avis avis) throws SQLException {
        String sql = "DELETE FROM avis WHERE id_avis = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, avis.getIdAvis());
            ps.executeUpdate();
        }
    }

    // ================= MÉTHODE NON UTILISÉE =================
    @Override
    public List<Technicien> getData() throws SQLException {
        return new ArrayList<>();
    }

    // ================= MÉTHODES SPÉCIFIQUES =================

    public List<Avis> getAvisByTechnicien(int idTech) throws SQLException {
        List<Avis> list = new ArrayList<>();
        String sql = "SELECT * FROM avis WHERE id_tech = ? ORDER BY date_avis DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idTech);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Avis avis = new Avis();
                    avis.setIdAvis(rs.getInt("id_avis"));
                    avis.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    avis.setNote(rs.getInt("note"));
                    avis.setCommentaire(rs.getString("commentaire"));
                    avis.setDateAvis(rs.getDate("date_avis"));
                    avis.setIdTech(rs.getInt("id_tech"));
                    avis.setIdDemande(rs.getInt("id_demande"));
                    list.add(avis);
                }
            }
        }
        return list;
    }

    public double getNoteMoyenneTechnicien(int idTech) throws SQLException {
        String sql = "SELECT AVG(note) as moyenne FROM avis WHERE id_tech = ? AND note IS NOT NULL";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idTech);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        }
        return 0.0;
    }
}