package Firma.controllers.GestionTechnicien;

import Firma.entities.GestionTechnicien.Technicien;
import Firma.entities.GestionTechnicien.Coordonnees;
import Firma.services.GestionTechnicien.GeocodageService;
import Firma.services.GestionTechnicien.LocalisationTechnicienService;
import Firma.services.GestionTechnicien.Technicienserv;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.Timer;

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
    @FXML private ListView<String> suggestionsListView;
    @FXML private CheckBox checkDisponibilite;
    @FXML private DatePicker dateNaissance;

    // ========== COMPOSANTS IMAGE ==========
    @FXML private ImageView imagePreview;
    @FXML private Button btnChoisirImage;
    @FXML private Button btnSupprimerImage;
    @FXML private Label lblNomImage;

    // ========== BOUTONS PARTAGE ==========
    @FXML private Button btnActiverPartage;
    @FXML private Button btnDesactiverPartage;
    @FXML private Label lblStatutPartage;

    // ========== BOUTONS PRINCIPAUX ==========
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnVoirListe;
    @FXML private Button btnVoirFront;
    @FXML private Button btnAccueil;

    // ========== SERVICES ==========
    private Technicienserv service = new Technicienserv();
    private GeocodageService geocodageService = new GeocodageService();
    private LocalisationTechnicienService localisationService = new LocalisationTechnicienService();

    // ========== VARIABLES ==========
    private File selectedImageFile;
    private Technicien technicienAModifier = null;
    private boolean isUpdating = false;
    private static final String IMAGE_DIRECTORY = "src/main/resources/images/";

    // ========== VARIABLES POUR LA RECHERCHE DE LOCALISATION ==========
    private List<String> suggestions = new ArrayList<>();
    private Timer searchTimer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AjouterTechnicienController initialisé");

        creerDossierImages();
        initialiserComboSpecialite();
        chargerImageDefaut();
        setupAgeDateListeners();
        setupLocalisationSearch();
        configurerBoutonsPartage();

        btnSupprimerImage.setDisable(true);
        checkDisponibilite.setSelected(true);
    }

    // ========== CONFIGURATION RECHERCHE LOCALISATION ==========
    private void setupLocalisationSearch() {
        txtLocalisation.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                suggestionsListView.setVisible(false);
                suggestionsListView.setManaged(false);
                return;
            }

            if (searchTimer != null) {
                searchTimer.cancel();
            }

            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        rechercherLocalisation(newVal);
                    });
                }
            }, 500);
        });

        suggestionsListView.setOnMouseClicked(e -> {
            String selected = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                txtLocalisation.setText(selected);
                suggestionsListView.setVisible(false);
                suggestionsListView.setManaged(false);
            }
        });
    }

    private void rechercherLocalisation(String query) {
        if (query.length() < 2) return; // Ignorer les recherches trop courtes

        try {
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + java.net.URLEncoder.encode(query, "UTF-8")
                    + "&format=json&limit=10&countrycodes=tn";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "FIRMA-Application/1.0")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenApply(java.net.http.HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            JsonNode root = objectMapper.readTree(response);

                            suggestions.clear();
                            for (JsonNode node : root) {
                                String displayName = node.get("display_name").asText();
                                String simplified = displayName.split(",")[0].trim();
                                suggestions.add(simplified);
                            }

                            javafx.application.Platform.runLater(() -> {
                                suggestionsListView.getItems().clear();
                                suggestionsListView.getItems().addAll(suggestions);

                                if (!suggestions.isEmpty()) {
                                    suggestionsListView.setVisible(true);
                                    suggestionsListView.setManaged(true);
                                } else {
                                    suggestionsListView.setVisible(false);
                                    suggestionsListView.setManaged(false);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("❌ Erreur de connexion: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== CONFIGURATION BOUTONS PARTAGE ==========
    private void configurerBoutonsPartage() {
        // Initialiser les boutons (par défaut partage activé)
        mettreAJourBoutonsPartage(true);

        btnActiverPartage.setOnAction(e -> activerPartage());
        btnDesactiverPartage.setOnAction(e -> desactiverPartage());
    }

    private void activerPartage() {
        mettreAJourBoutonsPartage(true);

        if (technicienAModifier != null) {
            try {
                localisationService.activerPartage(technicienAModifier.getId_tech(), true);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Partage activé pour " + technicienAModifier.getPrenom());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'activer le partage: " + e.getMessage());
            }
        }
    }

    private void desactiverPartage() {
        mettreAJourBoutonsPartage(false);

        if (technicienAModifier != null) {
            try {
                localisationService.activerPartage(technicienAModifier.getId_tech(), false);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Partage désactivé pour " + technicienAModifier.getPrenom());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de désactiver le partage: " + e.getMessage());
            }
        }
    }

    private void mettreAJourBoutonsPartage(boolean actif) {
        if (actif) {
            btnActiverPartage.setDisable(true);
            btnDesactiverPartage.setDisable(false);
            lblStatutPartage.setText("🟢 PARTAGE ACTIF");
            lblStatutPartage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            btnActiverPartage.setDisable(false);
            btnDesactiverPartage.setDisable(true);
            lblStatutPartage.setText("🔴 PARTAGE INACTIF");
            lblStatutPartage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
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

        // Tronquer la localisation si trop longue
        String localisation = txtLocalisation.getText().trim();
        if (localisation.length() > 255) {
            localisation = localisation.substring(0, 255);
            System.out.println("⚠️ Localisation tronquée à 255 caractères");
        }
        tech.setLocalisation(localisation);

        tech.setDisponibilite(checkDisponibilite.isSelected());
        tech.setDateNaissance(dateNaissance.getValue());
        tech.setImage(nomImage);

        service.addentitiy(tech);

        // Activer le partage par défaut
        localisationService.activerPartage(tech.getId_tech(), true);
        mettreAJourCoordonneesDepuisAdresse(tech.getId_tech(), tech.getLocalisation());

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "✅ Technicien ajouté avec succès !\nID: " + tech.getId_tech() +
                        "\nNom: " + tech.getPrenom() + " " + tech.getNom());

       // voirListe();
    }

    private void modifierTechnicienExistant(String nomImage) throws SQLException {
        technicienAModifier.setNom(txtNom.getText().trim());
        technicienAModifier.setPrenom(txtPrenom.getText().trim());
        technicienAModifier.setEmail(txtEmail.getText().trim());
        technicienAModifier.setTelephone(txtTelephone.getText().trim());
        technicienAModifier.setCin(txtCin.getText().trim());
        technicienAModifier.setAge(Integer.parseInt(txtAge.getText().trim()));
        technicienAModifier.setSpecialite(comboSpecialite.getValue());

        String localisation = txtLocalisation.getText().trim();
        if (localisation.length() > 255) {
            localisation = localisation.substring(0, 255);
        }
        technicienAModifier.setLocalisation(localisation);

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

       // voirListe();
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
            localisationService.mettreAJourPosition(idTech, 36.8065, 10.1815);
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

        // Vérifier l'état du partage
        try {
            boolean partageActif = localisationService.estPartageActif(tech.getId_tech());
            mettreAJourBoutonsPartage(partageActif);
        } catch (Exception e) {
            mettreAJourBoutonsPartage(false);
        }

        btnEnregistrer.setText("✅ Modifier Technicien");

        System.out.println("✏️ Modification du technicien: " + tech.getPrenom() + " " + tech.getNom());
    }

    // ========== MÉTHODES DE NAVIGATION ==========

    @FXML
    private void voirListe() {
        try {
            System.out.println("🔄 Navigation vers la liste BACK");

            // 1. Définition du chemin FXML
            URL fxmlUrl = getClass().getResource("/GestionTechnicien/ListeDesTechniciens.fxml");

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier ListeDesTechniciens.fxml introuvable !");
                return;
            }

            // 2. Chargement de la nouvelle interface
            Parent root = FXMLLoader.load(fxmlUrl);

            // 3. Récupération sécurisée du Stage (Fenêtre)
            // On essaie btnAnnuler, sinon btnEnregistrer, sinon btnVoirListe
            Stage stage = null;
            if (btnAnnuler != null && btnAnnuler.getScene() != null) {
                stage = (Stage) btnAnnuler.getScene().getWindow();
            } else if (btnEnregistrer != null && btnEnregistrer.getScene() != null) {
                stage = (Stage) btnEnregistrer.getScene().getWindow();
            } else if (btnVoirListe != null && btnVoirListe.getScene() != null) {
                stage = (Stage) btnVoirListe.getScene().getWindow();
            }

            // 4. Changement de scène si le stage est trouvé
            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Liste des Techniciens");
                stage.show();
            } else {
                System.err.println("❌ Erreur : Impossible de récupérer la fenêtre actuelle.");
                // Option de secours : créer une nouvelle fenêtre si l'actuelle est introuvable
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.show();
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void allerVersFront() {
        try {
            System.out.println("🔄 Navigation vers la liste FRONT");

            URL fxmlUrl = getClass().getResource("/GestionTechnicien/ListeDesTechniciensfront.fxml");

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

    @FXML
    private void allerAccueil() {
        try {
            System.out.println("🔄 Navigation vers l'accueil");

            URL fxmlUrl = getClass().getResource("/GestionEvenement/front.fxml");

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

    @FXML
    private void annuler() {
        voirListe();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}