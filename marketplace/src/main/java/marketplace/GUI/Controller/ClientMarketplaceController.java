package marketplace.GUI.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import marketplace.entities.Categorie;
import marketplace.entities.Equipement;
import marketplace.entities.ProductType;
import marketplace.entities.Terrain;
import marketplace.entities.Vehicule;
import marketplace.service.CategorieService;
import marketplace.service.EquipementService;
import marketplace.service.TerrainService;
import marketplace.service.VehiculeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Client Marketplace View
 * Displays products in a grid layout similar to Facebook Marketplace
 */
public class ClientMarketplaceController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterTypeCombo;

    @FXML
    private ComboBox<Categorie> filterCategoryCombo;

    @FXML
    private FlowPane productsGrid;

    @FXML
    private Label countLabel;
    
    private Object parentController;

    // Services
    private EquipementService equipementService;
    private VehiculeService vehiculeService;
    private TerrainService terrainService;
    private CategorieService categorieService;

    // Products lists
    private List<Equipement> allEquipements = new ArrayList<>();
    private List<Vehicule> allVehicules = new ArrayList<>();
    private List<Terrain> allTerrains = new ArrayList<>();
    private List<Categorie> allCategories = new ArrayList<>();

    // Combined products for display
    private List<ProductItem> allProducts = new ArrayList<>();
    private List<ProductItem> filteredProducts = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        equipementService = new EquipementService();
        vehiculeService = new VehiculeService();
        terrainService = new TerrainService();
        categorieService = new CategorieService();

        // Setup filter combos
        setupFilterCombos();

        // Load all products
        loadAllProducts();
    }
    
    /**
     * Set the parent controller for navigation purposes
     */
    public void setParentController(Object controller) {
        this.parentController = controller;
    }

    private void setupFilterCombos() {
        // Product type filter
        filterTypeCombo.getItems().addAll("Tous", "Équipement", "Véhicule", "Terrain");
        filterTypeCombo.setValue("Tous");

        // Load categories
        try {
            allCategories = categorieService.getEntities();
            filterCategoryCombo.getItems().add(null); // "All" option
            filterCategoryCombo.getItems().addAll(allCategories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllProducts() {
        allProducts.clear();

        try {
            // Load equipements
            allEquipements = equipementService.getEntities();
            for (Equipement e : allEquipements) {
                if (e.isDisponible()) {
                    allProducts.add(new ProductItem(
                            e.getId(),
                            e.getNom(),
                            e.getDescription(),
                            e.getPrixVente(),
                            "Équipement",
                            e.getCategorieId(),
                            e.getImageUrl(),
                            e
                    ));
                }
            }

            // Load vehicules
            allVehicules = vehiculeService.getEntities();
            for (Vehicule v : allVehicules) {
                if (v.isDisponible()) {
                    allProducts.add(new ProductItem(
                            v.getId(),
                            v.getNom(),
                            v.getDescription(),
                            v.getPrixJour(),
                            "Véhicule",
                            v.getCategorieId(),
                            v.getImageUrl(),
                            v
                    ));
                }
            }

            // Load terrains
            allTerrains = terrainService.getEntities();
            for (Terrain t : allTerrains) {
                if (t.isDisponible()) {
                    allProducts.add(new ProductItem(
                            t.getId(),
                            t.getTitre(),
                            t.getDescription(),
                            t.getPrixMois(),
                            "Terrain",
                            t.getCategorieId(),
                            t.getImageUrl(),
                            t
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        filteredProducts = new ArrayList<>(allProducts);
        displayProducts();
    }

    private void displayProducts() {
        productsGrid.getChildren().clear();

        for (ProductItem product : filteredProducts) {
            VBox card = createProductCard(product);
            productsGrid.getChildren().add(card);
        }

        countLabel.setText(filteredProducts.size() + " produit" + (filteredProducts.size() > 1 ? "s" : ""));
    }

    private VBox createProductCard(ProductItem product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(220);
        card.setPrefHeight(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        card.setPadding(new Insets(0, 0, 15, 0));
        card.setCursor(Cursor.HAND);

        // Drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        // Image container
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(220, 160);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 12 12 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        // Try to load image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                String imageUrl = product.getImageUrl();
                if (imageUrl.startsWith("file:") || imageUrl.startsWith("http")) {
                    imageView.setImage(new Image(imageUrl, true));
                } else {
                    // Try from resources
                    URL resourceUrl = getClass().getResource("/image/" + imageUrl);
                    if (resourceUrl != null) {
                        imageView.setImage(new Image(resourceUrl.toExternalForm()));
                    }
                }
            } catch (Exception e) {
                // Use placeholder
                setPlaceholderImage(imageView, product.getType());
            }
        } else {
            setPlaceholderImage(imageView, product.getType());
        }

        imageContainer.getChildren().add(imageView);

        // Product type badge
        Label typeBadge = new Label(product.getType());
        typeBadge.setStyle(getTypeBadgeStyle(product.getType()));
        typeBadge.setPadding(new Insets(3, 10, 3, 10));
        StackPane.setAlignment(typeBadge, Pos.TOP_LEFT);
        StackPane.setMargin(typeBadge, new Insets(10, 0, 0, 10));
        imageContainer.getChildren().add(typeBadge);

        // Product info
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10, 15, 0, 15));
        infoBox.setAlignment(Pos.TOP_LEFT);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190);

        Label priceLabel = new Label(formatPrice(product.getPrice(), product.getType()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");

        // Category label
        String categoryName = getCategoryName(product.getCategorieId());
        Label categoryLabel = new Label(categoryName);
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");

        infoBox.getChildren().addAll(nameLabel, priceLabel, categoryLabel);

        card.getChildren().addAll(imageContainer, infoBox);

        // Hover effect
        card.setOnMouseEntered(e -> {
            shadow.setRadius(15);
            shadow.setOffsetY(5);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });

        card.setOnMouseExited(e -> {
            shadow.setRadius(10);
            shadow.setOffsetY(3);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        });

        // Click handler - navigate to detail view
        card.setOnMouseClicked(e -> navigateToProductDetail(product));

        return card;
    }

    private void setPlaceholderImage(ImageView imageView, String type) {
        String imageName;
        switch (type) {
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
            URL url = getClass().getResource("/image/" + imageName);
            if (url != null) {
                imageView.setImage(new Image(url.toExternalForm()));
            }
        } catch (Exception ignored) {
        }
    }

    private String getTypeBadgeStyle(String type) {
        String bgColor;
        switch (type) {
            case "Équipement":
                bgColor = "#3498db";
                break;
            case "Véhicule":
                bgColor = "#e74c3c";
                break;
            case "Terrain":
                bgColor = "#27ae60";
                break;
            default:
                bgColor = "#95a5a6";
        }
        return "-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4;";
    }

    private String formatPrice(BigDecimal price, String type) {
        if (price == null) return "Prix non disponible";
        String formattedPrice = String.format("%.2f DT", price);
        switch (type) {
            case "Véhicule":
                return formattedPrice + " /jour";
            case "Terrain":
                return formattedPrice + " /mois";
            default:
                return formattedPrice;
        }
    }

    private String getCategoryName(int categorieId) {
        for (Categorie cat : allCategories) {
            if (cat.getId() == categorieId) {
                return cat.getNom();
            }
        }
        return "Non catégorisé";
    }

    private void navigateToProductDetail(ProductItem product) {
        try {
            System.out.println("Navigating to product detail: " + product.getName());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/marketplace/GUI/views/ProductDetailView.fxml"));
            Parent view = loader.load();

            // Pass the product and parent controller to the detail controller
            ProductDetailController controller = loader.getController();
            controller.setProduct(product);
            controller.setParentController(parentController);

            // Navigate by finding the parent StackPane
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                System.out.println("Content area found, loading detail view");
                contentArea.getChildren().setAll(view);
            } else {
                System.err.println("Content area not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load product detail view: " + e.getMessage());
        }
    }

    private StackPane findContentArea() {
        javafx.scene.Node node = productsGrid;
        System.out.println("Starting from: " + node.getClass().getSimpleName());
        
        while (node != null) {
            node = node.getParent();
            if (node != null) {
                System.out.println("Traversing: " + node.getClass().getSimpleName() + " (id=" + node.getId() + ")");
                if (node instanceof StackPane) {
                    StackPane sp = (StackPane) node;
                    if ("contentArea".equals(sp.getId())) {
                        System.out.println("Found contentArea!");
                        return sp;
                    }
                }
            }
        }
        
        // Fallback: find any parent StackPane
        System.out.println("Fallback: looking for any StackPane...");
        node = productsGrid;
        while (node != null) {
            node = node.getParent();
            if (node instanceof StackPane) {
                System.out.println("Found StackPane: " + node.getId());
                return (StackPane) node;
            }
        }
        return null;
    }

    @FXML
    void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void handleFilterChange(ActionEvent event) {
        // Check if type combo triggered the change - only update categories then
        if (event.getSource() == filterTypeCombo) {
            String selectedType = filterTypeCombo.getValue();
            updateCategoryComboForType(selectedType);
        }
        applyFilters();
    }
    
    // Flag to prevent recursion when updating category combo
    private boolean isUpdatingCategoryCombo = false;
    
    /**
     * Update the category combo to show only categories for the selected product type
     */
    private void updateCategoryComboForType(String selectedType) {
        if (isUpdatingCategoryCombo) return;
        isUpdatingCategoryCombo = true;
        
        try {
            Categorie previousSelection = filterCategoryCombo.getValue();
            filterCategoryCombo.getItems().clear();
            filterCategoryCombo.getItems().add(null); // "All" option
            
            if (selectedType == null || "Tous".equals(selectedType)) {
                // Show all categories
                filterCategoryCombo.getItems().addAll(allCategories);
            } else {
                // Filter categories by product type
                ProductType productType = mapTypeStringToProductType(selectedType);
                if (productType != null) {
                    List<Categorie> filteredCategories = allCategories.stream()
                        .filter(cat -> cat.getTypeProduit() == productType)
                        .collect(Collectors.toList());
                    filterCategoryCombo.getItems().addAll(filteredCategories);
                }
            }
            
            // Restore previous selection if still valid, otherwise reset
            if (previousSelection != null && filterCategoryCombo.getItems().contains(previousSelection)) {
                filterCategoryCombo.setValue(previousSelection);
            } else {
                filterCategoryCombo.setValue(null);
            }
        } finally {
            isUpdatingCategoryCombo = false;
        }
    }
    
    /**
     * Map UI type string to ProductType enum
     */
    private ProductType mapTypeStringToProductType(String typeString) {
        switch (typeString) {
            case "Équipement": return ProductType.EQUIPEMENT;
            case "Véhicule": return ProductType.VEHICULE;
            case "Terrain": return ProductType.TERRAIN;
            default: return null;
        }
    }

    @FXML
    void handleClearFilters(ActionEvent event) {
        searchField.clear();
        filterTypeCombo.setValue("Tous");
        filterCategoryCombo.setValue(null);
        filteredProducts = new ArrayList<>(allProducts);
        displayProducts();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedType = filterTypeCombo.getValue();
        Categorie selectedCategory = filterCategoryCombo.getValue();

        filteredProducts = allProducts.stream()
                .filter(p -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        boolean matchesName = p.getName().toLowerCase().contains(searchText);
                        boolean matchesDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(searchText);
                        if (!matchesName && !matchesDesc) return false;
                    }

                    // Type filter
                    if (selectedType != null && !selectedType.equals("Tous")) {
                        if (!p.getType().equals(selectedType)) return false;
                    }

                    // Category filter
                    if (selectedCategory != null) {
                        if (p.getCategorieId() != selectedCategory.getId()) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        displayProducts();
    }

    /**
     * Inner class to represent a unified product item
     */
    public static class ProductItem {
        private int id;
        private String name;
        private String description;
        private BigDecimal price;
        private String type;
        private int categorieId;
        private String imageUrl;
        private Object originalEntity;

        public ProductItem(int id, String name, String description, BigDecimal price, 
                           String type, int categorieId, String imageUrl, Object originalEntity) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.type = type;
            this.categorieId = categorieId;
            this.imageUrl = imageUrl;
            this.originalEntity = originalEntity;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public BigDecimal getPrice() { return price; }
        public String getType() { return type; }
        public int getCategorieId() { return categorieId; }
        public String getImageUrl() { return imageUrl; }
        public Object getOriginalEntity() { return originalEntity; }
    }
}
