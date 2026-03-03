package Firma.controllers.GestionTechnicien;

import Firma.entities.GestionTechnicien.Technicien;
import Firma.services.GestionTechnicien.Avisservice;
import Firma.services.GestionTechnicien.Technicienserv;
import Firma.tools.GestionEvenement.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ListeDesTechniciensfront implements Initializable {

    @FXML private FlowPane technicienGrid;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button btnCreerDemande;
    @FXML private Label lblInfo;
    @FXML private Button btnRetour;
    @FXML private Button btnTestCarte;
    @FXML private Button btnDecouvrirIA;


    private Technicienserv technicienService = new Technicienserv();
    private Avisservice avisService = new Avisservice();

    private static final String IMAGE_PATH = "src/main/resources/images/";
    private static final String AVATAR_PATH = "src/main/resources/images/avatar.png";

    private List<Technicien> tousLesTechniciens;
    private Technicien technicienSelectionne = null;
    private Integer idUtilisateurConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ ListeDesTechniciensfront initialisé");

        this.idUtilisateurConnecte = (SessionManager.getInstance().getUtilisateur() != null ? SessionManager.getInstance().getUtilisateur().getId() : 1);

        chargerTechniciens();

        refreshButton.setOnAction(e -> chargerTechniciens());
        btnCreerDemande.setOnAction(e -> creerDemande());

        if (btnRetour != null) {
            btnRetour.setOnAction(e -> retournerEnArriere());
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerTechniciens(newVal));
        if (btnDecouvrirIA != null) {
            btnDecouvrirIA.setOnAction(e -> ouvrirAssistantIA());
        }
    }

    /**
     * Retourne vers la liste des techniciens BACK
     */
    @FXML
    private void ouvrirLoginTechnicien() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/uploads/LoginTechnicien.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("🔧 Connexion Technicien");
            stage.setScene(new Scene(root, 400, 500));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la page de connexion: " + e.getMessage());
        }
    }
    @FXML
    private void retournerVersBack() {
        try {
            System.out.println("🔄 Retour vers la liste BACK");

            // Chemin vers la liste back
            URL fxmlUrl = getClass().getResource("/GestionTechnicien/ListeDesTechniciens.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier ListeDesTechniciens.fxml introuvable!");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) technicienGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des techniciens - BACK");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerTechniciens() {
        try {
            tousLesTechniciens = technicienService.getdata();
            System.out.println("✅ " + tousLesTechniciens.size() + " technicien(s) chargé(s)");
            afficherGrille(tousLesTechniciens);
            lblInfo.setText("📊 " + tousLesTechniciens.size() + " technicien(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les techniciens");
            e.printStackTrace();
        }
    }

    private void filtrerTechniciens(String recherche) {
        if (recherche == null || recherche.trim().isEmpty()) {
            afficherGrille(tousLesTechniciens);
            lblInfo.setText("📊 " + tousLesTechniciens.size() + " technicien(s)");
            return;
        }

        String searchLower = recherche.toLowerCase();
        List<Technicien> filtres = tousLesTechniciens.stream()
                .filter(t -> t.getNom().toLowerCase().contains(searchLower) ||
                        t.getPrenom().toLowerCase().contains(searchLower) ||
                        (t.getSpecialite() != null && t.getSpecialite().toLowerCase().contains(searchLower)))
                .toList();

        afficherGrille(filtres);
        lblInfo.setText("📊 " + filtres.size() + " résultat(s)");
    }

    private void afficherGrille(List<Technicien> techniciens) {
        technicienGrid.getChildren().clear();
        for (Technicien tech : techniciens) {
            VBox card = creerCarteTechnicien(tech);
            technicienGrid.getChildren().add(card);
        }
    }
    /**
     * Ouvre l'assistant IA de diagnostic
     */
    @FXML
    private void ouvrirAssistantIA() {
        try {
            // ✅ Essayer plusieurs chemins possibles
            URL fxmlUrl = null;
            String[] chemins = {
                    "/GestionTechnicien/DiagnosticAI.fxml",
                    "/DiagnosticAI.fxml",
                    "/fxml/DiagnosticAI.fxml"
            };

            for (String chemin : chemins) {
                fxmlUrl = getClass().getResource(chemin);
                if (fxmlUrl != null) {
                    System.out.println("✅ Fichier trouvé: " + chemin);
                    break;
                }
            }

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier DiagnosticAI.fxml introuvable!\n" +
                                "Vérifiez qu'il est dans src/main/resources/uploads/");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);

            Stage stage = new Stage();
            stage.setTitle("🧠 Assistant IA de diagnostic");
            stage.setScene(new Scene(root, 700, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir l'assistant IA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Image getAvatarImage() {
        File avatarFile = new File(AVATAR_PATH);
        if (avatarFile.exists()) {
            return new Image(avatarFile.toURI().toString());
        }
        return null;
    }

    private VBox creerCarteTechnicien(Technicien tech) {
        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setPrefHeight(500);
        card.setPadding(new Insets(20));
        card.setUserData(tech);

        String styleDeBase =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 2;";
        card.setStyle(styleDeBase);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        // EFFETS DE SOURIS
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8fff8;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-color: #1a961e;" +
                            "-fx-border-width: 3;"
            );
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(26, 150, 30, 0.4));
            hoverShadow.setRadius(15);
            hoverShadow.setOffsetY(5);
            card.setEffect(hoverShadow);
            card.setCursor(javafx.scene.Cursor.HAND);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(styleDeBase);
            card.setEffect(shadow);
            card.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        // DOUBLE-CLIC POUR VOIR LES DÉTAILS
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                afficherDetailsTechnicien(tech);
            }
        });

        // PHOTO OU AVATAR
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #1a961e;" +
                        "-fx-border-width: 2;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        String imgPath = tech.getImage();
        boolean imageChargee = false;

        // Essayer de charger la photo du technicien
        if (imgPath != null && !imgPath.isEmpty()) {
            File imgFile = new File(IMAGE_PATH + imgPath);
            if (imgFile.exists()) {
                try {
                    imageView.setImage(new Image(imgFile.toURI().toString()));
                    imageChargee = true;
                } catch (Exception e) {}
            }
        }

        if (!imageChargee) {
            // Utiliser l'avatar par défaut
            Image avatar = getAvatarImage();
            if (avatar != null) {
                imageView.setImage(avatar);
                imageChargee = true;
            }
        }

        if (imageChargee) {
            imageContainer.getChildren().add(imageView);
        } else {
            // Si même l'avatar n'existe pas, mettre une icône
            Label avatarIcon = new Label("👤");
            avatarIcon.setStyle("-fx-font-size: 60; -fx-text-fill: #1a961e;");
            imageContainer.getChildren().add(avatarIcon);
        }

        card.getChildren().add(imageContainer);

        // NOM
        Label nomLabel = new Label(tech.getPrenom() + " " + tech.getNom());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nomLabel.setTextFill(Color.web("#1a961e"));
        nomLabel.setWrapText(true);
        nomLabel.setAlignment(javafx.geometry.Pos.CENTER);
        nomLabel.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(nomLabel);

        // SPÉCIALITÉ
        Label specialiteLabel = new Label("🔧 " + (tech.getSpecialite() != null ? tech.getSpecialite() : "Non spécifiée"));
        specialiteLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");
        specialiteLabel.setMaxWidth(Double.MAX_VALUE);
        specialiteLabel.setAlignment(javafx.geometry.Pos.CENTER);
        card.getChildren().add(specialiteLabel);

        // NOTE MOYENNE (sans étoile, juste texte)
        try {
            double moyenne = avisService.getNoteMoyenneTechnicien(tech.getId_tech());
            Label moyenneLabel = new Label(String.format("Note moyenne: %.1f/10", moyenne));
            moyenneLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #666;");
            moyenneLabel.setAlignment(javafx.geometry.Pos.CENTER);
            moyenneLabel.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(moyenneLabel);
        } catch (SQLException e) {}

        // BOUTONS AVIS
        HBox avisBoutons = new HBox(10);
        avisBoutons.setAlignment(javafx.geometry.Pos.CENTER);
        avisBoutons.setPadding(new Insets(5, 0, 5, 0));

        Button btnLaisserAvis = new Button("Laisser avis");
        btnLaisserAvis.setStyle(
                "-fx-background-color: #f39c12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 10;" +
                        "-fx-cursor: hand;"
        );
        btnLaisserAvis.setOnAction(e -> ouvrirFormulaireAvis(tech));

        Button btnVoirAvis = new Button("Voir avis");
        btnVoirAvis.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 10;" +
                        "-fx-cursor: hand;"
        );
        btnVoirAvis.setOnAction(e -> voirAvisTechnicien(tech));

        avisBoutons.getChildren().addAll(btnLaisserAvis, btnVoirAvis);
        card.getChildren().add(avisBoutons);

        // DISPONIBILITÉ
        Label dispoLabel = new Label(tech.isDisponibilite() ? "Disponible" : "Non disponible");
        dispoLabel.setStyle(tech.isDisponibilite() ?
                "-fx-text-fill: green; -fx-font-weight: bold;" :
                "-fx-text-fill: red; -fx-font-weight: bold;");
        dispoLabel.setMaxWidth(Double.MAX_VALUE);
        dispoLabel.setAlignment(javafx.geometry.Pos.CENTER);
        card.getChildren().add(dispoLabel);

        // BOUTON SÉLECTIONNER
        Button btnSelectionner = new Button("SÉLECTIONNER");
        btnSelectionner.setStyle(
                "-fx-background-color: #4F772D;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 8 15;" +
                        "-fx-cursor: hand;"
        );
        btnSelectionner.setMaxWidth(Double.MAX_VALUE);
        btnSelectionner.setOnAction(e -> {
            technicienSelectionne = tech;
            lblInfo.setText("✅ Sélectionné: " + tech.getPrenom() + " " + tech.getNom());

            btnSelectionner.setStyle(
                    "-fx-background-color: #1a961e;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12;" +
                            "-fx-background-radius: 25;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"
            );
        });
        card.getChildren().add(btnSelectionner);

        return card;
    }

    // ===== DOUBLE-CLIC : AFFICHER LES DÉTAILS =====
    private void afficherDetailsTechnicien(Technicien tech) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails du technicien");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20;");

        // Photo ou avatar en grand
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: #1a961e;" +
                        "-fx-border-width: 3;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitHeight(160);
        imageView.setFitWidth(160);
        imageView.setPreserveRatio(true);

        String imgPath = tech.getImage();
        boolean imageChargee = false;

        if (imgPath != null && !imgPath.isEmpty()) {
            File imgFile = new File(IMAGE_PATH + imgPath);
            if (imgFile.exists()) {
                try {
                    imageView.setImage(new Image(imgFile.toURI().toString()));
                    imageChargee = true;
                } catch (Exception e) {}
            }
        }

        if (!imageChargee) {
            Image avatar = getAvatarImage();
            if (avatar != null) {
                imageView.setImage(avatar);
                imageChargee = true;
            }
        }

        if (imageChargee) {
            imageContainer.getChildren().add(imageView);
        } else {
            Label avatarIcon = new Label("👤");
            avatarIcon.setStyle("-fx-font-size: 80; -fx-text-fill: #1a961e;");
            imageContainer.getChildren().add(avatarIcon);
        }

        root.getChildren().add(imageContainer);

        // Grille d'informations
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setStyle("-fx-font-size: 14;");

        int row = 0;
        grid.add(new Label("🆔 ID :"), 0, row);
        grid.add(new Label(String.valueOf(tech.getId_tech())), 1, row++);

        grid.add(new Label("👤 Nom :"), 0, row);
        grid.add(new Label(tech.getPrenom() + " " + tech.getNom()), 1, row++);

        grid.add(new Label("🔧 Spécialité :"), 0, row);
        grid.add(new Label(tech.getSpecialite() != null ? tech.getSpecialite() : "Non spécifiée"), 1, row++);

        grid.add(new Label("📧 Email :"), 0, row);
        grid.add(new Label(tech.getEmail() != null ? tech.getEmail() : "Non renseigné"), 1, row++);

        grid.add(new Label("📞 Téléphone :"), 0, row);
        grid.add(new Label(tech.getTelephone() != null ? tech.getTelephone() : "Non renseigné"), 1, row++);

        grid.add(new Label("📍 Localisation :"), 0, row);
        grid.add(new Label(tech.getLocalisation() != null ? tech.getLocalisation() : "Non renseignée"), 1, row++);

        grid.add(new Label("🆔 CIN :"), 0, row);
        grid.add(new Label(tech.getCin() != null ? tech.getCin() : "Non renseigné"), 1, row++);

        grid.add(new Label("🎂 Âge :"), 0, row);
        grid.add(new Label(tech.getAge() + " ans"), 1, row++);

        grid.add(new Label("📅 Date naiss. :"), 0, row);
        grid.add(new Label(tech.getDateNaissance() != null ? tech.getDateNaissance().toString() : "Non renseignée"), 1, row++);

        Label dispoValue = new Label(tech.isDisponibilite() ? "Disponible" : "Non disponible");
        dispoValue.setStyle(tech.isDisponibilite() ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red; -fx-font-weight: bold;");
        grid.add(new Label("📊 Disponibilité :"), 0, row);
        grid.add(dispoValue, 1, row);

        root.getChildren().add(grid);

        Button btnFermer = new Button("FERMER");
        btnFermer.setStyle(
                "-fx-background-color: #1a961e;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 10 40;" +
                        "-fx-cursor: hand;"
        );
        btnFermer.setOnAction(e -> detailStage.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.getChildren().add(btnFermer);
        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root, 500, 650);
        detailStage.setScene(scene);
        detailStage.showAndWait();
    }

    // ========== GESTION DES AVIS ==========
    private void ouvrirFormulaireAvis(Technicien tech) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionTechnicien/AjouterAvis.fxml"));
            Parent root = loader.load();

            AjouterAvisSimpleController controller = loader.getController();
            controller.initData(idUtilisateurConnecte, tech.getId_tech(), tech.getPrenom() + " " + tech.getNom());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Laisser un avis - " + tech.getPrenom());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            chargerTechniciens();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void voirAvisTechnicien(Technicien tech) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionTechnicien/AfficherAvis.fxml"));
            Parent root = loader.load();

            AfficherAvisSimpleController controller = loader.getController();
            controller.initData(tech.getId_tech(), tech.getPrenom() + " " + tech.getNom());

            Stage stage = new Stage();
            stage.setTitle("Avis - " + tech.getPrenom());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les avis");
        }
    }
    @FXML

    private void ouvrirCarte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionTechnicien/CarteSnapchat.fxml"));
            Parent root = loader.load();

            // Priorité au technicien connecté en session, sinon technicien sélectionné.
            CarteSnapchatController carteController = loader.getController();
            int idTechSession = (SessionManager.getInstance().getUtilisateur() != null ? SessionManager.getInstance().getUtilisateur().getId() : 0);
            if (idTechSession > 0) {
                carteController.setIdTechnicien(idTechSession);
            }
            if (technicienSelectionne != null) {
                carteController.setTechnicienCible(
                        technicienSelectionne.getId_tech(),
                        technicienSelectionne.getPrenom() + " " + technicienSelectionne.getNom()
                );
            }

            Stage stage = new Stage();
            stage.setTitle("🗺️ Carte des techniciens");
            stage.setScene(new Scene(root, 900, 700));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la carte: " + e.getMessage());
        }
    }
    @FXML
    private void creerDemande() {
        if (technicienSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez d'abord sélectionner un technicien dans la liste.");
            return;
        }

        try {
            // 1. Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionTechnicien/AjouterDemandeFront.fxml"));
            Parent root = loader.load();

            // 2. Passer le technicien au contrôleur de la nouvelle fenêtre
            AjouterDemandeFrontController controller = loader.getController();
            controller.setTechnicienChoisi(technicienSelectionne);

            // 3. Créer la nouvelle scène et le nouveau Stage (Fenêtre)
            Stage popupStage = new Stage();
            popupStage.setTitle("Créer une demande pour " + technicienSelectionne.getPrenom());

            // Rendre la fenêtre modale (bloque la fenêtre principale tant qu'elle est ouverte)
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(btnCreerDemande.getScene().getWindow());

            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            // --- 4. Animation de Scale (Agrandissement fluide) ---
            root.setScaleX(0.7);
            root.setScaleY(0.7);
            root.setOpacity(0);

            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), root);
            st.setToX(1.0);
            st.setToY(1.0);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), root);
            ft.setToValue(1.0);

            // Lancer les animations en parallèle
            popupStage.setOnShown(e -> {
                st.play();
                ft.play();
            });

            popupStage.showAndWait(); // Attend la fermeture pour revenir à la liste

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de demande : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void retournerEnArriere() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/uploads/ListeTechnicien.fxml"));
            Stage stage = (Stage) technicienGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des techniciens");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}