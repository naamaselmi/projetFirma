package edu.connection3a7.services;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Statutevent;
import edu.connection3a7.entities.Type;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;

public class EvenementService implements IService<Evenement> {

    public static void reserverPlaces(int idEvenement, int nombrePlaces) throws SQLException {
        String requete = "UPDATE evenements SET places_disponibles = places_disponibles - ? " +
                "WHERE id_evenement = ? AND places_disponibles >= ?";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, nombrePlaces);
        pst.setInt(2, idEvenement);
        pst.setInt(3, nombrePlaces);

        int rowsAffected = pst.executeUpdate();

        if (rowsAffected == 0) {
            throw new SQLException("Pas assez de places disponibles");
        }

        System.out.println(nombrePlaces + " place(s) réservée(s)");
    }

    public static void libererPlaces(int idEvenement, int nombrePlaces) throws SQLException {
        String requete = "UPDATE evenements SET places_disponibles = places_disponibles + ? WHERE id_evenement = ?";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, nombrePlaces);
        pst.setInt(2, idEvenement);

        pst.executeUpdate();
        System.out.println(nombrePlaces + " place(s) libérée(s)");
    }

    @Override
    public void addEntity(Evenement evenement) throws SQLException {
        String requete = "INSERT INTO evenements (titre, description, image_url, type_evenement, " +
                "date_debut, date_fin, horaire_debut, horaire_fin ,lieu, adresse,capacite_max, " +
                "places_disponibles, organisateur, contact_email, contact_tel, statut, " +
                "date_creation, date_modification) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setString(1, evenement.getTitre());
        pst.setString(2, evenement.getDescription());
        pst.setString(3, evenement.getImageUrl());
        pst.setString(4, String.valueOf(evenement.getTypeEvenement()));
        pst.setDate(5,    Date.valueOf(evenement.getDateDebut()));
        pst.setDate(6,    Date.valueOf(evenement.getDateFin()));
        pst.setTime(7,    Time.valueOf(evenement.getHoraireDebut()));
        pst.setTime(8,    Time.valueOf(evenement.getHoraireFin()));
        pst.setString(9, evenement.getLieu());
        pst.setString(10, evenement.getAdresse());
        //pst.setBigDecimal(9, evenement.getLatitude());
        //pst.setBigDecimal(10, evenement.getLongitude());
        pst.setInt(11, evenement.getCapaciteMax());
        pst.setInt(12, evenement.getPlacesDisponibles());
        pst.setString(13, evenement.getOrganisateur());
        pst.setString(14, evenement.getContactEmail());
        pst.setString(15, evenement.getContactTel());
        pst.setString(16, evenement.getStatut() != null ? String.valueOf(evenement.getStatut()) : "actif");


        pst.executeUpdate();
        System.out.println("Événement ajouté avec succès");
    }

    @Override
    public void deleteEntity(Evenement evenement) throws SQLException {
        String requete = "DELETE FROM evenements WHERE id_evenement = ?";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, evenement.getIdEvenement());

        pst.executeUpdate();
        System.out.println("Événement supprimé avec succès");
    }

    @Override
    public void updateEntity(int id, Evenement evenement) throws SQLException {
        String requete = "UPDATE evenements SET titre = ?, description = ?, image_url = ?, " +
                "type_evenement = ?, date_debut = ?, date_fin = ?, horaire_debut = ?, horaire_fin = ?,lieu = ?, adresse = ?, " +
                "capacite_max = ?, places_disponibles = ?, " +
                "organisateur = ?, contact_email = ?, contact_tel = ?, statut = ?, " +
                "date_modification = NOW() WHERE id_evenement = ?";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);

        pst.setString(1, evenement.getTitre());
        pst.setString(2, evenement.getDescription());
        pst.setString(3, evenement.getImageUrl());
        pst.setString(4, String.valueOf(evenement.getTypeEvenement()));
        pst.setDate(5,    Date.valueOf(evenement.getDateDebut()));
        pst.setDate(6,    Date.valueOf(evenement.getDateFin()));
        pst.setTime(7,    Time.valueOf(evenement.getHoraireDebut()));
        pst.setTime(8,    Time.valueOf(evenement.getHoraireFin()));
        pst.setString(9, evenement.getLieu());
        pst.setString(10, evenement.getAdresse());
        //pst.setBigDecimal(9, evenement.getLatitude());
        //pst.setBigDecimal(10, evenement.getLongitude());
        pst.setInt(11, evenement.getCapaciteMax());
        pst.setInt(12, evenement.getPlacesDisponibles());
        pst.setString(13, evenement.getOrganisateur());
        pst.setString(14, evenement.getContactEmail());
        pst.setString(15, evenement.getContactTel());
        pst.setString(16, String.valueOf(evenement.getStatut()));
        pst.setInt(17, id);

        pst.executeUpdate();
        System.out.println("Événement mis à jour avec succès");
    }

    @Override
    public List<Evenement> getData() throws Exception {
        List<Evenement> data = new ArrayList<>();
        String requete = "SELECT * FROM evenements ORDER BY date_debut ASC";

        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(requete);

        while (rs.next()) {
            Evenement e = new Evenement();
            e.setIdEvenement(rs.getInt("id_evenement"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setImageUrl(rs.getString("image_url"));
            e.setTypeEvenement(Type.valueOf(rs.getString("type_evenement")));

            Date dateDebut = rs.getDate("date_debut");
            if (dateDebut != null) e.setDateDebut(dateDebut.toLocalDate());

            Date dateFin = rs.getDate("date_fin");
            if (dateFin != null) e.setDateFin(dateFin.toLocalDate());

            Time horaireDebut = rs.getTime("horaire_debut");
            if (horaireDebut != null) e.setHoraireDebut(horaireDebut.toLocalTime());

            Time horaireFin = rs.getTime("horaire_fin");
            if (horaireFin != null) e.setHoraireFin(horaireFin.toLocalTime());

            e.setLieu(rs.getString("lieu"));
            e.setAdresse(rs.getString("adresse"));
            e.setCapaciteMax(rs.getInt("capacite_max"));
            e.setPlacesDisponibles(rs.getInt("places_disponibles"));
            e.setOrganisateur(rs.getString("organisateur"));
            e.setContactEmail(rs.getString("contact_email"));
            e.setContactTel(rs.getString("contact_tel"));
            e.setStatut(Statutevent.valueOf(rs.getString("statut")));

            Timestamp dateCreation = rs.getTimestamp("date_creation");
            if (dateCreation != null) {
                e.setDateCreation(dateCreation.toLocalDateTime());
            }

            Timestamp dateModification = rs.getTimestamp("date_modification");
            if (dateModification != null) {
                e.setDateModification(dateModification.toLocalDateTime());
            }

            data.add(e);
        }

        return data;
    }
}
