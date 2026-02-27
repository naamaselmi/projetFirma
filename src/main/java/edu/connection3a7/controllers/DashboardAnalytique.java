package edu.connection3a7.controllers;

import edu.connection3a7.services.StatistiquesService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Map;

/**
 * Construit le contenu de l'onglet Dashboard analytique
 * avec des KPIs, graphiques (Pie, Bar, Line) et classements.
 * Tout est généré en Java pur (aucun FXML séparé).
 */
public class DashboardAnalytique {

    private final EvenementController controller;
    private final StatistiquesService statsService = new StatistiquesService();

    // Couleurs du thème Firma
    private static final String VERT = "#49ad32";
    private static final String VERT_CLAIR = "#e8f8e0";
    private static final String ORANGE = "#f5a623";
    private static final String ROUGE = "#e74c3c";
    private static final String BLEU = "#3498db";
    private static final String VIOLET = "#9b59b6";
    private static final String FOND = "#fefbde";
    private static final String BLANC = "white";

    public DashboardAnalytique(EvenementController controller) {
        this.controller = controller;
    }

    /**
     * Construit et retourne le contenu complet du dashboard.
     */
    public ScrollPane construireDashboard() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(25, 30, 30, 30));
        root.setStyle("-fx-background-color: " + FOND + ";");

        // ── Titre ──
        Label titre = new Label("📊 Dashboard Analytique");
        titre.setFont(Font.font("System", FontWeight.BOLD, 28));
        titre.setStyle("-fx-text-fill: " + VERT + ";");

        Label sousTitre = new Label("Vue d'ensemble de vos événements et participations");
        sousTitre.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");

        VBox headerBox = new VBox(4, titre, sousTitre);

        root.getChildren().add(headerBox);

        try {
            // ══════════════════════════════════════════════
            //  SECTION 1 : KPIs (cartes en ligne)
            // ══════════════════════════════════════════════
            root.getChildren().add(construireKPIs());

            // ══════════════════════════════════════════════
            //  SECTION 2 : Graphiques en ligne
            // ══════════════════════════════════════════════
            HBox chartsRow1 = new HBox(20);
            chartsRow1.setAlignment(Pos.TOP_CENTER);

            // PieChart — Répartition par type
            VBox pieTypeBox = wrapChart("Répartition par type", construirePieChart(statsService.repartitionParType()));
            HBox.setHgrow(pieTypeBox, Priority.ALWAYS);

            // PieChart — Répartition par statut
            VBox pieStatutBox = wrapChart("Statut des événements", construirePieChart(statsService.repartitionParStatut()));
            HBox.setHgrow(pieStatutBox, Priority.ALWAYS);

            chartsRow1.getChildren().addAll(pieTypeBox, pieStatutBox);
            root.getChildren().add(chartsRow1);

            // ══════════════════════════════════════════════
            //  SECTION 3 : Bar charts
            // ══════════════════════════════════════════════
            HBox chartsRow2 = new HBox(20);
            chartsRow2.setAlignment(Pos.TOP_CENTER);

            // BarChart — Top 5 événements
            VBox topEventsBox = wrapChart("🏆 Top 5 événements populaires",
                    construireBarChart(statsService.topEvenements(), "Participations", VERT));
            HBox.setHgrow(topEventsBox, Priority.ALWAYS);

            // BarChart — Places disponibles
            VBox placesBox = wrapChart("🪑 Places disponibles (événements actifs)",
                    construireBarChart(statsService.evenementsPlacesDisponibles(), "Places", BLEU));
            HBox.setHgrow(placesBox, Priority.ALWAYS);

            chartsRow2.getChildren().addAll(topEventsBox, placesBox);
            root.getChildren().add(chartsRow2);

            // ══════════════════════════════════════════════
            //  SECTION 4 : Line charts (tendances)
            // ══════════════════════════════════════════════
            HBox chartsRow3 = new HBox(20);
            chartsRow3.setAlignment(Pos.TOP_CENTER);

            VBox eventsLineBox = wrapChart("📈 Événements par mois",
                    construireLineChart(statsService.evenementsParMois(), "Événements"));
            HBox.setHgrow(eventsLineBox, Priority.ALWAYS);

            VBox partLineBox = wrapChart("📈 Inscriptions par mois",
                    construireLineChart(statsService.participationsParMois(), "Inscriptions"));
            HBox.setHgrow(partLineBox, Priority.ALWAYS);

            chartsRow3.getChildren().addAll(eventsLineBox, partLineBox);
            root.getChildren().add(chartsRow3);

            // ══════════════════════════════════════════════
            //  SECTION 5 : PieChart participations
            // ══════════════════════════════════════════════
            HBox chartsRow4 = new HBox(20);
            chartsRow4.setAlignment(Pos.TOP_CENTER);

            VBox piePartBox = wrapChart("Statut des participations",
                    construirePieChart(statsService.repartitionParticipationsParStatut()));
            HBox.setHgrow(piePartBox, Priority.ALWAYS);

            // Boîte "À venir"
            VBox aVenirBox = construireAVenir();
            HBox.setHgrow(aVenirBox, Priority.ALWAYS);

            chartsRow4.getChildren().addAll(piePartBox, aVenirBox);
            root.getChildren().add(chartsRow4);

        } catch (Exception e) {
            Label erreur = new Label("❌ Erreur lors du chargement des statistiques : " + e.getMessage());
            erreur.setStyle("-fx-text-fill: " + ROUGE + "; -fx-font-size: 14px;");
            root.getChildren().add(erreur);
            e.printStackTrace();
        }

        // Bouton rafraîchir
        javafx.scene.control.Button btnRefresh = new javafx.scene.control.Button("🔄 Rafraîchir");
        btnRefresh.setStyle(
                "-fx-background-color: " + VERT + "; -fx-text-fill: white; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 20;");
        btnRefresh.setOnAction(ev -> rafraichir());
        HBox refreshRow = new HBox(btnRefresh);
        refreshRow.setAlignment(Pos.CENTER);
        root.getChildren().add(refreshRow);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + FOND + "; -fx-background: " + FOND + ";");
        return scroll;
    }

    /**
     * Rafraîchit le contenu du dashboard en reconstruisant l'onglet.
     */
    private void rafraichir() {
        controller.rafraichirDashboard();
    }

    // ================================================================
    //  KPIs
    // ================================================================

    private HBox construireKPIs() throws Exception {
        HBox kpiRow = new HBox(15);
        kpiRow.setAlignment(Pos.CENTER);

        int totalEvents = statsService.countEvenements();
        int eventsActifs = statsService.countEvenementsActifs();
        int participations = statsService.countParticipationsConfirmees();
        int enAttente = statsService.countParticipationsEnAttente();
        int totalParticipants = statsService.countTotalParticipants();
        double taux = statsService.tauxRemplissageMoyen();
        int semaine = statsService.evenementsCetteSemaine();

        kpiRow.getChildren().addAll(
                creerKPI("📅", String.valueOf(totalEvents), "Événements", VERT),
                creerKPI("✅", String.valueOf(eventsActifs), "Actifs", "#2d8a1a"),
                creerKPI("👥", String.valueOf(totalParticipants), "Participants", BLEU),
                creerKPI("🎫", String.valueOf(participations), "Confirmées", ORANGE),
                creerKPI("⏳", String.valueOf(enAttente), "En attente", VIOLET),
                creerKPI("📊", taux + "%", "Remplissage", ROUGE),
                creerKPI("🗓", String.valueOf(semaine), "Cette semaine", "#16a085")
        );
        return kpiRow;
    }

    private VBox creerKPI(String emoji, String valeur, String label, String couleur) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setMinWidth(110);
        card.setMaxWidth(160);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;" +
                "-fx-border-color: #eeeeee;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle("-fx-font-size: 24px;");

        Label valLbl = new Label(valeur);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 26));
        valLbl.setStyle("-fx-text-fill: " + couleur + ";");

        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-font-weight: bold;");

        card.getChildren().addAll(emojiLbl, valLbl, labelLbl);
        return card;
    }

    // ================================================================
    //  PIE CHART
    // ================================================================

    private PieChart construirePieChart(Map<String, Integer> data) {
        PieChart chart = new PieChart();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            chart.getData().add(slice);
        }
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setPrefHeight(280);
        chart.setMaxHeight(280);
        chart.setStyle("-fx-font-size: 11px;");

        // Ajouter des tooltips
        for (PieChart.Data d : chart.getData()) {
            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            double pct = total > 0 ? (d.getPieValue() / total) * 100 : 0;
            Tooltip tooltip = new Tooltip(d.getName() + "\n" + String.format("%.1f%%", pct));
            tooltip.setShowDelay(Duration.millis(100));
            Tooltip.install(d.getNode(), tooltip);
        }
        return chart;
    }

    // ================================================================
    //  BAR CHART
    // ================================================================

    @SuppressWarnings("unchecked")
    private BarChart<String, Number> construireBarChart(Map<String, Integer> data, String label, String couleur) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(label);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCategoryGap(10);
        chart.setBarGap(2);
        chart.setPrefHeight(280);
        chart.setMaxHeight(280);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(label);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            // Tronquer les labels longs
            String key = entry.getKey();
            if (key.length() > 18) key = key.substring(0, 16) + "...";
            XYChart.Data<String, Number> d = new XYChart.Data<>(key, entry.getValue());
            series.getData().add(d);
        }

        chart.getData().add(series);

        // Appliquer la couleur après le rendu
        chart.applyCss();
        chart.layout();
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-bar-fill: " + couleur + ";");
                Tooltip tooltip = new Tooltip(d.getXValue() + ": " + d.getYValue());
                tooltip.setShowDelay(Duration.millis(100));
                Tooltip.install(d.getNode(), tooltip);
            }
        }

        return chart;
    }

    // ================================================================
    //  LINE CHART
    // ================================================================

    @SuppressWarnings("unchecked")
    private LineChart<String, Number> construireLineChart(Map<String, Integer> data, String label) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(label);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(280);
        chart.setMaxHeight(280);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(label);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    // ================================================================
    //  À VENIR
    // ================================================================

    private VBox construireAVenir() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;" +
                "-fx-border-radius: 14; -fx-border-color: #eee; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label titre = new Label("📅 Événements à venir");
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setStyle("-fx-text-fill: " + VERT + ";");

        box.getChildren().add(titre);

        try {
            int semaine = statsService.evenementsCetteSemaine();
            int mois = statsService.evenementsCeMois();
            int accompagnants = statsService.countAccompagnants();

            box.getChildren().addAll(
                    creerLigneInfo("🗓 Cette semaine", String.valueOf(semaine), VERT),
                    creerSeparateur(),
                    creerLigneInfo("📆 Ce mois", String.valueOf(mois), BLEU),
                    creerSeparateur(),
                    creerLigneInfo("👨‍👩‍👧‍👦 Total accompagnants", String.valueOf(accompagnants), ORANGE),
                    creerSeparateur(),
                    creerLigneInfo("📊 Taux remplissage moyen", statsService.tauxRemplissageMoyen() + "%", VIOLET)
            );
        } catch (Exception e) {
            Label err = new Label("Erreur: " + e.getMessage());
            err.setStyle("-fx-text-fill: " + ROUGE + "; -fx-font-size: 12px;");
            box.getChildren().add(err);
        }

        return box;
    }

    private HBox creerLigneInfo(String label, String valeur, String couleur) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        HBox.setHgrow(lblLabel, Priority.ALWAYS);

        Label lblValeur = new Label(valeur);
        lblValeur.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblValeur.setStyle("-fx-text-fill: " + couleur + ";");

        row.getChildren().addAll(lblLabel, lblValeur);
        return row;
    }

    private Region creerSeparateur() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #f0f0f0;");
        return sep;
    }

    // ================================================================
    //  UTILITAIRE WRAPPER
    // ================================================================

    private VBox wrapChart(String titre, javafx.scene.Node chart) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 14;" +
                "-fx-border-radius: 14; -fx-border-color: #eee; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        Label titleLbl = new Label(titre);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLbl.setStyle("-fx-text-fill: #333;");

        box.getChildren().addAll(titleLbl, chart);
        return box;
    }
}
