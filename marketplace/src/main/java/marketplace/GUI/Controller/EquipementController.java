package marketplace.GUI.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import marketplace.entities.Categorie;
import marketplace.entities.Equipement;
import marketplace.entities.Fournisseur;
import marketplace.entities.ProductType;
import marketplace.service.CategorieService;
import marketplace.service.EquipementService;
import marketplace.service.FournisseurService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EquipementController implements Initializable {

    // --- Services ---
    private final EquipementService equipementService = new EquipementService();
    private final CategorieService categorieService = new CategorieService();
    private final FournisseurService fournisseurService = new FournisseurService();

    // --- TabPane ---
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab tabCreer;
    @FXML
    private Tab tabModifier;
    @FXML
    private Tab tabListe;

    // --- CREATE Tab ---
    @FXML
    private TextField txtNom;
    @FXML
    private ComboBox<Categorie> cbCategorie;
    @FXML
    private ComboBox<Fournisseur> cbFournisseur;
    @FXML
    private TextField txtPrixAchat;
    @FXML
    private TextField txtPrixVente;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TextField txtSeuil;
    @FXML
    private TextField txtImage;
    @FXML
    private TextArea txtDescription;
    @FXML
    private CheckBox chkDisponible;

    // --- EDIT Tab ---
    @FXML
    private TextField txtSearchModif;
    @FXML
    private TextField txtNomModif;
    @FXML
    private ComboBox<Categorie> cbCategorieModif;
    @FXML
    private ComboBox<Fournisseur> cbFournisseurModif;
    @FXML
    private TextField txtPrixAchatModif;
    @FXML
    private TextField txtPrixVenteModif;
    @FXML
    private TextField txtQuantiteModif;
    @FXML
    private TextField txtSeuilModif;
    @FXML
    private TextField txtImageModif;
    @FXML
    private TextArea txtDescriptionModif;
    @FXML
    private CheckBox chkDisponibleModif;

    private Equipement currentEditingEquipement; // To hold the ID and other unchanged data

    // --- LIST Tab ---
    @FXML
    private TableView<Equipement> tableEquipements;
    @FXML
    private TableColumn<Equipement, Integer> colId;
    @FXML
    private TableColumn<Equipement, String> colImage; // Will hold URL but display image
    @FXML
    private TableColumn<Equipement, String> colNom;
    @FXML
    private TableColumn<Equipement, BigDecimal> colPrix;
    @FXML
    private TableColumn<Equipement, Integer> colQuantite;
    @FXML
    private TableColumn<Equipement, Void> colActions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadComboBoxes();
        setupTable();
        loadTableData();

        // Listen for tab selection to refresh list if needed
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == tabListe) {
                loadTableData();
            }
        });
    }

    private void loadComboBoxes() {
        try {
            // Categories
            List<Categorie> categories = categorieService.getCategoriesByType(ProductType.EQUIPEMENT);
            ObservableList<Categorie> obsCategories = FXCollections.observableArrayList(categories);
            cbCategorie.setItems(obsCategories);
            cbCategorieModif.setItems(obsCategories);

            // Suppliers
            List<Fournisseur> fournisseurs = fournisseurService.getActiveFournisseurs();
            ObservableList<Fournisseur> obsFournisseurs = FXCollections.observableArrayList(fournisseurs);
            cbFournisseur.setItems(obsFournisseurs);
            cbFournisseurModif.setItems(obsFournisseurs);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données des formulaires.");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixVente"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        // Image Column
        colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        // Basic check if it is a URL or local path.
                        // For simplicity, assuming valid URL or classpath resource if starts with /
                        Image image = new Image(imageUrl, 50, 50, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        // Fallback or ignore
                        setGraphic(null);
                    }
                }
            }
        });

        // Actions Column
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Equipement, Void> call(final TableColumn<Equipement, Void> param) {
                return new TableCell<>() {
                    private final Button btnView = new Button("Détails");
                    private final Button btnEdit = new Button("Modifier");
                    private final Button btnDelete = new Button("Supprimer");
                    private final HBox pane = new HBox(8, btnView, btnEdit, btnDelete);

                    {
                        // Modern button styling
                        btnView.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
                        btnEdit.setStyle("-fx-background-color: #ffa726; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white; " +
                                "-fx-background-radius: 5; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");

                        // Hover effects
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
                            Equipement equipement = getTableView().getItems().get(getIndex());
                            handleDelete(equipement);
                        });

                        btnEdit.setOnAction(event -> {
                            Equipement equipement = getTableView().getItems().get(getIndex());
                            prepareEdit(equipement);
                        });

                        btnView.setOnAction(event -> {
                            Equipement equipement = getTableView().getItems().get(getIndex());
                            showDetails(equipement);
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
            List<Equipement> list = equipementService.getEntities();
            tableEquipements.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des équipements.");
        }
    }

    // --- Handlers ---

    @FXML
    void handleAjouter(ActionEvent event) {
        if (!validateForm(txtNom, cbCategorie, cbFournisseur, txtPrixAchat, txtPrixVente, txtQuantite)) {
            return;
        }

        try {
            Equipement equipement = new Equipement(
                    cbCategorie.getValue().getId(),
                    cbFournisseur.getValue().getId(),
                    txtNom.getText(),
                    txtDescription.getText(),
                    new BigDecimal(txtPrixAchat.getText()),
                    new BigDecimal(txtPrixVente.getText()),
                    Integer.parseInt(txtQuantite.getText()));
            equipement.setSeuilAlerte(Integer.parseInt(txtSeuil.getText()));
            equipement.setImageUrl(txtImage.getText());
            equipement.setDisponible(chkDisponible.isSelected());

            equipementService.addEntity(equipement);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Équipement ajouté avec succès.");
            clearCreateForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "Veuillez vérifier les champs numériques (Prix, Quantité).");
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
            List<Equipement> results = equipementService.searchByName(query);
            if (results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Aucun équipement trouvé.");
            } else if (results.size() == 1) {
                // Determine logic: autofill if only one? Or ask user to prefer list selection?
                // Per requirement: "il recherche un élément et les champs se chargent
                // automatiquement"
                // If multiple found, maybe show a dialog or list?
                // For simplicity, lets use the first one or a clear log.
                fillEditForm(results.get(0));
            } else {
                // Multiple results: Pick first or warn.
                // Ideally, a small selector dialog. stick to first for MVP or Alert.
                // Or better: Let user know multiple found.
                // "Found X items. Loading the first one."
                fillEditForm(results.get(0));
                showAlert(Alert.AlertType.INFORMATION, "Info", "Plusieurs résultats trouvés. Le premier a été chargé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche.");
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (currentEditingEquipement == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun équipement sélectionné pour modification.");
            return;
        }

        if (!validateForm(txtNomModif, cbCategorieModif, cbFournisseurModif, txtPrixAchatModif, txtPrixVenteModif,
                txtQuantiteModif)) {
            return;
        }

        try {
            // Update entity object
            currentEditingEquipement.setNom(txtNomModif.getText());
            currentEditingEquipement.setCategorieId(cbCategorieModif.getValue().getId());
            currentEditingEquipement.setFournisseurId(cbFournisseurModif.getValue().getId());
            currentEditingEquipement.setDescription(txtDescriptionModif.getText());
            currentEditingEquipement.setPrixAchat(new BigDecimal(txtPrixAchatModif.getText()));
            currentEditingEquipement.setPrixVente(new BigDecimal(txtPrixVenteModif.getText()));
            currentEditingEquipement.setQuantiteStock(Integer.parseInt(txtQuantiteModif.getText()));
            currentEditingEquipement.setSeuilAlerte(Integer.parseInt(txtSeuilModif.getText()));
            currentEditingEquipement.setImageUrl(txtImageModif.getText());
            currentEditingEquipement.setDisponible(chkDisponibleModif.isSelected());

            equipementService.updateEntity(currentEditingEquipement);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Équipement modifié avec succès.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez vérifier les champs numériques.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données",
                    "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadTableData();
    }

    // --- Helpers ---

    private void handleDelete(Equipement equipement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + equipement.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                equipementService.deleteEntity(equipement);
                tableEquipements.getItems().remove(equipement);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'équipement.");
            }
        }
    }

    private void prepareEdit(Equipement equipement) {
        fillEditForm(equipement);
        mainTabPane.getSelectionModel().select(tabModifier);
    }

    private void fillEditForm(Equipement equipement) {
        this.currentEditingEquipement = equipement;

        txtNomModif.setText(equipement.getNom());
        txtDescriptionModif.setText(equipement.getDescription());
        txtPrixAchatModif.setText(equipement.getPrixAchat().toString());
        txtPrixVenteModif.setText(equipement.getPrixVente().toString());
        txtQuantiteModif.setText(String.valueOf(equipement.getQuantiteStock()));
        txtSeuilModif.setText(String.valueOf(equipement.getSeuilAlerte()));
        txtImageModif.setText(equipement.getImageUrl());
        chkDisponibleModif.setSelected(equipement.isDisponible());

        // Set ComboBox selections
        // Need to find the Categorie object in the list that matches ID
        for (Categorie c : cbCategorieModif.getItems()) {
            if (c.getId() == equipement.getCategorieId()) {
                cbCategorieModif.setValue(c);
                break;
            }
        }
        // Need to find the Fournisseur object in the list that matches ID
        for (Fournisseur f : cbFournisseurModif.getItems()) {
            if (f.getId() == equipement.getFournisseurId()) {
                cbFournisseurModif.setValue(f);
                break;
            }
        }
    }

    private void showDetails(Equipement equipement) {
        // Create custom dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'équipement");
        dialog.setHeaderText(null);

        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20, 25, 20, 25));
        grid.setStyle("-fx-background-color: white;");

        // Add image if available
        int row = 0;
        if (equipement.getImageUrl() != null && !equipement.getImageUrl().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(equipement.getImageUrl(), 150, 150, true, true));
                imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);");
                grid.add(imageView, 0, row, 2, 1);
                GridPane.setHalignment(imageView, javafx.geometry.HPos.CENTER);
                row++;
            } catch (Exception e) {
                // Image load failed, skip
            }
        }

        // Title
        Label titleLabel = new Label(equipement.getNom());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        grid.add(titleLabel, 0, row, 2, 1);
        GridPane.setMargin(titleLabel, new javafx.geometry.Insets(10, 0, 10, 0));
        row++;

        // Details
        addDetailRow(grid, row++, "ID:", String.valueOf(equipement.getId()));
        addDetailRow(grid, row++, "Prix d'achat:", equipement.getPrixAchat() + " €");
        addDetailRow(grid, row++, "Prix de vente:", equipement.getPrixVente() + " €");
        addDetailRow(grid, row++, "Stock disponible:", String.valueOf(equipement.getQuantiteStock()));
        addDetailRow(grid, row++, "Seuil d'alerte:", String.valueOf(equipement.getSeuilAlerte()));
        addDetailRow(grid, row++, "Disponibilité:", equipement.isDisponible() ? "✓ Disponible" : "✗ Indisponible");

        // Description
        if (equipement.getDescription() != null && !equipement.getDescription().isEmpty()) {
            Label descLabel = new Label("Description:");
            descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 13px;");
            grid.add(descLabel, 0, row, 2, 1);
            GridPane.setMargin(descLabel, new javafx.geometry.Insets(10, 0, 5, 0));
            row++;

            Label descValue = new Label(equipement.getDescription());
            descValue.setWrapText(true);
            descValue.setMaxWidth(400);
            descValue.setStyle("-fx-text-fill: #666; -fx-font-size: 13px; -fx-padding: 10; " +
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 6;");
            grid.add(descValue, 0, row, 2, 1);
            row++;
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Style the dialog
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

    private boolean validateForm(TextField nom, ComboBox<?> cat, ComboBox<?> fourn, TextField pAchat, TextField pVente,
            TextField quantite) {
        if (nom.getText().isEmpty() || cat.getValue() == null || fourn.getValue() == null ||
                pAchat.getText().isEmpty() || pVente.getText().isEmpty() || quantite.getText().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        return true;
    }

    private void clearCreateForm() {
        txtNom.clear();
        txtDescription.clear();
        txtPrixAchat.clear();
        txtPrixVente.clear();
        txtQuantite.clear();
        txtSeuil.setText("5");
        txtImage.clear();
        chkDisponible.setSelected(true);
        cbCategorie.getSelectionModel().clearSelection();
        cbFournisseur.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
