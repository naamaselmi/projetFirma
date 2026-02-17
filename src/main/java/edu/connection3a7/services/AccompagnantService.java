package edu.connection3a7.services;

import edu.connection3a7.entities.Accompagnant;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des accompagnants (relation One-to-Many avec participations).
 */
public class AccompagnantService {

    /**
     * Ajoute un accompagnant lié à une participation.
     */
    public void addAccompagnant(Accompagnant accompagnant) throws SQLException {
        String requete = "INSERT INTO accompagnants (id_participation, nom, prenom) VALUES (?, ?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, accompagnant.getIdParticipation());
        pst.setString(2, accompagnant.getNom());
        pst.setString(3, accompagnant.getPrenom());
        pst.executeUpdate();
    }

    /**
     * Ajoute une liste d'accompagnants pour une participation donnée.
     */
    public void addAccompagnants(int idParticipation, List<Accompagnant> accompagnants) throws SQLException {
        String requete = "INSERT INTO accompagnants (id_participation, nom, prenom) VALUES (?, ?, ?)";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);

        for (Accompagnant a : accompagnants) {
            pst.setInt(1, idParticipation);
            pst.setString(2, a.getNom());
            pst.setString(3, a.getPrenom());
            pst.addBatch();
        }
        pst.executeBatch();
    }

    /**
     * Récupère tous les accompagnants d'une participation.
     */
    public List<Accompagnant> getByParticipation(int idParticipation) throws SQLException {
        List<Accompagnant> data = new ArrayList<>();
        String requete = "SELECT * FROM accompagnants WHERE id_participation = ? ORDER BY id_accompagnant ASC";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idParticipation);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Accompagnant a = new Accompagnant();
            a.setIdAccompagnant(rs.getInt("id_accompagnant"));
            a.setIdParticipation(rs.getInt("id_participation"));
            a.setNom(rs.getString("nom"));
            a.setPrenom(rs.getString("prenom"));
            data.add(a);
        }
        return data;
    }

    /**
     * Supprime tous les accompagnants d'une participation (pour mise à jour).
     */
    public void deleteByParticipation(int idParticipation) throws SQLException {
        String requete = "DELETE FROM accompagnants WHERE id_participation = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idParticipation);
        pst.executeUpdate();
    }

    /**
     * Met à jour les accompagnants d'une participation :
     * supprime les anciens et insère les nouveaux.
     */
    public void updateAccompagnants(int idParticipation, List<Accompagnant> accompagnants) throws SQLException {
        deleteByParticipation(idParticipation);
        if (accompagnants != null && !accompagnants.isEmpty()) {
            addAccompagnants(idParticipation, accompagnants);
        }
    }

    /**
     * Récupère tous les accompagnants pour un événement donné (admin).
     */
    public List<Accompagnant> getByEvenement(int idEvenement) throws SQLException {
        List<Accompagnant> data = new ArrayList<>();
        String requete = "SELECT a.* FROM accompagnants a " +
                "JOIN participations p ON a.id_participation = p.id_participation " +
                "WHERE p.id_evenement = ? ORDER BY a.nom, a.prenom";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idEvenement);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Accompagnant a = new Accompagnant();
            a.setIdAccompagnant(rs.getInt("id_accompagnant"));
            a.setIdParticipation(rs.getInt("id_participation"));
            a.setNom(rs.getString("nom"));
            a.setPrenom(rs.getString("prenom"));
            data.add(a);
        }
        return data;
    }
}
