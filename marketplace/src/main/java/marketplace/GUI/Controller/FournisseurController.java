package marketplace.GUI.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import marketplace.entities.Fournisseur;
import marketplace.service.FournisseurService;
import marketplace.tools.MapPicker;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FournisseurController implements Initializable {

    // --- Service ---
    private final FournisseurService fournisseurService = new FournisseurService();

    // --- TabPane ---
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabCreer;
    @FXML private Tab tabModifier;
    @FXML private Tab tabListe;

    // --- CREATE Tab ---
    @FXML private TextField txtNomEntreprise;
    @FXML private TextField txtContactNom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private CheckBox chkActif;

    // --- EDIT Tab ---
    @FXML private TextField txtSearchModif;
    @FXML private TextField txtNomEntrepriseModif;
    @FXML private TextField txtContactNomModif;
    @FXML private TextField txtEmailModif;
    @FXML private TextField txtTelephoneModif;
    @FXML private TextField txtAdresseModif;
    @FXML private TextField txtVilleModif;
    @FXML private CheckBox chkActifModif;

    private Fournisseur currentEditingFournisseur;

    // --- LIST Tab ---
    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Integer> colId;
    @FXML private TableColumn<Fournisseur, String> colNomEntreprise;
    @FXML private TableColumn<Fournisseur, String> colContactNom;
    @FXML private TableColumn<Fournisseur, String> colEmail;
    @FXML private TableColumn<Fournisseur, String> colTelephone;
    @FXML private TableColumn<Fournisseur, Boolean> colActif;
    @FXML private TableColumn<Fournisseur, Void> colActions;
    
    @FXML private TextField txtSearchList;
    private List<Fournisseur> allFournisseurs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadTableData();
        setupSearchFilter();

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == tabListe) {
                loadTableData();
            }
        });
    }
    
    private void setupSearchFilter() {
        if (txtSearchList != null) {
            txtSearchList.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTable(newValue);
            });
        }
    }
    
    private void filterTable(String query) {
        if (allFournisseurs == null) return;
        
        if (query == null || query.trim().isEmpty()) {
            tableFournisseurs.getItems().setAll(allFournisseurs);
        } else {
            String lowerQuery = query.toLowerCase();
            List<Fournisseur> filtered = allFournisseurs.stream()
                .filter(f -> 
                    (f.getNomEntreprise() != null && f.getNomEntreprise().toLowerCase().contains(lowerQuery)) ||
                    (f.getContactNom() != null && f.getContactNom().toLowerCase().contains(lowerQuery)) ||
                    (f.getEmail() != null && f.getEmail().toLowerCase().contains(lowerQuery)) ||
                    (f.getTelephone() != null && f.getTelephone().toLowerCase().contains(lowerQuery)) ||
                    (f.getVille() != null && f.getVille().toLowerCase().contains(lowerQuery)) ||
                    String.valueOf(f.getId()).contains(lowerQuery)
                )
                .collect(java.util.stream.Collectors.toList());
            tableFournisseurs.getItems().setAll(filtered);
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomEntreprise.setCellValueFactory(new PropertyValueFactory<>("nomEntreprise"));
        colContactNom.setCellValueFactory(new PropertyValueFactory<>("contactNom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        
        // Format actif column with checkmark
        colActif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText(null);
                } else {
                    setText(actif ? "✓ Oui" : "✗ Non");
                    setStyle(actif ? "-fx-text-fill: #49ad32;" : "-fx-text-fill: #ef5350;");
                }
            }
        });

        // Actions Column
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Fournisseur, Void> call(final TableColumn<Fournisseur, Void> param) {
                return new TableCell<>() {
                    private final Button btnView = new Button("Détails");
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprimer");
                    private final HBox pane = new HBox(8, btnView, btnEdit, btnDelete);

                    {
                        btnView.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
                        btnEdit.setStyle("-fx-background-color: #ffa726; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");

                        btnView.setOnMouseEntered(e -> btnView.setStyle("-fx-background-color: #3d9129; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;"));
                        btnView.setOnMouseExited(e -> btnView.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;"));

                        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #fb8c00; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;"));
                        btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #ffa726; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;"));

                        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;"));
                        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;"));

                        btnDelete.setOnAction(event -> {
                            Fournisseur fournisseur = getTableView().getItems().get(getIndex());
                            handleDelete(fournisseur);
                        });

                        btnEdit.setOnAction(event -> {
                            Fournisseur fournisseur = getTableView().getItems().get(getIndex());
                            prepareEdit(fournisseur);
                        });

                        btnView.setOnAction(event -> {
                            Fournisseur fournisseur = getTableView().getItems().get(getIndex());
                            showDetails(fournisseur);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
    }

    private void loadTableData() {
        try {
            allFournisseurs = fournisseurService.getEntities();
            System.out.println("Fournisseurs chargés: " + allFournisseurs.size());
            tableFournisseurs.getItems().clear();
            tableFournisseurs.getItems().addAll(allFournisseurs);
            tableFournisseurs.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des fournisseurs.");
        }
    }

    // --- Handlers ---

    @FXML
    void handleAjouter(ActionEvent event) {
        if (!validateForm(txtNomEntreprise, txtContactNom, txtEmail, txtTelephone)) {
            return;
        }
        
        if (!validateTelephone(txtTelephone.getText())) {
            return;
        }
        
        if (!validateEmail(txtEmail.getText())) {
            return;
        }

        try {
            Fournisseur fournisseur = new Fournisseur(
                    txtNomEntreprise.getText(),
                    txtContactNom.getText(),
                    txtEmail.getText(),
                    txtTelephone.getText());

            fournisseur.setAdresse(txtAdresse.getText());
            fournisseur.setVille(txtVille.getText());
            fournisseur.setActif(chkActif.isSelected());

            fournisseurService.addEntity(fournisseur);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur ajouté avec succès.");
            clearCreateForm();
            loadTableData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    void handleRechercher(ActionEvent event) {
        String query = txtSearchModif.getText();
        if (query == null || query.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez saisir un terme de recherche.");
            return;
        }

        try {
            List<Fournisseur> allList = fournisseurService.getEntities();
            String queryLower = query.toLowerCase();
            List<Fournisseur> results = allList.stream()
                    .filter(f -> (f.getNomEntreprise() != null && f.getNomEntreprise().toLowerCase().contains(queryLower)) ||
                            (f.getContactNom() != null && f.getContactNom().toLowerCase().contains(queryLower)) ||
                            (f.getEmail() != null && f.getEmail().toLowerCase().contains(queryLower)))
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Aucun fournisseur trouvé.");
            } else {
                fillEditForm(results.get(0));
                if (results.size() > 1) {
                    showAlert(Alert.AlertType.INFORMATION, "Info", "Plusieurs résultats trouvés. Le premier a été chargé.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche.");
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (currentEditingFournisseur == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun fournisseur sélectionné pour modification.");
            return;
        }

        if (!validateForm(txtNomEntrepriseModif, txtContactNomModif, txtEmailModif, txtTelephoneModif)) {
            return;
        }
        
        if (!validateTelephone(txtTelephoneModif.getText())) {
            return;
        }
        
        if (!validateEmail(txtEmailModif.getText())) {
            return;
        }

        try {
            currentEditingFournisseur.setNomEntreprise(txtNomEntrepriseModif.getText());
            currentEditingFournisseur.setContactNom(txtContactNomModif.getText());
            currentEditingFournisseur.setEmail(txtEmailModif.getText());
            currentEditingFournisseur.setTelephone(txtTelephoneModif.getText());
            currentEditingFournisseur.setAdresse(txtAdresseModif.getText());
            currentEditingFournisseur.setVille(txtVilleModif.getText());
            currentEditingFournisseur.setActif(chkActifModif.isSelected());

            fournisseurService.updateEntity(currentEditingFournisseur);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur modifié avec succès.");
            loadTableData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        System.out.println("=== REFRESH Fournisseurs cliqué ===");
        loadTableData();
    }

    // --- Helpers ---

    private void handleDelete(Fournisseur fournisseur) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + fournisseur.getNomEntreprise() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                fournisseurService.deleteEntity(fournisseur);
                tableFournisseurs.getItems().remove(fournisseur);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le fournisseur.");
            }
        }
    }

    private void prepareEdit(Fournisseur fournisseur) {
        fillEditForm(fournisseur);
        mainTabPane.getSelectionModel().select(tabModifier);
    }

    private void fillEditForm(Fournisseur fournisseur) {
        this.currentEditingFournisseur = fournisseur;

        txtNomEntrepriseModif.setText(fournisseur.getNomEntreprise());
        txtContactNomModif.setText(fournisseur.getContactNom());
        txtEmailModif.setText(fournisseur.getEmail());
        txtTelephoneModif.setText(fournisseur.getTelephone());
        txtAdresseModif.setText(fournisseur.getAdresse());
        txtVilleModif.setText(fournisseur.getVille());
        chkActifModif.setSelected(fournisseur.isActif());
    }

    private void showDetails(Fournisseur fournisseur) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du fournisseur");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 25, 20, 25));
        grid.setStyle("-fx-background-color: white;");

        int row = 0;

        Label titleLabel = new Label(fournisseur.getNomEntreprise());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        grid.add(titleLabel, 0, row, 2, 1);
        GridPane.setMargin(titleLabel, new Insets(0, 0, 15, 0));
        row++;

        addDetailRow(grid, row++, "ID:", String.valueOf(fournisseur.getId()));
        addDetailRow(grid, row++, "Nom du contact:", fournisseur.getContactNom() != null ? fournisseur.getContactNom() : "-");
        addDetailRow(grid, row++, "Email:", fournisseur.getEmail() != null ? fournisseur.getEmail() : "-");
        addDetailRow(grid, row++, "Téléphone:", fournisseur.getTelephone() != null ? fournisseur.getTelephone() : "-");
        addDetailRow(grid, row++, "Adresse:", fournisseur.getAdresse() != null ? fournisseur.getAdresse() : "-");
        addDetailRow(grid, row++, "Ville:", fournisseur.getVille() != null ? fournisseur.getVille() : "-");
        addDetailRow(grid, row++, "Statut:", fournisseur.isActif() ? "✓ Actif" : "✗ Inactif");

        if (fournisseur.getDateCreation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            addDetailRow(grid, row++, "Date création:", fournisseur.getDateCreation().format(formatter));
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 13px;");
        grid.add(lblLabel, 0, row);

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: #333; -fx-font-size: 13px;");
        grid.add(lblValue, 1, row);
    }

    private boolean validateForm(TextField nomEntreprise, TextField contactNom, TextField email, TextField telephone) {
        if (nomEntreprise.getText().isEmpty() || contactNom.getText().isEmpty() ||
                email.getText().isEmpty() || telephone.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        return true;
    }

    private boolean validateTelephone(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) {
            return true;
        }
        // Format: xx xxx xxx or +216 xx xxx xxx
        String pattern1 = "^\\d{2}\\s?\\d{3}\\s?\\d{3}$";
        String pattern2 = "^\\+216\\s?\\d{2}\\s?\\d{3}\\s?\\d{3}$";
        if (!telephone.matches(pattern1) && !telephone.matches(pattern2)) {
            showAlert(Alert.AlertType.WARNING, "Téléphone invalide", 
                "Le numéro de téléphone doit être au format: xx xxx xxx ou +216 xx xxx xxx");
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        // Standard email format validation
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailPattern)) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", 
                "Veuillez saisir une adresse email valide (ex: exemple@domaine.com)");
            return false;
        }
        return true;
    }

    private void clearCreateForm() {
        txtNomEntreprise.clear();
        txtContactNom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtVille.clear();
        chkActif.setSelected(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleOpenMapCreate(ActionEvent event) {
        MapPicker mapPicker = new MapPicker();
        MapPicker.AddressResult result = mapPicker.showAndWait(
            (Stage) txtVille.getScene().getWindow(),
            txtAdresse.getText(),
            txtVille.getText()
        );
        
        if (result.isConfirmed()) {
            txtAdresse.setText(result.getAddress());
            txtVille.setText(result.getCity());
        }
    }
    
    @FXML
    private void handleOpenMapModif(ActionEvent event) {
        MapPicker mapPicker = new MapPicker();
        MapPicker.AddressResult result = mapPicker.showAndWait(
            (Stage) txtVilleModif.getScene().getWindow(),
            txtAdresseModif.getText(),
            txtVilleModif.getText()
        );
        
        if (result.isConfirmed()) {
            txtAdresseModif.setText(result.getAddress());
            txtVilleModif.setText(result.getCity());
        }
    }
}
