package com.examen.firmapi.controllers;

import com.examen.firmapi.entities.Profile;
import com.examen.firmapi.entities.Utilisateur;
import com.examen.firmapi.entities.Genre;
import com.examen.firmapi.services.UtilisateurService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class EditProfileController {

    private Utilisateur currentUser;
    private Profile currentProfile;
    private final UtilisateurService service = new UtilisateurService();

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private TextField codePostalField;
    @FXML private TextField paysField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextArea bioArea;
    @FXML private ComboBox<Genre> genreCombo;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private ImageView profilePreview;

    private String selectedImagePath;

    public void setUser(Utilisateur user) {
        this.currentUser = user;
        loadUserData();
    }

    private void loadUserData() {

        // ===== Load basic user data =====
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        adresseField.setText(currentUser.getAdresse());
        villeField.setText(currentUser.getVille());
        codePostalField.setText(currentUser.getCode_postal());

        // ===== Load profile from DB FIRST =====
        currentProfile = service.getProfileByUserId(currentUser.getId_utilisateur());

        // Safety: if profile does not exist
        if (currentProfile == null) {
            currentProfile = new Profile();
            currentProfile.setId_utilisateur(currentUser.getId_utilisateur());
        }

        // ===== Load picture =====
        if (currentProfile.getPhoto_profil() != null) {
            selectedImagePath = currentProfile.getPhoto_profil();
            File file = new File(selectedImagePath);

            if (file.exists()) {
                profilePreview.setImage(new Image(file.toURI().toString()));
            }
        }

        // ===== Load other profile fields =====
        bioArea.setText(currentProfile.getBio());
        paysField.setText(currentProfile.getPays());
        villeField.setText(currentProfile.getVille());

        if (currentProfile.getDate_naissance() != null) {
            dateNaissancePicker.setValue(currentProfile.getDate_naissance());
        }

        genreCombo.getItems().setAll(Genre.values());

        if (currentProfile.getGenre() != null) {
            genreCombo.setValue(currentProfile.getGenre());
        }

        // ===== Button actions =====
        saveBtn.setOnAction(e -> saveProfile());
        cancelBtn.setOnAction(e -> closeWindow());
    }

    private void saveProfile() {
        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setTelephone(telephoneField.getText());
        currentUser.setAdresse(adresseField.getText());
        currentUser.setVille(villeField.getText());
        currentUser.setCode_postal(codePostalField.getText());

        currentProfile.setPhoto_profil(selectedImagePath);
        currentProfile.setBio(bioArea.getText());
        currentProfile.setPays(paysField.getText());
        currentProfile.setVille(villeField.getText());
        currentProfile.setDate_naissance(dateNaissancePicker.getValue());
        currentProfile.setGenre(genreCombo.getValue());

        service.modifierUtilisateur(currentUser);
        service.updateProfile(currentProfile);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully");
        alert.showAndWait();
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleChoosePicture() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(saveBtn.getScene().getWindow());

        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            profilePreview.setImage(new Image(file.toURI().toString()));
        }
    }
}