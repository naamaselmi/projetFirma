package Firma.controllers.GestionMarketplace;

import Firma.services.GestionEvenement.StatistiquesService;
import Firma.services.GestionMarketplace.StatisticsService;
import Firma.tools.GestionEvenement.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Admin Accueil (Home) dashboard content panel.
 * Loads KPIs, charts and statistics from both Événements and Marketplace modules.
 */
public class AdminAccueilController implements Initializable {

    // ── FXML bindings ──
    @FXML private Label lblWelcome;
    @FXML private Label lblDate;
    @FXML private HBox shortcutContainer;

    // Event section
    @FXML private FlowPane eventKpiPane;
    @FXML private PieChart chartEventType;
    @FXML private BarChart<String, Number> chartParticipations;
    @FXML private VBox topEventsBox;

    // Marketplace section
    @FXML private FlowPane marketKpiPane;
    @FXML private PieChart chartDeliveryStatus;
    @FXML private PieChart chartStockCategory;
    @FXML private BarChart<String, Number> chartRevenue;

    // ── Services ──
    private final StatistiquesService eventStats = new StatistiquesService();
    private final StatisticsService marketStats = StatisticsService.getInstance();

    // ── Colours for KPI cards ──
    private static final String GREEN  = "#49ad32";
    private static final String ORANGE = "#e67e22";
    private static final String BLUE   = "#3498db";
    private static final String RED    = "#e74c3c";
    private static final String PURPLE = "#9b59b6";
    private static final String TEAL   = "#1abc9c";

    // ── Charts colours applied via CSS ──
    private static final String[] PIE_COLORS = {
        "#49ad32", "#3498db", "#e67e22", "#9b59b6", "#e74c3c", "#1abc9c", "#f39c12", "#2ecc71"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadWelcome();
        buildShortcuts();
        loadEventKPIs();
        loadEventCharts();
        loadTopEvents();
        loadMarketplaceKPIs();
        loadMarketplaceCharts();
        loadRevenueChart();
    }

    // ================================================================
    //  WELCOME
    // ================================================================
    private void loadWelcome() {
        try {
            var user = SessionManager.getInstance().getUtilisateur();
            if (user != null) {
                lblWelcome.setText("Bienvenue, " + user.getPrenom() + " " + user.getNom() + " 👋");
            } else {
                lblWelcome.setText("Bienvenue, Administrateur 👋");
            }
        } catch (Exception e) {
            lblWelcome.setText("Bienvenue, Administrateur 👋");
        }
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
    }

    // ================================================================
    //  SHORTCUT BUTTONS
    // ================================================================
    private void buildShortcuts() {
        shortcutContainer.getChildren().clear();
        shortcutContainer.setAlignment(Pos.CENTER);
        shortcutContainer.setSpacing(15);

        // label, colour, sidebar-button fx:id
        String[][] shortcuts = {
            {"📅  Événements",  GREEN,  "#btnEvenement"},
            {"🛒  Marketplace", ORANGE, "#btnMarketplace"},
            {"💬  Forum",       BLUE,   "#btnForum"},
            {"🔧  Technicien",  PURPLE, "#btnTechnicien"}
        };

        for (String[] s : shortcuts) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(200);
            card.setPrefHeight(70);
            card.setStyle(
                "-fx-background-color: " + s[1] + ";" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 6, 0, 0, 3);"
            );
            card.setPadding(new Insets(10, 15, 10, 15));

            Label lbl = new Label(s[0]);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");
            card.getChildren().add(lbl);

            // Hover effect
            String base = card.getStyle();
            card.setOnMouseEntered(e -> card.setStyle(base + "-fx-opacity: 0.85;"));
            card.setOnMouseExited(e -> card.setStyle(base));

            // Click → fire the matching sidebar button
            String sidebarId = s[2];
            card.setOnMouseClicked(e -> {
                Button sidebarBtn = (Button) shortcutContainer.getScene().lookup(sidebarId);
                if (sidebarBtn != null) sidebarBtn.fire();
            });

            shortcutContainer.getChildren().add(card);
        }
    }

    // ================================================================
    //  EVENT KPIs
    // ================================================================
    private void loadEventKPIs() {
        eventKpiPane.getChildren().clear();
        try {
            int total     = eventStats.countEvenements();
            int actifs    = eventStats.countEvenementsActifs();
            int confirmes = eventStats.countParticipationsConfirmees();
            int attente   = eventStats.countParticipationsEnAttente();
            double taux   = eventStats.tauxRemplissageMoyen();
            int semaine   = eventStats.evenementsCetteSemaine();

            eventKpiPane.getChildren().addAll(
                makeKpiCard("Total événements",   String.valueOf(total),     GREEN),
                makeKpiCard("Événements actifs",   String.valueOf(actifs),    BLUE),
                makeKpiCard("Participations OK",   String.valueOf(confirmes), GREEN),
                makeKpiCard("En attente",          String.valueOf(attente),   ORANGE),
                makeKpiCardWithBar("Taux remplissage", String.format("%.1f %%", taux), taux / 100.0, TEAL),
                makeKpiCard("Cette semaine",       String.valueOf(semaine),   PURPLE)
            );
        } catch (Exception e) {
            eventKpiPane.getChildren().add(errorLabel("Impossible de charger les KPI événements"));
            e.printStackTrace();
        }
    }

    // ================================================================
    //  EVENT CHARTS
    // ================================================================
    private void loadEventCharts() {
        try {
            // Pie: répartition par type
            Map<String, Integer> types = eventStats.repartitionParType();
            chartEventType.setData(FXCollections.observableArrayList());
            types.forEach((k, v) -> chartEventType.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
            applyPieColors(chartEventType);

            // Bar: participations par mois
            Map<String, Integer> monthly = eventStats.participationsParMois();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Participations");
            monthly.forEach((m, v) -> series.getData().add(new XYChart.Data<>(m, v)));
            chartParticipations.getData().clear();
            chartParticipations.getData().add(series);
            applyBarColor(chartParticipations, GREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    //  TOP EVENTS
    // ================================================================
    private void loadTopEvents() {
        try {
            Map<String, Integer> top = eventStats.topEvenements();
            int rank = 1;
            for (Map.Entry<String, Integer> entry : top.entrySet()) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 10, 6, 10));
                row.setStyle("-fx-background-color: " + (rank % 2 == 0 ? "#f9f9f9" : "white") + "; -fx-background-radius: 6;");

                Label rankLbl = new Label("#" + rank);
                rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: " + GREEN + "; -fx-min-width: 30;");

                Label nameLbl = new Label(entry.getKey());
                nameLbl.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
                HBox.setHgrow(nameLbl, Priority.ALWAYS);

                Label countLbl = new Label(entry.getValue() + " participations");
                countLbl.setStyle("-fx-font-size: 13; -fx-text-fill: #888;");

                row.getChildren().addAll(rankLbl, nameLbl, countLbl);
                topEventsBox.getChildren().add(row);
                rank++;
            }
            if (top.isEmpty()) {
                topEventsBox.getChildren().add(new Label("Aucun événement trouvé"));
            }
        } catch (Exception e) {
            topEventsBox.getChildren().add(errorLabel("Impossible de charger le classement"));
            e.printStackTrace();
        }
    }

    // ================================================================
    //  MARKETPLACE KPIs
    // ================================================================
    private void loadMarketplaceKPIs() {
        marketKpiPane.getChildren().clear();
        try {
            Map<String, Integer> counts = marketStats.getTotalCounts();
            BigDecimal revenue = marketStats.getTotalRevenue();
            BigDecimal eqValue = marketStats.getTotalEquipmentValue();
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRENCH);

            marketKpiPane.getChildren().addAll(
                makeKpiCard("Équipements",  String.valueOf(counts.getOrDefault("equipements", 0)), ORANGE),
                makeKpiCard("Véhicules",    String.valueOf(counts.getOrDefault("vehicules", 0)),    BLUE),
                makeKpiCard("Terrains",     String.valueOf(counts.getOrDefault("terrains", 0)),     TEAL),
                makeKpiCard("Fournisseurs", String.valueOf(counts.getOrDefault("fournisseurs", 0)), PURPLE),
                makeKpiCard("Commandes",    String.valueOf(counts.getOrDefault("commandes", 0)),    GREEN),
                makeKpiCard("Locations",    String.valueOf(counts.getOrDefault("locations", 0)),    RED),
                makeKpiCard("Revenu total", nf.format(revenue) + " TND",                           GREEN),
                makeKpiCard("Valeur stock", nf.format(eqValue) + " TND",                           ORANGE)
            );
        } catch (Exception e) {
            marketKpiPane.getChildren().add(errorLabel("Impossible de charger les KPI marketplace"));
            e.printStackTrace();
        }
    }

    // ================================================================
    //  MARKETPLACE CHARTS
    // ================================================================
    private void loadMarketplaceCharts() {
        try {
            // Delivery status pie
            Map<String, Integer> delivery = marketStats.getOrdersByDeliveryStatus();
            chartDeliveryStatus.setData(FXCollections.observableArrayList());
            delivery.forEach((k, v) -> chartDeliveryStatus.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
            applyPieColors(chartDeliveryStatus);

            // Stock category pie
            Map<String, Integer> stock = marketStats.getEquipmentCountByCategory();
            chartStockCategory.setData(FXCollections.observableArrayList());
            stock.forEach((k, v) -> chartStockCategory.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
            applyPieColors(chartStockCategory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    //  REVENUE CHART
    // ================================================================
    private void loadRevenueChart() {
        try {
            Map<String, BigDecimal> monthly = marketStats.getMonthlyRevenue();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenu");
            monthly.forEach((m, v) -> series.getData().add(new XYChart.Data<>(m, v.doubleValue())));
            chartRevenue.getData().clear();
            chartRevenue.getData().add(series);
            applyBarColor(chartRevenue, ORANGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================================================================
    //  UI HELPERS
    // ================================================================

    private VBox makeKpiCard(String title, String value, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(185);
        card.setPrefHeight(95);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + color + "22;" +
            "-fx-border-width: 0 0 3 0;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        );

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #777;");

        card.getChildren().addAll(valLbl, titleLbl);
        return card;
    }

    private VBox makeKpiCardWithBar(String title, String value, double progress, String color) {
        VBox card = makeKpiCard(title, value, color);
        ProgressBar bar = new ProgressBar(Math.min(progress, 1.0));
        bar.setPrefWidth(150);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: " + color + ";");
        card.getChildren().add(bar);
        card.setPrefHeight(115);
        return card;
    }

    private Label errorLabel(String msg) {
        Label lbl = new Label("⚠ " + msg);
        lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13;");
        return lbl;
    }

    private void applyPieColors(PieChart chart) {
        // Delay colour application to after CSS is applied
        chart.layout();
        for (int i = 0; i < chart.getData().size(); i++) {
            PieChart.Data d = chart.getData().get(i);
            String colour = PIE_COLORS[i % PIE_COLORS.length];
            d.getNode().setStyle("-fx-pie-color: " + colour + ";");
        }
    }

    private void applyBarColor(BarChart<String, Number> chart, String color) {
        chart.layout();
        chart.lookupAll(".default-color0.chart-bar")
             .forEach(node -> node.setStyle("-fx-bar-fill: " + color + ";"));
    }
}
