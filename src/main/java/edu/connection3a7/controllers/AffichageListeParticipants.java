package edu.connection3a7.controllers;

import edu.connection3a7.entities.Accompagnant;
import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Participation;
import edu.connection3a7.services.ParticipationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Popup d'affichage des participants inscrits à un événement (côté Admin).
 */
public class AffichageListeParticipants {

    private final EvenementController controller;

    AffichageListeParticipants(EvenementController controller) {
        this.controller = controller;
    }

    // Raccourcis
    private ParticipationService partService() { return controller.getParticipationService(); }

    // ============================================================
    //  POPUP PARTICIPANTS
    // ============================================================

    @SuppressWarnings("unchecked")
    void afficherParticipantsGrid(Evenement e) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Participants — " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(750);
        popup.setMinHeight(520);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #fefbde;");

        // ── En-tête ──
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 30, 18, 30));
        header.setStyle("-fx-background-color: #49ad32;");

        Label titleLabel = new Label("Liste des participants");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitleLabel = new Label(e.getTitre());
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        // ── Contenu ──
        VBox listContainer = new VBox(0);
        listContainer.setPadding(new Insets(15, 25, 15, 25));

        try {
            List<Map<String, Object>> participants =
                    partService().getParticipantsDetailsByEvent(e.getIdEvenement());

            // Compteur accompagnants
            int totalAccomp = 0;
            for (Map<String, Object> entry : participants) {
                List<Accompagnant> accs = (List<Accompagnant>) entry.get("accompagnants");
                if (accs != null) totalAccomp += accs.size();
            }

            HBox statsBar = new HBox(25);
            statsBar.setAlignment(Pos.CENTER_LEFT);
            statsBar.setPadding(new Insets(0, 0, 12, 0));
            statsBar.getChildren().addAll(
                    makeStatChip(String.valueOf(participants.size()), "inscriptions"),
                    makeStatChip(String.valueOf(totalAccomp), "accompagnants"),
                    makeStatChip(String.valueOf(participants.size() + totalAccomp), "total personnes")
            );
            listContainer.getChildren().add(statsBar);

            // En-tête de tableau
            HBox tableHeader = new HBox(0);
            tableHeader.setAlignment(Pos.CENTER_LEFT);
            tableHeader.setPadding(new Insets(10, 15, 10, 15));
            tableHeader.setStyle("-fx-background-color: #f5f5f0; -fx-background-radius: 6 6 0 0;");
            tableHeader.getChildren().addAll(
                    makeColHeader("#", 35),
                    makeColHeader("Nom complet", 180),
                    makeColHeader("Email", 200),
                    makeColHeader("Statut", 80),
                    makeColHeader("Accompagnants", 100),
                    makeColHeader("Commentaire", 140)
            );
            listContainer.getChildren().add(tableHeader);

            if (participants.isEmpty()) {
                Label noData = new Label("Aucun participant inscrit pour cet evenement.");
                noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #888; -fx-padding: 30;");
                listContainer.getChildren().add(noData);
            } else {
                for (int i = 0; i < participants.size(); i++) {
                    Map<String, Object> entry = participants.get(i);
                    listContainer.getChildren().add(creerLigneParticipant(entry, i));
                }
            }

        } catch (SQLException ex) {
            Label errLabel = new Label("Erreur : " + ex.getMessage());
            errLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: red; -fx-padding: 20;");
            listContainer.getChildren().add(errLabel);
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #fefbde; -fx-background: #fefbde;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Footer ──
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 25, 15, 25));
        footer.setStyle("-fx-background-color: #fefbde; -fx-border-color: #e8e4c0; -fx-border-width: 1 0 0 0;");

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefHeight(36);
        btnFermer.setPrefWidth(100);
        btnFermer.setStyle("-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
        btnFermer.setOnAction(ev -> popup.close());
        footer.getChildren().add(btnFermer);

        root.getChildren().addAll(header, scrollPane, footer);

        Scene scene = new Scene(root, 780, 540);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // ============================================================
    //  LIGNE PARTICIPANT (DÉPLIABLE)
    // ============================================================

    @SuppressWarnings("unchecked")
    private VBox creerLigneParticipant(Map<String, Object> entry, int index) {
        String nom = (String) entry.get("nom");
        String prenom = (String) entry.get("prenom");
        String email = (String) entry.get("email");
        Participation p = (Participation) entry.get("participation");
        List<Accompagnant> accompagnants = (List<Accompagnant>) entry.get("accompagnants");

        VBox wrapper = new VBox(0);
        String bgColor = (index % 2 == 0) ? "white" : "#fafaf5";

        // ── Ligne principale ──
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setStyle("-fx-background-color: " + bgColor
                + "; -fx-border-color: #f0ece0; -fx-border-width: 0 0 1 0;");

        Label lblNum = new Label(String.valueOf(index + 1));
        lblNum.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa; -fx-font-weight: bold;");
        lblNum.setPrefWidth(35);
        lblNum.setMinWidth(35);

        Label lblNom = new Label(OutilsInterfaceGraphique.nullSafe(prenom) + " " + OutilsInterfaceGraphique.nullSafe(nom));
        lblNom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        lblNom.setPrefWidth(180);
        lblNom.setMinWidth(180);
        lblNom.setWrapText(true);

        Label lblEmail = new Label(OutilsInterfaceGraphique.nullSafe(email));
        lblEmail.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        lblEmail.setPrefWidth(200);
        lblEmail.setMinWidth(200);
        lblEmail.setWrapText(true);

        String statutText = p.getStatut() != null ? p.getStatut().name() : "-";
        Label lblStatut = new Label(statutText);
        boolean isConfirme = statutText.equalsIgnoreCase("confirme") || statutText.equalsIgnoreCase("CONFIRME");
        lblStatut.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 8; "
                + "-fx-background-radius: 10; "
                + "-fx-background-color: " + (isConfirme ? "#e8f8e0" : "#fde8e8") + "; "
                + "-fx-text-fill: " + (isConfirme ? "#2d8a1a" : "#c0392b") + ";");
        lblStatut.setPrefWidth(80);
        lblStatut.setMinWidth(80);

        int nbAcc = (accompagnants != null) ? accompagnants.size() : 0;
        Label lblAccCount = new Label(nbAcc > 0 ? nbAcc + " personne(s)" : "Aucun");
        lblAccCount.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                + (nbAcc > 0 ? "#49ad32" : "#aaa") + "; -fx-font-weight: bold;");
        lblAccCount.setPrefWidth(100);
        lblAccCount.setMinWidth(100);

        String comm = p.getCommentaire() != null ? p.getCommentaire() : "-";
        if (comm.length() > 25) comm = comm.substring(0, 25) + "...";
        Label lblComm = new Label(comm);
        lblComm.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
        lblComm.setPrefWidth(140);
        lblComm.setMinWidth(140);
        lblComm.setWrapText(true);

        row.getChildren().addAll(lblNum, lblNom, lblEmail, lblStatut, lblAccCount, lblComm);
        wrapper.getChildren().add(row);

        // ── Section accompagnants dépliable ──
        if (nbAcc > 0) {
            VBox accSection = new VBox(3);
            accSection.setPadding(new Insets(6, 15, 10, 50));
            accSection.setStyle("-fx-background-color: " + bgColor + ";");
            accSection.setVisible(false);
            accSection.setManaged(false);

            Label accTitle = new Label("Accompagnants :");
            accTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
            accSection.getChildren().add(accTitle);

            for (Accompagnant a : accompagnants) {
                HBox accRow = new HBox(10);
                accRow.setAlignment(Pos.CENTER_LEFT);
                Label bullet = new Label("  •");
                bullet.setStyle("-fx-font-size: 11px; -fx-text-fill: #49ad32;");
                Label accName = new Label(OutilsInterfaceGraphique.nullSafe(a.getPrenom()) + " " + OutilsInterfaceGraphique.nullSafe(a.getNom()));
                accName.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;");
                accRow.getChildren().addAll(bullet, accName);
                accSection.getChildren().add(accRow);
            }

            // Clic sur la ligne pour déplier
            row.setStyle(row.getStyle() + " -fx-cursor: hand;");
            row.setOnMouseClicked(ev -> {
                boolean visible = !accSection.isVisible();
                accSection.setVisible(visible);
                accSection.setManaged(visible);
            });

            // Indicateur visuel "dépliable"
            Label expandIcon = new Label("\u25B8");
            expandIcon.setStyle("-fx-font-size: 11px; -fx-text-fill: #49ad32; -fx-font-weight: bold;");
            expandIcon.setMinWidth(15);
            row.getChildren().add(0, expandIcon);
            lblNum.setPrefWidth(20);
            lblNum.setMinWidth(20);

            accSection.visibleProperty().addListener((obs, oldVal, newVal) ->
                    expandIcon.setText(newVal ? "\u25BE" : "\u25B8"));

            wrapper.getChildren().add(accSection);
        }

        return wrapper;
    }

    // ============================================================
    //  UTILITAIRES
    // ============================================================

    private HBox makeStatChip(String value, String label) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #49ad32;");
        Label lblLbl = new Label(label);
        lblLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");
        chip.getChildren().addAll(valLbl, lblLbl);
        return chip;
    }

    private Label makeColHeader(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #666;");
        lbl.setPrefWidth(width);
        lbl.setMinWidth(width);
        return lbl;
    }
}
