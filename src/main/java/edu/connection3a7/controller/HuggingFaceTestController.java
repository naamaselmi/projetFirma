package edu.connection3a7.controller;

import edu.connection3a7.service.HuggingFaceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class HuggingFaceTestController {

    @FXML private TextArea txtInput;
    @FXML private TextArea txtOutput;
    @FXML private ComboBox<String> modelSelector;
    @FXML private Button btnSend;

    private HuggingFaceService huggingFaceService;

    @FXML
    public void initialize() {
        huggingFaceService = new HuggingFaceService();

        modelSelector.getItems().addAll(
                "google-t5/t5-small",
                "gpt2",
                "HuggingFaceH4/zephyr-7b-beta"
        );
        modelSelector.setValue("google-t5/t5-small");
    }

    @FXML
    private void sendQuery() {
        String input = txtInput.getText();
        String model = modelSelector.getValue();

        if (input.isEmpty()) {
            showAlert("Veuillez entrer une question");
            return;
        }

        btnSend.setDisable(true);
        txtOutput.setText("⏳ Envoi de la requête...");

        new Thread(() -> {
            try {
                String response = huggingFaceService.query(model, input);

                javafx.application.Platform.runLater(() -> {
                    txtOutput.setText(response);
                    btnSend.setDisable(false);
                });

            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    txtOutput.setText("❌ Erreur: " + e.getMessage());
                    btnSend.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.show();
    }
}