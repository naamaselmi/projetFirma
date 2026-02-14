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
import marketplace.entities.Vehicule;
import marketplace.service.CategorieService;
import marketplace.service.VehiculeService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VehiculeController implements Initializable {

    // --- Services ---
    private final VehiculeService vehiculeService = new VehiculeService();
    private final CategorieService categorieService = new CategorieService();

    // --- TabPane ---
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabCreer;
    @FXML private Tab tabModifier;
    @FXML private Tab tabListe;

    // --- CREATE Tab ---
    @FXML private TextField txtNom;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField txtMarque;
    @FXML private TextField txtModele;
    @FXML private TextField txtImmatriculation;
    @FXML private TextField txtPrixJour;
    @FXML private TextField txtPrixSemaine;
    @FXML private TextField txtPrixMois;
    @FXML private TextField txtCaution;
    @FXML private TextField txtImage;
    @FXML private TextArea txtDescription;
    @FXML private CheckBox chkDisponible;

    // --- EDIT Tab ---
    @FXML private TextField txtSearchModif;
    @FXML private TextField txtNomModif;
    @FXML private ComboBox<Categorie> cbCategorieModif;
    @FXML private TextField txtMarqueModif;
    @FXML private TextField txtModeleModif;
    @FXML private TextField txtImmatriculationModif;
    @FXML private TextField txtPrixJourModif;
    @FXML private TextField txtPrixSemaineModif;
    @FXML private TextField txtPrixMoisModif;
    @FXML private TextField txtCautionModif;
    @FXML private TextField txtImageModif;
    @FXML private TextArea txtDescriptionModif;
    @FXML private CheckBox chkDisponibleModif;

    private Vehicule currentEditingVehicule;

    // --- LIST Tab ---
    @FXML private TableView<Vehicule> tableVehicules;
    @FXML private TableColumn<Vehicule, Integer> colId;
    @FXML private TableColumn<Vehicule, String> colImage;
    @FXML private TableColumn<Vehicule, String> colNom;
    @FXML private TableColumn<Vehicule, String> colMarque;
    @FXML private TableColumn<Vehicule, String> colModele;
    @FXML private TableColumn<Vehicule, BigDecimal> colPrixJour;
    @FXML private TableColumn<Vehicule, Void> colActions;

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
            List<Categorie> categories = categorieService.getCategoriesByType(ProductType.VEHICULE);
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
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colPrixJour.setCellValueFactory(new PropertyValueFactory<>("prixJour"));

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
            public TableCell<Vehicule, Void> call(final TableColumn<Vehicule, Void> param) {
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
                            Vehicule vehicule = getTableView().getItems().get(getIndex());
                            handleDelete(vehicule);
                        });

                        btnEdit.setOnAction(event -> {
                            Vehicule vehicule = getTableView().getItems().get(getIndex());
                            prepareEdit(vehicule);
                        });

                        btnView.setOnAction(event -> {
                            Vehicule vehicule = getTableView().getItems().get(getIndex());
                            showDetails(vehicule);
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
            List<Vehicule> list = vehiculeService.getEntities();
            tableVehicules.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des véhicules.");
        }
    }

    // --- Handlers ---

    @FXML
    void handleAjouter(ActionEvent event) {
        if (!validateForm(txtNom, cbCategorie, txtPrixJour)) {
            return;
        }

        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setCategorieId(cbCategorie.getValue().getId());
            vehicule.setNom(txtNom.getText());
            vehicule.setPrixJour(new BigDecimal(txtPrixJour.getText()));
            vehicule.setDescription(txtDescription.getText());
            vehicule.setMarque(txtMarque.getText());
            vehicule.setModele(txtModele.getText());
            vehicule.setImmatriculation(txtImmatriculation.getText());
            vehicule.setPrixSemaine(parseDecimalOrNull(txtPrixSemaine.getText()));
            vehicule.setPrixMois(parseDecimalOrNull(txtPrixMois.getText()));
            vehicule.setCaution(parseDecimalOrNull(txtCaution.getText()));
            vehicule.setImageUrl(txtImage.getText());
            vehicule.setDisponible(chkDisponible.isSelected());

            vehiculeService.addEntity(vehicule);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule ajouté avec succès.");
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
            List<Vehicule> results = vehiculeService.search(query);
            if (results.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Aucun véhicule trouvé.");
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
        if (currentEditingVehicule == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun véhicule sélectionné pour modification.");
            return;
        }

        if (!validateForm(txtNomModif, cbCategorieModif, txtPrixJourModif)) {
            return;
        }

        try {
            currentEditingVehicule.setNom(txtNomModif.getText());
            currentEditingVehicule.setCategorieId(cbCategorieModif.getValue().getId());
            currentEditingVehicule.setDescription(txtDescriptionModif.getText());
            currentEditingVehicule.setMarque(txtMarqueModif.getText());
            currentEditingVehicule.setModele(txtModeleModif.getText());
            currentEditingVehicule.setImmatriculation(txtImmatriculationModif.getText());
            currentEditingVehicule.setPrixJour(new BigDecimal(txtPrixJourModif.getText()));
            currentEditingVehicule.setPrixSemaine(parseDecimalOrNull(txtPrixSemaineModif.getText()));
            currentEditingVehicule.setPrixMois(parseDecimalOrNull(txtPrixMoisModif.getText()));
            currentEditingVehicule.setCaution(parseDecimalOrNull(txtCautionModif.getText()));
            currentEditingVehicule.setImageUrl(txtImageModif.getText());
            currentEditingVehicule.setDisponible(chkDisponibleModif.isSelected());

            vehiculeService.updateEntity(currentEditingVehicule);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule modifié avec succès.");

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

    private void handleDelete(Vehicule vehicule) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + vehicule.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                vehiculeService.deleteEntity(vehicule);
                tableVehicules.getItems().remove(vehicule);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le véhicule.");
            }
        }
    }

    private void prepareEdit(Vehicule vehicule) {
        fillEditForm(vehicule);
        mainTabPane.getSelectionModel().select(tabModifier);
    }

    private void fillEditForm(Vehicule vehicule) {
        this.currentEditingVehicule = vehicule;

        txtNomModif.setText(vehicule.getNom());
        txtDescriptionModif.setText(vehicule.getDescription());
        txtMarqueModif.setText(vehicule.getMarque());
        txtModeleModif.setText(vehicule.getModele());
        txtImmatriculationModif.setText(vehicule.getImmatriculation());
        txtPrixJourModif.setText(vehicule.getPrixJour() != null ? vehicule.getPrixJour().toString() : "");
        txtPrixSemaineModif.setText(vehicule.getPrixSemaine() != null ? vehicule.getPrixSemaine().toString() : "");
        txtPrixMoisModif.setText(vehicule.getPrixMois() != null ? vehicule.getPrixMois().toString() : "");
        txtCautionModif.setText(vehicule.getCaution() != null ? vehicule.getCaution().toString() : "");
        txtImageModif.setText(vehicule.getImageUrl());
        chkDisponibleModif.setSelected(vehicule.isDisponible());

        for (Categorie c : cbCategorieModif.getItems()) {
            if (c.getId() == vehicule.getCategorieId()) {
                cbCategorieModif.setValue(c);
                break;
            }
        }
    }

    private void showDetails(Vehicule vehicule) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du véhicule");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 25, 20, 25));
        grid.setStyle("-fx-background-color: white;");

        int row = 0;
        if (vehicule.getImageUrl() != null && !vehicule.getImageUrl().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(vehicule.getImageUrl(), 150, 150, true, true));
                imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);");
                grid.add(imageView, 0, row, 2, 1);
                GridPane.setHalignment(imageView, HPos.CENTER);
                row++;
            } catch (Exception e) { }
        }

        Label titleLabel = new Label(vehicule.getNom());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");
        grid.add(titleLabel, 0, row, 2, 1);
        GridPane.setMargin(titleLabel, new Insets(10, 0, 10, 0));
        row++;

        addDetailRow(grid, row++, "ID:", String.valueOf(vehicule.getId()));
        addDetailRow(grid, row++, "Marque:", vehicule.getMarque() != null ? vehicule.getMarque() : "-");
        addDetailRow(grid, row++, "Modèle:", vehicule.getModele() != null ? vehicule.getModele() : "-");
        addDetailRow(grid, row++, "Immatriculation:", vehicule.getImmatriculation() != null ? vehicule.getImmatriculation() : "-");
        addDetailRow(grid, row++, "Prix/Jour:", vehicule.getPrixJour() + " €");
        addDetailRow(grid, row++, "Prix/Semaine:", vehicule.getPrixSemaine() != null ? vehicule.getPrixSemaine() + " €" : "-");
        addDetailRow(grid, row++, "Prix/Mois:", vehicule.getPrixMois() != null ? vehicule.getPrixMois() + " €" : "-");
        addDetailRow(grid, row++, "Caution:", vehicule.getCaution() != null ? vehicule.getCaution() + " €" : "-");
        addDetailRow(grid, row++, "Disponibilité:", vehicule.isDisponible() ? "✓ Disponible" : "✗ Indisponible");

        if (vehicule.getDescription() != null && !vehicule.getDescription().isEmpty()) {
            Label descLabel = new Label("Description:");
            descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 13px;");
            grid.add(descLabel, 0, row, 2, 1);
            GridPane.setMargin(descLabel, new Insets(10, 0, 5, 0));
            row++;

            Label descValue = new Label(vehicule.getDescription());
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

    private boolean validateForm(TextField nom, ComboBox<?> cat, TextField prixJour) {
        if (nom.getText().isEmpty() || cat.getValue() == null || prixJour.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        return true;
    }

    private void clearCreateForm() {
        txtNom.clear();
        txtDescription.clear();
        txtMarque.clear();
        txtModele.clear();
        txtImmatriculation.clear();
        txtPrixJour.clear();
        txtPrixSemaine.clear();
        txtPrixMois.clear();
        txtCaution.clear();
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
