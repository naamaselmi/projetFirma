package com.examen.firmapi.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label welcomeText;

    @FXML
    private void initialize() {
        welcomeText.setText("Welcome to FIRMA 👋");
    }
}
