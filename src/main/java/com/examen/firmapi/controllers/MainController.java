package com.examen.firmapi.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.examen.firmapi.utils.LogoutUtil;
import javafx.stage.Stage;



public class MainController {

    @FXML
    private Label welcomeText;

    @FXML
    private void initialize() {
        welcomeText.setText("Welcome to FIRMA 👋");
    }

    @FXML
    private Button logoutButton;

    @FXML
    private void logout() {

        Stage stage = (Stage) logoutButton.getScene().getWindow();
        LogoutUtil.logout(stage);
    }
}
