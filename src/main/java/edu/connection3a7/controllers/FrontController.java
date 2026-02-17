package edu.connection3a7.controllers;

import edu.connection3a7.entities.Accompagnant;
import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Participation;
import edu.connection3a7.services.AccompagnantService;
import edu.connection3a7.services.EvenementService;
import edu.connection3a7.services.ParticipationService;
import edu.connection3a7.tools.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur principal de la vue Front (utilisateur).
 * Délègue les responsabilités à :
 *   - {@link ConstructionCartesVisiteur}           : cartes d'événements + popup détails
 *   - {@link GestionParticipationsVisiteur}   : formulaire participation, CRUD, liste
 *   - {@link AffichageTicketsEtExportPDF}          : tickets + export PDF
 *   - {@link OutilsInterfaceGraphique}                   : utilitaires UI partagés
 */
public class FrontController {

    // ===== FXML FIELDS =====
    @FXML private Button              btnAccueil;
    @FXML private Button              btnEvenement;
    @FXML private Button              btnMarketplace;
    @FXML private Button              btnForum;
    @FXML private Button              btnTechnicien;
    @FXML private Button              btnProfil;
    @FXML private Button              btnMesParticipations;
    @FXML private TextField           searchField;
    @FXML private Button              btnSearch;
    @FXML private ComboBox<String>    sortComboBox;
    @FXML private FlowPane            evenementsContainer;

    // ===== SERVICES =====
    private final EvenementService     service              = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();
    private final AccompagnantService  accompagnantService  = new AccompagnantService();

    // ===== FORMATTERS =====
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    // ===== HELPERS (délégation) =====
    private final ConstructionCartesVisiteur          cardBuilder;
    private final GestionParticipationsVisiteur  participationHelper;
    private final AffichageTicketsEtExportPDF         ticketHelper;

    // ===== STATE =====
    private List<Evenement> tousLesEvenements;
    private boolean affichageMesParticipationsActif = false;

    // Options de tri
    private static final String SORT_DATE_ASC  = "Date debut (croissant)";
    private static final String SORT_DATE_DESC = "Date debut (decroissant)";
    private static final String SORT_PLACES    = "Places disponibles";
    private static final String SORT_TITRE     = "Titre (A-Z)";
    private static final String SORT_TYPE      = "Type d'evenement";

    // ===== CONSTRUCTEUR =====
    public FrontController() {
        this.cardBuilder         = new ConstructionCartesVisiteur(this);
        this.participationHelper = new GestionParticipationsVisiteur(this);
        this.ticketHelper        = new AffichageTicketsEtExportPDF(this);
    }

    // ============================================================
    //  INIT
    // ============================================================

    @FXML
    public void initialize() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                SORT_DATE_ASC, SORT_DATE_DESC, SORT_PLACES, SORT_TITRE, SORT_TYPE));
        sortComboBox.setValue(SORT_DATE_ASC);

        // Configure le bouton des participations
        if (btnMesParticipations != null) {
            btnMesParticipations.setOnAction(ev -> participationHelper.afficherListeMesParticipations());
        }

        chargerDepuisBD();
    }

    // ============================================================
    //  CHARGEMENT BD
    // ============================================================

    private void chargerDepuisBD() {
        try {
            tousLesEvenements = service.getData();
            afficher(tousLesEvenements);
        } catch (SQLException ex) {
            evenementsContainer.getChildren().clear();
            Label err = new Label("Erreur de chargement : " + ex.getMessage());
            err.setStyle("-fx-text-fill: red;");
            evenementsContainer.getChildren().add(err);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ============================================================
    //  RECHERCHE + TRI
    // ============================================================

    @FXML
    void rechercher(ActionEvent event) {
        appliquerFiltreEtTri();
    }

    @FXML
    void trierEvenements(ActionEvent event) {
        appliquerFiltreEtTri();
    }

    private void appliquerFiltreEtTri() {
        if (tousLesEvenements == null) return;

        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Evenement> filtre = tousLesEvenements.stream()
                .filter(e -> query.isBlank()
                        || e.getTitre().toLowerCase().contains(query)
                        || (e.getOrganisateur() != null && e.getOrganisateur().toLowerCase().contains(query))
                        || (e.getLieu() != null && e.getLieu().toLowerCase().contains(query)))
                .collect(Collectors.toList());

        String tri = sortComboBox.getValue();
        if (tri != null) {
            switch (tri) {
                case SORT_DATE_ASC  -> filtre.sort(Comparator.comparing(
                        Evenement::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())));
                case SORT_DATE_DESC -> filtre.sort(Comparator.comparing(
                        Evenement::getDateDebut, Comparator.nullsLast(Comparator.reverseOrder())));
                case SORT_PLACES    -> filtre.sort(Comparator.comparingInt(
                        Evenement::getPlacesDisponibles).reversed());
                case SORT_TITRE     -> filtre.sort(Comparator.comparing(
                        e -> e.getTitre().toLowerCase()));
                case SORT_TYPE      -> filtre.sort(Comparator.comparing(
                        e -> e.getTypeEvenement() != null ? e.getTypeEvenement().name() : ""));
            }
        }

        afficher(filtre);
    }

    // ============================================================
    //  AFFICHAGE LISTE
    // ============================================================

    private void afficher(List<Evenement> liste) {
        evenementsContainer.getChildren().clear();
        if (liste == null || liste.isEmpty()) {
            Label noResult = new Label("Aucun evenement trouve.");
            noResult.setStyle("-fx-font-size: 15px; -fx-text-fill: #aaa; -fx-padding: 40;");
            evenementsContainer.getChildren().add(noResult);
            return;
        }
        for (Evenement e : liste) {
            evenementsContainer.getChildren().add(cardBuilder.creerCarteRiche(e));
        }
    }

    // ============================================================
    //  ACCESSEURS PACKAGE-PRIVATE (pour les helpers)
    // ============================================================

    DateTimeFormatter getDateFmt()                    { return dateFmt; }
    DateTimeFormatter getTimeFmt()                    { return timeFmt; }
    EvenementService getEvenementService()            { return service; }
    ParticipationService getParticipationService()    { return participationService; }
    AccompagnantService getAccompagnantService()      { return accompagnantService; }

    void rechargerListe() { chargerDepuisBD(); }

    int countParticipationsByEvent(int idEvenement) {
        try {
            return participationService.countParticipationsByEvent(idEvenement);
        } catch (SQLException ex) {
            System.err.println("Erreur comptage participations : " + ex.getMessage());
            return 0;
        }
    }

    // ── Cross-helper forwarding ──

    void showParticipationForm(Evenement e)   { participationHelper.afficherFormulaireParticipation(e); }
    void showMyParticipations(Evenement e)    { participationHelper.afficherMesParticipations(e); }
    void showTicket(Evenement e, Participation p) { ticketHelper.afficherTicket(e, p); }
    void showTicketCards(Participation p, Evenement e, String prenom, String nom, List<Accompagnant> acc) {
        ticketHelper.afficherCartesParticipation(p, e, prenom, nom, acc);
    }

    // ============================================================
    //  NAVIGATION
    // ============================================================

    @FXML void goToAccueil(ActionEvent e)     { System.out.println("Accueil"); }
    @FXML void goToEvenement(ActionEvent e)   { System.out.println("Evenement"); }
    @FXML void goToMarketplace(ActionEvent e) { System.out.println("Marketplace"); }
    @FXML void goToForum(ActionEvent e)       { System.out.println("Forum"); }
    @FXML void goToTechnicien(ActionEvent e)  { System.out.println("Technicien"); }
    @FXML void goToProfil(ActionEvent e)      { System.out.println("Profil"); }
    @FXML void deconnecter(ActionEvent e) {
        try {
            SessionManager.getInstance().setUtilisateur(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginApplication.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("FIRMA - Connexion");
            stage.show();
        } catch (Exception ex) {
            System.err.println("Erreur lors de la déconnexion : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
