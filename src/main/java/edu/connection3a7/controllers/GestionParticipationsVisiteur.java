package edu.connection3a7.controllers;

import edu.connection3a7.entities.*;
import edu.connection3a7.services.AccompagnantService;
import edu.connection3a7.services.EvenementService;
import edu.connection3a7.services.ParticipationService;
import edu.connection3a7.tools.SessionManager;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère les popups et la logique CRUD des participations (côté Front/utilisateur).
 */
public class GestionParticipationsVisiteur {

    private final FrontController controller;

    GestionParticipationsVisiteur(FrontController controller) {
        this.controller = controller;
    }

    // Raccourcis
    private DateTimeFormatter dateFmt()              { return controller.getDateFmt(); }
    private DateTimeFormatter timeFmt()              { return controller.getTimeFmt(); }
    private ParticipationService partService()       { return controller.getParticipationService(); }
    private AccompagnantService accompService()      { return controller.getAccompagnantService(); }
    private EvenementService eventService()          { return controller.getEvenementService(); }

    // ============================================================
    //  POPUP FORMULAIRE PARTICIPATION
    // ============================================================

    void afficherFormulaireParticipation(Evenement e) {
        // Recuperer l'utilisateur connecte
        Utilisateur u = SessionManager.getInstance().getUtilisateur();

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Participer - " + e.getTitre());
        popup.setResizable(true);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // Header vert
        VBox header = new VBox(5);
        header.setPadding(new Insets(20, 28, 20, 28));
        header.setStyle("-fx-background-color: #49ad32;");
        Label titleLbl = new Label("Inscription a l'evenement");
        titleLbl.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label eventLbl = new Label(e.getTitre());
        eventLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.93);");
        String dateStr = (e.getDateDebut() != null ? e.getDateDebut().format(dateFmt()) : "?")
                + "  ->  " + (e.getDateFin() != null ? e.getDateFin().format(dateFmt()) : "?");
        Label dateLbl = new Label("Date : " + dateStr);
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.82);");
        header.getChildren().addAll(titleLbl, eventLbl, dateLbl);
        if (e.getLieu() != null && !e.getLieu().isBlank()) {
            Label lieuLbl = new Label("Lieu : " + e.getLieu());
            lieuLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.82);");
            header.getChildren().add(lieuLbl);
        }

        // Formulaire scrollable
        VBox form = new VBox(13);
        form.setPadding(new Insets(20, 28, 10, 28));

        Label formTitle = new Label("Vos informations");
        formTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");

        // Prenom + Nom (pré-remplis depuis la session)
        HBox nameRow = new HBox(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfPrenom = OutilsInterfaceGraphique.makeField("Prenom *");
        TextField tfNom    = OutilsInterfaceGraphique.makeField("Nom *");
        HBox.setHgrow(tfPrenom, Priority.ALWAYS);
        HBox.setHgrow(tfNom, Priority.ALWAYS);
        if (u != null) {
            tfPrenom.setText(u.getPrenom() != null ? u.getPrenom() : "");
            tfNom.setText(u.getNom() != null ? u.getNom() : "");
        }
        nameRow.getChildren().addAll(tfPrenom, tfNom);

        TextField tfEmail = OutilsInterfaceGraphique.makeField("Email *");
        TextField tfTel   = OutilsInterfaceGraphique.makeField("Telephone");
        if (u != null) {
            tfEmail.setText(u.getEmail() != null ? u.getEmail() : "");
            tfTel.setText(u.getTelephone() != null ? u.getTelephone() : "");
        }

        // Nombre d'accompagnants
        HBox placesRow = new HBox(12);
        placesRow.setAlignment(Pos.CENTER_LEFT);
        Label placesLbl = new Label("Nombre d'accompagnants :");
        placesLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        placesLbl.setMinWidth(185);
        Spinner<Integer> spinAccomp = new Spinner<>(0, Math.max(0, e.getPlacesDisponibles() - 1), 0);
        spinAccomp.setPrefWidth(80);
        spinAccomp.setEditable(true);
        Label dispoLbl = new Label("(" + e.getPlacesDisponibles() + " places dispo)");
        dispoLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        placesRow.getChildren().addAll(placesLbl, spinAccomp, dispoLbl);

        // Conteneur dynamique pour les champs nom/prénom des accompagnants
        Label accompTitle = new Label("Informations des accompagnants");
        accompTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        accompTitle.setVisible(false);
        accompTitle.setManaged(false);

        VBox accompContainer = new VBox(8);
        accompContainer.setStyle("-fx-padding: 0;");

        // Mise à jour dynamique des champs accompagnants quand le spinner change
        spinAccomp.valueProperty().addListener((obs, oldVal, newVal) -> {
            accompContainer.getChildren().clear();
            boolean hasAccomp = newVal != null && newVal > 0;
            accompTitle.setVisible(hasAccomp);
            accompTitle.setManaged(hasAccomp);
            if (hasAccomp) {
                for (int i = 1; i <= newVal; i++) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    Label numLbl = new Label("Accompagnant " + i + " :");
                    numLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
                    numLbl.setMinWidth(120);
                    TextField tfAccPrenom = OutilsInterfaceGraphique.makeField("Prenom *");
                    TextField tfAccNom = OutilsInterfaceGraphique.makeField("Nom *");
                    HBox.setHgrow(tfAccPrenom, Priority.ALWAYS);
                    HBox.setHgrow(tfAccNom, Priority.ALWAYS);
                    row.getChildren().addAll(numLbl, tfAccPrenom, tfAccNom);
                    accompContainer.getChildren().add(row);
                }
            }
        });

        // Note
        Label noteLbl = new Label("Note (optionnel) :");
        noteLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        TextArea tfNote = new TextArea();
        tfNote.setPromptText("Un message pour l'organisateur...");
        tfNote.setPrefRowCount(3);
        tfNote.setWrapText(true);
        tfNote.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;"
        );

        Label lblErreur = new Label("");
        lblErreur.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        form.getChildren().addAll(formTitle, nameRow, tfEmail, tfTel, placesRow,
                accompTitle, accompContainer, noteLbl, tfNote, lblErreur);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        // Footer boutons
        HBox footer = new HBox(12);
        footer.setPadding(new Insets(14, 28, 22, 28));
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setPrefSize(100, 38);
        btnAnnuler.setStyle(
                "-fx-background-color: #f0f0f0; -fx-text-fill: #555; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnAnnuler.setOnAction(ev -> popup.close());

        Button btnConfirmer = new Button("Confirmer");
        btnConfirmer.setPrefSize(130, 38);
        btnConfirmer.setStyle(
                "-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnConfirmer.setOnAction(ev -> {
            // Validation
            if (tfPrenom.getText().isBlank() || tfNom.getText().isBlank() || tfEmail.getText().isBlank()) {
                lblErreur.setText("Veuillez remplir tous les champs obligatoires (*).");
                return;
            }
            if (tfPrenom.getText().trim().length() < 2) {
                lblErreur.setText("Le prenom doit contenir au moins 2 caracteres.");
                return;
            }
            if (tfNom.getText().trim().length() < 2) {
                lblErreur.setText("Le nom doit contenir au moins 2 caracteres.");
                return;
            }
            if (!tfPrenom.getText().trim().matches("^[A-Za-z\\u00C0-\\u017F\\s'-]+$")) {
                lblErreur.setText("Le prenom ne doit contenir que des lettres.");
                return;
            }
            if (!tfNom.getText().trim().matches("^[A-Za-z\\u00C0-\\u017F\\s'-]+$")) {
                lblErreur.setText("Le nom ne doit contenir que des lettres.");
                return;
            }
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!tfEmail.getText().trim().matches(emailRegex)) {
                lblErreur.setText("Format d'email invalide (ex: nom@domaine.com).");
                return;
            }
            if (!tfTel.getText().isBlank()) {
                String tel = tfTel.getText().trim().replaceAll("\\s+", "");
                if (!tel.matches("^[+]?[0-9]{8,15}$")) {
                    lblErreur.setText("Numero de telephone invalide (8 a 15 chiffres, + optionnel).");
                    return;
                }
            }
            int nbAccomp = spinAccomp.getValue();
            int totalPlaces = 1 + nbAccomp;
            if (totalPlaces > e.getPlacesDisponibles()) {
                lblErreur.setText("Pas assez de places (max : " + e.getPlacesDisponibles() + ").");
                return;
            }

            // Récupérer les accompagnants
            List<Accompagnant> accompagnants = new ArrayList<>();
            for (javafx.scene.Node node : accompContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    TextField accPrenom = null, accNom = null;
                    int fieldIdx = 0;
                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            if (fieldIdx == 0) accPrenom = (TextField) child;
                            else accNom = (TextField) child;
                            fieldIdx++;
                        }
                    }
                    if (accPrenom != null && accNom != null) {
                        if (accPrenom.getText().isBlank() || accNom.getText().isBlank()) {
                            lblErreur.setText("Veuillez remplir le nom et prénom de tous les accompagnants.");
                            return;
                        }
                        accompagnants.add(new Accompagnant(accNom.getText().trim(), accPrenom.getText().trim()));
                    }
                }
            }

            // Ajouter la participation avec accompagnants
            Participation result = ajouterParticipation(e, nbAccomp, tfNote.getText(), accompagnants);
            popup.close();
            if (result != null) {
                // Envoyer l'email de confirmation si le service est configuré
                if (edu.connection3a7.tools.EmailService.getInstance().isConfigured()) {
                    String emailDest = tfEmail.getText().trim();
                    String prenomDest = tfPrenom.getText().trim();
                    edu.connection3a7.tools.EmailService.getInstance()
                            .envoyerEmailConfirmation(emailDest, prenomDest, e, result.getCodeParticipation())
                            .thenAccept(success -> javafx.application.Platform.runLater(() -> {
                                if (success) {
                                    OutilsInterfaceGraphique.afficherAlerte(javafx.scene.control.Alert.AlertType.INFORMATION,
                                            "Email envoyé \u2709",
                                            "Un email de confirmation a été envoyé à " + emailDest + ".\n\n"
                                            + "Veuillez cliquer sur le lien dans l'email pour confirmer votre participation "
                                            + "et recevoir votre ticket PDF.");
                                } else {
                                    OutilsInterfaceGraphique.afficherAlerte(javafx.scene.control.Alert.AlertType.WARNING,
                                            "Email non envoyé",
                                            "L'email de confirmation n'a pas pu être envoyé.\n"
                                            + "Votre inscription est enregistrée (en attente de confirmation).");
                                }
                            }));
                    // Afficher un message immédiat avant que l'email n'arrive
                    OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION,
                            "Inscription enregistrée",
                            "Votre inscription est en attente de confirmation.\n\n"
                            + "\u2709 Un email de confirmation va être envoyé à " + tfEmail.getText().trim() + ".\n"
                            + "Cliquez sur le lien dans l'email pour confirmer et recevoir votre ticket PDF.");
                } else {
                    // Si email non configuré, confirmer directement et montrer les tickets
                    try {
                        partService().updateStatut(result.getIdParticipation(), Statut.CONFIRME);
                        result.setStatut(Statut.CONFIRME);
                    } catch (Exception ex) {
                        System.err.println("Erreur confirmation directe : " + ex.getMessage());
                    }
                    controller.showTicketCards(result, e, tfPrenom.getText(), tfNom.getText(), accompagnants);
                }
            }
        });

        footer.getChildren().addAll(btnAnnuler, btnConfirmer);
        root.getChildren().addAll(header, formScroll, footer);
        popup.setScene(new Scene(root, 520, 680));
        popup.showAndWait();
    }

    // ============================================================
    //  AJOUTER PARTICIPATION
    // ============================================================

    Participation ajouterParticipation(Evenement e, int nombreAccompagnants, String commentaire, List<Accompagnant> accompagnants) {
        try {
            Utilisateur u = SessionManager.getInstance().getUtilisateur();
            if (u == null) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Connexion requise",
                        "Vous devez être connecté pour participer.");
                return null;
            }

            if (partService().isUserAlreadyParticipating(u.getIdUtilisateur(), e.getIdEvenement())) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Déjà inscrit",
                        "Vous êtes déjà inscrit à cet événement. Cliquez sur 'Mes participations' pour modifier votre inscription.");
                return null;
            }

            Participation participation = new Participation();
            participation.setIdEvenement(e.getIdEvenement());
            participation.setIdUtilisateur(u.getIdUtilisateur());
            participation.setNombreAccompagnants(nombreAccompagnants);
            participation.setCommentaire(commentaire.trim().isEmpty() ? null : commentaire.trim());
            participation.setStatut(Statut.EN_ATTENTE);
            participation.setDateInscription(LocalDateTime.now());

            partService().addEntityWithAccompagnants(participation, accompagnants);
            return participation;
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur base de données",
                    "Impossible d'ajouter la participation : " + ex.getMessage());
            return null;
        }
    }

    // ============================================================
    //  MES PARTICIPATIONS
    // ============================================================

    void afficherMesParticipations(Evenement e) {
        Utilisateur u = SessionManager.getInstance().getUtilisateur();
        if (u == null) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Connexion requise",
                    "Vous devez être connecté pour accéder à vos participations.");
            return;
        }

        try {
            Participation participation = partService().getParticipationByUserAndEvent(u.getIdUtilisateur(), e.getIdEvenement());
            if (participation != null) {
                afficherDetailsParticipation(e, participation);
            } else {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Aucune participation",
                        "Vous n'êtes pas inscrit à cet événement.");
            }
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger vos participations : " + ex.getMessage());
        }
    }

    // ============================================================
    //  DETAILS PARTICIPATION
    // ============================================================

    void afficherDetailsParticipation(Evenement e, Participation participation) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Ma participation - " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(500);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: #49ad32;");

        Label titleLbl = new Label("Votre participation");
        titleLbl.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label eventLbl = new Label(e.getTitre());
        eventLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        VBox headerText = new VBox(5);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        headerText.getChildren().addAll(titleLbl, eventLbl);
        header.getChildren().add(headerText);

        // Corps
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: white;");
        body.setPadding(new Insets(20, 25, 20, 25));

        Label infoTitle = new Label("Détails de votre participation");
        infoTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");

        body.getChildren().addAll(
                infoTitle, OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Date d'inscription", participation.getDateInscription() != null ?
                        participation.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-"),
                OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Accompagnants", String.valueOf(participation.getNombreAccompagnants())),
                OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Total personnes", String.valueOf(1 + participation.getNombreAccompagnants())),
                OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Commentaire", participation.getCommentaire() != null ? participation.getCommentaire() : "-"),
                OutilsInterfaceGraphique.sep(),
                OutilsInterfaceGraphique.ligneInfo("Statut", participation.getStatut() != null ? participation.getStatut().name() : "-")
        );

        // Afficher les accompagnants avec leurs noms
        try {
            List<Accompagnant> accompagnants = accompService().getByParticipation(participation.getIdParticipation());
            if (!accompagnants.isEmpty()) {
                body.getChildren().add(OutilsInterfaceGraphique.sep());
                Label accompTitleLbl = new Label("Liste des accompagnants");
                accompTitleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
                accompTitleLbl.setPadding(new Insets(10, 0, 5, 0));
                body.getChildren().add(accompTitleLbl);
                for (int i = 0; i < accompagnants.size(); i++) {
                    Accompagnant a = accompagnants.get(i);
                    body.getChildren().add(OutilsInterfaceGraphique.ligneInfo("Accompagnant " + (i + 1), a.getPrenom() + " " + a.getNom()));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur chargement accompagnants: " + ex.getMessage());
        }

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Footer avec boutons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 25, 20, 25));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        Button btnTicket = new Button("Mon ticket");
        btnTicket.setPrefHeight(38);
        btnTicket.setStyle(
                "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 13px; " +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 0 16;"
        );
        btnTicket.setOnAction(ev -> controller.showTicket(e, participation));



        Button btnAnnuler = new Button("✗ Annuler participation");
        btnAnnuler.setPrefHeight(38);
        btnAnnuler.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; " +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnAnnuler.setOnAction(ev -> {
            if (confirmerAnnulationParticipation()) {
                annulerParticipation(participation);
                popup.close();
            }
        });

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefHeight(38);
        btnFermer.setPrefWidth(100);
        btnFermer.setStyle(
                "-fx-background-color: #f0f0f0; -fx-text-fill: #555; -fx-font-size: 13px; " +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnFermer.setOnAction(ev -> popup.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(btnTicket, btnAnnuler, spacer, btnFermer);

        root.getChildren().addAll(header, scroll, footer);
        popup.setScene(new Scene(root, 500, 550));
        popup.showAndWait();
    }

    // ============================================================
    //  MODIFIER PARTICIPATION
    // ============================================================

    void afficherFormulaireModificationParticipation(Evenement e, Participation participation) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Modifier - " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(520);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // Header
        VBox header = new VBox(5);
        header.setPadding(new Insets(20, 28, 20, 28));
        header.setStyle("-fx-background-color: #49ad32;");
        Label titleLbl = new Label("Modifier votre participation");
        titleLbl.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label eventLbl = new Label(e.getTitre());
        eventLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");
        header.getChildren().addAll(titleLbl, eventLbl);

        // Formulaire
        VBox form = new VBox(13);
        form.setPadding(new Insets(20, 28, 10, 28));

        // Nombre d'accompagnants
        HBox accompBox = new HBox(10);
        accompBox.setAlignment(Pos.CENTER_LEFT);
        Label lblAccomp = new Label("Nombre d'accompagnants :");
        lblAccomp.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        lblAccomp.setMinWidth(185);
        Spinner<Integer> spinAccomp = new Spinner<>(0, 100, participation.getNombreAccompagnants());
        spinAccomp.setPrefWidth(80);
        spinAccomp.setEditable(true);
        accompBox.getChildren().addAll(lblAccomp, spinAccomp);

        // Conteneur dynamique pour les champs accompagnants
        Label accompTitleLbl = new Label("Informations des accompagnants");
        accompTitleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        accompTitleLbl.setVisible(participation.getNombreAccompagnants() > 0);
        accompTitleLbl.setManaged(participation.getNombreAccompagnants() > 0);

        VBox accompContainer = new VBox(8);

        // Charger les accompagnants existants
        List<Accompagnant> existingAccomp = new ArrayList<>();
        try {
            existingAccomp = accompService().getByParticipation(participation.getIdParticipation());
        } catch (SQLException ex) {
            System.err.println("Erreur chargement accompagnants: " + ex.getMessage());
        }
        final List<Accompagnant> finalExisting = existingAccomp;

        // Méthode pour générer les champs accompagnants
        Runnable refreshAccompFields = () -> {
            accompContainer.getChildren().clear();
            int count = spinAccomp.getValue();
            boolean hasAccomp = count > 0;
            accompTitleLbl.setVisible(hasAccomp);
            accompTitleLbl.setManaged(hasAccomp);
            for (int i = 0; i < count; i++) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label numLbl = new Label("Accompagnant " + (i + 1) + " :");
                numLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
                numLbl.setMinWidth(120);
                TextField tfAccPrenom = OutilsInterfaceGraphique.makeField("Prenom *");
                TextField tfAccNom = OutilsInterfaceGraphique.makeField("Nom *");
                HBox.setHgrow(tfAccPrenom, Priority.ALWAYS);
                HBox.setHgrow(tfAccNom, Priority.ALWAYS);
                // Pré-remplir avec les données existantes
                if (i < finalExisting.size()) {
                    tfAccPrenom.setText(finalExisting.get(i).getPrenom());
                    tfAccNom.setText(finalExisting.get(i).getNom());
                }
                row.getChildren().addAll(numLbl, tfAccPrenom, tfAccNom);
                accompContainer.getChildren().add(row);
            }
        };

        // Initialiser les champs
        refreshAccompFields.run();

        spinAccomp.valueProperty().addListener((obs, oldVal, newVal) -> refreshAccompFields.run());

        // Commentaire
        Label lblComm = new Label("Commentaire :");
        lblComm.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TextArea txtComm = new TextArea();
        txtComm.setWrapText(true);
        txtComm.setPrefRowCount(3);
        txtComm.setText(participation.getCommentaire() != null ? participation.getCommentaire() : "");
        txtComm.setStyle("-fx-font-size: 13px; -fx-border-color: #c8c8c8; -fx-border-width: 1.5; " +
                "-fx-background-radius: 5; -fx-border-radius: 5;");

        Label lblErreur = new Label("");
        lblErreur.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        form.getChildren().addAll(accompBox, accompTitleLbl, accompContainer, lblComm, txtComm, lblErreur);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(14, 28, 22, 28));

        Button btnValider = new Button("Enregistrer");
        btnValider.setPrefHeight(40);
        btnValider.setPrefWidth(150);
        btnValider.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnValider.setOnAction(ev -> {
            // Récupérer les accompagnants
            List<Accompagnant> accompagnants = new ArrayList<>();
            for (javafx.scene.Node node : accompContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    TextField accPrenom = null, accNom = null;
                    int fieldIdx = 0;
                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            if (fieldIdx == 0) accPrenom = (TextField) child;
                            else accNom = (TextField) child;
                            fieldIdx++;
                        }
                    }
                    if (accPrenom != null && accNom != null) {
                        if (accPrenom.getText().isBlank() || accNom.getText().isBlank()) {
                            lblErreur.setText("Veuillez remplir le nom et prénom de tous les accompagnants.");
                            return;
                        }
                        accompagnants.add(new Accompagnant(accNom.getText().trim(), accPrenom.getText().trim()));
                    }
                }
            }
            modifierParticipation(participation, spinAccomp.getValue(), txtComm.getText(), accompagnants);
            popup.close();
        });

        Button btnAnnulerBtn = new Button("Annuler");
        btnAnnulerBtn.setPrefHeight(40);
        btnAnnulerBtn.setPrefWidth(150);
        btnAnnulerBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #555; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnAnnulerBtn.setOnAction(ev -> popup.close());

        buttonsBox.getChildren().addAll(btnValider, btnAnnulerBtn);
        root.getChildren().addAll(header, formScroll, buttonsBox);

        popup.setScene(new Scene(root, 520, 550));
        popup.showAndWait();
    }

    // ============================================================
    //  MODIFIER / ANNULER PARTICIPATION
    // ============================================================

    void modifierParticipation(Participation participation, int nombreAccompagnants, String commentaire, List<Accompagnant> accompagnants) {
        try {
            participation.setNombreAccompagnants(nombreAccompagnants);
            participation.setCommentaire(commentaire.trim().isEmpty() ? null : commentaire.trim());
            participation.setStatut(Statut.CONFIRME);

            partService().updateEntity(participation.getIdParticipation(), participation);
            accompService().updateAccompagnants(participation.getIdParticipation(), accompagnants);
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Participation mise à jour avec succès !");
            controller.rechargerListe();
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la participation : " + ex.getMessage());
        }
    }

    boolean confirmerAnnulationParticipation() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler votre participation ?");
        confirm.setContentText("Êtes-vous sûr de vouloir annuler votre participation ? Cette action est irréversible.");
        return confirm.showAndWait().map(response -> response == ButtonType.OK).orElse(false);
    }

    void annulerParticipation(Participation participation) {
        try {
            partService().deleteEntity(participation);
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Participation annulée avec succès !");
            controller.rechargerListe();
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler la participation : " + ex.getMessage());
        }
    }

    // ============================================================
    //  LISTE MES PARTICIPATIONS
    // ============================================================

    void afficherListeMesParticipations() {
        Utilisateur u = SessionManager.getInstance().getUtilisateur();
        if (u == null) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.WARNING, "Connexion requise",
                    "Vous devez être connecté pour voir vos participations.");
            return;
        }

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Mes participations");
        popup.setResizable(true);
        popup.setMinWidth(600);
        popup.setMinHeight(500);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: #49ad32;");

        Label titleLbl = new Label("Mes participations");
        titleLbl.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label userLbl = new Label(u.getNom() + " " + u.getPrenom());
        userLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");

        VBox headerText = new VBox(5);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        headerText.getChildren().addAll(titleLbl, userLbl);
        header.getChildren().add(headerText);

        // Corps
        VBox body = new VBox(12);
        body.setStyle("-fx-background-color: white;");
        body.setPadding(new Insets(20, 25, 20, 25));

        try {
            List<Participation> mesParticipations = partService().getParticipationsByUser(u.getIdUtilisateur());

            if (mesParticipations.isEmpty()) {
                Label lblVide = new Label("Vous n'êtes inscrit(e) à aucun événement.");
                lblVide.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
                body.getChildren().add(lblVide);
            } else {
                for (Participation p : mesParticipations) {
                    try {
                        body.getChildren().add(creerCarteParticipation(p));
                    } catch (SQLException ex) {
                        System.err.println("Erreur création carte participation: " + ex.getMessage());
                    }
                }
            }
        } catch (SQLException ex) {
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger vos participations : " + ex.getMessage());
        }

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15, 25, 20, 25));
        footer.setStyle("-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefSize(100, 38);
        btnFermer.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnFermer.setOnAction(ev -> popup.close());
        footer.getChildren().add(btnFermer);

        root.getChildren().addAll(header, scroll, footer);
        popup.setScene(new Scene(root, 600, 550));
        popup.showAndWait();
    }

    // ============================================================
    //  CARTE PARTICIPATION (dans la liste)
    // ============================================================

    private HBox creerCarteParticipation(Participation p) throws SQLException {
        EvenementService evenementService = new EvenementService();
        Evenement e = evenementService.getById(p.getIdEvenement());

        if (e == null) return new HBox();

        HBox carte = new HBox(15);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setPrefHeight(110);
        carte.setStyle("-fx-border-color: #e1dfdf; -fx-border-width: 1.5; -fx-background-color: #fafafa; " +
                "-fx-background-radius: 8; -fx-border-radius: 8;");
        carte.setPadding(new Insets(12, 15, 12, 15));

        // Image
        VBox imgBox = new VBox();
        imgBox.setAlignment(Pos.CENTER);
        imgBox.setPrefSize(75, 75);
        imgBox.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-background-color: #e8f5e9; -fx-background-radius: 6;");
        if (e.getImageUrl() != null && !e.getImageUrl().isBlank()) {
            try {
                String url = e.getImageUrl().startsWith("http")
                        ? e.getImageUrl()
                        : new File(e.getImageUrl()).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 75, 75, true, true));
                imgBox.getChildren().add(iv);
            } catch (Exception ex) {
                imgBox.getChildren().add(OutilsInterfaceGraphique.makePlaceholderLabel());
            }
        } else {
            imgBox.getChildren().add(OutilsInterfaceGraphique.makePlaceholderLabel());
        }

        // Infos
        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);
        Label lblTitre = new Label(e.getTitre());
        lblTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        Label lblDate = new Label("Date: " + (e.getDateDebut() != null ? e.getDateDebut().format(dateFmt()) : "-"));
        lblDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label lblAccomp = new Label("Accompagnants: " + p.getNombreAccompagnants() + " | Total: " + (1 + p.getNombreAccompagnants()));
        lblAccomp.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        infos.getChildren().addAll(lblTitre, lblDate, lblAccomp);

        // Boutons
        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER);

        Button btnTicket = new Button("\uD83C\uDFAB");
        btnTicket.setPrefSize(36, 36);
        btnTicket.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        btnTicket.setTooltip(new Tooltip("Voir mon ticket"));
        btnTicket.setOnAction(ev -> {
            try {
                EvenementService evtSvc = new EvenementService();
                controller.showTicket(evtSvc.getById(p.getIdEvenement()), p);
            } catch (SQLException ex) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });

        Button btnEditer = new Button("✏");
        btnEditer.setPrefSize(36, 36);
        btnEditer.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        btnEditer.setTooltip(new Tooltip("Modifier"));
        btnEditer.setOnAction(ev -> {
            try {
                afficherFormulaireModificationParticipation(evenementService.getById(p.getIdEvenement()), p);
            } catch (SQLException ex) {
                OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });

        Button btnSupprimer = new Button("✗");
        btnSupprimer.setPrefSize(36, 36);
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 5;");
        btnSupprimer.setTooltip(new Tooltip("Annuler"));
        btnSupprimer.setOnAction(ev -> {
            if (confirmerAnnulationParticipation()) {
                annulerParticipation(p);
            }
        });

        boutons.getChildren().addAll(btnTicket, btnEditer, btnSupprimer);
        carte.getChildren().addAll(imgBox, infos, boutons);
        return carte;
    }
}
