package edu.connection3a7.controllers;

import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Statutevent;
import edu.connection3a7.entities.Utilisateur;
import edu.connection3a7.services.ParticipationService;
import edu.connection3a7.tools.SessionManager;
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

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * Construit les cartes d'événements et le popup de détails (côté Front/utilisateur).
 */
public class ConstructionCartesVisiteur {

    private final FrontController controller;

    ConstructionCartesVisiteur(FrontController controller) {
        this.controller = controller;
    }

    // Raccourcis
    private DateTimeFormatter dateFmt()        { return controller.getDateFmt(); }
    private DateTimeFormatter timeFmt()        { return controller.getTimeFmt(); }
    private ParticipationService partService() { return controller.getParticipationService(); }

    // ============================================================
    //  CARTE EVENEMENT
    // ============================================================

    VBox creerCarteRiche(Evenement e) {
        // ── Carte verticale (grid) ──
        VBox card = new VBox(0);
        card.setPrefWidth(280);
        card.setMinWidth(260);
        card.setMaxWidth(300);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #e8e4c0;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);"
        );

        // ── Image en haut ──
        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(160);
        imgBox.setMinHeight(160);
        imgBox.setMaxHeight(160);
        imgBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 14 14 0 0;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl()
                        : new File(e.getImageUrl()).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 300, 160, false, true));
                iv.setFitWidth(300);
                iv.setFitHeight(160);
                iv.setStyle("-fx-background-radius: 14 14 0 0;");
                imgBox.getChildren().add(iv);
            } catch (Exception ex) {
                imgBox.getChildren().add(OutilsInterfaceGraphique.makePlaceholderLabel());
            }
        } else {
            imgBox.getChildren().add(OutilsInterfaceGraphique.makePlaceholderLabel());
        }

        // Badges superposés sur l'image
        HBox badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.TOP_LEFT);
        badgeRow.setPadding(new Insets(8, 8, 0, 8));
        if (e.getTypeEvenement() != null) {
            badgeRow.getChildren().add(OutilsInterfaceGraphique.makeBadge(e.getTypeEvenement().name().toUpperCase(), "#e8f8e0", "#2d8a1a"));
        }
        if (e.getStatut() != null) {
            boolean actif = e.getStatut() == Statutevent.actif;
            badgeRow.getChildren().add(OutilsInterfaceGraphique.makeBadge(
                    actif ? "ACTIF" : e.getStatut().name().toUpperCase(),
                    actif ? "#e8f8e0" : "#fde8e8",
                    actif ? "#2d8a1a" : "#c0392b"
            ));
        }
        StackPane.setAlignment(badgeRow, Pos.TOP_LEFT);
        imgBox.getChildren().add(badgeRow);

        // ── Corps ──
        VBox body = new VBox(6);
        body.setPadding(new Insets(12, 14, 8, 14));

        // Titre
        Label lblTitre = new Label(e.getTitre());
        lblTitre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        lblTitre.setWrapText(true);
        lblTitre.setMaxHeight(40);

        // Description courte
        String descText = (e.getDescription() != null && !e.getDescription().isBlank())
                ? e.getDescription() : "Aucune description disponible.";
        if (descText.length() > 80) descText = descText.substring(0, 80) + "...";
        Label lblDesc = new Label(descText);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxHeight(34);

        // Infos compactes
        VBox infoBox = new VBox(3);
        String dateStr = (e.getDateDebut() != null ? e.getDateDebut().format(dateFmt()) : "?")
                + " → " + (e.getDateFin() != null ? e.getDateFin().format(dateFmt()) : "?");
        Label lblDate = new Label("📅 " + dateStr);
        lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
        infoBox.getChildren().add(lblDate);

        if (e.getLieu() != null && !e.getLieu().isBlank()) {
            Label lblLieu = new Label("📍 " + e.getLieu());
            lblLieu.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            infoBox.getChildren().add(lblLieu);
        }
        if (e.getOrganisateur() != null && !e.getOrganisateur().isBlank()) {
            Label lblOrg = new Label("👤 " + e.getOrganisateur());
            lblOrg.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            infoBox.getChildren().add(lblOrg);
        }

        // ── Jauge de remplissage ──
        int total = e.getCapaciteMax();
        int dispo = e.getPlacesDisponibles();
        int participants = total - dispo;
        int nbParticipations = controller.countParticipationsByEvent(e.getIdEvenement());
        double pct = total > 0 ? (double) participants / total : 0;

        VBox jaugeBox = new VBox(3);
        jaugeBox.setPadding(new Insets(4, 0, 2, 0));
        Label jaugeLbl = new Label(participants + "/" + total + " places · " + nbParticipations + " inscrits");
        jaugeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #666; -fx-font-weight: bold;");
        HBox barContainer = new HBox();
        barContainer.setPrefHeight(5);
        barContainer.setMaxWidth(Double.MAX_VALUE);
        barContainer.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 3;");
        HBox barFill = new HBox();
        barFill.setPrefHeight(5);
        barFill.prefWidthProperty().bind(barContainer.widthProperty().multiply(pct));
        String barColor = pct >= 0.9 ? "#e74c3c" : pct >= 0.6 ? "#f39c12" : "#49ad32";
        barFill.setStyle("-fx-background-color: " + barColor + "; -fx-background-radius: 3;");
        barContainer.getChildren().add(barFill);
        jaugeBox.getChildren().addAll(jaugeLbl, barContainer);

        // ── Météo contextuelle ──
        HBox meteoBox = new HBox(6);
        meteoBox.setAlignment(Pos.CENTER_LEFT);
        meteoBox.setPadding(new Insets(4, 0, 2, 0));
        Label meteoLabel = new Label();
        meteoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        meteoBox.getChildren().add(meteoLabel);

        if (e.getDateDebut() != null && e.getLieu() != null && !e.getLieu().isBlank()
                && WeatherService.getInstance().isConfigured()) {
            meteoLabel.setText("⌛ Météo...");
            WeatherService.getInstance().getMeteo(e.getLieu(), e.getAdresse(), e.getDateDebut())
                    .thenAccept(result -> Platform.runLater(() -> {
                        if (result != null) {
                            meteoLabel.setText(result.getResumeCourt());
                            meteoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #444; -fx-font-weight: bold;");
                        } else {
                            meteoBox.setVisible(false);
                            meteoBox.setManaged(false);
                        }
                    }));
        } else {
            meteoBox.setVisible(false);
            meteoBox.setManaged(false);
        }

        // ── Boutons horizontaux ──
        HBox btnRow = new HBox(6);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(6, 0, 4, 0));

        Button btnDetails = new Button("Détails");
        btnDetails.setPrefHeight(30);
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDetails, Priority.ALWAYS);
        btnDetails.setStyle(
                "-fx-background-color: white; -fx-text-fill: #49ad32; -fx-font-size: 11px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 16; -fx-border-color: #49ad32;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 16; -fx-cursor: hand;"
        );
        btnDetails.setOnAction(ev -> afficherDetails(e));

        boolean hasLocation = (e.getLieu() != null && !e.getLieu().isBlank())
                || (e.getAdresse() != null && !e.getAdresse().isBlank());
        Button btnMaps = new Button("Maps");
        btnMaps.setPrefHeight(30);
        btnMaps.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnMaps, Priority.ALWAYS);
        btnMaps.setStyle(
                "-fx-background-color: " + (hasLocation ? "#4285F4" : "#cccccc") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 16; -fx-cursor: " + (hasLocation ? "hand" : "default") + ";"
        );
        btnMaps.setDisable(!hasLocation);
        if (hasLocation) {
            final String lieu = e.getLieu(), adresse = e.getAdresse();
            btnMaps.setOnAction(ev -> OutilsInterfaceGraphique.ouvrirMaps(lieu, adresse));
        }

        btnRow.getChildren().addAll(btnDetails, btnMaps);

        // ── Bouton Participer (pleine largeur) ──
        boolean peutParticiper = dispo > 0 && e.getStatut() == Statutevent.actif;
        Utilisateur userConnecte = SessionManager.getInstance().getUtilisateur();
        boolean dejaInscrit = false;
        if (userConnecte != null) {
            try {
                dejaInscrit = partService().isUserAlreadyParticipating(userConnecte.getIdUtilisateur(), e.getIdEvenement());
            } catch (SQLException ex) {
                System.err.println("Erreur vérification participation : " + ex.getMessage());
            }
        }

        Button btnParticiper;
        if (dejaInscrit) {
            btnParticiper = new Button("📋 Mes participations");
            btnParticiper.setStyle(
                    "-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 12px;" +
                            "-fx-font-weight: bold; -fx-background-radius: 18; -fx-cursor: hand;"
            );
            btnParticiper.setOnAction(ev -> controller.showMyParticipations(e));
        } else {
            btnParticiper = new Button(peutParticiper ? "Participer" : "Complet");
            btnParticiper.setStyle(
                    "-fx-background-color: " + (peutParticiper ? "#49ad32" : "#cccccc") + ";" +
                            "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 18; -fx-cursor: " + (peutParticiper ? "hand" : "default") + ";"
            );
            btnParticiper.setDisable(!peutParticiper);
            if (peutParticiper) btnParticiper.setOnAction(ev -> controller.showParticipationForm(e));
        }
        btnParticiper.setPrefHeight(34);
        btnParticiper.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(lblTitre, lblDesc, infoBox, jaugeBox, meteoBox, btnRow, btnParticiper);
        card.getChildren().addAll(imgBox, body);
        return card;
    }

    // ============================================================
    //  POPUP DETAILS
    // ============================================================

    void afficherDetails(Evenement e) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Details - " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(500);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: #49ad32;");

        VBox imgBox = new VBox();
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefSize(72, 72);
        imgBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl() : new File(e.getImageUrl()).toURI().toString();
                imgBox.getChildren().add(new ImageView(new Image(url, 72, 72, true, true)));
            } catch (Exception ex) { imgBox.getChildren().add(new Label("?")); }
        } else {
            Label ph = new Label("IMG");
            ph.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
            imgBox.getChildren().add(ph);
        }

        VBox headerText = new VBox(5);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        Label titreLabel = new Label(e.getTitre());
        titreLabel.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: white;");
        titreLabel.setWrapText(true);
        Label typeLabel = new Label(e.getTypeEvenement() != null ? "Type : " + e.getTypeEvenement().name() : "");
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.88);");
        headerText.getChildren().addAll(titreLabel, typeLabel);
        header.getChildren().addAll(imgBox, headerText);

        // Corps scrollable
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: white;");
        body.setPadding(new Insets(5, 25, 5, 25));

        // Description complète
        VBox descSection = new VBox(6);
        descSection.setPadding(new Insets(14, 0, 14, 0));
        Label descTitle = new Label("Description");
        descTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        TextArea descArea = new TextArea(
                e.getDescription() != null && !e.getDescription().isBlank() ? e.getDescription() : "Aucune description.");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-control-inner-background: #f9f9f9;" +
                        "-fx-border-color: #ebebeb;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );
        descSection.getChildren().addAll(descTitle, descArea);

        int total = e.getCapaciteMax();
        int dispo = e.getPlacesDisponibles();
        int participants = total - dispo;
        int nbParticipationsConfirmees = controller.countParticipationsByEvent(e.getIdEvenement());

        body.getChildren().addAll(
                descSection, OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Organisateur", OutilsInterfaceGraphique.nullSafe(e.getOrganisateur())), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Date debut",   e.getDateDebut() != null ? e.getDateDebut().format(dateFmt()) : "-"), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Date fin",     e.getDateFin()   != null ? e.getDateFin().format(dateFmt()) : "-"), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Horaire",      (e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt()) : "-")
                        + "  ->  "
                        + (e.getHoraireFin() != null ? e.getHoraireFin().format(timeFmt()) : "-")), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Lieu",         OutilsInterfaceGraphique.nullSafe(e.getLieu())), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Adresse",      OutilsInterfaceGraphique.nullSafe(e.getAdresse())), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Participants", participants + " / " + total), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Participations confirmees", String.valueOf(nbParticipationsConfirmees)), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Places restantes", String.valueOf(dispo)), OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Statut",       e.getStatut() != null ? e.getStatut().name() : "-")
        );

        // ── Section météo dans les détails ──
        VBox meteoSection = new VBox(6);
        meteoSection.setPadding(new Insets(10, 0, 10, 0));
        meteoSection.setVisible(false);
        meteoSection.setManaged(false);
        body.getChildren().addAll(OutilsInterfaceGraphique.sep(), meteoSection);

        if (e.getDateDebut() != null && e.getLieu() != null && !e.getLieu().isBlank()
                && WeatherService.getInstance().isConfigured()) {
            Label meteoTitle = new Label("☀ Météo prévue");
            meteoTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
            Label meteoLoading = new Label("⌛ Chargement des prévisions...");
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

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 25, 20, 25));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        boolean hasLocation = (e.getLieu() != null && !e.getLieu().isBlank())
                || (e.getAdresse() != null && !e.getAdresse().isBlank());
        Button btnMapsPopup = new Button("Voir sur Google Maps");
        btnMapsPopup.setPrefHeight(37);
        btnMapsPopup.setStyle(
                "-fx-background-color: " + (hasLocation ? "#4285F4" : "#ccc") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-cursor: " + (hasLocation ? "hand" : "default") + ";"+
                        "-fx-padding: 0 18;"
        );
        btnMapsPopup.setDisable(!hasLocation);
        if (hasLocation) {
            final String lieu = e.getLieu(), adresse = e.getAdresse();
            btnMapsPopup.setOnAction(ev -> OutilsInterfaceGraphique.ouvrirMaps(lieu, adresse));
        }

        boolean peutParticiper = dispo > 0 && e.getStatut() == Statutevent.actif;
        Button btnPartPopup = new Button(peutParticiper ? "Participer" : "Complet");
        btnPartPopup.setPrefHeight(37);
        btnPartPopup.setStyle(
                "-fx-background-color: " + (peutParticiper ? "#49ad32" : "#ccc") + ";" +
                        "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-cursor: " + (peutParticiper ? "hand" : "default") + ";" +
                        "-fx-padding: 0 20;"
        );
        btnPartPopup.setDisable(!peutParticiper);
        if (peutParticiper) btnPartPopup.setOnAction(ev -> { popup.close(); controller.showParticipationForm(e); });

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefSize(100, 37);
        btnFermer.setStyle(
                "-fx-background-color: #f0f0f0; -fx-text-fill: #555; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnFermer.setOnAction(ev -> popup.close());

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(btnMapsPopup, spacer, btnPartPopup, btnFermer);

        root.getChildren().addAll(header, scroll, footer);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        popup.setScene(new Scene(root, 510, 600));
        popup.showAndWait();
    }
}
