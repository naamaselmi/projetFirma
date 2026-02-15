package edu.connection3a7.controllers;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Statutevent;
import edu.connection3a7.entities.Type;
import edu.connection3a7.services.EvenementService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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

    // ===== SERVICE & STATE =====
    private final EvenementService service = new EvenementService();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private String createImagePath = null;
    private String modifyImagePath = null;
    private int    selectedEvenementId = -1;

    // ============================================================
    //  INITIALISATION
    // ============================================================
    @FXML
    public void initialize() {
        createType.setItems(FXCollections.observableArrayList(Type.values()));
        modifyType.setItems(FXCollections.observableArrayList(Type.values()));
        chargerListe(null);
    }

    // ============================================================
    //  LISTE
    // ============================================================

    private void chargerListe(String query) {
        evenementsContainer.getChildren().clear();
        try {
            List<Evenement> liste = service.getData();
            for (Evenement e : liste) {
                if (query != null && !query.isBlank()
                        && !e.getTitre().toLowerCase().contains(query.toLowerCase())) {
                    continue;
                }
                evenementsContainer.getChildren().add(creerCarteEvenement(e));
            }
        } catch (SQLException ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements : " + ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HBox creerCarteEvenement(Evenement e) {
        HBox carte = new HBox(15);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setPrefHeight(100);
        carte.setStyle("-fx-border-color: #e1dfdf; -fx-border-width: 2; -fx-background-color: white; -fx-background-radius: 5; -fx-border-radius: 5;");
        carte.setPadding(new Insets(10, 15, 10, 15));

        // ── Image ──
        VBox imgBox = new VBox();
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefWidth(80);
        imgBox.setPrefHeight(80);
        imgBox.setStyle("-fx-border-color: #e1dfdf; -fx-border-width: 1; -fx-background-color: #e1dfdf;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl()
                        : new File(e.getImageUrl()).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 80, 80, true, true));
                imgBox.getChildren().add(iv);
            } catch (Exception ex) {
                imgBox.getChildren().add(placeholderImg());
            }
        } else {
            imgBox.getChildren().add(placeholderImg());
        }

        // ── Infos ──
        VBox infos = new VBox(5);
        HBox.setHgrow(infos, Priority.ALWAYS);
        Label lblTitre = new Label("Titre : " + e.getTitre());
        lblTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label lblOrg = new Label("Organisateur : " + nullSafe(e.getOrganisateur()));
        lblOrg.setStyle("-fx-font-size: 13px;");
        Label lblDate = new Label("Du " + e.getDateDebut() + " au " + e.getDateFin()
                + "  |  " + nullSafe(e.getHoraireDebut()) + " → " + nullSafe(e.getHoraireFin()));
        lblDate.setStyle("-fx-font-size: 13px;");
        infos.getChildren().addAll(lblTitre, lblOrg, lblDate);

        // ── Capacité ──
        HBox capacite = new HBox(5);
        capacite.setAlignment(Pos.CENTER);
        Label iconCap = new Label("👤");
        iconCap.setStyle("-fx-font-size: 20px;");
        Label lblCap = new Label(e.getPlacesDisponibles() + "/" + e.getCapaciteMax());
        lblCap.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        capacite.getChildren().addAll(iconCap, lblCap);

        // ── Boutons ──
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER);

        Button btnDetails = new Button("ℹ");
        btnDetails.setPrefSize(40, 40);
        btnDetails.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnDetails.setTooltip(new Tooltip("Voir les détails"));
        btnDetails.setOnAction(ev -> afficherDetails(e));

        Button btnModif = new Button("✏");
        btnModif.setPrefSize(40, 40);
        btnModif.setStyle("-fx-background-color: #38b6ff; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 5;");
        btnModif.setTooltip(new Tooltip("Modifier"));
        btnModif.setOnAction(ev -> ouvrirModifier(e));

        Button btnSuppr = new Button("🗑");
        btnSuppr.setPrefSize(40, 40);
        btnSuppr.setStyle("-fx-background-color: #ff3131; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 5;");
        btnSuppr.setTooltip(new Tooltip("Supprimer"));
        btnSuppr.setOnAction(ev -> supprimerEvenement(e));

        boutons.getChildren().addAll(btnDetails, btnModif, btnSuppr);
        carte.getChildren().addAll(imgBox, infos, capacite, boutons);
        return carte;
    }

    // ============================================================
    //  POPUP DÉTAILS
    // ============================================================

    private void afficherDetails(Evenement e) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Détails : " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(520);
        popup.setMinHeight(600);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // ── En-tête vert ──
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: #49ad32;");

        // Image
        VBox imgBox = new VBox();
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefSize(75, 75);
        imgBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl()
                        : new File(e.getImageUrl()).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 75, 75, true, true));
                imgBox.getChildren().add(iv);
            } catch (Exception ex) {
                Label ph = new Label("📷");
                ph.setStyle("-fx-font-size: 30px;");
                imgBox.getChildren().add(ph);
            }
        } else {
            Label ph = new Label("📷");
            ph.setStyle("-fx-font-size: 30px;");
            imgBox.getChildren().add(ph);
        }

        VBox headerText = new VBox(5);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        Label titreLabel = new Label(e.getTitre());
        titreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        titreLabel.setWrapText(true);

        // Badge statut
        String statutStr = e.getStatut() != null ? e.getStatut().name().toUpperCase() : "";
        Label statutBadge = new Label("  " + statutStr + "  ");
        statutBadge.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 2 8;");

        Label typeLabel = new Label(e.getTypeEvenement() != null ? "Type : " + e.getTypeEvenement().name() : "");
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        headerText.getChildren().addAll(titreLabel, statutBadge, typeLabel);
        header.getChildren().addAll(imgBox, headerText);

        // ── Corps scrollable ──
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: white;");
        body.setPadding(new Insets(5, 25, 5, 25));

        // Description complète (TextArea read-only pour scroll si longue)
        VBox descSection = new VBox(6);
        descSection.setPadding(new Insets(14, 0, 14, 0));
        Label descTitle = new Label("📋  Description");
        descTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextArea descArea = new TextArea(e.getDescription() != null && !e.getDescription().isBlank()
                ? e.getDescription() : "—");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-font-size: 13px; -fx-control-inner-background: #f9f9f9; "
                + "-fx-border-color: #ebebeb; -fx-border-radius: 5; -fx-background-radius: 5;");
        descSection.getChildren().addAll(descTitle, descArea);

        body.getChildren().addAll(
                descSection,
                separateur(),
                ligneInfo("👤", "Organisateur",  nullSafe(e.getOrganisateur())),
                separateur(),
                ligneInfo("📅", "Date début",    nullSafe(e.getDateDebut())),
                separateur(),
                ligneInfo("📅", "Date fin",      nullSafe(e.getDateFin())),
                separateur(),
                ligneInfo("🕐", "Horaire début", e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt) : "—"),
                separateur(),
                ligneInfo("🕐", "Horaire fin",   e.getHoraireFin()   != null ? e.getHoraireFin().format(timeFmt) : "—"),
                separateur(),
                ligneInfo("📍", "Lieu",          nullSafe(e.getLieu())),
                separateur(),
                ligneInfo("🏠", "Adresse",       nullSafe(e.getAdresse())),
                separateur(),
                ligneInfo("🎟", "Places",        e.getPlacesDisponibles() + " / " + e.getCapaciteMax())
        );

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Boutons footer ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 25, 20, 25));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        // Bouton Google Maps
        boolean hasLocation = (e.getLieu() != null && !e.getLieu().isBlank())
                || (e.getAdresse() != null && !e.getAdresse().isBlank());

        Button btnMaps = new Button("📍  Voir sur Google Maps");
        btnMaps.setPrefHeight(38);
        btnMaps.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnMaps.setDisable(!hasLocation);
        if (!hasLocation) {
            btnMaps.setTooltip(new Tooltip("Aucune adresse renseignée"));
            btnMaps.setStyle("-fx-background-color: #aaaaaa; -fx-text-fill: white; -fx-font-size: 13px; "
                    + "-fx-background-radius: 20;");
        }
        btnMaps.setOnAction(ev -> ouvrirGoogleMaps(e.getLieu(), e.getAdresse()));

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefHeight(38);
        btnFermer.setPrefWidth(100);
        btnFermer.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnFermer.setOnAction(ev -> popup.close());

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(btnMaps, spacer, btnFermer);

        root.getChildren().addAll(header, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 520, 620);
        popup.setScene(scene);
        popup.showAndWait();
    }

    /** Ouvre Google Maps dans le navigateur avec lieu + adresse */
    private void ouvrirGoogleMaps(String lieu, String adresse) {
        try {
            String recherche = "";
            if (lieu != null && !lieu.isBlank() && adresse != null && !adresse.isBlank()) {
                recherche = lieu + ", " + adresse;
            } else if (lieu != null && !lieu.isBlank()) {
                recherche = lieu;
            } else {
                recherche = adresse;
            }
            String encoded = URLEncoder.encode(recherche, StandardCharsets.UTF_8);
            String url = "https://www.google.com/maps/search/?api=1&query=" + encoded;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir Google Maps : " + ex.getMessage());
        }
    }

    /** Une ligne icone + label + valeur */
    private HBox ligneInfo(String emoji, String label, String valeur) {
        HBox ligne = new HBox(12);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(11, 0, 11, 0));

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 15px;");
        icon.setMinWidth(22);

        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        lbl.setMinWidth(120);

        Label val = new Label(valeur == null || valeur.isBlank() ? "—" : valeur);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #222;");
        val.setWrapText(true);
        HBox.setHgrow(val, Priority.ALWAYS);

        ligne.getChildren().addAll(icon, lbl, val);
        return ligne;
    }

    private Region separateur() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #f0f0f0;");
        return sep;
    }

    // ============================================================
    //  CRÉER
    // ============================================================

    @FXML
    void ajouterImage(ActionEvent event) {
        File file = choisirImage();
        if (file != null) createImagePath = file.getAbsolutePath();
    }

    @FXML
    void creerEvenement(ActionEvent event) {
        if (createTitre.getText().isBlank() || createDateDebut.getValue() == null
                || createDateFin.getValue() == null || createNombrePlaces.getText().isBlank()) {
            afficherAlerte(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir : Titre, Dates et Nombre de places.");
            return;
        }
        LocalTime heureDebut = parseTime(createTempsDebut.getText());
        LocalTime heureFin   = parseTime(createTempsFin.getText());
        if (heureDebut == null || heureFin == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Horaire invalide", "Format attendu : HH:mm (ex: 09:30)");
            return;
        }
        try {
            Evenement e = new Evenement();
            e.setTitre(createTitre.getText().trim());
            e.setDescription(createDescription.getText().trim());
            e.setOrganisateur(createOrganisateur.getText().trim());
            e.setTypeEvenement(createType.getValue());
            e.setDateDebut(createDateDebut.getValue());
            e.setDateFin(createDateFin.getValue());
            e.setHoraireDebut(heureDebut);
            e.setHoraireFin(heureFin);
            e.setCapaciteMax(Integer.parseInt(createNombrePlaces.getText().trim()));
            e.setPlacesDisponibles(Integer.parseInt(createNombrePlaces.getText().trim()));
            e.setLieu(createLieu.getText().trim());
            e.setAdresse(createAdresse.getText().trim());
            e.setImageUrl(createImagePath);
            e.setStatut(Statutevent.actif);

            service.addEntity(e);
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Événement créé avec succès !");
            clearCreateForm();
            chargerListe(null);
            mainTabPane.getSelectionModel().select(0);

        } catch (NumberFormatException ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Le nombre de places doit être un entier.");
        } catch (SQLException ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur base de données", ex.getMessage());
        }
    }

    // ============================================================
    //  MODIFIER
    // ============================================================

    private void ouvrirModifier(Evenement e) {
        selectedEvenementId = e.getIdEvenement();
        modifyTitre.setText(e.getTitre());
        modifyDescription.setText(nullSafe(e.getDescription()));
        modifyOrganisateur.setText(nullSafe(e.getOrganisateur()));
        modifyType.setValue(e.getTypeEvenement());
        modifyDateDebut.setValue(e.getDateDebut());
        modifyDateFin.setValue(e.getDateFin());
        modifyTempsDebut.setText(e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt) : "");
        modifyTempsFin.setText(e.getHoraireFin() != null ? e.getHoraireFin().format(timeFmt) : "");
        modifyNombrePlaces.setText(String.valueOf(e.getCapaciteMax()));
        modifyLieu.setText(nullSafe(e.getLieu()));
        modifyAdresse.setText(nullSafe(e.getAdresse()));
        modifyImagePath = e.getImageUrl();
        mainTabPane.getSelectionModel().select(2);
    }

    @FXML
    void rechercherPourModifier(ActionEvent event) {
        String query = modifySearchField.getText().trim();
        if (query.isBlank()) return;
        try {
            service.getData().stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(query.toLowerCase()))
                    .findFirst()
                    .ifPresentOrElse(
                            this::ouvrirModifier,
                            () -> afficherAlerte(Alert.AlertType.WARNING, "Introuvable",
                                    "Aucun événement trouvé pour : " + query));
        } catch (SQLException ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void modifierImage(ActionEvent event) {
        File file = choisirImage();
        if (file != null) modifyImagePath = file.getAbsolutePath();
    }

    @FXML
    void modifierEvenement(ActionEvent event) {
        if (selectedEvenementId == -1) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucun événement sélectionné",
                    "Cliquez sur ✏ dans la liste ou cherchez un événement d'abord.");
            return;
        }
        if (modifyTitre.getText().isBlank() || modifyDateDebut.getValue() == null
                || modifyDateFin.getValue() == null || modifyNombrePlaces.getText().isBlank()) {
            afficherAlerte(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir : Titre, Dates et Nombre de places.");
            return;
        }
        LocalTime heureDebut = parseTime(modifyTempsDebut.getText());
        LocalTime heureFin   = parseTime(modifyTempsFin.getText());
        if (heureDebut == null || heureFin == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Horaire invalide", "Format attendu : HH:mm (ex: 09:30)");
            return;
        }
        try {
            Evenement e = new Evenement();
            e.setTitre(modifyTitre.getText().trim());
            e.setDescription(modifyDescription.getText().trim());
            e.setOrganisateur(modifyOrganisateur.getText().trim());
            e.setTypeEvenement(modifyType.getValue());
            e.setDateDebut(modifyDateDebut.getValue());
            e.setDateFin(modifyDateFin.getValue());
            e.setHoraireDebut(heureDebut);
            e.setHoraireFin(heureFin);
            e.setCapaciteMax(Integer.parseInt(modifyNombrePlaces.getText().trim()));
            e.setPlacesDisponibles(Integer.parseInt(modifyNombrePlaces.getText().trim()));
            e.setLieu(modifyLieu.getText().trim());
            e.setAdresse(modifyAdresse.getText().trim());
            e.setImageUrl(modifyImagePath);
            e.setStatut(Statutevent.actif);

            service.updateEntity(selectedEvenementId, e);
            afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès !");
            selectedEvenementId = -1;
            chargerListe(null);
            mainTabPane.getSelectionModel().select(0);

        } catch (NumberFormatException ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Le nombre de places doit être un entier.");
        } catch (SQLException ex) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur base de données", ex.getMessage());
        }
    }

    // ============================================================
    //  SUPPRIMER
    // ============================================================

    private void supprimerEvenement(Evenement e) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement ?");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + e.getTitre() + "\" ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.deleteEntity(e);
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé !");
                    chargerListe(null);
                } catch (SQLException ex) {
                    afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
                }
            }
        });
    }

    // ============================================================
    //  NAVIGATION
    // ============================================================

    @FXML void goToAccueil(ActionEvent event)     { System.out.println("Accueil"); }
    @FXML void goToEvenement(ActionEvent event)   { System.out.println("Événement"); }
    @FXML void goToMarketplace(ActionEvent event) { System.out.println("Marketplace"); }
    @FXML void goToForum(ActionEvent event)       { System.out.println("Forum"); }
    @FXML void goToTechnicien(ActionEvent event)  { System.out.println("Technicien"); }
    @FXML void goToUtilisateur(ActionEvent event) { System.out.println("Utilisateur"); }
    @FXML void deconnecter(ActionEvent event)     { System.out.println("Déconnexion"); }

    // ============================================================
    //  UTILITAIRES
    // ============================================================

    @FXML
    void rechercher(ActionEvent event) {
        chargerListe(searchField.getText());
    }

    private File choisirImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        return fc.showOpenDialog(btnAccueil.getScene().getWindow());
    }

    private void clearCreateForm() {
        createTitre.clear();
        createDescription.clear();
        createOrganisateur.clear();
        createType.setValue(null);
        createDateDebut.setValue(null);
        createDateFin.setValue(null);
        createTempsDebut.clear();
        createTempsFin.clear();
        createNombrePlaces.clear();
        createLieu.clear();
        createAdresse.clear();
        createImagePath = null;
    }

    private Label placeholderImg() {
        Label lbl = new Label("Image");
        lbl.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        return lbl;
    }

    private String nullSafe(Object o) {
        return o == null ? "" : o.toString();
    }

    private LocalTime parseTime(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return LocalTime.parse(text.trim(), timeFmt);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}