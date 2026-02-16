package marketplace.service;

import marketplace.entities.Utilisateur;
import marketplace.tools.DB_connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilisateurService {

    public Utilisateur login(String email, String password) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";

        try (Connection con = DB_connection.getInstance().getConnection();
                PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("type_user"));
                }
            }
        }
        return null; // Login failed
    }
    
    public Utilisateur getById(int id) throws SQLException {
        String query = "SELECT * FROM utilisateurs WHERE id = ?";
        
        try (Connection con = DB_connection.getInstance().getConnection();
                PreparedStatement stmt = con.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("type_user"));
                }
            }
        }
        return null;
    }
}
