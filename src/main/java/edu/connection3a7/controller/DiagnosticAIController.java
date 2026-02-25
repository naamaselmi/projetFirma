package edu.connection3a7.controller;

import edu.connection3a7.service.DiagnosticIAService;
import edu.connection3a7.service.HuggingFaceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DiagnosticAIController {

    @FXML private TextArea txtDescription;
    @FXML private TextArea txtResultat;
    @FXML private ListView<String> listQuestions;
    @FXML private Button btnDiagnostiquer;
    @FXML private Button btnFermer;
    @FXML private ComboBox<String> comboModel;
    @FXML private CheckBox chkModeHorsLigne;

    private HuggingFaceService huggingFaceService;
    private DiagnosticIAService diagnosticService;

    @FXML
    public void initialize() {
        huggingFaceService = new HuggingFaceService();
        diagnosticService = new DiagnosticIAService();
        txtResultat.setEditable(false);

        // Configurer le sélecteur de modèle
        comboModel.getItems().addAll(
                "google-t5/t5-small (recommandé)",
                "gpt2 (rapide)",
                "HuggingFaceH4/zephyr-7b-beta (puissant)"
        );
        comboModel.setValue("google-t5/t5-small (recommandé)");

        // Mode hors ligne par défaut
        chkModeHorsLigne = new CheckBox("Mode hors ligne (IA locale)");
    }

    @FXML
    private void diagnostiquer() {
        String description = txtDescription.getText();

        if (description == null || description.trim().isEmpty()) {
            showAlert("Veuillez décrire le problème");
            return;
        }

        btnDiagnostiquer.setDisable(true);
        txtResultat.setText("⏳ Analyse en cours...");

        // ✅ UTILISER LA NOUVELLE MÉTHODE POUR CLIENT
        if (chkModeHorsLigne.isSelected()) {
            // Mode hors ligne - utiliser l'IA locale pour client
            var result = diagnosticService.diagnostiquerPourClient(description);
            txtResultat.setText(result.toString());
            listQuestions.getItems().clear(); // Plus de questions pour le client
            btnDiagnostiquer.setDisable(false);
        } else {
            // Mode en ligne - utiliser HuggingFace
            new Thread(() -> {
                try {
                    String selected = comboModel.getValue();
                    String modelName = selected.split(" ")[0];

                    // Prompt adapté pour le client
                    String prompt = String.format(
                            "Tu es un assistant pour clients. Analyse ce problème et donne:\n" +
                                    "1. Le type de problème probable\n" +
                                    "2. Des solutions que le client peut essayer immédiatement\n" +
                                    "3. Le type de technicien à appeler\n" +
                                    "4. Le niveau d'urgence\n\n" +
                                    "Problème: %s",
                            description
                    );

                    String response = huggingFaceService.query(modelName, prompt);

                    javafx.application.Platform.runLater(() -> {
                        txtResultat.setText(response);
                        btnDiagnostiquer.setDisable(false);
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        // Fallback sur l'IA locale
                        var result = diagnosticService.diagnostiquerPourClient(description);
                        txtResultat.setText(result.toString() +
                                "\n\n⚠️ HuggingFace indisponible, utilisation du diagnostic local.");
                        btnDiagnostiquer.setDisable(false);
                    });
                }
            }).start();
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}