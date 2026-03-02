package com.examen.firmapi.controllers;

import com.examen.firmapi.entities.Utilisateur;
import com.examen.firmapi.services.UtilisateurService;
import com.examen.firmapi.utils.LogoutUtil;
import com.examen.firmapi.utils.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private Label welcomeText;

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView profileImage;

    @FXML
    private MenuButton profileMenu;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    private Utilisateur currentUser;

    @FXML
    public void initialize() {

        welcomeText.setText("Welcome to FIRMA 👋");

        currentUser = UserSession.getUser();

        if (currentUser != null) {

            // 🔹 Set name
            userNameLabel.setText(currentUser.getNom() + " " + currentUser.getPrenom());

            String imagePath = utilisateurService
                    .getProfileByUserId(currentUser.getId_utilisateur())
                    .getPhoto_profil();

            Image image;

            if (imagePath != null && new java.io.File(imagePath).exists()) {
                image = new Image(new java.io.File(imagePath).toURI().toString());
            } else {
                image = new Image(
                        getClass().getResourceAsStream("/images/default-profile.png")
                );
            }

            profileImage.setImage(image);

            // 🔹 Make image circular
            Circle clip = new Circle(17.5, 17.5, 17.5);
            profileImage.setClip(clip);

            System.out.println("✅ Main view loaded for: " + currentUser.getEmail());
        } else {
            System.out.println("⛔ No session found in MainController!");
        }
    }

    // 🔹 Logout
    @FXML
    private void logout() {
        Stage stage = (Stage) profileMenu.getScene().getWindow();
        LogoutUtil.logout(stage);
    }

    // 🔹 Open Edit Profile Window
    @FXML
    private void handleEditProfile() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/examen/firmapi/edit-profile.fxml")
            );

            Scene scene = new Scene(loader.load());

            // 🔹 Get controller and pass user
            EditProfileController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Cannot load edit-profile.fxml");
        }
    }
}