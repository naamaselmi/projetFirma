package edu.connection3a7.controller;

import edu.connection3a7.entities.Avis;
import edu.connection3a7.service.Avisservice;
import edu.connection3a7.tools.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherAvisSimpleController implements Initializable {

    @FXML private Label lblNomTechnicien;
    @FXML private Label lblMoyenne;
    @FXML private Label lblTotal;
    @FXML private VBox avisContainer;
    @FXML private Button btnFermer;

    private Avisservice avisService = new Avisservice();
    private Integer idTechnicien;
    private String nomTechnicien;
    private Integer idUtilisateurConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AfficherAvisSimpleController initialisé");

        SessionManager session = SessionManager.getInstance();
        idUtilisateurConnecte = session.getIdUtilisateur();

        if (btnFermer != null) {
            btnFermer.setOnAction(e -> fermer());
        }
    }

    public void initData(Integer idTech, String nomTech) {
        this.idTechnicien = idTech;
        this.nomTechnicien = nomTech;

        lblNomTechnicien.setText("Avis pour " + nomTech);
        chargerAvis();
    }

    private void chargerAvis() {
        try {
            avisContainer.getChildren().clear();

            List<Avis> avisList = avisService.getAvisByTechnicien(idTechnicien);
            System.out.println("📊 Nombre d'avis trouvés: " + avisList.size());

            if (avisList.isEmpty()) {
                Label message = new Label("Aucun avis pour ce technicien");
                message.setStyle("-fx-padding: 20; -fx-text-fill: #666; -fx-font-size: 14;");
                message.setMaxWidth(Double.MAX_VALUE);
                message.setAlignment(javafx.geometry.Pos.CENTER);
                avisContainer.getChildren().add(message);
                return;
            }

            // Calculer la moyenne
            double moyenne = avisList.stream()
                    .mapToInt(Avis::getNote)
                    .average()
                    .orElse(0.0);

            lblMoyenne.setText(String.format("⭐ %.1f / 10", moyenne));
            lblTotal.setText(avisList.size() + " avis");

            // Afficher chaque avis
            for (Avis avis : avisList) {
                VBox avisCard = creerCarteAvis(avis);
                avisContainer.getChildren().add(avisCard);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les avis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox creerCarteAvis(Avis avis) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );

        // ===== EN-TÊTE =====
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Note avec étoiles
        Label noteLabel = new Label("⭐ " + avis.getNote() + "/10");
        noteLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-text-fill: #1a961e;"
        );

        // Date
        Label dateLabel = new Label("📅 " + avis.getDateAvis().toString());
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(noteLabel, spacer, dateLabel);

        // ===== COMMENTAIRE =====
        Label commentaireLabel;
        if (avis.getCommentaire() != null && !avis.getCommentaire().trim().isEmpty()) {
            commentaireLabel = new Label(avis.getCommentaire());
            commentaireLabel.setWrapText(true);
            commentaireLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333; -fx-padding: 5 0;");
        } else {
            commentaireLabel = new Label("Pas de commentaire");
            commentaireLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 5 0;");
        }

        // ===== BARRE DE PROGRESSION =====
        ProgressBar progressBar = new ProgressBar(avis.getNote() / 10.0);
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(12);
        progressBar.setStyle(
                "-fx-accent: #1a961e;" +
                        "-fx-background-radius: 10;"
        );

        HBox progressBox = new HBox(10);
        progressBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        progressBox.getChildren().addAll(new Label("Note:"), progressBar);

        // ===== BOUTONS (pour le propriétaire) =====
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        boolean estProprietaire = idUtilisateurConnecte != null &&
                idUtilisateurConnecte.equals(avis.getIdUtilisateur());

        if (estProprietaire) {
            Button modifierBtn = new Button("MODIFIER");
            modifierBtn.setStyle(
                    "-fx-background-color: #f39c12;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 10;" +
                            "-fx-cursor: hand;"
            );
            modifierBtn.setOnAction(e -> modifierAvis(avis));

            Button supprimerBtn = new Button("SUPPRIMER");
            supprimerBtn.setStyle(
                    "-fx-background-color: #e74c3c;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11;" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 10;" +
                            "-fx-cursor: hand;"
            );
            supprimerBtn.setOnAction(e -> supprimerAvis(avis));

            buttonBox.getChildren().addAll(modifierBtn, supprimerBtn);
        } else {
            Label userLabel = new Label("Avis de Utilisateur #" + avis.getIdUtilisateur());
            userLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11; -fx-font-style: italic;");
            buttonBox.getChildren().add(userLabel);
        }

        card.getChildren().addAll(header, commentaireLabel, progressBox, buttonBox);

        return card;
    }

    private void modifierAvis(Avis avis) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/uploads/ModifierAvis.fxml"));
            Parent root = loader.load();

            ModifierAvisController controller = loader.getController();
            controller.initData(avis, this::chargerAvis);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier avis");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire");
            e.printStackTrace();
        }
    }

    private void supprimerAvis(Avis avis) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cet avis ?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                avisService.delet(avis);
                chargerAvis();
                showAlert("Succès", "Avis supprimé");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}