package marketplace.GUI.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import marketplace.entities.*;
import marketplace.service.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for admin location management view
 */
public class LocationAdminController implements Initializable {
    
    @FXML private TableView<Location> locationTable;
    @FXML private TableColumn<Location, Integer> colId;
    @FXML private TableColumn<Location, String> colNumero;
    @FXML private TableColumn<Location, String> colType;
    @FXML private TableColumn<Location, String> colElement;
    @FXML private TableColumn<Location, String> colUtilisateur;
    @FXML private TableColumn<Location, String> colDateDebut;
    @FXML private TableColumn<Location, String> colDateFin;
    @FXML private TableColumn<Location, Integer> colDuree;
    @FXML private TableColumn<Location, String> colPrix;
    @FXML private TableColumn<Location, String> colCaution;
    @FXML private TableColumn<Location, String> colStatut;
    @FXML private TableColumn<Location, Void> colActions;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    
    @FXML private Label totalCountLabel;
    @FXML private Label activeCountLabel;
    @FXML private Label pendingCountLabel;
    
    private LocationService locationService;
    private VehiculeService vehiculeService;
    private TerrainService terrainService;
    private UtilisateurService utilisateurService;
    
    private ObservableList<Location> allLocations;
    private FilteredList<Location> filteredLocations;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        locationService = new LocationService();
        vehiculeService = new VehiculeService();
        terrainService = new TerrainService();
        utilisateurService = new UtilisateurService();
        
        setupFilters();
        setupTableColumns();
        loadLocations();
        setupSearch();
    }
    
    private void setupFilters() {
        // Type filter
        typeFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "Véhicule", "Terrain"
        ));
        typeFilterCombo.setValue("Tous");
        typeFilterCombo.setOnAction(e -> applyFilters());
        
        // Status filter
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "Tous", "En attente", "Confirmée", "En cours", "Terminée", "Annulée"
        ));
        statusFilterCombo.setValue("Tous");
        statusFilterCombo.setOnAction(e -> applyFilters());
    }
    
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroLocation"));
        
        // Type column
        colType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getTypeLocation();
            return new SimpleStringProperty("vehicule".equalsIgnoreCase(type) ? "Véhicule" : "Terrain");
        });
        
        // Element name column
        colElement.setCellValueFactory(cellData -> {
            Location loc = cellData.getValue();
            String elementName = getElementName(loc);
            return new SimpleStringProperty(elementName);
        });
        
        // User column
        colUtilisateur.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUtilisateurId();
            String userName = getUserName(userId);
            return new SimpleStringProperty(userName);
        });
        
        // Date columns
        colDateDebut.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateDebut() != null) {
                return new SimpleStringProperty(cellData.getValue().getDateDebut().format(DATE_FORMAT));
            }
            return new SimpleStringProperty("-");
        });
        
        colDateFin.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateFin() != null) {
                return new SimpleStringProperty(cellData.getValue().getDateFin().format(DATE_FORMAT));
            }
            return new SimpleStringProperty("-");
        });
        
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeJours"));
        
        // Price columns
        colPrix.setCellValueFactory(cellData -> {
            BigDecimal prix = cellData.getValue().getPrixTotal();
            return new SimpleStringProperty(String.format("%.2f DT", prix != null ? prix : BigDecimal.ZERO));
        });
        
        colCaution.setCellValueFactory(cellData -> {
            BigDecimal caution = cellData.getValue().getCaution();
            return new SimpleStringProperty(String.format("%.2f DT", caution != null ? caution : BigDecimal.ZERO));
        });
        
        // Status column with colored badge
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Location loc = getTableRow().getItem();
                    Label badge = createStatusBadge(loc.getStatut());
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStatut.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        
        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnConfirm = new Button();
            private final Button btnCancel = new Button();
            private final Button btnComplete = new Button();
            private final HBox hbox = new HBox(5);
            
            {
                // Confirm button (checkmark)
                SVGPath confirmIcon = new SVGPath();
                confirmIcon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
                confirmIcon.setFill(Color.WHITE);
                confirmIcon.setScaleX(0.7);
                confirmIcon.setScaleY(0.7);
                btnConfirm.setGraphic(confirmIcon);
                btnConfirm.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 4; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");
                btnConfirm.setTooltip(new Tooltip("Confirmer"));
                btnConfirm.setOnAction(e -> handleConfirm(getTableRow().getItem()));
                
                // Cancel button (X)
                SVGPath cancelIcon = new SVGPath();
                cancelIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
                cancelIcon.setFill(Color.WHITE);
                cancelIcon.setScaleX(0.7);
                cancelIcon.setScaleY(0.7);
                btnCancel.setGraphic(cancelIcon);
                btnCancel.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 4; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");
                btnCancel.setTooltip(new Tooltip("Annuler"));
                btnCancel.setOnAction(e -> handleCancel(getTableRow().getItem()));
                
                // Complete button (flag)
                SVGPath completeIcon = new SVGPath();
                completeIcon.setContent("M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6z");
                completeIcon.setFill(Color.WHITE);
                completeIcon.setScaleX(0.7);
                completeIcon.setScaleY(0.7);
                btnComplete.setGraphic(completeIcon);
                btnComplete.setStyle("-fx-background-color: #3498db; -fx-background-radius: 4; -fx-min-width: 28; -fx-min-height: 28; -fx-cursor: hand;");
                btnComplete.setTooltip(new Tooltip("Terminer"));
                btnComplete.setOnAction(e -> handleComplete(getTableRow().getItem()));
                
                hbox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Location loc = getTableRow().getItem();
                    hbox.getChildren().clear();
                    
                    // Show buttons based on status
                    if (loc.getStatut() == RentalStatus.EN_ATTENTE) {
                        hbox.getChildren().addAll(btnConfirm, btnCancel);
                    } else if (loc.getStatut() == RentalStatus.CONFIRMEE || loc.getStatut() == RentalStatus.EN_COURS) {
                        hbox.getChildren().addAll(btnComplete, btnCancel);
                    }
                    // No actions for TERMINEE or ANNULEE
                    
                    setGraphic(hbox);
                }
            }
        });
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void loadLocations() {
        try {
            List<Location> locations = locationService.getEntities();
            allLocations = FXCollections.observableArrayList(locations);
            filteredLocations = new FilteredList<>(allLocations, p -> true);
            locationTable.setItems(filteredLocations);
            applyFilters();
            locationTable.refresh();
            updateStats();
        } catch (SQLException e) {
            System.err.println("Error loading locations: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les locations");
        }
    }
    
    private void applyFilters() {
        String search = searchField.getText().toLowerCase().trim();
        String typeFilter = typeFilterCombo.getValue();
        String statusFilter = statusFilterCombo.getValue();
        
        filteredLocations.setPredicate(location -> {
            // Type filter
            if (typeFilter != null && !typeFilter.equals("Tous")) {
                String locType = location.getTypeLocation();
                if (typeFilter.equals("Véhicule") && !"vehicule".equalsIgnoreCase(locType)) {
                    return false;
                }
                if (typeFilter.equals("Terrain") && !"terrain".equalsIgnoreCase(locType)) {
                    return false;
                }
            }
            
            // Status filter
            if (statusFilter != null && !statusFilter.equals("Tous")) {
                RentalStatus status = location.getStatut();
                switch (statusFilter) {
                    case "En attente": if (status != RentalStatus.EN_ATTENTE) return false; break;
                    case "Confirmée": if (status != RentalStatus.CONFIRMEE) return false; break;
                    case "En cours": if (status != RentalStatus.EN_COURS) return false; break;
                    case "Terminée": if (status != RentalStatus.TERMINEE) return false; break;
                    case "Annulée": if (status != RentalStatus.ANNULEE) return false; break;
                }
            }
            
            // Search filter
            if (!search.isEmpty()) {
                String numero = location.getNumeroLocation() != null ? location.getNumeroLocation().toLowerCase() : "";
                String userName = getUserName(location.getUtilisateurId()).toLowerCase();
                String elementName = getElementName(location).toLowerCase();
                
                if (!numero.contains(search) && !userName.contains(search) && !elementName.contains(search)) {
                    return false;
                }
            }
            
            return true;
        });
        
        updateStats();
    }
    
    private void updateStats() {
        if (allLocations == null) return;
        
        int total = allLocations.size();
        long active = allLocations.stream()
            .filter(l -> l.getStatut() == RentalStatus.EN_COURS || l.getStatut() == RentalStatus.CONFIRMEE)
            .count();
        long pending = allLocations.stream()
            .filter(l -> l.getStatut() == RentalStatus.EN_ATTENTE)
            .count();
        
        totalCountLabel.setText("Total: " + total);
        activeCountLabel.setText("En cours: " + active);
        pendingCountLabel.setText("En attente: " + pending);
    }
    
    private String getElementName(Location loc) {
        try {
            if ("vehicule".equalsIgnoreCase(loc.getTypeLocation())) {
                return vehiculeService.getEntities().stream()
                    .filter(v -> v.getId() == loc.getElementId())
                    .findFirst()
                    .map(v -> v.getMarque() + " " + v.getModele())
                    .orElse("Véhicule #" + loc.getElementId());
            } else {
                return terrainService.getEntities().stream()
                    .filter(t -> t.getId() == loc.getElementId())
                    .findFirst()
                    .map(Terrain::getTitre)
                    .orElse("Terrain #" + loc.getElementId());
            }
        } catch (SQLException e) {
            return "Element #" + loc.getElementId();
        }
    }
    
    private String getUserName(int userId) {
        try {
            Utilisateur user = utilisateurService.getById(userId);
            if (user != null) {
                return user.getPrenom() + " " + user.getNom();
            }
            return "Utilisateur #" + userId;
        } catch (SQLException e) {
            return "Utilisateur #" + userId;
        }
    }
    
    private Label createStatusBadge(RentalStatus status) {
        Label badge = new Label();
        badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");
        
        if (status == null) {
            badge.setText("Inconnu");
            badge.setStyle(badge.getStyle() + "-fx-background-color: #888; -fx-text-fill: white;");
            return badge;
        }
        
        switch (status) {
            case EN_ATTENTE:
                badge.setText("En attente");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #f39c12; -fx-text-fill: white;");
                break;
            case CONFIRMEE:
                badge.setText("Confirmée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #3498db; -fx-text-fill: white;");
                break;
            case EN_COURS:
                badge.setText("En cours");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #27ae60; -fx-text-fill: white;");
                break;
            case TERMINEE:
                badge.setText("Terminée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #95a5a6; -fx-text-fill: white;");
                break;
            case ANNULEE:
                badge.setText("Annulée");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                break;
            default:
                badge.setText(status.getValue());
                badge.setStyle(badge.getStyle() + "-fx-background-color: #888; -fx-text-fill: white;");
        }
        
        return badge;
    }
    
    private void handleConfirm(Location location) {
        if (location == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la location");
        confirm.setHeaderText("Voulez-vous confirmer cette location ?");
        confirm.setContentText("Location #" + location.getId() + " - " + getElementName(location));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                locationService.updateStatus(location.getId(), RentalStatus.CONFIRMEE);
                loadLocations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Location confirmée avec succès");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de confirmer la location: " + e.getMessage());
            }
        }
    }
    
    private void handleCancel(Location location) {
        if (location == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la location");
        confirm.setHeaderText("Voulez-vous annuler cette location ?");
        confirm.setContentText("Location #" + location.getId() + " - " + getElementName(location));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                locationService.updateStatus(location.getId(), RentalStatus.ANNULEE);
                // Make the product available again
                makeProductAvailable(location);
                loadLocations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Location annulée");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler la location: " + e.getMessage());
            }
        }
    }
    
    private void handleComplete(Location location) {
        if (location == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Terminer la location");
        confirm.setHeaderText("Voulez-vous marquer cette location comme terminée ?");
        confirm.setContentText("Location #" + location.getId() + " - " + getElementName(location));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                locationService.updateStatus(location.getId(), RentalStatus.TERMINEE);
                // Make the product available again
                makeProductAvailable(location);
                loadLocations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Location terminée");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de terminer la location: " + e.getMessage());
            }
        }
    }
    
    /**
     * Make the product (vehicle or terrain) available again
     */
    private void makeProductAvailable(Location location) {
        try {
            String type = location.getTypeLocation();
            int elementId = location.getElementId();
            System.out.println("Making product available: type=" + type + ", elementId=" + elementId);
            
            if (type != null && type.toLowerCase().contains("vehicule")) {
                vehiculeService.updateDisponibilite(elementId, true);
            } else if (type != null && type.toLowerCase().contains("terrain")) {
                terrainService.updateDisponibilite(elementId, true);
            } else {
                System.err.println("Unknown location type: " + type);
            }
        } catch (SQLException e) {
            System.err.println("Error updating product availability: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadLocations();
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les locations");
        fileChooser.setInitialFileName("locations_export.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(locationTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Header
                writer.println("ID,Numéro,Type,Élément,Utilisateur,Date Début,Date Fin,Durée,Prix,Caution,Statut");
                
                // Data
                for (Location loc : filteredLocations) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%d,%.2f,%.2f,\"%s\"%n",
                        loc.getId(),
                        loc.getNumeroLocation() != null ? loc.getNumeroLocation() : "",
                        loc.getTypeLocation(),
                        getElementName(loc),
                        getUserName(loc.getUtilisateurId()),
                        loc.getDateDebut() != null ? loc.getDateDebut().format(DATE_FORMAT) : "",
                        loc.getDateFin() != null ? loc.getDateFin().format(DATE_FORMAT) : "",
                        loc.getDureeJours(),
                        loc.getPrixTotal() != null ? loc.getPrixTotal() : BigDecimal.ZERO,
                        loc.getCaution() != null ? loc.getCaution() : BigDecimal.ZERO,
                        loc.getStatut() != null ? loc.getStatut().getValue() : ""
                    );
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export réussi: " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export: " + e.getMessage());
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
