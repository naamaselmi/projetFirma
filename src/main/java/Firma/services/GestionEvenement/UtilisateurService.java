package Firma.services.GestionEvenement;

import Firma.entities.GestionEvenement.Role;
import Firma.entities.GestionEvenement.Utilisateur;
import Firma.interfaces.GestionEvenement.IService;
import Firma.tools.GestionEvenement.MyConnection;

import java.sql.*;
import java.util.List;

public class UtilisateurService implements IService<Utilisateur> {
    @Override
    public void addEntity(Utilisateur utilisateur) throws SQLException {

    }

    @Override
    public void deleteEntity(Utilisateur utilisateur) throws SQLException {

    }

    @Override
    public void updateEntity(int id, Utilisateur utilisateur) throws SQLException {

    }

    @Override
    public List<Utilisateur> getData() throws Exception {
        return List.of();
    }

    private final Connection cnx = MyConnection.getInstance().getCnx();

    /**
     * Récupère un utilisateur par son ID.
     */
    public Utilisateur getById(int id) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id"),
                            Role.valueOf(rs.getString("type_user")),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("telephone"),
                            rs.getString("adresse"),
                            rs.getString("ville"),
                            rs.getTimestamp("date_creation").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }

    public Utilisateur login(String email, String password) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id"),
                            Role.valueOf(rs.getString("type_user")),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("telephone"),
                            rs.getString("adresse"),
                            rs.getString("ville"),
                            rs.getTimestamp("date_creation").toLocalDateTime()
                    );
                }
            }
        }
        return null; // email/mot de passe incorrect
    }

}


