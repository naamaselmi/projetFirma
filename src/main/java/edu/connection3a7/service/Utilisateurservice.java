package edu.connection3a7.service;

import edu.connection3a7.entities.Utilisateur;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Utilisateurservice {

    private Connection cnx;

    public Utilisateurservice() {
        cnx = MyConnection.getInstance().getCnx();
    }

    // ================= ADD =================
    public void addUtilisateur(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, telephone, adresse, ville, code_postal, role, mot_de_passe, date_inscription) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getTelephone());
        ps.setString(5, u.getAdresse());
        ps.setString(6, u.getVille());
        ps.setString(7, u.getCodePostal());
        ps.setString(8, u.getRole());
        ps.setString(9, u.getMotDePasse());
        ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));

        ps.executeUpdate();

        // Récupérer l'ID généré
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            u.setIdUtilisateur(rs.getInt(1));
        }
    }

    // ================= DELETE =================
    public void deleteUtilisateur(Utilisateur u) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, u.getIdUtilisateur());
        ps.executeUpdate();
    }

    // ================= UPDATE =================
    public void updateUtilisateur(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, telephone=?, adresse=?, ville=?, code_postal=?, role=?, mot_de_passe=? " +
                "WHERE id_utilisateur=?";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, u.getNom());
        ps.setString(2, u.getPrenom());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getTelephone());
        ps.setString(5, u.getAdresse());
        ps.setString(6, u.getVille());
        ps.setString(7, u.getCodePostal());
        ps.setString(8, u.getRole());
        ps.setString(9, u.getMotDePasse());
        ps.setInt(10, u.getIdUtilisateur());

        ps.executeUpdate();
    }

    // ================= GET ALL =================
    public List<Utilisateur> getAllUtilisateurs() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();

        String sql = "SELECT * FROM utilisateur";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Utilisateur u = new Utilisateur(
                    rs.getInt("id_utilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("code_postal"),
                    rs.getString("role"),
                    rs.getString("mot_de_passe"),
                    rs.getTimestamp("date_inscription")
            );
            list.add(u);
        }
        return list;
    }

    // ================= GET BY ID =================
    public Utilisateur getUtilisateurById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Utilisateur(
                    rs.getInt("id_utilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("code_postal"),
                    rs.getString("role"),
                    rs.getString("mot_de_passe"),
                    rs.getTimestamp("date_inscription")
            );
        }
        return null;
    }

    // ================= GET BY EMAIL =================
    public Utilisateur getUtilisateurByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Utilisateur(
                    rs.getInt("id_utilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("code_postal"),
                    rs.getString("role"),
                    rs.getString("mot_de_passe"),
                    rs.getTimestamp("date_inscription")
            );
        }
        return null;
    }

    // ================= AUTHENTIFICATION =================
    public Utilisateur authentifier(String email, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, motDePasse);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Utilisateur(
                    rs.getInt("id_utilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getString("code_postal"),
                    rs.getString("role"),
                    rs.getString("mot_de_passe"),
                    rs.getTimestamp("date_inscription")
            );
        }
        return null;
    }
}