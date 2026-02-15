package edu.connection3a7.services;

import edu.connection3a7.entities.Participation;
import edu.connection3a7.entities.Personne;
import edu.connection3a7.entities.Statut;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService implements IService<Participation> {
    private EvenementService evenementService = new EvenementService();
    @Override
    public void addEntity(Participation participation) throws SQLException {
        // Réserver les places d'abord
        int totalPlaces = 1 + participation.getNombreAccompagnants();
        evenementService.reserverPlaces(participation.getIdEvenement(), totalPlaces);

        String requete = "INSERT INTO participations (id_evenement, id_utilisateur, statut, " +
                "date_inscription, nombre_accompagnants, commentaire) " +
                "VALUES (?, ?, ?, NOW(), ?, ?)";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, participation.getIdEvenement());
        pst.setInt(2, participation.getIdUtilisateur());
        pst.setString(3, participation.getStatut() != null ? String.valueOf(participation.getStatut()) : "en_attente");
        pst.setInt(4, participation.getNombreAccompagnants());
        pst.setString(5, participation.getCommentaire());

        pst.executeUpdate();
        System.out.println("Participation ajoutée avec succès");
    }

    @Override
    public void deleteEntity(Participation participation) throws SQLException {
        // Libérer les places d'abord
        int totalPlaces = 1 + participation.getNombreAccompagnants();
        evenementService.libererPlaces(participation.getIdEvenement(), totalPlaces);

        String requete = "DELETE FROM participations WHERE id_participation = ?";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, participation.getIdParticipation());

        pst.executeUpdate();
        System.out.println("Participation supprimée avec succès");
    }

    @Override
    public void updateEntity(int id, Participation participation) throws SQLException {
        Participation ancienneParticipation = getById(id);

        if (ancienneParticipation != null) {
            int anciennesPlaces = 1 + ancienneParticipation.getNombreAccompagnants();
            int nouvellesPlaces = 1 + participation.getNombreAccompagnants();
            int difference = nouvellesPlaces - anciennesPlaces;

            if (difference > 0) {
                evenementService.reserverPlaces(participation.getIdEvenement(), difference);
            } else if (difference < 0) {
                evenementService.libererPlaces(participation.getIdEvenement(), Math.abs(difference));
            }
        }

        String requete = "UPDATE participations SET statut = ?, nombre_accompagnants = ?, " +
                "commentaire = ? WHERE id_participation = ?";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setString(1, String.valueOf(participation.getStatut()));
        pst.setInt(2, participation.getNombreAccompagnants());
        pst.setString(3, participation.getCommentaire());
        pst.setInt(4, id);

        pst.executeUpdate();
        System.out.println("Participation mise à jour avec succès");
    }

    @Override
    public List<Participation> getData() throws Exception {
        List<Participation> data = new ArrayList<>();
        String requete = "SELECT * FROM participations ORDER BY date_inscription DESC";

        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(requete);

        while (rs.next()) {
            Participation p = mapParticipation(rs);
            data.add(p);
        }

        return data;
    }


    public Participation getById(int id) throws SQLException {
        String requete = "SELECT * FROM participations WHERE id_participation = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, id);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapParticipation(rs);
        }

        return null;
    }

    private Participation mapParticipation(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setIdParticipation(rs.getInt("id_participation"));
        p.setIdEvenement(rs.getInt("id_evenement"));
        p.setIdUtilisateur(rs.getInt("id_utilisateur"));
        p.setStatut(Statut.valueOf(rs.getString("statut")));

        Timestamp dateInscription = rs.getTimestamp("date_inscription");
        if (dateInscription != null) {
            p.setDateInscription(dateInscription.toLocalDateTime());
        }

        Timestamp dateAnnulation = rs.getTimestamp("date_annulation");
        if (dateAnnulation != null) {
            p.setDateAnnulation(dateAnnulation.toLocalDateTime());
        }

        p.setNombreAccompagnants(rs.getInt("nombre_accompagnants"));
        p.setCommentaire(rs.getString("commentaire"));

        return p;
}}
