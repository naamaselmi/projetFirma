package edu.connection3a7.controllers;

import edu.connection3a7.entities.Role;
import edu.connection3a7.entities.Utilisateur;
import edu.connection3a7.services.UtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoginController {

    @FXML private ImageView logoImage;
    @FXML private ImageView sloganImage;
    @FXML private ImageView firmaImage;
    @FXML private Button cancelButton;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordTextField;

    @FXML
    public void initialize() {
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/image/logo.png")));
            sloganImage.setImage(new Image(getClass().getResourceAsStream("/image/slogan.png")));
            firmaImage.setImage(new Image(getClass().getResourceAsStream("/image/firma.png")));
        } catch (Exception e) {
            System.err.println("Erreur chargement images : " + e.getMessage());
        }
    }

    @FXML
    public void cancelButtonOnClick(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void loginButtonOnClick(ActionEvent e) {
        String email    = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        // Validation champs vides
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir l'email et le mot de passe !");
            return;
        }

        try {
            UtilisateurService service = new UtilisateurService();
            Utilisateur utilisateur = service.login(email, password);

            if (utilisateur != null) {
                loginMessage.setStyle("-fx-text-fill: #088002;");
                loginMessage.setText("Bienvenue " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " !");
                navigateToDashboard(utilisateur);
            } else {
                showError("Email ou mot de passe incorrect !");
            }

        } catch (Exception ex) {
            showError("Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void navigateToDashboard(Utilisateur utilisateur) {
        try {
            // Choisir le bon FXML selon le rôle
            String fxmlFile;
            if (utilisateur.getRole() == Role.admin) {
                fxmlFile = "/Dashboard.fxml";
            } else {
                fxmlFile = "/front.fxml"; // interface utilisateur par défaut
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle(utilisateur.getRole() == Role.admin ? "Dashboard Admin" : "Espace Utilisateur");
            stage.show();

            System.out.println("Navigation vers : " + fxmlFile);

        } catch (Exception e) {
            showError("Erreur lors du chargement de l'interface !");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        loginMessage.setStyle("-fx-text-fill: #ff0000;");
        loginMessage.setText(message);
    }
}