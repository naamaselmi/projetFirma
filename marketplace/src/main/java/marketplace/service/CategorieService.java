package marketplace.service;

import marketplace.entities.Categorie;
import marketplace.entities.ProductType;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Categorie entity
 */
public class CategorieService implements IService<Categorie> {

    @Override
    public void addEntity(Categorie categorie) throws SQLException {
        String requete = "INSERT INTO categories (nom, type_produit, description) VALUES (?, ?, ?)";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, categorie.getNom());
        pst.setString(2, categorie.getTypeProduit().getValue());
        pst.setString(3, categorie.getDescription());

        pst.executeUpdate();
        System.out.println("Categorie added: " + categorie.getNom());
    }

    @Override
    public void deleteEntity(Categorie categorie) throws SQLException {
        String requete = "DELETE FROM categories WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, categorie.getId());

        pst.executeUpdate();
        System.out.println("Categorie deleted: " + categorie.getNom());
    }

    @Override
    public void updateEntity(Categorie categorie) throws SQLException {
        String requete = "UPDATE categories SET nom = ?, type_produit = ?, description = ? WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, categorie.getNom());
        pst.setString(2, categorie.getTypeProduit().getValue());
        pst.setString(3, categorie.getDescription());
        pst.setInt(4, categorie.getId());

        pst.executeUpdate();
        System.out.println("Categorie updated: " + categorie.getNom());
    }

    @Override
    public List<Categorie> getEntities() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String requete = "SELECT * FROM categories ORDER BY type_produit, nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Categorie categorie = new Categorie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    ProductType.fromString(rs.getString("type_produit")),
                    rs.getString("description"));
            categories.add(categorie);
        }

        return categories;
    }

    /**
     * Get categories by product type
     */
    public List<Categorie> getCategoriesByType(ProductType type) throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String requete = "SELECT * FROM categories WHERE type_produit = ? ORDER BY nom";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, type.getValue());
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Categorie categorie = new Categorie(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    ProductType.fromString(rs.getString("type_produit")),
                    rs.getString("description"));
            categories.add(categorie);
        }

        return categories;
    }
}
