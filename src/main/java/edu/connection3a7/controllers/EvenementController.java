package edu.connection3a7.controllers;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Type;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur principal du Dashboard admin (Dashboard.fxml).
 * Délègue la logique métier à :
 *   - {@link ConstructionCartesEvenement}       — cartes et popup détails
 *   - {@link FormulaireCreationModificationEvenement}        — CRUD formulaires
 *   - {@link AffichageListeParticipants} — grille participants
 */
public class EvenementController {

    // ===== NAVIGATION =====
    @FXML private Button btnAccueil;
    @FXML private Button btnEvenement;
    @FXML private Button btnMarketplace;
    @FXML private Button btnForum;
    @FXML private Button btnTechnicien;
    @FXML private Button btnUtilisateur;

    // ===== TABPANE =====
    @FXML private TabPane mainTabPane;

    // ===== LISTE =====
    @FXML private TextField searchField;
    @FXML private Button    btnSearch;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private VBox      evenementsContainer;

    // ===== CRÉER =====
    @FXML private TextField      createTitre;
    @FXML private TextArea       createDescription;
    @FXML private TextField      createOrganisateur;
    @FXML private ComboBox<Type> createType;
    @FXML private DatePicker     createDateDebut;
    @FXML private DatePicker     createDateFin;
    @FXML private TextField      createTempsDebut;
    @FXML private TextField      createTempsFin;
    @FXML private TextField      createNombrePlaces;
    @FXML private TextField      createLieu;
    @FXML private TextField      createAdresse;
    @FXML private Label          createImageLabel;
    @FXML private Button         btnGenererIA;

    // ===== MODIFIER =====
    @FXML private TextField      modifySearchField;
    @FXML private TextField      modifyTitre;
    @FXML private TextArea       modifyDescription;
    @FXML private TextField      modifyOrganisateur;
    @FXML private ComboBox<Type> modifyType;
    @FXML private DatePicker     modifyDateDebut;
    @FXML private DatePicker     modifyDateFin;
    @FXML private TextField      modifyTempsDebut;
    @FXML private TextField      modifyTempsFin;
    @FXML private TextField      modifyNombrePlaces;
    @FXML private TextField      modifyLieu;
    @FXML private TextField      modifyAdresse;
    @FXML private Label          modifyImageLabel;
    @FXML private Button         btnGenererIAModif;

    // ===== SERVICES & STATE =====
    private final EvenementService     service              = new EvenementService();
    private final ParticipationService participationService = new ParticipationService();
    private final AccompagnantService  accompagnantService  = new AccompagnantService();
    private final DateTimeFormatter    timeFmt              = DateTimeFormatter.ofPattern("HH:mm");

    private String createImagePath   = null;
    private String modifyImagePath   = null;
    private int    selectedEvenementId = -1;

    // ===== HELPERS =====
    private ConstructionCartesEvenement       cardBuilder;
    private FormulaireCreationModificationEvenement        formHelper;
    private AffichageListeParticipants participantsHelper;
    private DashboardAnalytique        dashboardHelper;

    // ===== DASHBOARD =====
    @FXML private VBox dashboardContainer;

    // ============================================================
    //  INITIALISATION
    // ============================================================

    @FXML
    public void initialize() {
        cardBuilder       = new ConstructionCartesEvenement(this);
        formHelper        = new FormulaireCreationModificationEvenement(this);
        participantsHelper = new AffichageListeParticipants(this);
        dashboardHelper    = new DashboardAnalytique(this);

        // Dashboard analytique
        chargerDashboard();

        createType.setItems(FXCollections.observableArrayList(Type.values()));
        modifyType.setItems(FXCollections.observableArrayList(Type.values()));

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Date (plus récent)", "Date (plus ancien)",
                "Titre (A-Z)", "Titre (Z-A)",
                "Places disponibles", "Statut"
        ));
        sortComboBox.setValue("Date (plus récent)");
        sortComboBox.setOnAction(ev -> chargerListe(searchField.getText()));

        chargerListe(null);
    }

    // ============================================================
    //  LISTE
    // ============================================================

    void chargerListe(String query) {
        evenementsContainer.getChildren().clear();
        try {
            List<Evenement> liste = new ArrayList<>(service.getData());

            // Filtrer
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase();
                liste.removeIf(e -> !e.getTitre().toLowerCase().contains(q));
            }

            // Trier
            String tri = sortComboBox.getValue();
            if (tri != null) {
                switch (tri) {
                    case "Date (plus récent)":
                        liste.sort((a, b) -> {
                            if (a.getDateDebut() == null) return 1;
                            if (b.getDateDebut() == null) return -1;
                            return b.getDateDebut().compareTo(a.getDateDebut());
                        });
                        break;
                    case "Date (plus ancien)":
                        liste.sort((a, b) -> {
                            if (a.getDateDebut() == null) return 1;
                            if (b.getDateDebut() == null) return -1;
                            return a.getDateDebut().compareTo(b.getDateDebut());
                        });
                        break;
                    case "Titre (A-Z)":
                        liste.sort((a, b) -> OutilsInterfaceGraphique.nullSafe(a.getTitre())
                                .compareToIgnoreCase(OutilsInterfaceGraphique.nullSafe(b.getTitre())));
                        break;
                    case "Titre (Z-A)":
                        liste.sort((a, b) -> OutilsInterfaceGraphique.nullSafe(b.getTitre())
                                .compareToIgnoreCase(OutilsInterfaceGraphique.nullSafe(a.getTitre())));
                        break;
                    case "Places disponibles":
                        liste.sort((a, b) -> Integer.compare(b.getPlacesDisponibles(), a.getPlacesDisponibles()));
                        break;
                    case "Statut":
                        liste.sort((a, b) -> {
                            String sa = a.getStatut() != null ? a.getStatut().name() : "";
                            String sb = b.getStatut() != null ? b.getStatut().name() : "";
                            return sa.compareToIgnoreCase(sb);
                        });
                        break;
                }
            }

            for (Evenement e : liste) {
                evenementsContainer.getChildren().add(cardBuilder.creerCarteEvenement(e));
            }
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les événements : " + ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ============================================================
    //  DÉLÉGATION @FXML → HELPERS
    // ============================================================

    @FXML void ajouterImage(ActionEvent event)        { formHelper.ajouterImage(); }
    @FXML void genererImageIA(ActionEvent event)      { formHelper.genererImageIA(); }
    @FXML void creerEvenement(ActionEvent event)      { formHelper.creerEvenement(); }
    @FXML void rechercherPourModifier(ActionEvent event) { formHelper.rechercherPourModifier(); }
    @FXML void modifierImage(ActionEvent event)       { formHelper.modifierImage(); }
    @FXML void genererImageIAModification(ActionEvent event) { formHelper.genererImageIAModification(); }
    @FXML void modifierEvenement(ActionEvent event)   { formHelper.modifierEvenement(); }

    // Appelé par les boutons des cartes (via cardBuilder)
    void ouvrirModifier(Evenement e)       { formHelper.ouvrirModifier(e); }
    void annulerEvenement(Evenement e)     { formHelper.annulerEvenement(e); }
    void supprimerEvenement(Evenement e)   { formHelper.supprimerEvenement(e); }
    void showParticipantsGrid(Evenement e) { participantsHelper.afficherParticipantsGrid(e); }

    // ============================================================
    //  NAVIGATION
    // ============================================================

    @FXML void goToAccueil(ActionEvent event)     { System.out.println("Accueil"); }
    @FXML void goToEvenement(ActionEvent event)   { System.out.println("Événement"); }
    @FXML void goToMarketplace(ActionEvent event) { System.out.println("Marketplace"); }
    @FXML void goToForum(ActionEvent event)       { System.out.println("Forum"); }
    @FXML void goToTechnicien(ActionEvent event)  { System.out.println("Technicien"); }
    @FXML void goToUtilisateur(ActionEvent event) { System.out.println("Utilisateur"); }

    @FXML void deconnecter(ActionEvent event) {
        try {
            SessionManager.getInstance().setUtilisateur(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginApplication.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("FIRMA - Connexion");
            stage.show();
        } catch (Exception ex) {
            System.err.println("Erreur lors de la déconnexion : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML void rechercher(ActionEvent event) {
        chargerListe(searchField.getText());
    }

    // ============================================================
    //  DASHBOARD
    // ============================================================

    private void chargerDashboard() {
        if (dashboardContainer != null) {
            dashboardContainer.getChildren().clear();
            dashboardContainer.getChildren().add(dashboardHelper.construireDashboard());
        }
    }

    void rafraichirDashboard() {
        chargerDashboard();
    }

    // ============================================================
    //  ACCESSEURS PACKAGE-PRIVATE (pour les helpers)
    // ============================================================

    // Services
    EvenementService     getEvenementService()     { return service; }
    ParticipationService getParticipationService()  { return participationService; }
    AccompagnantService  getAccompagnantService()   { return accompagnantService; }
    DateTimeFormatter    getTimeFmt()               { return timeFmt; }

    // Navigation button (pour FileChooser owner)
    Button getBtnAccueil() { return btnAccueil; }

    // TabPane
    TabPane getMainTabPane() { return mainTabPane; }

    // Créer – champs
    TextField      getCreateTitre()        { return createTitre; }
    TextArea       getCreateDescription()  { return createDescription; }
    TextField      getCreateOrganisateur() { return createOrganisateur; }
    ComboBox<Type> getCreateType()         { return createType; }
    DatePicker     getCreateDateDebut()    { return createDateDebut; }
    DatePicker     getCreateDateFin()      { return createDateFin; }
    TextField      getCreateTempsDebut()   { return createTempsDebut; }
    TextField      getCreateTempsFin()     { return createTempsFin; }
    TextField      getCreateNombrePlaces() { return createNombrePlaces; }
    TextField      getCreateLieu()         { return createLieu; }
    TextField      getCreateAdresse()      { return createAdresse; }
    Label          getCreateImageLabel()   { return createImageLabel; }
    String         getCreateImagePath()    { return createImagePath; }
    void           setCreateImagePath(String p) { this.createImagePath = p; }
    Button         getBtnGenererIA()       { return btnGenererIA; }

    // Modifier – champs
    TextField      getModifySearchField()  { return modifySearchField; }
    TextField      getModifyTitre()        { return modifyTitre; }
    TextArea       getModifyDescription()  { return modifyDescription; }
    TextField      getModifyOrganisateur() { return modifyOrganisateur; }
    ComboBox<Type> getModifyType()         { return modifyType; }
    DatePicker     getModifyDateDebut()    { return modifyDateDebut; }
    DatePicker     getModifyDateFin()      { return modifyDateFin; }
    TextField      getModifyTempsDebut()   { return modifyTempsDebut; }
    TextField      getModifyTempsFin()     { return modifyTempsFin; }
    TextField      getModifyNombrePlaces() { return modifyNombrePlaces; }
    TextField      getModifyLieu()         { return modifyLieu; }
    TextField      getModifyAdresse()      { return modifyAdresse; }
    Label          getModifyImageLabel()   { return modifyImageLabel; }
    String         getModifyImagePath()    { return modifyImagePath; }
    void           setModifyImagePath(String p) { this.modifyImagePath = p; }
    Button         getBtnGenererIAModif()  { return btnGenererIAModif; }

    // État sélection
    int  getSelectedEvenementId()          { return selectedEvenementId; }
    void setSelectedEvenementId(int id)    { this.selectedEvenementId = id; }
}
