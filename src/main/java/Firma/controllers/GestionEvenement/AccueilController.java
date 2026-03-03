package Firma.controllers.GestionEvenement;

import Firma.entities.GestionEvenement.Utilisateur;
import Firma.services.GestionEvenement.EvenementService;
import Firma.services.GestionEvenement.StatistiquesService;
import Firma.services.GestionMarketplace.StatisticsService;
import Firma.tools.GestionEvenement.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Contrôleur de la page d'Accueil — tableau de bord principal.
 * Affiche des KPIs, graphiques et raccourcis vers les différents modules.
 */
public class AccueilController {

    // ===== FXML ─ Navigation =====
    @FXML private Button btnAccueil;
    @FXML private Button btnEvenement;
    @FXML private Button btnMarketplace;
    @FXML private Button btnForum;
    @FXML private Button btnTechnicien;
    @FXML private Button btnProfil;

    // ===== FXML ─ Welcome =====
    @FXML private Label lblWelcome;
    @FXML private Label lblDate;

    // ===== FXML ─ KPI panes =====
    @FXML private FlowPane eventKpiPane;
    @FXML private FlowPane marketKpiPane;

    // ===== FXML ─ Charts =====
    @FXML private PieChart chartEventType;
    @FXML private BarChart<String, Number> chartParticipations;
    @FXML private PieChart chartOrderStatus;
    @FXML private PieChart chartStockCategory;

    // ===== FXML ─ Lists =====
    @FXML private VBox upcomingEventsBox;
    @FXML private VBox topEventsBox;

    // ===== Services =====
    private final StatistiquesService eventStats = new StatistiquesService();
    private final StatisticsService marketStats = StatisticsService.getInstance();

    // ================================================================
    //  INITIALIZE
    // ================================================================

    @FXML
    public void initialize() {
        loadWelcome();
        loadEventKPIs();
        loadEventCharts();
        loadUpcomingEvents();
        loadTopEvents();
        loadMarketplaceKPIs();
        loadMarketplaceCharts();
    }

    // ================================================================
    //  WELCOME
    // ================================================================

    private void loadWelcome() {
        Utilisateur u = SessionManager.getInstance().getUtilisateur();
        if (u != null) {
            lblWelcome.setText("Bienvenue, " + u.getPrenom() + " " + u.getNom() + " 👋");
        } else {
            lblWelcome.setText("Bienvenue 👋");
        }
        lblDate.setText("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
    }

    // ================================================================
    //  EVENT KPIs
    // ================================================================

    private void loadEventKPIs() {
        try {
            int total      = eventStats.countEvenements();
            int actifs     = eventStats.countEvenementsActifs();
            int confirmed  = eventStats.countParticipationsConfirmees();
            int enAttente  = eventStats.countParticipationsEnAttente();
            int semaine    = eventStats.evenementsCetteSemaine();
            double taux    = eventStats.tauxRemplissageMoyen();

            eventKpiPane.getChildren().addAll(
                    makeKpiCard("📊 Total événements",    String.valueOf(total),     "#49ad32"),
                    makeKpiCard("✅ Actifs",               String.valueOf(actifs),     "#27ae60"),
                    makeKpiCard("🎫 Participations",       String.valueOf(confirmed),  "#2980b9"),
                    makeKpiCard("⏳ En attente",           String.valueOf(enAttente),  "#f39c12"),
                    makeKpiCard("📅 Cette semaine",        String.valueOf(semaine),    "#8e44ad"),
                    makeKpiCardWithBar("📈 Remplissage", String.format("%.1f%%", taux), taux / 100.0, "#49ad32")
            );
        } catch (SQLException ex) {
            eventKpiPane.getChildren().add(makeErrorLabel("Impossible de charger les statistiques événements."));
        }
    }

    // ================================================================
    //  EVENT CHARTS
    // ================================================================

    private void loadEventCharts() {
        // PieChart — events by type
        try {
            Map<String, Integer> byType = eventStats.repartitionParType();
            chartEventType.setData(FXCollections.observableArrayList());
            byType.forEach((k, v) -> chartEventType.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
            chartEventType.setStartAngle(90);
        } catch (SQLException ex) {
            System.err.println("Erreur chargement chart type: " + ex.getMessage());
        }

        // BarChart — participations per month
        try {
            Map<String, Integer> parMois = eventStats.participationsParMois();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Participations");
            parMois.forEach((m, v) -> series.getData().add(new XYChart.Data<>(m.length() > 7 ? m.substring(5) : m, v)));
            chartParticipations.getData().clear();
            chartParticipations.getData().add(series);
            chartParticipations.setCategoryGap(6);
            chartParticipations.setBarGap(2);
        } catch (SQLException ex) {
            System.err.println("Erreur chargement chart participations: " + ex.getMessage());
        }
    }

    // ================================================================
    //  UPCOMING EVENTS
    // ================================================================

    private void loadUpcomingEvents() {
        try {
            var events = new EvenementService().getData();
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            var upcoming = events.stream()
                    .filter(e -> e.getDateDebut() != null && !e.getDateDebut().isBefore(today) && !e.getDateDebut().isAfter(nextWeek))
                    .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
                    .limit(5)
                    .toList();

            if (upcoming.isEmpty()) {
                Label lbl = new Label("Aucun événement prévu cette semaine.");
                lbl.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
                upcomingEventsBox.getChildren().add(lbl);
            } else {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
                for (var e : upcoming) {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8, 12, 8, 12));
                    row.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 8;");

                    Label dateLbl = new Label(e.getDateDebut().format(fmt));
                    dateLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #49ad32; -fx-min-width: 60;");

                    Label titreLbl = new Label(e.getTitre());
                    titreLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                    HBox.setHgrow(titreLbl, Priority.ALWAYS);

                    Label lieuLbl = new Label(e.getLieu() != null ? "📍 " + e.getLieu() : "");
                    lieuLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

                    Label statusLbl = new Label(e.getStatut() != null ? e.getStatut().name() : "");
                    statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: #49ad32; -fx-background-radius: 10; -fx-padding: 2 8;");

                    row.getChildren().addAll(dateLbl, titreLbl, lieuLbl, statusLbl);
                    upcomingEventsBox.getChildren().add(row);
                }
            }
        } catch (Exception ex) {
            upcomingEventsBox.getChildren().add(makeErrorLabel("Erreur chargement événements."));
        }
    }

    // ================================================================
    //  TOP EVENTS
    // ================================================================

    private void loadTopEvents() {
        try {
            Map<String, Integer> top = eventStats.topEvenements();
            if (top.isEmpty()) {
                topEventsBox.getChildren().add(new Label("Aucune donnée disponible."));
                return;
            }
            int rank = 1;
            int maxVal = top.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            for (Map.Entry<String, Integer> entry : top.entrySet()) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 12, 6, 12));

                String medal = rank <= 3 ? new String[]{"🥇", "🥈", "🥉"}[rank - 1] : "  " + rank + ".";
                Label rankLbl = new Label(medal);
                rankLbl.setStyle("-fx-font-size: 16px; -fx-min-width: 32;");

                Label nameLbl = new Label(entry.getKey());
                nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-min-width: 180;");
                nameLbl.setMaxWidth(250);

                ProgressBar bar = new ProgressBar(maxVal > 0 ? (double) entry.getValue() / maxVal : 0);
                bar.setPrefWidth(200);
                bar.setPrefHeight(14);
                bar.setStyle("-fx-accent: #49ad32;");
                HBox.setHgrow(bar, Priority.ALWAYS);

                Label countLbl = new Label(entry.getValue() + " participants");
                countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

                row.getChildren().addAll(rankLbl, nameLbl, bar, countLbl);
                topEventsBox.getChildren().add(row);
                rank++;
            }
        } catch (SQLException ex) {
            topEventsBox.getChildren().add(makeErrorLabel("Erreur chargement classement."));
        }
    }

    // ================================================================
    //  MARKETPLACE KPIs
    // ================================================================

    private void loadMarketplaceKPIs() {
        try {
            Map<String, Integer> counts = marketStats.getTotalCounts();
            BigDecimal revenue = marketStats.getTotalRevenue();
            BigDecimal stockValue = marketStats.getTotalEquipmentValue();

            int equipements = counts.getOrDefault("equipements", 0);
            int vehicules   = counts.getOrDefault("vehicules", 0);
            int terrains    = counts.getOrDefault("terrains", 0);
            int commandes   = counts.getOrDefault("commandes", 0);
            int locations   = counts.getOrDefault("locations", 0);

            marketKpiPane.getChildren().addAll(
                    makeKpiCard("📦 Équipements",  String.valueOf(equipements), "#e67e22"),
                    makeKpiCard("🚗 Véhicules",    String.valueOf(vehicules),   "#3498db"),
                    makeKpiCard("🏗 Terrains",      String.valueOf(terrains),    "#27ae60"),
                    makeKpiCard("🛒 Commandes",     String.valueOf(commandes),   "#e74c3c"),
                    makeKpiCard("📋 Locations",     String.valueOf(locations),   "#8e44ad"),
                    makeKpiCard("💰 Revenus",       String.format("%.0f DT", revenue),  "#2c3e50"),
                    makeKpiCard("📊 Valeur stock",  String.format("%.0f DT", stockValue), "#16a085")
            );
        } catch (SQLException ex) {
            marketKpiPane.getChildren().add(makeErrorLabel("Impossible de charger les statistiques marketplace."));
        }
    }

    // ================================================================
    //  MARKETPLACE CHARTS
    // ================================================================

    private void loadMarketplaceCharts() {
        // PieChart — orders by delivery status
        try {
            Map<String, Integer> deliveryStatus = marketStats.getOrdersByDeliveryStatus();
            chartOrderStatus.setData(FXCollections.observableArrayList());
            deliveryStatus.forEach((k, v) -> chartOrderStatus.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
            chartOrderStatus.setStartAngle(90);
        } catch (SQLException ex) {
            System.err.println("Erreur chargement chart commandes: " + ex.getMessage());
        }

        // PieChart — equipment by category
        try {
            Map<String, Integer> byCategory = marketStats.getEquipmentCountByCategory();
            chartStockCategory.setData(FXCollections.observableArrayList());
            byCategory.forEach((k, v) -> chartStockCategory.getData().add(new PieChart.Data(k + " (" + v + ")", v)));
            chartStockCategory.setStartAngle(90);
        } catch (SQLException ex) {
            System.err.println("Erreur chargement chart stock: " + ex.getMessage());
        }
    }

    // ================================================================
    //  UI BUILDERS
    // ================================================================

    private VBox makeKpiCard(String title, String value, String color) {
        VBox card = new VBox(6);
        card.setPrefWidth(185);
        card.setPrefHeight(95);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }

    private VBox makeKpiCardWithBar(String title, String value, double progress, String color) {
        VBox card = makeKpiCard(title, value, color);
        ProgressBar bar = new ProgressBar(Math.min(progress, 1.0));
        bar.setPrefWidth(155);
        bar.setPrefHeight(10);
        bar.setStyle("-fx-accent: " + color + ";");
        card.getChildren().add(bar);
        card.setPrefHeight(110);
        return card;
    }

    private Label makeErrorLabel(String msg) {
        Label lbl = new Label("⚠ " + msg);
        lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        return lbl;
    }

    // ================================================================
    //  NAVIGATION
    // ================================================================

    @FXML void goToAccueil(ActionEvent e)     { /* Already here */ }

    @FXML void goToEvenement(ActionEvent e)   { loadView("/GestionEvenement/front.fxml", "FIRMA - Événements", e); }
    @FXML void goToMarketplace(ActionEvent e) { loadView("/GestionMarketplace/marketplace/GUI/views/client_dashboard.fxml", "FIRMA - Marketplace", e); }
    @FXML void goToForum(ActionEvent e)       { System.out.println("Forum"); }
    @FXML void goToTechnicien(ActionEvent e)  { System.out.println("Technicien"); }
    @FXML void goToProfil(ActionEvent e)      { System.out.println("Profil"); }

    @FXML void deconnecter(ActionEvent e) {
        try {
            SessionManager.getInstance().setUtilisateur(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginApplication.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root));
            stage.setWidth(742);
            stage.setHeight(480);
            stage.centerOnScreen();
            stage.setTitle("FIRMA - Connexion");
            stage.show();
        } catch (Exception ex) {
            System.err.println("Erreur lors de la déconnexion : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, String title, ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle(title);
            stage.show();
        } catch (Exception ex) {
            System.err.println("Erreur navigation vers " + fxmlPath + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
