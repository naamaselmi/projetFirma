package Firma.controllers.GestionMarketplace;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import Firma.entities.GestionMarketplace.Location;
import Firma.entities.GestionMarketplace.RentalStatus;
import Firma.entities.GestionMarketplace.Terrain;
import Firma.entities.GestionMarketplace.Vehicule;
import Firma.services.GestionMarketplace.CartService;
import Firma.services.GestionMarketplace.LocationService;
import Firma.services.GestionMarketplace.TerrainService;
import Firma.services.GestionMarketplace.VehiculeService;
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
    
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
    
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
            
            long activeCount = userLocations.stream()
                .filter(loc -> loc.getStatut() == RentalStatus.EN_COURS || loc.getStatut() == RentalStatus.CONFIRMEE)
                .count();
            long pendingCount = userLocations.stream()
                .filter(loc -> loc.getStatut() == RentalStatus.EN_ATTENTE)
                .count();
            
            BigDecimal totalCautions = userLocations.stream()
                .filter(loc -> loc.getStatut() == RentalStatus.EN_COURS || loc.getStatut() == RentalStatus.CONFIRMEE || loc.getStatut() == RentalStatus.EN_ATTENTE)
                .map(Location::getCaution)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            rentalCountLabel.setText(userLocations.size() + " location(s)");
            activeCountLabel.setText(String.valueOf(activeCount));
            pendingCountLabel.setText(String.valueOf(pendingCount));
            totalCautionsLabel.setText(String.format("%.2f DT", totalCautions));
            
            for (Location location : userLocations) {
                StackPane card = createRentalCard(location);
                rentalsContainer.getChildren().add(card);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading rentals: " + e.getMessage());
            showEmptyState("Erreur de chargement");
        }
    }
    
    private void showEmptyState(String message) {
        emptyLabel.setText(message);
        emptyLabel.setVisible(true);
        emptyLabel.setManaged(true);
        rentalCountLabel.setText("0 location(s)");
        activeCountLabel.setText("0");
        pendingCountLabel.setText("0");
        totalCautionsLabel.setText("0.00 DT");
    }

    
    private StackPane createRentalCard(Location location) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-padding: 12;");
        
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        SVGPath typeIcon = new SVGPath();
        String iconColor;
        String itemName = "Location #" + location.getId();
        
        if ("vehicule".equalsIgnoreCase(location.getTypeLocation())) {
            typeIcon.setContent("M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99z");
            iconColor = "#e74c3c";
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
        
        Label nameLabel = new Label(itemName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        Label statusBadge = createStatusBadge(location.getStatut());
        
        topRow.getChildren().addAll(typeIcon, nameLabel, statusBadge);
        
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateText = location.getDateDebut().format(dateFormat) + " → " + location.getDateFin().format(dateFormat);
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label durationLabel = new Label(location.getDureeJours() + " jour(s)");
        durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        
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
        
        if (location.getNumeroLocation() != null && !location.getNumeroLocation().isEmpty()) {
            Label refLabel = new Label("Réf: " + location.getNumeroLocation());
            refLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
            card.getChildren().addAll(topRow, dateLabel, durationLabel, priceRow, refLabel);
        } else {
            card.getChildren().addAll(topRow, dateLabel, durationLabel, priceRow);
        }
        
        StackPane cardContainer = new StackPane();
        cardContainer.getChildren().add(card);
        
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
    
    private void handleDeleteRental(Location location) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la location");
        confirm.setHeaderText("Voulez-vous supprimer cette location de votre historique ?");
        confirm.setContentText("Location #" + location.getId() + " - " + location.getTypeLocation());
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                makeProductAvailable(location);
                locationService.deleteEntity(location);
                loadRentals();
            } catch (SQLException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText(null);
                error.setContentText("Impossible de supprimer la location: " + e.getMessage());
                error.showAndWait();
            }
        }
    }
    
    private void makeProductAvailable(Location location) {
        try {
            String type = location.getTypeLocation();
            if ("vehicule".equalsIgnoreCase(type)) {
                List<Vehicule> vehicules = vehiculeService.getEntities();
                for (Vehicule v : vehicules) {
                    if (v.getId() == location.getElementId()) {
                        v.setDisponible(true);
                        vehiculeService.updateEntity(v);
                        break;
                    }
                }
            } else if ("terrain".equalsIgnoreCase(type)) {
                List<Terrain> terrains = terrainService.getEntities();
                for (Terrain t : terrains) {
                    if (t.getId() == location.getElementId()) {
                        t.setDisponible(true);
                        terrainService.updateEntity(t);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error making product available: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
