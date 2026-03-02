package com.examen.firmapi.controllers;

import com.examen.firmapi.entities.Role;
import com.examen.firmapi.entities.Utilisateur;
import com.examen.firmapi.services.UtilisateurService;
import com.examen.firmapi.utils.TempPasswordStore;
import com.examen.firmapi.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public class AuthController {

    @FXML
    private StackPane contentPane;

    private final UtilisateurService service = new UtilisateurService();

    @FXML
    public void initialize() throws IOException {
        showLogin();
    }

    // ========================
    // SHOW LOGIN
    // ========================

    @FXML
    public void showLogin() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/examen/firmapi/login-form.fxml")
        );

        Parent view = loader.load();
        contentPane.getChildren().setAll(view);

        TextField emailField = (TextField) view.lookup("#emailField");
        PasswordField passwordField = (PasswordField) view.lookup("#passwordField");
        Button loginBtn = (Button) view.lookup("#loginBtn");
        Hyperlink forgotPasswordLink = (Hyperlink) view.lookup("#forgotPasswordLink");

        forgotPasswordLink.setOnAction(e -> handleForgotPassword());

        loginBtn.setOnAction(e -> {

            String email = emailField.getText();
            String password = passwordField.getText();

            Utilisateur user = service.login(email, password);

            if (user != null) {

                String tempPassword = TempPasswordStore.get(email);

                if (tempPassword != null && password.equals(tempPassword)) {

                    try {
                        showChangePassword(user);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    return;
                }

                redirectUser(user);

            } else {
                new Alert(Alert.AlertType.ERROR,
                        "Email ou mot de passe incorrect").show();
            }
        });
    }

    // ========================
    // SHOW SIGNUP
    // ========================

    @FXML
    public void showSignUp() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/examen/firmapi/signup-form.fxml")
        );

        Parent view = loader.load();
        contentPane.getChildren().setAll(view);

        TextField nomField = (TextField) view.lookup("#nomField");
        TextField prenomField = (TextField) view.lookup("#prenomField");
        TextField emailField = (TextField) view.lookup("#emailField");
        PasswordField passwordField = (PasswordField) view.lookup("#passwordField");
        TextField telephoneField = (TextField) contentPane.lookup("#telephoneField");
        TextField adresseField = (TextField) contentPane.lookup("#adresseField");
        TextField villeField = (TextField) contentPane.lookup("#villeField");
        TextField codePostalField = (TextField) contentPane.lookup("#codePostalField");
        Button signupBtn = (Button) view.lookup("#signupBtn");

        signupBtn.setOnAction(e -> {

            try {
                // Condition de saisie (validation)
                if (nomField.getText().isEmpty()
                        || prenomField.getText().isEmpty()
                        || emailField.getText().isEmpty()
                        || passwordField.getText().isEmpty()) {

                    new Alert(Alert.AlertType.WARNING,
                            "Veuillez remplir tous les champs obligatoires").show();
                    return;
                }

                Utilisateur u = new Utilisateur();
                u.setNom(nomField.getText());
                u.setPrenom(prenomField.getText());
                u.setEmail(emailField.getText());
                u.setMot_de_passe(passwordField.getText());

                u.setTelephone(telephoneField.getText());
                u.setAdresse(adresseField.getText());
                u.setVille(villeField.getText());
                u.setCode_postal(codePostalField.getText());

                u.setRole(Role.UTILISATEUR);
                u.setDate_inscription(new Date());

                service.ajouterUtilisateur(u);

                new Alert(Alert.AlertType.INFORMATION,
                        "Compte créé avec succès").show();

                try {
                    showLogin();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (IllegalArgumentException ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Erreur de saisie: " + ex.getMessage()).show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Une erreur inattendue est survenue.").show();
                ex.printStackTrace();
            }
        });
    }

    // ========================
    // SHOW CHANGE PASSWORD
    // ========================

    private void showChangePassword(Utilisateur user) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/examen/firmapi/change-password.fxml")
        );

        Parent view = loader.load();
        contentPane.getChildren().setAll(view);

        PasswordField newPasswordField =
                (PasswordField) view.lookup("#newPasswordField");

        PasswordField confirmPasswordField =
                (PasswordField) view.lookup("#confirmPasswordField");

        Button changePasswordBtn =
                (Button) view.lookup("#changePasswordBtn");

        changePasswordBtn.setOnAction(e -> {

            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Please fill all fields").show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                new Alert(Alert.AlertType.ERROR,
                        "Passwords do not match").show();
                return;
            }

            try {
                // Update password in DB
                service.updatePassword(user.getEmail(), newPass);

                // Remove temp password
                TempPasswordStore.remove(user.getEmail());

                new Alert(Alert.AlertType.INFORMATION,
                        "Password changed successfully").show();

                // Auto login after change
                redirectUser(user);

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR,
                        ex.getMessage()).show();
            }

        });
    }

    private void redirectUser(Utilisateur user) {
        System.out.println("➡ Redirecting user...");
        UserSession.setUser(user);

        Stage stage = (Stage) contentPane.getScene().getWindow();

        try {

            if (user.getRole() == Role.ADMIN) {

                stage.setScene(new Scene(
                        FXMLLoader.load(
                                getClass().getResource("/com/examen/firmapi/utilisateur-view.fxml")
                        )
                ));

            } else {

                stage.setScene(new Scene(
                        FXMLLoader.load(
                                getClass().getResource("/com/examen/firmapi/main-view.fxml")
                        )
                ));
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @FXML
    private void handleForgotPassword() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Enter your registered email");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(email -> {
            try {
                service.resetPassword(email);

                new Alert(Alert.AlertType.INFORMATION,
                        "Temporary password sent to your email.").show();

            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        e.getMessage()).show();
            }
        });
    }
}