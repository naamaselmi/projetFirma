package marketplace.GUI.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import marketplace.entities.Equipement;
import marketplace.entities.ProductType;
import marketplace.entities.Terrain;
import marketplace.entities.Vehicule;
import marketplace.service.CartService;
import marketplace.service.CategorieService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class ProductDetailController {
    
    @FXML private ImageView productImage;
    @FXML private StackPane imageContainer;
    @FXML private Label typeBadge;
    @FXML private Label productName;
    @FXML private Label priceLabel;
    @FXML private Label priceSubLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox detailsBox;
    @FXML private javafx.scene.control.Button actionButton;
    @FXML private SVGPath actionIcon;
    @FXML private VBox cautionBox;
    @FXML private Label cautionLabel;
    
    // Quantity selector for equipment
    @FXML private HBox quantityBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Label stockInfoLabel;
    
    // Rental date pickers for vehicles/terrains
    @FXML private VBox rentalDatesBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label rentalDurationLabel;
    
    private ClientMarketplaceController.ProductItem currentProduct;
    private Object parentController;
    private CategorieService categorieService;
    private CartService cartService;
    
    @FXML
    public void initialize() {
        categorieService = new CategorieService();
        cartService = CartService.getInstance();
        
        // Setup date pickers for rental
        if (startDatePicker != null && endDatePicker != null) {
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusDays(1));
            
            // Update duration label when dates change
            startDatePicker.valueProperty().addListener((obs, old, newVal) -> updateRentalDuration());
            endDatePicker.valueProperty().addListener((obs, old, newVal) -> updateRentalDuration());
        }
    }
    
    /**
     * Update the rental duration label based on selected dates
     */
    private void updateRentalDuration() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            
            if (end.isAfter(start)) {
                long days = ChronoUnit.DAYS.between(start, end);
                rentalDurationLabel.setText("Durée: " + days + " jour(s)");
                rentalDurationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #49ad32; -fx-font-weight: bold;");
            } else {
                rentalDurationLabel.setText("La date de fin doit être après la date de début");
                rentalDurationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }
    
    /**
     * Sets the product to display
     */
    public void setProduct(ClientMarketplaceController.ProductItem product) {
        this.currentProduct = product;
        displayProduct();
    }
    
    /**
     * Sets the parent controller for navigation
     */
    public void setParentController(Object controller) {
        this.parentController = controller;
    }
    
    /**
     * Display product details based on type
     */
    private void displayProduct() {
        if (currentProduct == null) return;
        
        // Set image
        loadProductImage();
        
        // Set name
        productName.setText(currentProduct.getName());
        
        // Set type badge with appropriate color
        typeBadge.setText(currentProduct.getType());
        String badgeColor;
        switch (currentProduct.getType()) {
            case "Équipement":
                badgeColor = "#3498db";
                break;
            case "Véhicule":
                badgeColor = "#e74c3c";
                break;
            case "Terrain":
                badgeColor = "#27ae60";
                break;
            default:
                badgeColor = "#49ad32";
        }
        typeBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20;");
        
        // Set price
        priceLabel.setText(String.format("%.2f DT", currentProduct.getPrice()));
        
        // Configure action button and details based on product type
        Object entity = currentProduct.getOriginalEntity();
        
        if (entity instanceof Equipement) {
            displayEquipementDetails((Equipement) entity);
        } else if (entity instanceof Vehicule) {
            displayVehiculeDetails((Vehicule) entity);
        } else if (entity instanceof Terrain) {
            displayTerrainDetails((Terrain) entity);
        }
    }
    
    /**
     * Load product image from URL, file, or resource
     */
    private void loadProductImage() {
        String imageUrl = currentProduct.getImageUrl();
        Image image = null;
        
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    // Web URL
                    image = new Image(imageUrl, true);
                } else if (imageUrl.startsWith("file:")) {
                    // File URL
                    image = new Image(imageUrl, true);
                } else {
                    // Try loading from resources first
                    java.net.URL resourceUrl = getClass().getResource("/image/" + imageUrl);
                    if (resourceUrl != null) {
                        image = new Image(resourceUrl.toExternalForm());
                    } else {
                        // Try as absolute file path
                        File file = new File(imageUrl);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        
        // If no image loaded, use placeholder based on product type
        if (image == null || image.isError()) {
            image = loadPlaceholderImage();
        }
        
        productImage.setImage(image);
    }
    
    /**
     * Load a placeholder image based on product type
     */
    private Image loadPlaceholderImage() {
        String imageName;
        switch (currentProduct.getType()) {
            case "Équipement":
                imageName = "i4.png";
                break;
            case "Véhicule":
                imageName = "i1.png";
                break;
            case "Terrain":
                imageName = "i2.png";
                break;
            default:
                imageName = "i4.png";
        }
        try {
            java.net.URL url = getClass().getResource("/image/" + imageName);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception ignored) {}
        return null;
    }
    
    /**
     * Display details for Equipement (for purchase)
     */
    private void displayEquipementDetails(Equipement equipement) {
        // Configure for purchase - add to cart
        actionButton.setText("Ajouter au panier");
        actionButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-cursor: hand;");
        actionIcon.setContent("M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z");
        
        priceSubLabel.setText("Prix d'achat");
        
        // Hide caution box
        cautionBox.setVisible(false);
        cautionBox.setManaged(false);
        
        // Hide rental dates box
        rentalDatesBox.setVisible(false);
        rentalDatesBox.setManaged(false);
        
        // Show quantity selector
        int stock = equipement.getQuantiteStock();
        if (stock > 0) {
            quantityBox.setVisible(true);
            quantityBox.setManaged(true);
            
            // Setup spinner with stock limit
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, stock, 1);
            quantitySpinner.setValueFactory(valueFactory);
            stockInfoLabel.setText("(" + stock + " en stock)");
        } else {
            quantityBox.setVisible(false);
            quantityBox.setManaged(false);
            actionButton.setDisable(true);
            actionButton.setText("Rupture de stock");
        }
        
        // Set description
        descriptionLabel.setText(equipement.getDescription() != null && !equipement.getDescription().isEmpty() 
            ? equipement.getDescription() 
            : "Aucune description disponible.");
        
        // Add specific details
        clearDetails();
        addDetailRow("Référence", "EQ-" + String.format("%04d", equipement.getId()));
        addDetailRow("Stock disponible", equipement.getQuantiteStock() + " unités");
        
        // Get category name
        if (equipement.getCategorieId() > 0) {
            try {
                var categories = categorieService.getEntities();
                for (var cat : categories) {
                    if (cat.getId() == equipement.getCategorieId()) {
                        addDetailRow("Catégorie", cat.getNom());
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        if (equipement.getFournisseurId() > 0) {
            addDetailRow("Fournisseur ID", String.valueOf(equipement.getFournisseurId()));
        }
    }
    
    /**
     * Display details for Vehicule (for rental)
     */
    private void displayVehiculeDetails(Vehicule vehicule) {
        // Configure for rental - add to cart
        actionButton.setText("Ajouter au panier");
        actionButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-cursor: hand;");
        actionIcon.setContent("M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z");
        
        priceSubLabel.setText("Prix par jour");
        
        // Show caution box
        cautionBox.setVisible(true);
        cautionBox.setManaged(true);
        cautionLabel.setText("Caution: " + String.format("%.2f DT", vehicule.getCaution() != null ? vehicule.getCaution().doubleValue() : 0.0));
        
        // Hide quantity box
        quantityBox.setVisible(false);
        quantityBox.setManaged(false);
        
        // Show rental date pickers if available
        if (vehicule.isDisponible()) {
            rentalDatesBox.setVisible(true);
            rentalDatesBox.setManaged(true);
            updateRentalDuration();
        } else {
            rentalDatesBox.setVisible(false);
            rentalDatesBox.setManaged(false);
            actionButton.setDisable(true);
            actionButton.setText("Non disponible");
        }
        
        // Set description
        String description = String.format("%s - %s", 
            vehicule.getMarque(), 
            vehicule.getModele());
        if (vehicule.getDescription() != null && !vehicule.getDescription().isEmpty()) {
            description += "\n\n" + vehicule.getDescription();
        }
        descriptionLabel.setText(description);
        
        // Add specific details
        clearDetails();
        addDetailRow("Marque", vehicule.getMarque());
        addDetailRow("Modèle", vehicule.getModele());
        addDetailRow("Immatriculation", vehicule.getImmatriculation());
        
        String statutLocation = vehicule.isDisponible() ? "Disponible" : "En location";
        addDetailRow("Statut", statutLocation);
    }
    
    /**
     * Display details for Terrain (for rental)
     */
    private void displayTerrainDetails(Terrain terrain) {
        // Configure for rental - add to cart
        actionButton.setText("Ajouter au panier");
        actionButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-cursor: hand;");
        actionIcon.setContent("M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z");
        
        priceSubLabel.setText("Prix par jour");
        
        // Show caution box
        cautionBox.setVisible(true);
        cautionBox.setManaged(true);
        cautionLabel.setText("Caution: " + String.format("%.2f DT", terrain.getCaution() != null ? terrain.getCaution().doubleValue() : 0.0));
        
        // Hide quantity box
        quantityBox.setVisible(false);
        quantityBox.setManaged(false);
        
        // Show rental date pickers if available
        if (terrain.isDisponible()) {
            rentalDatesBox.setVisible(true);
            rentalDatesBox.setManaged(true);
            updateRentalDuration();
        } else {
            rentalDatesBox.setVisible(false);
            rentalDatesBox.setManaged(false);
            actionButton.setDisable(true);
            actionButton.setText("Non disponible");
        }
        
        // Set description
        descriptionLabel.setText(terrain.getDescription() != null && !terrain.getDescription().isEmpty() 
            ? terrain.getDescription() 
            : "Aucune description disponible.");
        
        // Add specific details
        clearDetails();
        addDetailRow("Adresse", terrain.getAdresse() != null ? terrain.getAdresse() : "Non spécifiée");
        addDetailRow("Superficie", String.format("%.2f hectares", terrain.getSuperficieHectares() != null ? terrain.getSuperficieHectares().doubleValue() : 0.0));
        addDetailRow("Ville", terrain.getVille() != null ? terrain.getVille() : "Non spécifiée");
        
        String statutLocation = terrain.isDisponible() ? "Disponible" : "En location";
        addDetailRow("Statut", statutLocation);
    }
    
    /**
     * Clear all detail rows (except the header)
     */
    private void clearDetails() {
        // Keep only the first child (the header label)
        while (detailsBox.getChildren().size() > 1) {
            detailsBox.getChildren().remove(1);
        }
    }
    
    /**
     * Add a detail row with label and value
     */
    private void addDetailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(5, 0, 5, 10));
        
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666; -fx-min-width: 120;");
        
        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        valueNode.setWrapText(true);
        
        row.getChildren().addAll(labelNode, valueNode);
        detailsBox.getChildren().add(row);
    }
    
    /**
     * Handle back button - return to marketplace
     */
    @FXML
    private void handleBack() {
        try {
            // Navigate back to marketplace view
            if (parentController instanceof ClientDashboardController) {
                ClientDashboardController dashboardController = (ClientDashboardController) parentController;
                dashboardController.handleMarketplace(new javafx.event.ActionEvent());
            }
        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle main action button (Add to Cart)
     */
    @FXML
    private void handleAction() {
        if (currentProduct == null) return;
        
        Object entity = currentProduct.getOriginalEntity();
        
        if (entity instanceof Equipement) {
            // Add equipment to cart
            Equipement equipement = (Equipement) entity;
            int quantity = quantitySpinner.getValue();
            
            boolean added = cartService.addEquipment(equipement, quantity);
            if (added) {
                showSuccessAlert("Équipement ajouté", 
                    currentProduct.getName() + " (x" + quantity + ") a été ajouté au panier.");
            } else {
                showErrorAlert("Stock insuffisant", 
                    "La quantité demandée dépasse le stock disponible.");
            }
        } else if (entity instanceof Vehicule) {
            // Validate dates
            if (!validateRentalDates()) return;
            
            Vehicule vehicule = (Vehicule) entity;
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            boolean added = cartService.addVehicleRental(vehicule, startDate, endDate);
            if (added) {
                long days = ChronoUnit.DAYS.between(startDate, endDate);
                showSuccessAlert("Véhicule ajouté", 
                    currentProduct.getName() + " a été ajouté au panier pour " + days + " jour(s).");
            } else {
                showErrorAlert("Déjà dans le panier", 
                    "Ce véhicule est déjà dans votre panier.");
            }
        } else if (entity instanceof Terrain) {
            // Validate dates
            if (!validateRentalDates()) return;
            
            Terrain terrain = (Terrain) entity;
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            boolean added = cartService.addTerrainRental(terrain, startDate, endDate);
            if (added) {
                long days = ChronoUnit.DAYS.between(startDate, endDate);
                showSuccessAlert("Terrain ajouté", 
                    currentProduct.getName() + " a été ajouté au panier pour " + days + " jour(s).");
            } else {
                showErrorAlert("Déjà dans le panier", 
                    "Ce terrain est déjà dans votre panier.");
            }
        }
    }
    
    /**
     * Validate rental date selection
     */
    private boolean validateRentalDates() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            showErrorAlert("Dates requises", "Veuillez sélectionner les dates de début et de fin.");
            return false;
        }
        
        if (!startDate.isBefore(endDate)) {
            showErrorAlert("Dates invalides", "La date de fin doit être après la date de début.");
            return false;
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            showErrorAlert("Date invalide", "La date de début ne peut pas être dans le passé.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Show success alert
     */
    private void showSuccessAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show error alert
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Handle contact button
     */
    @FXML
    private void handleContact() {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Contacter le vendeur");
        infoAlert.setHeaderText("Fonctionnalité en développement");
        infoAlert.setContentText("La messagerie sera disponible prochainement.\n\nPour le moment, veuillez nous contacter par:\n• Email: contact@marketplace.tn\n• Téléphone: +216 71 XXX XXX");
        infoAlert.showAndWait();
    }
}
