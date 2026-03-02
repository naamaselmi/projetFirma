package com.examen.firmapi.services;

import com.examen.firmapi.entities.Genre;
import com.examen.firmapi.entities.Profile;
import com.examen.firmapi.entities.Role;
import com.examen.firmapi.entities.Utilisateur;
import com.examen.firmapi.utils.DBConnection;
import com.examen.firmapi.utils.EmailUtil;
import com.examen.firmapi.utils.TempPasswordStore;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // LOGIN
    public Utilisateur login(String email, String password) {

        String sql = "SELECT * FROM utilisateurs WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String hashedPassword = rs.getString("mot_de_passe");

                if (BCrypt.checkpw(password, hashedPassword)) {

                    Utilisateur u = new Utilisateur();
                    u.setId_utilisateur(rs.getInt("id_utilisateur"));
                    u.setNom(rs.getString("nom"));
                    u.setPrenom(rs.getString("prenom"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(Role.valueOf(rs.getString("role").toUpperCase()));

                    return u;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //RESET PWD
    public void resetPassword(String email) throws Exception {

        String checkSql = "SELECT * FROM utilisateurs WHERE email = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            throw new Exception("Email not found.");
        }

        // 1️⃣ Generate temporary password
        String tempPassword = generateTemporaryPassword();

        // 2️⃣ Encrypt it
        String hashed = BCrypt.hashpw(tempPassword, BCrypt.gensalt(12));

        // 3️⃣ Update DB
        String updateSql = "UPDATE utilisateurs SET mot_de_passe = ? WHERE email = ?";
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setString(1, hashed);
        updateStmt.setString(2, email);
        updateStmt.executeUpdate();

        // 4️⃣ Store plain version in memory
        TempPasswordStore.save(email, tempPassword);

        // 5️⃣ Send email (you will implement EmailUtil)
        EmailUtil.sendEmail(email, tempPassword);
    }

    // CHANGE PASSWORD AFTER TEMP LOGIN
    public void updatePassword(String email, String newPassword) throws Exception {

        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("Password must contain at least 6 characters.");
        }

        // 1️⃣ Check user exists
        String checkSql = "SELECT id_utilisateur FROM utilisateurs WHERE email = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            throw new Exception("User not found.");
        }

        // 2️⃣ Encrypt new password
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));

        // 3️⃣ Update database
        String updateSql = "UPDATE utilisateurs SET mot_de_passe = ? WHERE email = ?";
        PreparedStatement updateStmt = connection.prepareStatement(updateSql);
        updateStmt.setString(1, hashed);
        updateStmt.setString(2, email);

        int rows = updateStmt.executeUpdate();

        if (rows == 0) {
            throw new Exception("Password update failed.");
        }

        System.out.println("✅ Password updated successfully for " + email);
    }

    public String generateTemporaryPassword() {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    public Profile getProfileByUserId(int userId) {

        String sql = "SELECT * FROM profile WHERE id_utilisateur = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Profile profile = new Profile();

                profile.setId_profile(rs.getInt("id_profile"));
                profile.setId_utilisateur(rs.getInt("id_utilisateur"));
                profile.setPhoto_profil(rs.getString("photo_profil"));
                profile.setBio(rs.getString("bio"));

                Date dateNaiss = rs.getDate("date_naissance");
                if (dateNaiss != null)
                    profile.setDate_naissance(dateNaiss.toLocalDate());

                String genreStr = rs.getString("genre");
                if (genreStr != null)
                    profile.setGenre(Genre.valueOf(genreStr.toUpperCase()));

                profile.setPays(rs.getString("pays"));
                profile.setVille(rs.getString("ville"));
                profile.setDerniere_mise_a_jour(rs.getTimestamp("derniere_mise_a_jour"));

                return profile;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // important
    }

    // Update profile
    public boolean updateProfile(Profile profile) {
        String sql = "UPDATE profile SET photo_profil = ?, bio = ?, date_naissance = ?, genre = ?, pays = ?, ville = ?, derniere_mise_a_jour = ? WHERE id_profile = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, profile.getPhoto_profil());
            ps.setString(2, profile.getBio());
            if (profile.getDate_naissance() != null) {
                ps.setDate(3, java.sql.Date.valueOf(profile.getDate_naissance()));
            } else {
                ps.setDate(3, null);
            }
            ps.setString(4, profile.getGenre() != null ? profile.getGenre().name().toLowerCase() : null);
            ps.setString(5, profile.getPays());
            ps.setString(6, profile.getVille());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.setInt(8, profile.getId_profile());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

