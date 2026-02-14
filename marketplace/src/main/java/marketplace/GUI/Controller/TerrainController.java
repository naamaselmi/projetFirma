package marketplace.GUI.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import marketplace.entities.Categorie;
import marketplace.entities.ProductType;
import marketplace.entities.Terrain;
import marketplace.service.CategorieService;
import marketplace.service.TerrainService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {

    // --- Services ---
    private final TerrainService terrainService = new TerrainService();
    private final CategorieService categorieService = new CategorieService();

    // --- TabPane ---
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabCreer;
    @FXML private Tab tabModifier;
    @FXML private Tab tabListe;

    // --- CREATE Tab ---
    @FXML private TextField txtTitre;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField txtSuperficie;
    @FXML private TextField txtVille;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtPrixMois;
    @FXML private TextField txtPrixAnnee;
    @FXML private TextField txtImage;
    @FXML private TextArea txtDescription;
    @FXML private CheckBox chkDisponible;

    // --- EDIT Tab ---
    @FXML private TextField txtSearchModif;
    @FXML private TextField txtTitreModif;
    @FXML private ComboBox<Categorie> cbCategorieModif;
    @FXML private TextField txtSuperficieModif;
    @FXML private TextField txtVilleModif;
    @FXML private TextField txtAdresseModif;
    @FXML private TextField txtPrixMoisModif;
    @FXML private TextField txtPrixAnneeModif;
    @FXML private TextField txtImageModif;
    @FXML private TextArea txtDescriptionModif;
    @FXML private CheckBox chkDisponibleModif;

    private Terrain currentEditingTerrain;

    // --- LIST Tab ---
    @FXML private TableView<Terrain> tableTerrains;
    @FXML private TableColumn<Terrain, Integer> colId;
    @FXML private TableColumn<Terrain, String> colImage;
    @FXML private TableColumn<Terrain, String> colTitre;
    @FXML private TableColumn<Terrain, String> colVille;
    @FXML private TableColumn<Terrain, BigDecimal> colSuperficie;
    @FXML private TableColumn<Terrain, BigDecimal> colPrixAnnee;
    @FXML private TableColumn<Terrain, Void> colActions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadComboBoxes();
        setupTable();
        loadTableData();

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == tabListe) {
                loadTableData();
            }
        });
    }

    private void loadComboBoxes() {
        try {
            List<Categorie> categories = categorieService.getCategoriesByType(ProductType.TERRAIN);
            ObservableList<Categorie> obsCategories = FXCollections.observableArrayList(categories);
            cbCategorie.setItems(obsCategories);
            cbCategorieModif.setItems(obsCategories);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories.");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficieHectares"));
        colPrixAnnee.setCellValueFactory(new PropertyValueFactory<>("prixAnnee"));

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
                        Image image = new Image(imageUrl, 50, 50, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Actions Column
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Terrain, Void> call(final TableColumn<Terrain, Void> param) {
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
                            Terrain terrain = getTableView().getItems().get(getIndex());
                            handleDelete(terrain);
                        });

                        btnEdit.setOnAction(event -> {
                            Terrain terrain = getTableView().getItems().get(getIndex());
                            prepareEdit(terrain);
                        });

                        btnView.setOnAction(event -> {
                            Terrain terrain = getTableView().getItems().get(getIndex());
                            showDetails(terrain);
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
            List<Terrain> list = terrainService.getEntities();
            tableTerrains.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des terrains.");
        }
    }

    // --- Handlers ---

    @FXML
    void handleAjouter(ActionEvent event) {
        if (!validateForm(txtTitre, cbCategorie, txtVille, txtPrixAnnee)) {
            return;
        }

        try {
            Terrain terrain = new Terrain(
                    cbCategorie.getValue().getId(),
                    txtTitre.getText(),
                    parseDecimalOrNull(txtSuperficie.getText()),
                    txtVille.getText(),
                    new BigDecimal(txtPrixAnnee.getText()));

            terrain.setDescription(txtDescription.getText());
            terrain.setAdresse(txtAdresse.getText());
            terrain.setPrixMois(parseDecimalOrNull(txtPrixMois.getText()));
            terrain.setImageUrl(txtImage.getText());
            terrain.setDisponible(chkDisponible.isSelected());

            terrainService.addEntity(terrain);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Terrain ajouté avec succès.");
            clearCreateForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez vérifier les champs numériques.");
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
            List<Terrain> results = terrainService.searchByTitle(query);
            if (results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Aucun terrain trouvé.");
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
        if (currentEditingTerrain == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun terrain sélectionné pour modification.");
            return;
        }

        if (!validateForm(txtTitreModif, cbCategorieModif, txtVilleModif, txtPrixAnneeModif)) {
            return;
        }

        try {
            currentEditingTerrain.setTitre(txtTitreModif.getText());
            currentEditingTerrain.setCategorieId(cbCategorieModif.getValue().getId());
            currentEditingTerrain.setDescription(txtDescriptionModif.getText());
            currentEditingTerrain.setSuperficieHectares(parseDecimalOrNull(txtSuperficieModif.getText()));
            currentEditingTerrain.setVille(txtVilleModif.getText());
            currentEditingTerrain.setAdresse(txtAdresseModif.getText());
            currentEditingTerrain.setPrixMois(parseDecimalOrNull(txtPrixMoisModif.getText()));
            currentEditingTerrain.setPrixAnnee(new BigDecimal(txtPrixAnneeModif.getText()));
            currentEditingTerrain.setImageUrl(txtImageModif.getText());
            currentEditingTerrain.setDisponible(chkDisponibleModif.isSelected());

            terrainService.updateEntity(currentEditingTerrain);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Terrain modifié avec succès.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez vérifier les champs numériques.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadTableData();
    }

    // --- Helpers ---

    private void handleDelete(Terrain terrain) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + terrain.getTitre() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                terrainService.deleteEntity(terrain);
                tableTerrains.getItems().remove(terrain);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le terrain.");
            }
        }
    }

    private void prepareEdit(Terrain terrain) {
        fillEditForm(terrain);
        mainTabPane.getSelectionModel().select(tabModifier);
    }

    private void fillEditForm(Terrain terrain) {
        this.currentEditingTerrain = terrain;

        txtTitreModif.setText(terrain.getTitre());
        txtDescriptionModif.setText(terrain.getDescription());
        txtSuperficieModif.setText(terrain.getSuperficieHectares() != null ? terrain.getSuperficieHectares().toString() : "");
        txtVilleModif.setText(terrain.getVille());
        txtAdresseModif.setText(terrain.getAdresse());
        txtPrixMoisModif.setText(terrain.getPrixMois() != null ? terrain.getPrixMois().toString() : "");
        txtPrixAnneeModif.setText(terrain.getPrixAnnee() != null ? terrain.getPrixAnnee().toString() : "");
        txtImageModif.setText(terrain.getImageUrl());
        chkDisponibleModif.setSelected(terrain.isDisponible());

        for (Categorie c : cbCategorieModif.getItems()) {
            if (c.getId() == terrain.getCategorieId()) {
                cbCategorieModif.setValue(c);
                break;
            }
        }
    }

    private void showDetails(Terrain terrain) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du terrain");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 25, 20, 25));
        grid.setStyle("-fx-background-color: white;");

        int row = 0;
        if (terrain.getImageUrl() != null && !terrain.getImageUrl().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(terrain.getImageUrl(), 150, 150, true, true));
                imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);");
                grid.add(imageView, 0, row, 2, 1);
                GridPane.setHalignment(imageView, HPos.CENTER);
                row++;
            } catch (Exception e) { }
        }

        Label titleLabel = new Label(terrain.getTitre());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        grid.add(titleLabel, 0, row, 2, 1);
        GridPane.setMargin(titleLabel, new Insets(10, 0, 10, 0));
        row++;

        addDetailRow(grid, row++, "ID:", String.valueOf(terrain.getId()));
        addDetailRow(grid, row++, "Ville:", terrain.getVille());
        addDetailRow(grid, row++, "Adresse:", terrain.getAdresse() != null ? terrain.getAdresse() : "-");
        addDetailRow(grid, row++, "Superficie:", terrain.getSuperficieHectares() + " hectares");
        addDetailRow(grid, row++, "Prix/Mois:", terrain.getPrixMois() != null ? terrain.getPrixMois() + " €" : "-");
        addDetailRow(grid, row++, "Prix/Année:", terrain.getPrixAnnee() + " €");
        addDetailRow(grid, row++, "Disponibilité:", terrain.isDisponible() ? "✓ Disponible" : "✗ Indisponible");

        if (terrain.getDescription() != null && !terrain.getDescription().isEmpty()) {
            Label descLabel = new Label("Description:");
            descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 13px;");
            grid.add(descLabel, 0, row, 2, 1);
            GridPane.setMargin(descLabel, new Insets(10, 0, 5, 0));
            row++;

            Label descValue = new Label(terrain.getDescription());
            descValue.setWrapText(true);
            descValue.setMaxWidth(400);
            descValue.setStyle("-fx-text-fill: #666; -fx-font-size: 13px; -fx-padding: 10; " +
                    "-fx-background-color: #f8f9fa; -fx-background-radius: 6;");
            grid.add(descValue, 0, row, 2, 1);
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

    private BigDecimal parseDecimalOrNull(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(text);
    }

    private boolean validateForm(TextField titre, ComboBox<?> cat, TextField ville, TextField prixAnnee) {
        if (titre.getText().isEmpty() || cat.getValue() == null ||
                ville.getText().isEmpty() || prixAnnee.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        return true;
    }

    private void clearCreateForm() {
        txtTitre.clear();
        txtDescription.clear();
        txtSuperficie.clear();
        txtVille.clear();
        txtAdresse.clear();
        txtPrixMois.clear();
        txtPrixAnnee.clear();
        txtImage.clear();
        chkDisponible.setSelected(true);
        cbCategorie.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
