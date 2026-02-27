package edu.connection3a7.controllers;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.services.ParticipationService;
import edu.connection3a7.tools.WeatherService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * Construit les cartes d'événements et le popup de détails (côté Admin/Dashboard).
 */
public class ConstructionCartesEvenement {

    private final EvenementController controller;

    ConstructionCartesEvenement(EvenementController controller) {
        this.controller = controller;
    }

    // Raccourcis
    private DateTimeFormatter timeFmt()          { return controller.getTimeFmt(); }
    private ParticipationService partService()   { return controller.getParticipationService(); }

    // ============================================================
    //  CARTE EVENEMENT
    // ============================================================

    HBox creerCarteEvenement(Evenement e) {
        HBox carte = new HBox(15);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setPrefHeight(100);
        carte.setStyle("-fx-border-color: #e1dfdf; -fx-border-width: 2; -fx-background-color: white; -fx-background-radius: 5; -fx-border-radius: 5;");
        carte.setPadding(new Insets(10, 15, 10, 15));

        // ── Image ──
        VBox imgBox = new VBox();
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefWidth(80);
        imgBox.setPrefHeight(80);
        imgBox.setStyle("-fx-border-color: #e1dfdf; -fx-border-width: 1; -fx-background-color: #e1dfdf;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl()
                        : new File(e.getImageUrl()).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 80, 80, true, true));
                imgBox.getChildren().add(iv);
            } catch (Exception ex) {
                imgBox.getChildren().add(placeholderImg());
            }
        } else {
            imgBox.getChildren().add(placeholderImg());
        }

        // ── Infos ──
        VBox infos = new VBox(5);
        HBox.setHgrow(infos, Priority.ALWAYS);
        Label lblTitre = new Label("Titre : " + e.getTitre());
        lblTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label lblOrg = new Label("Organisateur : " + OutilsInterfaceGraphique.nullSafe(e.getOrganisateur()));
        lblOrg.setStyle("-fx-font-size: 13px;");
        Label lblDate = new Label("Du " + e.getDateDebut() + " au " + e.getDateFin()
                + "  |  " + OutilsInterfaceGraphique.nullSafe(e.getHoraireDebut()) + " → " + OutilsInterfaceGraphique.nullSafe(e.getHoraireFin()));
        lblDate.setStyle("-fx-font-size: 13px;");
        infos.getChildren().addAll(lblTitre, lblOrg, lblDate);

        // ── Capacité et Participations ──
        HBox capacite = new HBox(20);
        capacite.setAlignment(Pos.CENTER_LEFT);

        HBox placesBox = new HBox(5);
        placesBox.setAlignment(Pos.CENTER);
        Label lblPlaces = new Label("Places : " + e.getPlacesDisponibles() + "/" + e.getCapaciteMax());
        lblPlaces.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        placesBox.getChildren().add(lblPlaces);

        HBox participationsBox = new HBox(5);
        participationsBox.setAlignment(Pos.CENTER);
        Label iconParticipations = new Label("👥");
        iconParticipations.setStyle("-fx-font-size: 16px;");
        Label lblParticipations = new Label();
        lblParticipations.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        try {
            int count = partService().countParticipationsByEvent(e.getIdEvenement());
            lblParticipations.setText("Participations : " + count);
        } catch (SQLException ex) {
            lblParticipations.setText("Participations : N/A");
        }

        participationsBox.getChildren().addAll(iconParticipations, lblParticipations);
        capacite.getChildren().addAll(placesBox, participationsBox);

        // ── Statut badge ──
        boolean estAnnule = e.getStatut() != null
                && e.getStatut().name().equalsIgnoreCase("annule");
        boolean estTermine = e.getStatut() != null
                && e.getStatut().name().equalsIgnoreCase("termine");
        boolean estInactif = estAnnule || estTermine;

        if (e.getStatut() != null) {
            Label statutLabel = new Label(e.getStatut().name().toUpperCase());
            String statutBg = estAnnule ? "#fde8e8" : estTermine ? "#fff3cd" : "#e8f8e0";
            String statutFg = estAnnule ? "#c0392b" : estTermine ? "#856404" : "#2d8a1a";
            statutLabel.setStyle("-fx-background-color: " + statutBg + "; -fx-text-fill: " + statutFg + "; "
                    + "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 3 10;");
            infos.getChildren().add(statutLabel);
        }

        // ── Boutons ──
        String btnBaseStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 6 12;";

        VBox boutons = new VBox(5);
        boutons.setAlignment(Pos.CENTER_RIGHT);
        boutons.setMinWidth(130);

        Button btnDetails = new Button("Details");
        btnDetails.setPrefWidth(120);
        btnDetails.setPrefHeight(28);
        btnDetails.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; " + btnBaseStyle);
        btnDetails.setOnAction(ev -> afficherDetails(e));

        Button btnParticipants = new Button("Participants");
        btnParticipants.setPrefWidth(120);
        btnParticipants.setPrefHeight(28);
        btnParticipants.setStyle("-fx-background-color: white; -fx-text-fill: #49ad32; -fx-border-color: #49ad32; -fx-border-width: 1.5; " + btnBaseStyle);
        btnParticipants.setOnAction(ev -> controller.showParticipantsGrid(e));

        HBox actionRow = new HBox(5);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnModif = new Button("Modifier");
        btnModif.setPrefWidth(70);
        btnModif.setPrefHeight(26);
        btnModif.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #555; " + btnBaseStyle);
        btnModif.setOnAction(ev -> controller.ouvrirModifier(e));

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefWidth(70);
        btnAnnuler.setPrefHeight(26);
        btnAnnuler.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; " + btnBaseStyle);
        btnAnnuler.setOnAction(ev -> controller.annulerEvenement(e));

        Button btnSuppr = new Button("Supprimer");
        btnSuppr.setPrefWidth(85);
        btnSuppr.setPrefHeight(26);
        btnSuppr.setStyle("-fx-background-color: #fde8e8; -fx-text-fill: #c0392b; " + btnBaseStyle);
        btnSuppr.setOnAction(ev -> controller.supprimerEvenement(e));

        if (estInactif) {
            btnModif.setDisable(true);
            btnAnnuler.setDisable(true);
            btnModif.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #bbb; " + btnBaseStyle + " -fx-cursor: default; -fx-opacity: 0.5;");
            btnAnnuler.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #bbb; " + btnBaseStyle + " -fx-cursor: default; -fx-opacity: 0.5;");
        }
        if (estAnnule) {
            carte.setStyle(carte.getStyle() + " -fx-opacity: 0.65;");
        }

        actionRow.getChildren().addAll(btnModif, btnAnnuler, btnSuppr);
        boutons.getChildren().addAll(btnDetails, btnParticipants, actionRow);
        carte.getChildren().addAll(imgBox, infos, capacite, boutons);
        return carte;
    }

    // ============================================================
    //  POPUP DÉTAILS
    // ============================================================

    void afficherDetails(Evenement e) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Détails : " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(520);
        popup.setMinHeight(600);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // ── En-tête vert ──
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: #49ad32;");

        VBox imgBox = new VBox();
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefSize(75, 75);
        imgBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl()
                        : new File(e.getImageUrl()).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 75, 75, true, true));
                imgBox.getChildren().add(iv);
            } catch (Exception ex) {
                Label ph = new Label("📷");
                ph.setStyle("-fx-font-size: 30px;");
                imgBox.getChildren().add(ph);
            }
        } else {
            Label ph = new Label("📷");
            ph.setStyle("-fx-font-size: 30px;");
            imgBox.getChildren().add(ph);
        }

        VBox headerText = new VBox(5);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        Label titreLabel = new Label(e.getTitre());
        titreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        titreLabel.setWrapText(true);

        String statutStr = e.getStatut() != null ? e.getStatut().name().toUpperCase() : "";
        Label statutBadge = new Label("  " + statutStr + "  ");
        statutBadge.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 2 8;");

        Label typeLabel = new Label(e.getTypeEvenement() != null ? "Type : " + e.getTypeEvenement().name() : "");
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        headerText.getChildren().addAll(titreLabel, statutBadge, typeLabel);
        header.getChildren().addAll(imgBox, headerText);

        // ── Corps scrollable ──
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: white;");
        body.setPadding(new Insets(5, 25, 5, 25));

        VBox descSection = new VBox(6);
        descSection.setPadding(new Insets(14, 0, 14, 0));
        Label descTitle = new Label("📋  Description");
        descTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextArea descArea = new TextArea(e.getDescription() != null && !e.getDescription().isBlank()
                ? e.getDescription() : "—");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-font-size: 13px; -fx-control-inner-background: #f9f9f9; "
                + "-fx-border-color: #ebebeb; -fx-border-radius: 5; -fx-background-radius: 5;");
        descSection.getChildren().addAll(descTitle, descArea);

        body.getChildren().addAll(
                descSection,
                separateur(),
                ligneInfo("👤", "Organisateur",  OutilsInterfaceGraphique.nullSafe(e.getOrganisateur())),
                separateur(),
                ligneInfo("📅", "Date début",    OutilsInterfaceGraphique.nullSafe(e.getDateDebut())),
                separateur(),
                ligneInfo("📅", "Date fin",      OutilsInterfaceGraphique.nullSafe(e.getDateFin())),
                separateur(),
                ligneInfo("🕐", "Horaire début", e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt()) : "—"),
                separateur(),
                ligneInfo("🕐", "Horaire fin",   e.getHoraireFin()   != null ? e.getHoraireFin().format(timeFmt()) : "—"),
                separateur(),
                ligneInfo("📍", "Lieu",          OutilsInterfaceGraphique.nullSafe(e.getLieu())),
                separateur(),
                ligneInfo("🏠", "Adresse",       OutilsInterfaceGraphique.nullSafe(e.getAdresse())),
                separateur(),
                ligneInfo("🪑", "Places",        e.getPlacesDisponibles() + " / " + e.getCapaciteMax()),
                separateur(),
                ligneInfo("👥", "Participations", genererInfoParticipations(e))
        );

        // ── Section météo ──
        VBox meteoSection = new VBox(6);
        meteoSection.setPadding(new Insets(10, 0, 10, 0));
        meteoSection.setVisible(false);
        meteoSection.setManaged(false);
        body.getChildren().addAll(separateur(), meteoSection);

        if (e.getDateDebut() != null && e.getLieu() != null && !e.getLieu().isBlank()
                && WeatherService.getInstance().isConfigured()) {
            Label meteoTitle = new Label("☀️  Météo prévue");
            meteoTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
            Label meteoLoading = new Label("⌛ Chargement...");
            meteoLoading.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            meteoSection.getChildren().addAll(meteoTitle, meteoLoading);
            meteoSection.setVisible(true);
            meteoSection.setManaged(true);

            WeatherService.getInstance().getMeteo(e.getLieu(), e.getAdresse(), e.getDateDebut())
                    .thenAccept(result -> Platform.runLater(() -> {
                        meteoSection.getChildren().clear();
                        meteoSection.getChildren().add(meteoTitle);
                        if (result != null) {
                            HBox tempRow = new HBox(10);
                            tempRow.setAlignment(Pos.CENTER_LEFT);
                            Label emoji = new Label(result.getEmoji());
                            emoji.setStyle("-fx-font-size: 28px;");
                            VBox tempInfo = new VBox(2);
                            Label tempLabel = new Label(Math.round(result.temperature) + "°C");
                            tempLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");
                            Label descLabel = new Label(result.description);
                            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                            tempInfo.getChildren().addAll(tempLabel, descLabel);
                            tempRow.getChildren().addAll(emoji, tempInfo);

                            Label minMax = new Label("↓ " + Math.round(result.tempMin) + "°C  /  ↑ " + Math.round(result.tempMax) + "°C");
                            minMax.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

                            Label fiabilite = new Label(result.getFiabilite());
                            fiabilite.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-font-style: italic;");

                            meteoSection.getChildren().addAll(tempRow, minMax, fiabilite);
                        } else {
                            Label noData = new Label("Données météo indisponibles");
                            noData.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
                            meteoSection.getChildren().add(noData);
                        }
                    }));
        }

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Footer ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 25, 20, 25));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        boolean hasLocation = (e.getLieu() != null && !e.getLieu().isBlank())
                || (e.getAdresse() != null && !e.getAdresse().isBlank());

        Button btnMaps = new Button("📍  Voir sur Google Maps");
        btnMaps.setPrefHeight(38);
        btnMaps.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnMaps.setDisable(!hasLocation);
        if (!hasLocation) {
            btnMaps.setTooltip(new Tooltip("Aucune adresse renseignée"));
            btnMaps.setStyle("-fx-background-color: #aaaaaa; -fx-text-fill: white; -fx-font-size: 13px; "
                    + "-fx-background-radius: 20;");
        }
        btnMaps.setOnAction(ev -> ouvrirGoogleMaps(e.getLieu(), e.getAdresse()));

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefHeight(38);
        btnFermer.setPrefWidth(100);
        btnFermer.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnFermer.setOnAction(ev -> popup.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(btnMaps, spacer, btnFermer);

        root.getChildren().addAll(header, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 520, 620);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // ============================================================
    //  UTILITAIRES INTERNES
    // ============================================================

    private void ouvrirGoogleMaps(String lieu, String adresse) {
        try {
            String recherche = "";
            if (lieu != null && !lieu.isBlank() && adresse != null && !adresse.isBlank()) {
                recherche = lieu + ", " + adresse;
            } else if (lieu != null && !lieu.isBlank()) {
                recherche = lieu;
            } else {
                recherche = adresse;
            }
            String encoded = URLEncoder.encode(recherche, StandardCharsets.UTF_8);
            String url = "https://www.google.com/maps/search/?api=1&query=" + encoded;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir Google Maps : " + ex.getMessage());
        }
    }

    private String genererInfoParticipations(Evenement e) {
        try {
            int count = partService().countParticipationsByEvent(e.getIdEvenement());
            int totalParticipants = partService().countTotalParticipantsByEvent(e.getIdEvenement());
            return count + " participation(s) - " + totalParticipants + " participant(s)";
        } catch (SQLException ex) {
            return "N/A";
        }
    }

    private HBox ligneInfo(String emoji, String label, String valeur) {
        HBox ligne = new HBox(12);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(11, 0, 11, 0));

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 15px;");
        icon.setMinWidth(22);

        Label lbl = new Label(label + " :");
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #555;");
        lbl.setMinWidth(120);

        Label val = new Label(valeur == null || valeur.isBlank() ? "—" : valeur);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #222;");
        val.setWrapText(true);
        HBox.setHgrow(val, Priority.ALWAYS);

        ligne.getChildren().addAll(icon, lbl, val);
        return ligne;
    }

    private Region separateur() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #f0f0f0;");
        return sep;
    }

    private Label placeholderImg() {
        Label lbl = new Label("Image");
        lbl.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        return lbl;
    }
}
