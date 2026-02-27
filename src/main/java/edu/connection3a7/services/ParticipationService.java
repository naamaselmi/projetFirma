package edu.connection3a7.services;

import edu.connection3a7.entities.Accompagnant;
import edu.connection3a7.entities.Participation;
import edu.connection3a7.entities.Personne;
import edu.connection3a7.entities.Statut;
import edu.connection3a7.interfaces.IService;
import edu.connection3a7.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ParticipationService implements IService<Participation> {
    private EvenementService evenementService = new EvenementService();
    private AccompagnantService accompagnantService = new AccompagnantService();

    /**
     * Génère un code de participation unique : PART-XXXXX (5 caractères alphanumériques)
     */
    private String genererCodeParticipation() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder("PART-");
        for (int i = 0; i < 5; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    @Override
    public void addEntity(Participation participation) throws SQLException {
        // Réserver les places d'abord
        int totalPlaces = 1 + participation.getNombreAccompagnants();
        evenementService.reserverPlaces(participation.getIdEvenement(), totalPlaces);

        // Générer un code de participation unique
        String code = genererCodeParticipation();
        participation.setCodeParticipation(code);

        String requete = "INSERT INTO participations (id_evenement, id_utilisateur, statut, " +
                "date_inscription, nombre_accompagnants, commentaire, code_participation) " +
                "VALUES (?, ?, ?, NOW(), ?, ?, ?)";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, participation.getIdEvenement());
        pst.setInt(2, participation.getIdUtilisateur());
        pst.setString(3, participation.getStatut() != null ? String.valueOf(participation.getStatut()) : "en_attente");
        pst.setInt(4, participation.getNombreAccompagnants());
        pst.setString(5, participation.getCommentaire());
        pst.setString(6, code);

        pst.executeUpdate();

        // Récupérer l'id généré
        ResultSet generatedKeys = pst.getGeneratedKeys();
        if (generatedKeys.next()) {
            participation.setIdParticipation(generatedKeys.getInt(1));
        }

        System.out.println("Participation ajoutée avec succès (id=" + participation.getIdParticipation() + ", code=" + code + ")");
    }

    /**
     * Ajoute une participation avec la liste des accompagnants (noms/prénoms).
     */
    public void addEntityWithAccompagnants(Participation participation, List<Accompagnant> accompagnants) throws SQLException {
        addEntity(participation);
        if (accompagnants != null && !accompagnants.isEmpty()) {
            accompagnantService.addAccompagnants(participation.getIdParticipation(), accompagnants);
        }
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

    /**
     * Récupère une participation par son code unique.
     */
    public Participation getByCode(String codeParticipation) throws SQLException {
        String requete = "SELECT * FROM participations WHERE code_participation = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setString(1, codeParticipation);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapParticipation(rs);
        }

        return null;
    }

    /**
     * Met à jour uniquement le statut d'une participation.
     */
    public void updateStatut(int idParticipation, Statut nouveauStatut) throws SQLException {
        String requete = "UPDATE participations SET statut = ? WHERE id_participation = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setString(1, String.valueOf(nouveauStatut));
        pst.setInt(2, idParticipation);
        pst.executeUpdate();
        System.out.println("Statut participation " + idParticipation + " mis à jour → " + nouveauStatut);
    }

    /**
     * Compte le nombre de participations confirmées pour un événement
     */
    public int countParticipationsByEvent(int idEvenement) throws SQLException {
        String requete = "SELECT COUNT(*) as total FROM participations WHERE id_evenement = ? AND statut = 'CONFIRME'";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idEvenement);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    /**
     * Récupère toutes les participations pour un événement
     */
    public List<Participation> getParticipationsByEvent(int idEvenement) throws SQLException {
        List<Participation> data = new ArrayList<>();
        String requete = "SELECT * FROM participations WHERE id_evenement = ? ORDER BY date_inscription DESC";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idEvenement);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Participation p = mapParticipation(rs);
            data.add(p);
        }

        return data;
    }

    /**
     * Vérifie si un utilisateur est déjà inscrit à un événement
     */
    public boolean isUserAlreadyParticipating(int idUtilisateur, int idEvenement) throws SQLException {
        String requete = "SELECT COUNT(*) as total FROM participations WHERE id_utilisateur = ? AND id_evenement = ? AND statut IN ('CONFIRME', 'EN_ATTENTE')";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idUtilisateur);
        pst.setInt(2, idEvenement);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getInt("total") > 0;
        }

        return false;
    }

    /**
     * Récupère la participation d'un utilisateur pour un événement
     */
    public Participation getParticipationByUserAndEvent(int idUtilisateur, int idEvenement) throws SQLException {
        String requete = "SELECT * FROM participations WHERE id_utilisateur = ? AND id_evenement = ? AND statut IN ('CONFIRME', 'EN_ATTENTE')";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idUtilisateur);
        pst.setInt(2, idEvenement);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return mapParticipation(rs);
        }

        return null;
    }

    /**
     * Récupère toutes les participations d'un utilisateur
     */
    public List<Participation> getParticipationsByUser(int idUtilisateur) throws SQLException {
        List<Participation> data = new ArrayList<>();
        String requete = "SELECT * FROM participations WHERE id_utilisateur = ? AND statut IN ('CONFIRME', 'EN_ATTENTE') ORDER BY date_inscription DESC";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idUtilisateur);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Participation p = mapParticipation(rs);
            data.add(p);
        }

        return data;
    }

    /**
     * Compte le nombre total de participants (incluant les accompagnants) pour un événement
     */
    public int countTotalParticipantsByEvent(int idEvenement) throws SQLException {
        String requete = "SELECT COALESCE(SUM(nombre_accompagnants), 0) + COUNT(*) as total FROM participations WHERE id_evenement = ? AND statut = 'CONFIRME'";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idEvenement);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    /**
     * Récupère la liste complète des participants pour un événement (Vue Admin).
     * Retourne une map : Participation → (nom_utilisateur, prenom_utilisateur, liste_accompagnants).
     */
    public List<Map<String, Object>> getParticipantsDetailsByEvent(int idEvenement) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String requete = "SELECT p.*, u.nom AS user_nom, u.prenom AS user_prenom, u.email AS user_email " +
                "FROM participations p " +
                "JOIN utilisateurs u ON p.id_utilisateur = u.id_utilisateur " +
                "WHERE p.id_evenement = ? ORDER BY p.date_inscription DESC";

        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
        pst.setInt(1, idEvenement);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            Participation p = mapParticipation(rs);
            entry.put("participation", p);
            entry.put("nom", rs.getString("user_nom"));
            entry.put("prenom", rs.getString("user_prenom"));
            entry.put("email", rs.getString("user_email"));
            // Charger les accompagnants
            List<Accompagnant> accompagnants = accompagnantService.getByParticipation(p.getIdParticipation());
            entry.put("accompagnants", accompagnants);
            result.add(entry);
        }
        return result;
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

        try {
            p.setCodeParticipation(rs.getString("code_participation"));
        } catch (SQLException ignored) {
            // Colonne peut ne pas exister dans les anciennes bases
        }

        return p;
    }
}
