package edu.connection3a7.controller;

import edu.connection3a7.entities.Technicien;
import edu.connection3a7.entities.Coordonnees;
import edu.connection3a7.service.GeocodageService;
import edu.connection3a7.service.LocalisationTechnicienService;
import edu.connection3a7.service.Technicienserv;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class AjouterTechnicienController implements Initializable {

    // ========== CHAMPS DU FORMULAIRE ==========
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCin;
    @FXML private TextField txtAge;
    @FXML private ComboBox<String> comboSpecialite;
    @FXML private TextField txtLocalisation;
    @FXML private CheckBox checkDisponibilite;
    @FXML private DatePicker dateNaissance;

    // ========== COMPOSANTS IMAGE ==========
    @FXML private ImageView imagePreview;
    @FXML private Button btnChoisirImage;
    @FXML private Button btnSupprimerImage;
    @FXML private Label lblNomImage;

    // ========== BOUTONS ==========
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnVoirListe;      // Bouton Voir Liste (back)
    @FXML private Button btnVoirFront;      // Bouton Voir Front (client)
    @FXML private Button btnAccueil;         // Bouton Accueil

    // ========== SERVICES ==========
    private Technicienserv service = new Technicienserv();
    private GeocodageService geocodageService = new GeocodageService();
    private LocalisationTechnicienService localisationService = new LocalisationTechnicienService();

    // ========== VARIABLES ==========
    private File selectedImageFile;
    private Technicien technicienAModifier = null;
    private boolean isUpdating = false;
    private static final String IMAGE_DIRECTORY = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AjouterTechnicienController initialisé");

        creerDossierImages();
        initialiserComboSpecialite();
        chargerImageDefaut();
        setupAgeDateListeners();

        btnSupprimerImage.setDisable(true);
        checkDisponibilite.setSelected(true);

        // Configuration des boutons de navigation
        configurerBoutons();
    }

    private void creerDossierImages() {
        File dossier = new File(IMAGE_DIRECTORY);
        if (!dossier.exists()) {
            dossier.mkdirs();
            System.out.println("📁 Dossier images créé: " + IMAGE_DIRECTORY);
        }
    }

    private void initialiserComboSpecialite() {
        comboSpecialite.getItems().addAll(
                "Informatique", "Réseau", "Matériel", "Maintenance",
                "Électronique", "Plomberie", "Électricité", "Mécanique",
                "Sécurité", "Dépannage", "Installation", "Autre"
        );
    }

    private void configurerBoutons() {
        // Les boutons sont déjà liés via FXML avec onAction
        // Pas besoin de setOnAction ici
    }

    private void setupAgeDateListeners() {
        dateNaissance.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (!isUpdating && newDate != null) {
                isUpdating = true;
                int age = Period.between(newDate, LocalDate.now()).getYears();
                txtAge.setText(String.valueOf(age));
                isUpdating = false;
            }
        });

        txtAge.textProperty().addListener((obs, oldText, newText) -> {
            if (!isUpdating && newText != null && !newText.trim().isEmpty()) {
                try {
                    int age = Integer.parseInt(newText.trim());
                    if (age > 0 && age < 120) {
                        isUpdating = true;
                        LocalDate dateNaiss = LocalDate.now().minusYears(age);
                        dateNaissance.setValue(dateNaiss);
                        isUpdating = false;
                    }
                } catch (NumberFormatException e) {
                    // Ignorer
                }
            }
        });
    }

    private void chargerImageDefaut() {
        try {
            File defaultFile = new File(IMAGE_DIRECTORY + "avatar.png");
            if (defaultFile.exists()) {
                Image image = new Image(defaultFile.toURI().toString());
                imagePreview.setImage(image);
                return;
            }
            defaultFile = new File(IMAGE_DIRECTORY + "default-avatar.png");
            if (defaultFile.exists()) {
                Image image = new Image(defaultFile.toURI().toString());
                imagePreview.setImage(image);
                return;
            }
            imagePreview.setImage(null);
            imagePreview.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1;");
        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement image par défaut");
        }
    }

    @FXML
    private void parcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        String userHome = System.getProperty("user.home");
        File picturesDir = new File(userHome, "Pictures");
        if (picturesDir.exists()) {
            fileChooser.setInitialDirectory(picturesDir);
        }

        Stage stage = (Stage) btnChoisirImage.getScene().getWindow();
        File fichierChoisi = fileChooser.showOpenDialog(stage);

        if (fichierChoisi != null) {
            selectedImageFile = fichierChoisi;

            try {
                Image image = new Image(fichierChoisi.toURI().toString());
                imagePreview.setImage(image);
                lblNomImage.setText("📷 " + fichierChoisi.getName());
                lblNomImage.setStyle("-fx-text-fill: #1a961e; -fx-font-weight: bold;");
                btnSupprimerImage.setDisable(false);
                System.out.println("✅ Image sélectionnée: " + fichierChoisi.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image");
            }
        }
    }

    @FXML
    private void supprimerImageSelectionnee() {
        selectedImageFile = null;
        chargerImageDefaut();
        lblNomImage.setText("Aucune image sélectionnée");
        lblNomImage.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        btnSupprimerImage.setDisable(true);
    }

    private String sauvegarderImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }

        String extension = getFileExtension(sourceFile.getName());
        String nomUnique = "tech_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        Path destination = Paths.get(IMAGE_DIRECTORY + nomUnique);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✅ Image copiée: " + sourceFile.getName() + " → " + nomUnique);
        return nomUnique;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return (lastDot == -1) ? ".png" : fileName.substring(lastDot);
    }

    @FXML
    private void ajouterTechnicien() {
        enregistrerTechnicien();
    }

    @FXML
    private void enregistrerTechnicien() {
        if (!validerChamps()) {
            return;
        }

        try {
            String nomImage = null;
            if (selectedImageFile != null) {
                nomImage = sauvegarderImage(selectedImageFile);
            } else if (technicienAModifier != null && technicienAModifier.getImage() != null) {
                nomImage = technicienAModifier.getImage();
            }

            if (technicienAModifier == null) {
                ajouterNouveauTechnicien(nomImage);
            } else {
                modifierTechnicienExistant(nomImage);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ajouterNouveauTechnicien(String nomImage) throws SQLException {
        Technicien tech = new Technicien();

        tech.setNom(txtNom.getText().trim());
        tech.setPrenom(txtPrenom.getText().trim());
        tech.setEmail(txtEmail.getText().trim());
        tech.setTelephone(txtTelephone.getText().trim());
        tech.setCin(txtCin.getText().trim());
        tech.setAge(Integer.parseInt(txtAge.getText().trim()));
        tech.setSpecialite(comboSpecialite.getValue());
        tech.setLocalisation(txtLocalisation.getText().trim());
        tech.setDisponibilite(checkDisponibilite.isSelected());
        tech.setDateNaissance(dateNaissance.getValue());
        tech.setImage(nomImage);

        service.addentitiy(tech);
        mettreAJourCoordonneesDepuisAdresse(tech.getId_tech(), tech.getLocalisation());

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "✅ Technicien ajouté avec succès !\nID: " + tech.getId_tech() +
                        "\nNom: " + tech.getPrenom() + " " + tech.getNom());

        voirListe();
    }

    private void modifierTechnicienExistant(String nomImage) throws SQLException {
        technicienAModifier.setNom(txtNom.getText().trim());
        technicienAModifier.setPrenom(txtPrenom.getText().trim());
        technicienAModifier.setEmail(txtEmail.getText().trim());
        technicienAModifier.setTelephone(txtTelephone.getText().trim());
        technicienAModifier.setCin(txtCin.getText().trim());
        technicienAModifier.setAge(Integer.parseInt(txtAge.getText().trim()));
        technicienAModifier.setSpecialite(comboSpecialite.getValue());
        technicienAModifier.setLocalisation(txtLocalisation.getText().trim());
        technicienAModifier.setDisponibilite(checkDisponibilite.isSelected());
        technicienAModifier.setDateNaissance(dateNaissance.getValue());
        technicienAModifier.setImage(nomImage);

        service.update(technicienAModifier);
        mettreAJourCoordonneesDepuisAdresse(
                technicienAModifier.getId_tech(),
                technicienAModifier.getLocalisation()
        );

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "✅ Technicien modifié avec succès !\n" +
                        technicienAModifier.getPrenom() + " " + technicienAModifier.getNom());

        voirListe();
    }

    private boolean validerChamps() {
        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            return false;
        }
        if (txtPrenom.getText() == null || txtPrenom.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le prénom est obligatoire");
            return false;
        }
        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'email est obligatoire");
            return false;
        }
        if (!txtEmail.getText().contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email invalide");
            return false;
        }
        if (comboSpecialite.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une spécialité");
            return false;
        }
        if (txtAge.getText() == null || txtAge.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'âge est obligatoire");
            return false;
        }
        try {
            int age = Integer.parseInt(txtAge.getText().trim());
            if (age <= 0 || age > 120) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'âge doit être entre 1 et 120 ans");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'âge doit être un nombre");
            return false;
        }
        if (dateNaissance.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de naissance est obligatoire");
            return false;
        }
        return true;
    }

    private void mettreAJourCoordonneesDepuisAdresse(int idTech, String adresse) {
        Optional<Coordonnees> resultat = geocodageService.geocoderAdresse(adresse);
        if (resultat.isEmpty()) {
            System.out.println("⚠️ Adresse non géocodée pour le technicien " + idTech + ": " + adresse);
            return;
        }

        Coordonnees coordonnees = resultat.get();
        localisationService.mettreAJourPosition(idTech, coordonnees.getLatitude(), coordonnees.getLongitude());
        System.out.println(String.format(
                "✅ Coordonnées mises à jour (tech %d): LAT=%.6f | LNG=%.6f",
                idTech,
                coordonnees.getLatitude(),
                coordonnees.getLongitude()
        ));
    }

    public void setTechnicienAModifier(Technicien tech) {
        this.technicienAModifier = tech;

        txtNom.setText(tech.getNom());
        txtPrenom.setText(tech.getPrenom());
        txtEmail.setText(tech.getEmail());
        txtTelephone.setText(tech.getTelephone());
        txtCin.setText(tech.getCin());
        txtAge.setText(String.valueOf(tech.getAge()));
        comboSpecialite.setValue(tech.getSpecialite());
        txtLocalisation.setText(tech.getLocalisation());
        checkDisponibilite.setSelected(tech.isDisponibilite());
        dateNaissance.setValue(tech.getDateNaissance());

        if (tech.getImage() != null && !tech.getImage().isEmpty()) {
            try {
                File imageFile = new File(IMAGE_DIRECTORY + tech.getImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imagePreview.setImage(image);
                    lblNomImage.setText("📷 " + tech.getImage());
                    lblNomImage.setStyle("-fx-text-fill: #1a961e; -fx-font-weight: bold;");
                    btnSupprimerImage.setDisable(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnEnregistrer.setText("✅ Modifier Technicien");

        System.out.println("✏️ Modification du technicien: " + tech.getPrenom() + " " + tech.getNom());
    }

    // ========== MÉTHODES DE NAVIGATION CORRIGÉES ==========

    /**
     * ✅ Aller vers la liste back (gestion technicien)
     */
    @FXML
    private void voirListe() {
        try {
            System.out.println("🔄 Navigation vers la liste BACK");

            // Le fichier BACK est dans /uploads/ListeDesTechniciens.fxml
            URL fxmlUrl = getClass().getResource("/uploads/ListeDesTechniciens.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier ListeDesTechniciens.fxml introuvable!");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des techniciens");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Aller vers la liste front (client)
     */
    @FXML
    private void allerVersFront() {
        try {
            System.out.println("🔄 Navigation vers la liste FRONT");

            // Le fichier FRONT est à la racine /ListeDesTechniciensfront.fxml
            URL fxmlUrl = getClass().getResource("/ListeDesTechniciensfront.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier ListeDesTechniciensfront.fxml introuvable!");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des techniciens - Client");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste front: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Aller vers l'accueil
     */
    @FXML
    private void allerAccueil() {
        try {
            System.out.println("🔄 Navigation vers l'accueil");

            URL fxmlUrl = getClass().getResource("/uploads/Accueil.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier Accueil.fxml introuvable!");
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'accueil");
            e.printStackTrace();
        }
    }

    /**
     * ✅ Annuler et retourner à la liste back
     */
    @FXML
    private void annuler() {
        voirListe();
    }

    /**
     * ✅ Afficher une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}