package marketplace.service;

import marketplace.entities.Fournisseur;
import marketplace.interfaces.IService;
import marketplace.tools.DB_connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Fournisseur entity
 */
public class FournisseurService implements IService<Fournisseur> {

    @Override
    public void addEntity(Fournisseur fournisseur) throws SQLException {
        String requete = "INSERT INTO fournisseurs (nom_entreprise, contact_nom, email, telephone, " +
                "adresse, ville, actif) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, fournisseur.getNomEntreprise());
        pst.setString(2, fournisseur.getContactNom());
        pst.setString(3, fournisseur.getEmail());
        pst.setString(4, fournisseur.getTelephone());
        pst.setString(5, fournisseur.getAdresse());
        pst.setString(6, fournisseur.getVille());
        pst.setBoolean(7, fournisseur.isActif());

        pst.executeUpdate();
        System.out.println("Fournisseur added: " + fournisseur.getNomEntreprise());
    }

    @Override
    public void deleteEntity(Fournisseur fournisseur) throws SQLException {
        String requete = "DELETE FROM fournisseurs WHERE id = ?";
        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setInt(1, fournisseur.getId());

        pst.executeUpdate();
        System.out.println("Fournisseur deleted: " + fournisseur.getNomEntreprise());
    }

    @Override
    public void updateEntity(Fournisseur fournisseur) throws SQLException {
        String requete = "UPDATE fournisseurs SET nom_entreprise = ?, contact_nom = ?, email = ?, " +
                "telephone = ?, adresse = ?, ville = ?, actif = ? WHERE id = ?";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        pst.setString(1, fournisseur.getNomEntreprise());
        pst.setString(2, fournisseur.getContactNom());
        pst.setString(3, fournisseur.getEmail());
        pst.setString(4, fournisseur.getTelephone());
        pst.setString(5, fournisseur.getAdresse());
        pst.setString(6, fournisseur.getVille());
        pst.setBoolean(7, fournisseur.isActif());
        pst.setInt(8, fournisseur.getId());

        pst.executeUpdate();
        System.out.println("Fournisseur updated: " + fournisseur.getNomEntreprise());
    }

    @Override
    public List<Fournisseur> getEntities() throws SQLException {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String requete = "SELECT * FROM fournisseurs ORDER BY nom_entreprise";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Fournisseur fournisseur = new Fournisseur(
                    rs.getInt("id"),
                    rs.getString("nom_entreprise"),
                    rs.getString("contact_nom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getBoolean("actif"),
                    dateCreation);
            fournisseurs.add(fournisseur);
        }

        return fournisseurs;
    }

    /**
     * Get only active suppliers
     */
    public List<Fournisseur> getActiveFournisseurs() throws SQLException {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String requete = "SELECT * FROM fournisseurs WHERE actif = true ORDER BY nom_entreprise";

        PreparedStatement pst = DB_connection.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;

            Fournisseur fournisseur = new Fournisseur(
                    rs.getInt("id"),
                    rs.getString("nom_entreprise"),
                    rs.getString("contact_nom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("ville"),
                    rs.getBoolean("actif"),
                    dateCreation);
            fournisseurs.add(fournisseur);
        }

        return fournisseurs;
    }
}
