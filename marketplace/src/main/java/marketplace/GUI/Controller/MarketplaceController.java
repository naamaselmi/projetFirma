package marketplace.GUI.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import marketplace.service.StatisticsService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class MarketplaceController implements Initializable {

    @FXML
    private ImageView imgEquipements;

    @FXML
    private ImageView imgFournisseurs;

    @FXML
    private ImageView imgTerrains;

    @FXML
    private ImageView imgVehicules;
    
    // Statistics Labels
    @FXML private Label totalEquipLabel;
    @FXML private Label totalVehicleLabel;
    @FXML private Label totalTerrainLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label lastUpdateLabel;
    
    // Charts
    @FXML private PieChart stockStatusChart;
    @FXML private PieChart ordersStatusChart;
    @FXML private PieChart rentalTypeChart;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> categoryChart;
    
    private StatisticsService statisticsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statisticsService = StatisticsService.getInstance();
        
        // Load images
        try {
            setImage(imgEquipements, "/image/i4.png");
            setImage(imgTerrains, "/image/i2.png");
            setImage(imgVehicules, "/image/i1.png");
            setImage(imgFournisseurs, "/image/i3.png");
        } catch (Exception e) {
            System.err.println("Images not found or error loading them.");
        }
        
        // Load statistics
        loadStatistics();
    }
    
    /**
     * Load all statistics and populate charts
     */
    private void loadStatistics() {
        try {
            // Load summary counts
            loadSummaryCounts();
            
            // Load charts
            loadStockStatusChart();
            loadOrdersStatusChart();
            loadRentalTypeChart();
            loadRevenueChart();
            loadCategoryChart();
            
            // Update timestamp
            if (lastUpdateLabel != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                lastUpdateLabel.setText("Dernière mise à jour: " + timestamp);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load summary count cards
     */
    private void loadSummaryCounts() {
        try {
            Map<String, Integer> counts = statisticsService.getTotalCounts();
            
            if (totalEquipLabel != null) {
                totalEquipLabel.setText(String.valueOf(counts.getOrDefault("equipements", 0)));
            }
            if (totalVehicleLabel != null) {
                totalVehicleLabel.setText(String.valueOf(counts.getOrDefault("vehicules", 0)));
            }
            if (totalTerrainLabel != null) {
                totalTerrainLabel.setText(String.valueOf(counts.getOrDefault("terrains", 0)));
            }
            if (totalOrdersLabel != null) {
                totalOrdersLabel.setText(String.valueOf(counts.getOrDefault("commandes", 0)));
            }
            
            // Revenue
            BigDecimal revenue = statisticsService.getTotalRevenue();
            if (totalRevenueLabel != null) {
                totalRevenueLabel.setText(String.format("%.0f DT", revenue));
            }
            
        } catch (Exception e) {
            System.err.println("Error loading counts: " + e.getMessage());
        }
    }
    
    /**
     * Load stock status pie chart
     */
    private void loadStockStatusChart() {
        if (stockStatusChart == null) return;
        
        try {
            Map<String, Integer> data = statisticsService.getEquipmentStockStatus();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            
            stockStatusChart.setData(pieData);
            stockStatusChart.setTitle("");
            
            // Apply colors
            applyPieChartColors(stockStatusChart, new String[]{"#27ae60", "#f39c12", "#e74c3c"});
            
        } catch (Exception e) {
            System.err.println("Error loading stock chart: " + e.getMessage());
        }
    }
    
    /**
     * Load orders status pie chart
     */
    private void loadOrdersStatusChart() {
        if (ordersStatusChart == null) return;
        
        try {
            Map<String, Integer> data = statisticsService.getOrdersByPaymentStatus();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            
            ordersStatusChart.setData(pieData);
            ordersStatusChart.setTitle("");
            
            // Apply colors
            applyPieChartColors(ordersStatusChart, new String[]{"#f39c12", "#27ae60", "#3498db", "#e74c3c"});
            
        } catch (Exception e) {
            System.err.println("Error loading orders chart: " + e.getMessage());
        }
    }
    
    /**
     * Load rental type pie chart
     */
    private void loadRentalTypeChart() {
        if (rentalTypeChart == null) return;
        
        try {
            Map<String, Integer> data = statisticsService.getRentalsByType();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }
            
            if (pieData.isEmpty()) {
                pieData.add(new PieChart.Data("Aucune location", 1));
            }
            
            rentalTypeChart.setData(pieData);
            rentalTypeChart.setTitle("");
            
            // Apply colors
            applyPieChartColors(rentalTypeChart, new String[]{"#e74c3c", "#27ae60"});
            
        } catch (Exception e) {
            System.err.println("Error loading rental chart: " + e.getMessage());
        }
    }
    
    /**
     * Load monthly revenue line chart
     */
    private void loadRevenueChart() {
        if (revenueChart == null) return;
        
        try {
            Map<String, BigDecimal> data = statisticsService.getMonthlyRevenue();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenus");
            
            for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            revenueChart.getData().clear();
            revenueChart.getData().add(series);
            revenueChart.setLegendVisible(false);
            
            // Style the line - make it thicker and more visible
            revenueChart.setCreateSymbols(true);
            
            // Apply thicker line style after adding data
            revenueChart.applyCss();
            if (!series.getData().isEmpty()) {
                javafx.scene.Node line = series.getNode().lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke-width: 4px; -fx-stroke: #e74c3c;");
                }
                // Make symbols bigger
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 6px; -fx-padding: 6px;");
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading revenue chart: " + e.getMessage());
        }
    }
    
    /**
     * Load equipment by category bar chart
     */
    private void loadCategoryChart() {
        if (categoryChart == null) return;
        
        try {
            Map<String, Integer> data = statisticsService.getEquipmentCountByCategory();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Équipements");
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                // Truncate long category names
                String name = entry.getKey();
                if (name != null && name.length() > 15) {
                    name = name.substring(0, 12) + "...";
                }
                series.getData().add(new XYChart.Data<>(name, entry.getValue()));
            }
            
            categoryChart.getData().clear();
            categoryChart.getData().add(series);
            categoryChart.setLegendVisible(false);
            
        } catch (Exception e) {
            System.err.println("Error loading category chart: " + e.getMessage());
        }
    }
    
    /**
     * Apply custom colors to pie chart slices
     */
    private void applyPieChartColors(PieChart chart, String[] colors) {
        // Use Platform.runLater to ensure nodes are created before styling
        Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data data : chart.getData()) {
                if (data.getNode() != null) {
                    String color = colors[i % colors.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
                i++;
            }
            
            // Also update legend colors to match
            for (javafx.scene.Node node : chart.lookupAll(".chart-legend-item-symbol")) {
                for (String styleClass : node.getStyleClass()) {
                    if (styleClass.startsWith("data")) {
                        int index = Integer.parseInt(styleClass.replace("data", ""));
                        if (index < colors.length) {
                            node.setStyle("-fx-background-color: " + colors[index] + ";");
                        }
                    }
                }
            }
        });
    }

    private void setImage(ImageView imageView, String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            imageView.setImage(new Image(url.toExternalForm()));
        }
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));

            // Get the scene and find the contentArea StackPane
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene scene = source.getScene();
            
            if (scene != null) {
                // Look for the contentArea StackPane by traversing from root
                Parent root = scene.getRoot();
                StackPane contentArea = findContentArea(root);
                
                if (contentArea != null) {
                    contentArea.getChildren().setAll(view);
                } else {
                    System.err.println("Could not find Content Area (StackPane) to navigate.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FXML file: " + fxmlPath);
        }
    }
    
    /**
     * Recursively find the contentArea StackPane in the scene graph
     */
    private StackPane findContentArea(Parent parent) {
        // Check if this is a StackPane with the contentArea style or ID
        if (parent instanceof StackPane) {
            StackPane sp = (StackPane) parent;
            // The contentArea is the main content area in AdminDashboard (center of BorderPane)
            // It has padding and background color
            if (sp.getId() != null && sp.getId().equals("contentArea")) {
                return sp;
            }
        }
        
        // Search children
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                StackPane found = findContentArea((Parent) child);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }

    // Helper for placeholder navigation
    private void showPlaceholder(ActionEvent event, String title) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.scene.Scene scene = source.getScene();
        
        if (scene != null) {
            Parent root = scene.getRoot();
            StackPane contentArea = findContentArea(root);
            
            if (contentArea != null) {
                Label label = new Label("Gestion " + title);
                label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
                contentArea.getChildren().clear();
                contentArea.getChildren().add(label);
            }
        }
    }

    @FXML
    void handleEquipements(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/EquipementView.fxml");
    }

    @FXML
    void handleFournisseurs(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/FournisseurView.fxml");
    }

    @FXML
    void handleTerrains(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/TerrainView.fxml");
    }

    @FXML
    void handleVehicules(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/VehiculeView.fxml");
    }

    @FXML
    void handleLocations(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/LocationAdminView.fxml");
    }

    @FXML
    void handleCommandes(ActionEvent event) {
        navigateTo(event, "/marketplace/GUI/views/CommandeAdminView.fxml");
    }
}
