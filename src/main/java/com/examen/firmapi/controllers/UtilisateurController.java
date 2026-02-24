package com.examen.firmapi.controllers;

import com.examen.firmapi.entities.Role;
import com.examen.firmapi.entities.Utilisateur;
import com.examen.firmapi.services.UtilisateurService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Date;
import java.util.Optional;
import com.examen.firmapi.utils.LogoutUtil;
import javafx.stage.Stage;

public class UtilisateurController {

    // ===== Form =====
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private TextField codePostalField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleBox;


    // ===== Table =====
    @FXML private TableView<Utilisateur> table;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, Role> colRole;
    @FXML private TableColumn<Utilisateur, Void> colAction;

    @FXML
    private ImageView logoImage;

    private final UtilisateurService service = new UtilisateurService();

    private Utilisateur selectedUtilisateur = null;

    @FXML
    private Button logoutButton;

    @FXML
    private void logout() {

        Stage stage = (Stage) logoutButton.getScene().getWindow();
        LogoutUtil.logout(stage);
    }

    @FXML
    public void initialize() {

        Image logo = new Image(
                getClass().getResource("/images/logo.png").toExternalForm()
        );

        logoImage.setImage(logo);

        // ComboBox enum
        roleBox.setItems(FXCollections.observableArrayList(Role.values()));

        // Columns
        colNom.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNom()));

        colPrenom.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPrenom()));

        colRole.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getRole()));

        // Action column (Delete button)
        addDeleteButtonToTable();

        refreshTable();

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedUtilisateur = newSelection;
                        remplirFormulaire(newSelection);
                    }
                });

    }

    @FXML
    private void ajouterUtilisateur() {

        try {

            // Basic front validation
            if (nomField.getText().isEmpty()
                    || prenomField.getText().isEmpty()
                    || emailField.getText().isEmpty()
                    || telephoneField.getText().isEmpty()
                    || passwordField.getText().isEmpty()
                    || roleBox.getValue() == null) {

                showAlert(Alert.AlertType.WARNING,
                        "Champs manquants",
                        "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            Utilisateur u = new Utilisateur();
            u.setNom(nomField.getText());
            u.setPrenom(prenomField.getText());
            u.setEmail(emailField.getText());
            u.setTelephone(telephoneField.getText());
            u.setAdresse(adresseField.getText());
            u.setVille(villeField.getText());
            u.setCode_postal(codePostalField.getText());
            u.setRole(roleBox.getValue());
            u.setMot_de_passe(passwordField.getText());
            u.setDate_inscription(new Date());

            service.ajouterUtilisateur(u);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Utilisateur ajouté avec succès ✅");

            refreshTable();
            clearForm();

        } catch (IllegalArgumentException e) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur de saisie",
                    e.getMessage());

        } catch (Exception e) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Une erreur inattendue est survenue.");
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierUtilisateur() {

        if (selectedUtilisateur == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Sélection requise",
                    "Veuillez sélectionner un utilisateur à modifier.");
            return;
        }

        try {

            selectedUtilisateur.setNom(nomField.getText());
            selectedUtilisateur.setPrenom(prenomField.getText());
            selectedUtilisateur.setEmail(emailField.getText());
            selectedUtilisateur.setTelephone(telephoneField.getText());
            selectedUtilisateur.setAdresse(adresseField.getText());
            selectedUtilisateur.setVille(villeField.getText());
            selectedUtilisateur.setCode_postal(codePostalField.getText());
            selectedUtilisateur.setRole(roleBox.getValue());

            service.modifierUtilisateur(selectedUtilisateur);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Utilisateur modifié avec succès ✅");

            refreshTable();
            clearForm();
            selectedUtilisateur = null;

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Erreur lors de la modification.");
            e.printStackTrace();
        }
    }



    // ===== DELETE BUTTON LOGIC =====
    private void addDeleteButtonToTable() {

        colAction.setCellFactory(param -> new TableCell<>() {

            private final Button deleteBtn = new Button("🗑");

            {
                deleteBtn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-font-size: 14px;" +
                                "-fx-cursor: hand;"
                );

                deleteBtn.setOnAction(event -> {

                    Utilisateur u = getTableView().getItems().get(getIndex());

                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Confirmation de suppression");
                    confirmation.setHeaderText("Suppression utilisateur");
                    confirmation.setContentText(
                            "Êtes-vous sûr de vouloir supprimer l'utilisateur :\n\n"
                                    + u.getNom() + " " + u.getPrenom() + " ?"
                    );

                    ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
                    ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

                    confirmation.getButtonTypes().setAll(yesButton, cancelButton);

                    Optional<ButtonType> result = confirmation.showAndWait();

                    if (result.isPresent() && result.get() == yesButton) {

                        service.supprimerUtilisateur(u.getId_utilisateur());

                        showAlert(Alert.AlertType.INFORMATION,
                                "Suppression réussie",
                                "Utilisateur supprimé avec succès ✅");

                        refreshTable();
                    }
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(
                service.afficherUtilisateurs()
        ));
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        adresseField.clear();
        villeField.clear();
        codePostalField.clear();
        passwordField.clear();
        roleBox.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void remplirFormulaire(Utilisateur u) {

        nomField.setText(u.getNom());
        prenomField.setText(u.getPrenom());
        emailField.setText(u.getEmail());
        telephoneField.setText(u.getTelephone());
        adresseField.setText(u.getAdresse());
        villeField.setText(u.getVille());
        codePostalField.setText(u.getCode_postal());
        roleBox.setValue(u.getRole());
    }


}
