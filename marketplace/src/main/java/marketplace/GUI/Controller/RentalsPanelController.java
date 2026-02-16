package marketplace.GUI.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import marketplace.entities.Location;
import marketplace.entities.RentalStatus;
import marketplace.entities.Terrain;
import marketplace.entities.Vehicule;
import marketplace.service.CartService;
import marketplace.service.LocationService;
import marketplace.service.TerrainService;
import marketplace.service.VehiculeService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Rentals Panel
 * Displays user's rental history
 */
public class RentalsPanelController implements Initializable {
    
    @FXML private VBox rentalsPanel;
    @FXML private VBox rentalsContainer;
    @FXML private Label rentalCountLabel;
    @FXML private Label emptyLabel;
    @FXML private Label activeCountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label totalCautionsLabel;
    @FXML private Button closeButton;
    @FXML private Button refreshButton;
    
    private LocationService locationService;
    private VehiculeService vehiculeService;
    private TerrainService terrainService;
    private Runnable onCloseCallback;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        locationService = new LocationService();
        vehiculeService = new VehiculeService();
        terrainService = new TerrainService();
        loadRentals();
    }
    
    /**
     * Set callback for close action
     */
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
    
    /**
     * Load user's rentals from database
     */
    public void loadRentals() {
        rentalsContainer.getChildren().clear();
        
        int userId = CartService.getInstance().getCurrentUserId();
        if (userId <= 0) {
            showEmptyState("Veuillez vous connecter pour voir vos locations");
            return;
        }
        
        try {
            List<Location> allLocations = locationService.getEntities();
            List<Location> userLocations = allLocations.stream()
                .filter(loc -> loc.getUtilisateurId() == userId)
                .collect(Collectors.toList());
            
            if (userLocations.isEmpty()) {
                showEmptyState("Vous n'avez aucune location");
                return;
            }
            
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);
            
            // Count by status
            long activeCount = userLocations.stream()
                .filter(loc -> loc.getStatut() == RentalStatus.EN_COURS || loc.getStatut() == RentalStatus.CONFIRMEE)
                .count();
            long pendingCount = userLocations.stream()
                .filter(loc -> loc.getStatut() == RentalStatus.EN_ATTENTE)
                .count();
            
            // Calculate total cautions for active/pending rentals
            BigDecimal totalCautions = userLocations.stream()
                .filter(loc -> loc.getStatut() == RentalStatus.EN_COURS || loc.getStatut() == RentalStatus.CONFIRMEE || loc.getStatut() == RentalStatus.EN_ATTENTE)
                .map(Location::getCaution)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Update summary labels
            rentalCountLabel.setText(userLocations.size() + " location(s)");
            activeCountLabel.setText(String.valueOf(activeCount));
            pendingCountLabel.setText(String.valueOf(pendingCount));
            totalCautionsLabel.setText(String.format("%.2f DT", totalCautions));
            
            // Create rental cards
            for (Location location : userLocations) {
                StackPane card = createRentalCard(location);
                rentalsContainer.getChildren().add(card);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading rentals: " + e.getMessage());
            showEmptyState("Erreur de chargement");
        }
    }
    
    /**
     * Show empty state message
     */
    private void showEmptyState(String message) {
        emptyLabel.setText(message);
        emptyLabel.setVisible(true);
        emptyLabel.setManaged(true);
        rentalsContainer.getChildren().clear();
        rentalsContainer.getChildren().add(emptyLabel);
        
        rentalCountLabel.setText("0 location(s)");
        activeCountLabel.setText("0");
        pendingCountLabel.setText("0");
        totalCautionsLabel.setText("0.00 DT");
    }
    
    /**
     * Create a card UI for a rental
     */
    private StackPane createRentalCard(Location location) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-padding: 12;");
        
        // Top row: Type icon and name
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Type icon
        SVGPath typeIcon = new SVGPath();
        String iconColor;
        String itemName = "Location #" + location.getId();
        
        if ("vehicule".equalsIgnoreCase(location.getTypeLocation())) {
            typeIcon.setContent("M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99z");
            iconColor = "#e74c3c";
            // Get vehicle name
            try {
                Vehicule vehicule = vehiculeService.getEntities().stream()
                    .filter(v -> v.getId() == location.getElementId())
                    .findFirst().orElse(null);
                if (vehicule != null) {
                    itemName = vehicule.getMarque() + " " + vehicule.getModele();
                }
            } catch (SQLException ignored) {}
        } else {
            typeIcon.setContent("M12 3L2 12h3v8h14v-8h3L12 3zm0 12.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z");
            iconColor = "#27ae60";
            // Get terrain name
            try {
                Terrain terrain = terrainService.getEntities().stream()
                    .filter(t -> t.getId() == location.getElementId())
                    .findFirst().orElse(null);
                if (terrain != null) {
                    itemName = terrain.getTitre();
                }
            } catch (SQLException ignored) {}
        }
        typeIcon.setFill(Color.web(iconColor));
        typeIcon.setScaleX(1.2);
        typeIcon.setScaleY(1.2);
        
        // Name
        Label nameLabel = new Label(itemName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        // Status badge
        Label statusBadge = createStatusBadge(location.getStatut());
        
        topRow.getChildren().addAll(typeIcon, nameLabel, statusBadge);
        
        // Date row
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateText = location.getDateDebut().format(dateFormat) + " → " + location.getDateFin().format(dateFormat);
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Duration
        Label durationLabel = new Label(location.getDureeJours() + " jour(s)");
        durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        
        // Price row
        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        
        Label priceLabel = new Label(String.format("%.2f DT", location.getPrixTotal()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        
        if (location.getCaution() != null && location.getCaution().compareTo(BigDecimal.ZERO) > 0) {
            Label cautionLabel = new Label("+ " + String.format("%.2f DT", location.getCaution()) + " caution");
            cautionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e67e22;");
            priceRow.getChildren().addAll(priceLabel, cautionLabel);
        } else {
            priceRow.getChildren().add(priceLabel);
        }
        
        // Reference number
        if (location.getNumeroLocation() != null && !location.getNumeroLocation().isEmpty()) {
            Label refLabel = new Label("Réf: " + location.getNumeroLocation());
            refLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
            card.getChildren().addAll(topRow, dateLabel, durationLabel, priceRow, refLabel);
        } else {
            card.getChildren().addAll(topRow, dateLabel, durationLabel, priceRow);
        }
        
        // Wrap in StackPane to allow positioning delete button
        StackPane cardContainer = new StackPane();
        cardContainer.getChildren().add(card);
        
        // Add delete button for terminated or cancelled rentals
        if (location.getStatut() == RentalStatus.TERMINEE || location.getStatut() == RentalStatus.ANNULEE) {
            Button deleteBtn = new Button();
            SVGPath deleteIcon = new SVGPath();
            deleteIcon.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
            deleteIcon.setFill(Color.WHITE);
            deleteIcon.setScaleX(0.6);
            deleteIcon.setScaleY(0.6);
            deleteBtn.setGraphic(deleteIcon);
            deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 50; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20; -fx-padding: 0; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> handleDeleteRental(location));
            
            StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteBtn, new Insets(-5, -5, 0, 0));
            cardContainer.getChildren().add(deleteBtn);
        }
        
        return cardContainer;
    }
    
    /**
     * Create status badge
     */
    private Label createStatusBadge(RentalStatus status) {
        Label badge = new Label();
        badge.setPadding(new Insets(2, 8, 2, 8));
        badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4;");
        
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
    
    @FXML
    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadRentals();
    }
    
    /**
     * Handle deletion of a rental
     */
    private void handleDeleteRental(Location location) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la location");
        confirm.setHeaderText("Voulez-vous supprimer cette location de votre historique ?");
        confirm.setContentText("Location #" + location.getId() + " - " + location.getTypeLocation());
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Ensure product is available before deleting the rental record
                makeProductAvailable(location);
                locationService.deleteEntity(location);
                loadRentals(); // Refresh the list
            } catch (SQLException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText(null);
                error.setContentText("Impossible de supprimer la location: " + e.getMessage());
                error.showAndWait();
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
}
