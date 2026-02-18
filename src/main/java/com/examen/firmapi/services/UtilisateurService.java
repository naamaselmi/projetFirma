package com.examen.firmapi.services;

import com.examen.firmapi.entities.Role;
import com.examen.firmapi.entities.Utilisateur;
import com.examen.firmapi.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private Connection connection;

    public UtilisateurService() {
        connection = DBConnection.getInstance().getConnection();
    }

    // ✅ CREATE
    public void ajouterUtilisateur(Utilisateur u) {

        // 🔎 1️⃣ Contrôle de saisie
        if (u.getNom() == null || u.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }

        if (u.getPrenom() == null || u.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        }

        if (u.getEmail() == null || !u.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email invalide.");
        }

        if (u.getTelephone() == null || !u.getTelephone().matches("\\d{8,15}")) {
            throw new IllegalArgumentException("Téléphone invalide (8-15 chiffres).");
        }

        if (u.getMot_de_passe() == null || u.getMot_de_passe().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères.");
        }

        if (u.getRole() == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }

        if (u.getDate_inscription() == null) {
            u.setDate_inscription(new java.util.Date());
        }

        // 🔐 2️⃣ Encrypt password with bcrypt
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(
                u.getMot_de_passe(),
                org.mindrot.jbcrypt.BCrypt.gensalt(12)
        );

        String userSql = "INSERT INTO utilisateurs " +
                "(nom, prenom, email, telephone, adresse, ville, code_postal, role, mot_de_passe, date_inscription) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String profileSql = "INSERT INTO profile " +
                "(id_utilisateur, photo_profil, bio, date_naissance, genre, pays, ville, derniere_mise_a_jour) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {

            // 🔥 START TRANSACTION
            connection.setAutoCommit(false);

            // ================= INSERT USER =================
            PreparedStatement psUser = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);

            psUser.setString(1, u.getNom());
            psUser.setString(2, u.getPrenom());
            psUser.setString(3, u.getEmail());
            psUser.setString(4, u.getTelephone());
            psUser.setString(5, u.getAdresse());
            psUser.setString(6, u.getVille());
            psUser.setString(7, u.getCode_postal());
            psUser.setString(8, u.getRole().name().toLowerCase());
            psUser.setString(9, hashedPassword);
            psUser.setDate(10, new java.sql.Date(u.getDate_inscription().getTime()));

            psUser.executeUpdate();

            // 🔑 GET GENERATED ID
            ResultSet generatedKeys = psUser.getGeneratedKeys();
            int userId;

            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Échec récupération ID utilisateur.");
            }

            // ================= INSERT PROFILE =================
            PreparedStatement psProfile = connection.prepareStatement(profileSql);

            psProfile.setInt(1, userId);
            psProfile.setString(2, null); // photo_profil default null
            psProfile.setString(3, null); // bio default null
            psProfile.setDate(4, null);   // date_naissance null
            psProfile.setString(5, null); // genre null
            psProfile.setString(6, null); // pays null
            psProfile.setString(7, null); // ville null
            psProfile.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            psProfile.executeUpdate();

            // ✅ COMMIT
            connection.commit();
            System.out.println("✅ Utilisateur + Profile ajoutés avec succès");

        } catch (Exception e) {

            try {
                connection.rollback();
                System.out.println("⛔ Transaction annulée");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ✅ UPDATE
    public void modifierUtilisateur(Utilisateur u) {

        String sql = "UPDATE utilisateurs SET " +
                "nom = ?, prenom = ?, email = ?, telephone = ?, adresse = ?, " +
                "ville = ?, code_postal = ?, role = ? " +
                "WHERE id_utilisateur = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getAdresse());
            ps.setString(6, u.getVille());
            ps.setString(7, u.getCode_postal());
            ps.setString(8, u.getRole().name().toLowerCase());
            ps.setInt(9, u.getId_utilisateur());

            ps.executeUpdate();
            System.out.println("✅ Utilisateur modifié");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ✅ READ
    public List<Utilisateur> afficherUtilisateurs() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Utilisateur u = new Utilisateur();

                u.setId_utilisateur(rs.getInt("id_utilisateur"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setAdresse(rs.getString("adresse"));
                u.setVille(rs.getString("ville"));
                u.setCode_postal(rs.getString("code_postal"));
                u.setRole(Role.valueOf(rs.getString("role").toUpperCase()));
                u.setMot_de_passe(rs.getString("mot_de_passe"));
                u.setDate_inscription(rs.getDate("date_inscription"));

                list.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ✅ DELETE
    public void supprimerUtilisateur(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id_utilisateur = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Utilisateur supprimé");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

