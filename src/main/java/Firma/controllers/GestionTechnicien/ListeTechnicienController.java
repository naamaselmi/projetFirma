package Firma.controllers.GestionTechnicien;

import Firma.entities.GestionTechnicien.Technicien;
import Firma.services.GestionTechnicien.LocalisationTechnicienService;
import Firma.services.GestionTechnicien.Technicienserv;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class ListeTechnicienController implements Initializable {

    @FXML private FlowPane technicienGrid;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button btnAjouter;
    @FXML private Label lblInfo;
    @FXML private Button btnAnnuler;
    @FXML private Button btnRetour;
    @FXML private Button btnActiverPosition;

    private Technicienserv service = new Technicienserv();
    private static final String IMAGE_PATH = "src/main/resources/images/";
    private static final String AVATAR_PATH = "src/main/resources/images/avatar.png";
    private List<Technicien> tousLesTechniciens;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ ListeTechnicienController initialisé");

        chargerTechniciens();

        refreshButton.setOnAction(e -> chargerTechniciens());
        btnAjouter.setOnAction(e -> ouvrirAjoutTechnicien());


        if (btnRetour != null) {
            btnRetour.setOnAction(e -> retournerAListe());
        }

        if (btnActiverPosition != null) {
            btnActiverPosition.setOnAction(e -> activerPosition());
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerTechniciens(newVal));
    }

    /**
     * Active la position d'un technicien sur la carte
     */
    @FXML
    private void activerPosition() {
        try {
            // Demander l'ID du technicien
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("📍 Activation de position");
            dialog.setHeaderText("Activer votre position sur la carte");
            dialog.setContentText("Entrez votre ID de technicien:");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String input = result.get();
                if (input.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un ID");
                    return;
                }

                int idTech = Integer.parseInt(input);

                // ✅ CORRECTION ICI : Appel sur l'instance, pas sur la classe
                Technicien tech = service.getTechnicienById(idTech);

                if (tech == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Technicien introuvable avec l'ID " + idTech);
                    return;
                }

                // Activer le partage de position
                LocalisationTechnicienService locService = new LocalisationTechnicienService();
                locService.activerPartage(idTech, true);


                // Message de confirmation
                Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                confirm.setTitle("✅ Succès");
                confirm.setHeaderText("Position activée !");
                confirm.setContentText("Technicien: " + tech.getPrenom() + " " + tech.getNom() +
                        "\nID: " + idTech +
                        "\n\nVous apparaissez maintenant sur la carte.");
                confirm.showAndWait();

                System.out.println("📍 Position activée pour " + tech.getPrenom() + " " + tech.getNom());
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID doit être un nombre");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerTechniciens() {
        try {
            tousLesTechniciens = service.getdata();
            System.out.println("✅ " + tousLesTechniciens.size() + " technicien(s) chargé(s)");

            afficherGrille(tousLesTechniciens);
            lblInfo.setText("📊 " + tousLesTechniciens.size() + " technicien(s) trouvé(s)");

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
        card.setPrefHeight(450);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: #1a961e;" +
                        "-fx-border-width: 3;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,100,0,0.3), 15, 0, 0, 8);"
        );

        // ===== CONTENEUR POUR LA PHOTO =====
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #1a961e;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        ImageView imageView = new ImageView();
        imageView.setFitHeight(140);
        imageView.setFitWidth(140);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 10; -fx-background-radius: 10;");

        String imgPath = tech.getImage();
        boolean imageChargee = false;

        // Essayer de charger la photo du technicien
        if (imgPath != null && !imgPath.isEmpty()) {
            File imgFile = new File(IMAGE_PATH + imgPath);
            if (imgFile.exists()) {
                try {
                    imageView.setImage(new Image(imgFile.toURI().toString()));
                    imageChargee = true;
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur chargement image: " + imgPath);
                }
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
            avatarIcon.setStyle("-fx-font-size: 70; -fx-text-fill: #1a961e;");
            imageContainer.getChildren().add(avatarIcon);
        }

        card.getChildren().add(imageContainer);

        // ===== NOM =====
        Label nomLabel = new Label(tech.getPrenom() + " " + tech.getNom());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nomLabel.setTextFill(Color.web("#1a961e"));
        nomLabel.setWrapText(true);
        nomLabel.setAlignment(javafx.geometry.Pos.CENTER);
        nomLabel.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(nomLabel);

        // ===== SPÉCIALITÉ =====
        if (tech.getSpecialite() != null && !tech.getSpecialite().isEmpty()) {
            Label specialiteLabel = new Label("🔧 " + tech.getSpecialite());
            specialiteLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            specialiteLabel.setTextFill(Color.web("#333"));
            specialiteLabel.setStyle(
                    "-fx-background-color: #e8f5e9;" +
                            "-fx-padding: 5 15;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;"
            );
            specialiteLabel.setMaxWidth(Double.MAX_VALUE);
            specialiteLabel.setAlignment(javafx.geometry.Pos.CENTER);
            card.getChildren().add(specialiteLabel);
        }

        // ===== EMAIL =====
        if (tech.getEmail() != null && !tech.getEmail().isEmpty()) {
            Label emailLabel = new Label("📧 " + tech.getEmail());
            emailLabel.setWrapText(true);
            emailLabel.setStyle("-fx-font-size: 12;");
            card.getChildren().add(emailLabel);
        }

        // ===== TÉLÉPHONE =====
        if (tech.getTelephone() != null && !tech.getTelephone().isEmpty()) {
            Label telLabel = new Label("📞 " + tech.getTelephone());
            telLabel.setStyle("-fx-font-size: 12;");
            card.getChildren().add(telLabel);
        }

        // ===== LOCALISATION =====
        if (tech.getLocalisation() != null && !tech.getLocalisation().isEmpty()) {
            Label locLabel = new Label("📍 " + tech.getLocalisation());
            locLabel.setStyle("-fx-font-size: 12;");
            card.getChildren().add(locLabel);
        }

        // ===== DISPONIBILITÉ =====
        Label dispoLabel = new Label(tech.isDisponibilite() ? "✅ DISPONIBLE" : "❌ NON DISPONIBLE");
        dispoLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        dispoLabel.setStyle(tech.isDisponibilite() ?
                "-fx-text-fill: white; -fx-background-color: #1a961e; -fx-padding: 8 15; -fx-background-radius: 25;" :
                "-fx-text-fill: white; -fx-background-color: #e74c3c; -fx-padding: 8 15; -fx-background-radius: 25;");
        dispoLabel.setMaxWidth(Double.MAX_VALUE);
        dispoLabel.setAlignment(javafx.geometry.Pos.CENTER);
        card.getChildren().add(dispoLabel);

        // ===== BOUTONS =====
        HBox boutons = new HBox(15);
        boutons.setAlignment(javafx.geometry.Pos.CENTER);
        boutons.setPadding(new Insets(10, 0, 0, 0));

        Button modifierBtn = new Button("✏️ MODIFIER");
        modifierBtn.setStyle(
                "-fx-background-color: #f39c12;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 8 15;" +
                        "-fx-cursor: hand;"
        );
        modifierBtn.setPrefWidth(130);
        modifierBtn.setOnAction(e -> modifierTechnicien(tech));

        Button supprimerBtn = new Button("🗑️ SUPPRIMER");
        supprimerBtn.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 8 15;" +
                        "-fx-cursor: hand;"
        );
        supprimerBtn.setPrefWidth(130);
        supprimerBtn.setOnAction(e -> supprimerTechnicien(tech));

        boutons.getChildren().addAll(modifierBtn, supprimerBtn);
        card.getChildren().add(boutons);

        // ===== EFFETS DE SOURIS =====
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8fff8;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-color: #1a961e;" +
                            "-fx-border-width: 4;" +
                            "-fx-effect: dropshadow(three-pass-box, #1a961e, 20, 0, 0, 10);" +
                            "-fx-scale-x: 1.02; -fx-scale-y: 1.02;"
            );
            card.setCursor(javafx.scene.Cursor.HAND);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-color: #1a961e;" +
                            "-fx-border-width: 3;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,100,0,0.3), 15, 0, 0, 8);" +
                            "-fx-scale-x: 1; -fx-scale-y: 1;"
            );
            card.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        // ===== DOUBLE-CLIC POUR VOIR LES DÉTAILS =====
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                afficherDetailsTechnicien(tech);
            }
        });

        return card;
    }

    private void retournerAListe() {
        System.out.println("🔄 Bouton Retour cliqué - Page actualisée");
        chargerTechniciens();
    }

    private void afficherDetailsTechnicien(Technicien tech) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Détails du technicien - " + tech.getPrenom() + " " + tech.getNom());

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20;");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 15; -fx-padding: 10;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
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
            avatarIcon.setStyle("-fx-font-size: 100; -fx-text-fill: #1a961e;");
            imageContainer.getChildren().add(avatarIcon);
        }

        root.getChildren().add(imageContainer);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
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

        Label dispoLabel = new Label(tech.isDisponibilite() ? "✅ Disponible" : "❌ Non disponible");
        dispoLabel.setStyle(tech.isDisponibilite() ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red; -fx-font-weight: bold;");
        grid.add(new Label("📊 Disponibilité :"), 0, row);
        grid.add(dispoLabel, 1, row++);

        root.getChildren().add(grid);

        Button fermerBtn = new Button("FERMER");
        fermerBtn.setStyle(
                "-fx-background-color: #1a961e;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 10 40;" +
                        "-fx-cursor: hand;"
        );
        fermerBtn.setOnAction(e -> popupStage.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.getChildren().add(fermerBtn);
        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root, 500, 600);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void modifierTechnicien(Technicien tech) {
        try {
            URL fxmlUrl = getClass().getResource("/GestionTechnicien/AjouterTechnicien.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Récupérer le contrôleur du formulaire pour lui injecter le technicien
            AjouterTechnicienController controller = loader.getController();
            controller.setTechnicienAModifier(tech);

            // --- OUVERTURE DANS UNE FENÊTRE MODALE ---
            Stage popupStage = new Stage();
            popupStage.setTitle("Modifier Technicien : " + tech.getNom());
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(technicienGrid.getScene().getWindow());

            popupStage.setScene(new Scene(root));

            popupStage.showAndWait();
            chargerTechniciens(); // Rafraîchir après modification

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture: " + e.getMessage());
        }
    }

    private void supprimerTechnicien(Technicien tech) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + tech.getPrenom() + " " + tech.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delet(tech);
                chargerTechniciens();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Technicien supprimé");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ===== MÉTHODE AVEC @FXML POUR LE BOUTON NOUVEAU TECHNICIEN =====
    @FXML
    private void ouvrirAjoutTechnicien() {
        try {
            URL fxmlUrl = getClass().getResource("/GestionTechnicien/AjouterTechnicien.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier FXML introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // --- CRÉATION DE LA NOUVELLE FENÊTRE ---
            Stage popupStage = new Stage();
            popupStage.setTitle("Ajouter un nouveau technicien");

            // Bloquer l'interaction avec la fenêtre principale tant que celle-ci est ouverte
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(technicienGrid.getScene().getWindow());

            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            // Rafraîchir la liste automatiquement après la fermeture de la fenêtre
            popupStage.showAndWait();
            chargerTechniciens();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void allerVersDemandesBack() {
        try {
            URL fxmlUrl = getClass().getResource("/GestionTechnicien/AjouterDemandeBack.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier AjouterDemandeBack.fxml introuvable!");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) technicienGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des demandes");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des demandes");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML
    private void fermerFenetre() {

        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
}