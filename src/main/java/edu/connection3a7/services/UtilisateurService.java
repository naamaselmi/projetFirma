package edu.connection3a7.services;

import edu.connection3a7.entities.Personne;
import edu.connection3a7.entities.Role;
import edu.connection3a7.entities.Utilisateur;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

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

    public Utilisateur login(String email, String password) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
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
                            Role.valueOf(rs.getString("role")),
                            rs.getString("mot_de_passe"),
                            rs.getTimestamp("date_inscription").toLocalDateTime()
                    );
                }
            }
        }
        return null; // email/mot de passe incorrect
    }

}


