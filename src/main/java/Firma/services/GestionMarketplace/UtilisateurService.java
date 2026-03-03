package Firma.services.GestionMarketplace;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Firma.entities.GestionMarketplace.Utilisateur;
import Firma.interfaces.GestionMarketplace.IService;
import Firma.tools.GestionMarketplace.DB_connection;

/**
 * Service for Utilisateur entity in Marketplace
 */
public class UtilisateurService implements IService<Utilisateur> {

    @Override
    public void addEntity(Utilisateur utilisateur) throws SQLException {
        String requete = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, type_user) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, utilisateur.getNom());
        pst.setString(2, utilisateur.getPrenom());
        pst.setString(3, utilisateur.getEmail());
        pst.setString(4, utilisateur.getMotDePasse());
        pst.setString(5, utilisateur.getTypeUser());
        pst.executeUpdate();
        System.out.println("Utilisateur added: " + utilisateur.getNom());
    }

    @Override
    public void deleteEntity(Utilisateur utilisateur) throws SQLException {
        String requete = "DELETE FROM utilisateurs WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, utilisateur.getId());
        pst.executeUpdate();
        System.out.println("Utilisateur deleted: " + utilisateur.getNom());
    }

    @Override
    public void updateEntity(Utilisateur utilisateur) throws SQLException {
        String requete = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, type_user = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, utilisateur.getNom());
        pst.setString(2, utilisateur.getPrenom());
        pst.setString(3, utilisateur.getEmail());
        pst.setString(4, utilisateur.getMotDePasse());
        pst.setString(5, utilisateur.getTypeUser());
        pst.setInt(6, utilisateur.getId());
        pst.executeUpdate();
        System.out.println("Utilisateur updated: " + utilisateur.getNom());
    }

    @Override
    public List<Utilisateur> getEntities() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String requete = "SELECT * FROM utilisateurs";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            utilisateurs.add(new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("type_user")));
        }

        return utilisateurs;
    }

    /**
     * Get utilisateur by ID
     */
    public Utilisateur getById(int id) throws SQLException {
        String requete = "SELECT * FROM utilisateurs WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("type_user"));
        }

        return null;
    }

    /**
     * Get utilisateur by email
     */
    public Utilisateur getByEmail(String email) throws SQLException {
        String requete = "SELECT * FROM utilisateurs WHERE email = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("type_user"));
        }

        return null;
    }
}
