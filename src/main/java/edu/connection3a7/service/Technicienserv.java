package edu.connection3a7.service;

import edu.connection3a7.entities.Technicien;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Technicienserv implements IService<Technicien> {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void addentitiy(Technicien technicien) throws SQLException {
        String sql = "INSERT INTO technicien " +
                "(id_utilisateur, nom, prenom, email, specialite, telephone, disponibilite, localisation, image, age, date_naissance, cin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setObject(1, technicien.getId_utilisateur());
            pst.setString(2, technicien.getNom());
            pst.setString(3, technicien.getPrenom());
            pst.setString(4, technicien.getEmail());
            pst.setString(5, technicien.getSpecialite());
            pst.setString(6, technicien.getTelephone());
            pst.setBoolean(7, technicien.isDisponibilite());
            pst.setString(8, technicien.getLocalisation());
            pst.setString(9, technicien.getImage());
            pst.setInt(10, technicien.getAge());
            pst.setDate(11, Date.valueOf(technicien.getDateNaissance()));
            pst.setString(12, technicien.getCin());

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    technicien.setId_tech(rs.getInt(1));
                }
            }

            System.out.println("✅ Technicien ajouté → ID: " + technicien.getId_tech());
        }
    }
    public Technicien getTechnicienByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM technicien WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Technicien t = new Technicien();
                t.setId_tech(rs.getInt("id_tech"));
                t.setNom(rs.getString("nom"));
                t.setPrenom(rs.getString("prenom"));
                t.setEmail(rs.getString("email"));
                t.setPassword(rs.getString("password"));  // ✅ Maintenant ça marche !
                t.setSpecialite(rs.getString("specialite"));
                t.setTelephone(rs.getString("telephone"));
                t.setDisponibilite(rs.getBoolean("disponibilite"));
                return t;
            }
        }
        return null;
    }
    /**
     * Récupère un technicien par son ID
     * @param id L'ID du technicien
     * @return Le technicien trouvé ou null
     */
    public Technicien getTechnicienById(int id) throws SQLException {
        String sql = "SELECT * FROM technicien WHERE id_tech = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Technicien t = new Technicien();
                t.setId_tech(rs.getInt("id_tech"));
                t.setNom(rs.getString("nom"));
                t.setPrenom(rs.getString("prenom"));
                t.setEmail(rs.getString("email"));
                t.setSpecialite(rs.getString("specialite"));
                t.setTelephone(rs.getString("telephone"));
                t.setDisponibilite(rs.getBoolean("disponibilite"));
                t.setLocalisation(rs.getString("localisation"));
                t.setImage(rs.getString("image"));
                t.setCin(rs.getString("cin"));
                t.setAge(rs.getInt("age"));
                t.setDateNaissance(rs.getDate("date_naissance") != null ?
                        rs.getDate("date_naissance").toLocalDate() : null);

                // 🔥 Si tu as ajouté le champ password
                try {
                    t.setPassword(rs.getString("password"));
                } catch (SQLException e) {
                    // La colonne password n'existe pas encore
                }

                return t;
            }
        }
        return null;
    }
    @Override
    public void update(Technicien technicien) throws SQLException {
        String sql = "UPDATE technicien SET " +
                "id_utilisateur = ?, nom = ?, prenom = ?, email = ?, specialite = ?, " +
                "telephone = ?, disponibilite = ?, localisation = ?, image = ?, " +
                "age = ?, date_naissance = ?, cin = ? " +
                "WHERE id_tech = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setObject(1, technicien.getId_utilisateur());
            pst.setString(2, technicien.getNom());
            pst.setString(3, technicien.getPrenom());
            pst.setString(4, technicien.getEmail());
            pst.setString(5, technicien.getSpecialite());
            pst.setString(6, technicien.getTelephone());
            pst.setBoolean(7, technicien.isDisponibilite());
            pst.setString(8, technicien.getLocalisation());
            pst.setString(9, technicien.getImage());
            pst.setInt(10, technicien.getAge());
            pst.setDate(11, Date.valueOf(technicien.getDateNaissance()));
            pst.setString(12, technicien.getCin());
            pst.setInt(13, technicien.getId_tech());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Technicien mis à jour (ID: " + technicien.getId_tech() + ")");
            } else {
                System.out.println("Aucun technicien trouvé avec ID " + technicien.getId_tech());
            }
        }
    }

    @Override
    public void delet(Technicien technicien) throws SQLException {
        if (technicien == null || technicien.getId_tech() <= 0) {
            throw new IllegalArgumentException("Technicien invalide ou ID manquant");
        }

        String sql = "DELETE FROM technicien WHERE id_tech = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, technicien.getId_tech());
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Technicien supprimé (ID: " + technicien.getId_tech() + ")");
            } else {
                System.out.println("Aucun technicien supprimé pour ID " + technicien.getId_tech());
            }
        }
    }

    @Override
    public List<Technicien> getdata() throws SQLException {
        List<Technicien> liste = new ArrayList<>();
        String sql = "SELECT * FROM technicien ORDER BY nom, prenom";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Technicien t = new Technicien();
                t.setId_tech(rs.getInt("id_tech"));
                t.setId_utilisateur(rs.getObject("id_utilisateur", Integer.class));
                t.setNom(rs.getString("nom"));
                t.setPrenom(rs.getString("prenom"));
                t.setEmail(rs.getString("email"));
                t.setSpecialite(rs.getString("specialite"));
                t.setTelephone(rs.getString("telephone"));
                t.setDisponibilite(rs.getBoolean("disponibilite"));
                t.setLocalisation(rs.getString("localisation"));
                t.setImage(rs.getString("image"));
                t.setAge(rs.getInt("age"));
                Date date = rs.getDate("date_naissance");
                if (date != null) {
                    t.setDateNaissance(date.toLocalDate());
                }
                t.setCin(rs.getString("cin"));
                liste.add(t);
            }
        }
        return liste;
    }

    @Override
    public List<Technicien> getData() throws SQLException {
        return List.of();
    }

    // 🔹 Méthode supplémentaire pour rechercher par CIN
    public Technicien chercherParCin(String cin) throws SQLException {
        Technicien t = null;
        String sql = "SELECT * FROM technicien WHERE cin = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, cin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    t = new Technicien();
                    t.setId_tech(rs.getInt("id_tech"));
                    t.setId_utilisateur(rs.getObject("id_utilisateur", Integer.class));
                    t.setNom(rs.getString("nom"));
                    t.setPrenom(rs.getString("prenom"));
                    t.setEmail(rs.getString("email"));
                    t.setSpecialite(rs.getString("specialite"));
                    t.setTelephone(rs.getString("telephone"));
                    t.setDisponibilite(rs.getBoolean("disponibilite"));
                    t.setLocalisation(rs.getString("localisation"));
                    t.setImage(rs.getString("image"));
                    t.setAge(rs.getInt("age"));
                    Date date = rs.getDate("date_naissance");
                    if (date != null) t.setDateNaissance(date.toLocalDate());
                    t.setCin(rs.getString("cin"));
                }
            }
        }
        return t;
    }
}
