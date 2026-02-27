package edu.connection3a7.controllers;

import edu.connection3a7.entities.Accompagnant;
import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Participation;
import edu.connection3a7.entities.Utilisateur;
import edu.connection3a7.services.AccompagnantService;
import edu.connection3a7.tools.QRCodeUtil;
import edu.connection3a7.tools.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère l'affichage des tickets de participation et leur export PDF.
 */
public class AffichageTicketsEtExportPDF {

    private final FrontController controller;

    AffichageTicketsEtExportPDF(FrontController controller) {
        this.controller = controller;
    }

    // Raccourcis
    private DateTimeFormatter dateFmt()         { return controller.getDateFmt(); }
    private DateTimeFormatter timeFmt()         { return controller.getTimeFmt(); }
    private AccompagnantService accompService() { return controller.getAccompagnantService(); }

    // ============================================================
    //  CARTES DE PARTICIPATION (après inscription)
    // ============================================================

    void afficherCartesParticipation(Participation participation, Evenement e,
                                     String prenomPrincipal, String nomPrincipal,
                                     List<Accompagnant> accompagnants) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Cartes de participation");
        popup.setResizable(true);
        popup.setMinWidth(550);
        popup.setMinHeight(450);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #fefbde;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 30, 18, 30));
        header.setStyle("-fx-background-color: #49ad32;");
        Label titleLabel = new Label("Inscription reussie !");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitleLabel = new Label("Voici vos cartes de participation");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Cards container
        VBox cardsContainer = new VBox(20);
        cardsContainer.setPadding(new Insets(20, 30, 20, 30));
        cardsContainer.setAlignment(Pos.TOP_CENTER);

        String baseCode = participation.getCodeParticipation();

        // === Carte du participant principal ===
        cardsContainer.getChildren().add(
                creerCarteParticipationVisuelle(baseCode, prenomPrincipal, nomPrincipal,
                        "Participant principal", e)
        );

        // === Cartes des accompagnants ===
        if (accompagnants != null) {
            for (int i = 0; i < accompagnants.size(); i++) {
                Accompagnant a = accompagnants.get(i);
                String accCode = baseCode + "-A" + (i + 1);
                cardsContainer.getChildren().add(
                        creerCarteParticipationVisuelle(accCode, a.getPrenom(), a.getNom(),
                                "Accompagnant " + (i + 1), e)
                );
            }
        }

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #fefbde; -fx-background: #fefbde;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 30, 18, 30));
        footer.setStyle("-fx-background-color: #fefbde; -fx-border-color: #e8e4c0; -fx-border-width: 1 0 0 0;");

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefSize(110, 38);
        btnFermer.setStyle(
                "-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnFermer.setOnAction(ev -> popup.close());
        footer.getChildren().add(btnFermer);

        root.getChildren().addAll(header, scrollPane, footer);

        popup.setScene(new Scene(root, 580, 520));
        popup.showAndWait();

        // Recharger la liste
        controller.rechargerListe();
    }

    // ============================================================
    //  CARTE VISUELLE DE PARTICIPATION
    // ============================================================

    VBox creerCarteParticipationVisuelle(String code, String prenom, String nom,
                                         String role, Evenement e) {
        VBox card = new VBox(0);
        card.setMaxWidth(480);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e0dcc0;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        // ── Bande verte en haut avec code ──
        HBox cardHeader = new HBox();
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(14, 18, 14, 18));
        cardHeader.setStyle("-fx-background-color: #49ad32; -fx-background-radius: 12 12 0 0;");

        VBox headerLeft = new VBox(2);
        HBox.setHgrow(headerLeft, Priority.ALWAYS);
        Label lblEvent = new Label(e.getTitre());
        lblEvent.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblEvent.setWrapText(true);
        Label lblRole = new Label(role);
        lblRole.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        headerLeft.getChildren().addAll(lblEvent, lblRole);

        VBox headerRight = new VBox(2);
        headerRight.setAlignment(Pos.CENTER_RIGHT);
        Label lblCodeTitle = new Label("CODE");
        lblCodeTitle.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");
        Label lblCode = new Label(code);
        lblCode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;" +
                "-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 6; -fx-padding: 4 10;");
        headerRight.getChildren().addAll(lblCodeTitle, lblCode);

        cardHeader.getChildren().addAll(headerLeft, headerRight);

        // ── Corps de la carte ──
        VBox cardBody = new VBox(10);
        cardBody.setPadding(new Insets(16, 18, 16, 18));

        // Nom du participant
        HBox nomRow = new HBox(10);
        nomRow.setAlignment(Pos.CENTER_LEFT);
        Label lblNomLabel = new Label("Participant :");
        lblNomLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: bold;");
        lblNomLabel.setMinWidth(100);
        Label lblNomValue = new Label((prenom != null ? prenom : "") + " " + (nom != null ? nom : ""));
        lblNomValue.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222;");
        nomRow.getChildren().addAll(lblNomLabel, lblNomValue);

        // Ligne séparatrice
        Region sep1 = new Region();
        sep1.setPrefHeight(1);
        sep1.setStyle("-fx-background-color: #f0ece0;");

        // Date
        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Label lblDateLabel = new Label("Date :");
        lblDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: bold;");
        lblDateLabel.setMinWidth(100);
        String dateStr = (e.getDateDebut() != null ? e.getDateDebut().format(dateFmt()) : "?");
        if (e.getDateFin() != null && !e.getDateFin().equals(e.getDateDebut())) {
            dateStr += " au " + e.getDateFin().format(dateFmt());
        }
        Label lblDateValue = new Label(dateStr);
        lblDateValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        dateRow.getChildren().addAll(lblDateLabel, lblDateValue);

        // Horaire
        HBox horaireRow = new HBox(10);
        horaireRow.setAlignment(Pos.CENTER_LEFT);
        Label lblHoraireLabel = new Label("Horaire :");
        lblHoraireLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: bold;");
        lblHoraireLabel.setMinWidth(100);
        String horaireStr = (e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt()) : "?") +
                " - " + (e.getHoraireFin() != null ? e.getHoraireFin().format(timeFmt()) : "?");
        Label lblHoraireValue = new Label(horaireStr);
        lblHoraireValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        horaireRow.getChildren().addAll(lblHoraireLabel, lblHoraireValue);

        // Lieu
        HBox lieuRow = new HBox(10);
        lieuRow.setAlignment(Pos.CENTER_LEFT);
        Label lblLieuLabel = new Label("Lieu :");
        lblLieuLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: bold;");
        lblLieuLabel.setMinWidth(100);
        String lieuStr = (e.getLieu() != null && !e.getLieu().isBlank()) ? e.getLieu() : "-";
        if (e.getAdresse() != null && !e.getAdresse().isBlank()) lieuStr += ", " + e.getAdresse();
        Label lblLieuValue = new Label(lieuStr);
        lblLieuValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
        lblLieuValue.setWrapText(true);
        lieuRow.getChildren().addAll(lblLieuLabel, lblLieuValue);

        Region sep2 = new Region();
        sep2.setPrefHeight(1);
        sep2.setStyle("-fx-background-color: #f0ece0;");

        // QR Code
        String fullName = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
        String qrContent = QRCodeUtil.construireContenuTicket(code, fullName.trim(), e.getTitre());
        javafx.scene.image.ImageView qrImageView = QRCodeUtil.genererQRCodeImageView(qrContent, 120);

        VBox qrBox = new VBox(4);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setPadding(new Insets(6, 0, 2, 0));
        Label lblQR = new Label("Scannez pour valider");
        lblQR.setStyle("-fx-font-size: 9px; -fx-text-fill: #999; -fx-font-style: italic;");
        qrBox.getChildren().addAll(qrImageView, lblQR);

        // Statut Confirmé
        HBox statutRow = new HBox(10);
        statutRow.setAlignment(Pos.CENTER);
        Label lblStatut = new Label("CONFIRME");
        lblStatut.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d8a1a;" +
                "-fx-background-color: #e8f8e0; -fx-background-radius: 12; -fx-padding: 4 16;");
        statutRow.getChildren().add(lblStatut);

        cardBody.getChildren().addAll(nomRow, sep1, dateRow, horaireRow, lieuRow, sep2, qrBox, statutRow);

        card.getChildren().addAll(cardHeader, cardBody);
        return card;
    }

    // ============================================================
    //  AFFICHER TICKET (consultation)
    // ============================================================

    void afficherTicket(Evenement e, Participation participation) {
        if (e == null || participation == null) return;

        Utilisateur u = SessionManager.getInstance().getUtilisateur();
        String prenomUser = (u != null) ? u.getPrenom() : "";
        String nomUser = (u != null) ? u.getNom() : "";

        // Charger les accompagnants depuis la BD
        List<Accompagnant> accompagnants = new ArrayList<>();
        try {
            accompagnants = accompService().getByParticipation(participation.getIdParticipation());
        } catch (SQLException ex) {
            System.err.println("Erreur chargement accompagnants: " + ex.getMessage());
        }

        String code = participation.getCodeParticipation();
        if (code == null || code.isBlank()) {
            code = "PART-" + String.format("%05d", participation.getIdParticipation());
        }

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Mon ticket — " + e.getTitre());
        popup.setResizable(true);
        popup.setMinWidth(550);
        popup.setMinHeight(400);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #fefbde;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 30, 18, 30));
        header.setStyle("-fx-background-color: #49ad32;");
        Label titleLabel = new Label("Mon ticket de participation");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitleLabel = new Label(e.getTitre());
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Cards
        VBox cardsContainer = new VBox(20);
        cardsContainer.setPadding(new Insets(20, 30, 20, 30));
        cardsContainer.setAlignment(Pos.TOP_CENTER);

        // Carte du participant principal
        cardsContainer.getChildren().add(
                creerCarteParticipationVisuelle(code, prenomUser, nomUser, "Participant principal", e)
        );

        // Cartes des accompagnants
        for (int i = 0; i < accompagnants.size(); i++) {
            Accompagnant a = accompagnants.get(i);
            String accCode = code + "-A" + (i + 1);
            cardsContainer.getChildren().add(
                    creerCarteParticipationVisuelle(accCode, a.getPrenom(), a.getNom(), "Accompagnant " + (i + 1), e)
            );
        }

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #fefbde; -fx-background: #fefbde;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 30, 18, 30));
        footer.setStyle("-fx-background-color: #fefbde; -fx-border-color: #e8e4c0; -fx-border-width: 1 0 0 0;");

        // Capture final variables for lambda
        final String finalCode = code;
        final List<Accompagnant> finalAccompagnants = accompagnants;

        Button btnExportPDF = new Button("Exporter PDF");
        btnExportPDF.setPrefHeight(38);
        btnExportPDF.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 0 18;"
        );
        btnExportPDF.setOnAction(ev -> exporterTicketPDF(e, participation, finalCode, prenomUser, nomUser, finalAccompagnants, popup));

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefSize(110, 38);
        btnFermer.setStyle(
                "-fx-background-color: #49ad32; -fx-text-fill: white; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"
        );
        btnFermer.setOnAction(ev -> popup.close());

        Region spacerFooter = new Region();
        HBox.setHgrow(spacerFooter, Priority.ALWAYS);
        footer.getChildren().addAll(btnExportPDF, spacerFooter, btnFermer);

        root.getChildren().addAll(header, scrollPane, footer);
        popup.setScene(new Scene(root, 580, 520));
        popup.showAndWait();
    }

    // ============================================================
    //  EXPORT PDF
    // ============================================================

    private void exporterTicketPDF(Evenement e, Participation participation, String code,
                                    String prenom, String nom, List<Accompagnant> accompagnants, Stage parentPopup) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le ticket PDF");
        fileChooser.setInitialFileName("ticket_" + code + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(parentPopup);
        if (file == null) return;

        try {
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(30, 40, 30, 40);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            DeviceRgb vertPrimaire = new DeviceRgb(73, 173, 50);   // #49ad32
            DeviceRgb vertClair    = new DeviceRgb(232, 248, 224); // #e8f8e0
            DeviceRgb gris         = new DeviceRgb(102, 102, 102); // #666
            DeviceRgb grisClair    = new DeviceRgb(240, 236, 224); // #f0ece0

            // ── Titre principal ──
            doc.add(new Paragraph("TICKET DE PARTICIPATION")
                    .setFont(fontBold).setFontSize(22).setFontColor(vertPrimaire)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
            doc.add(new Paragraph(e.getTitre())
                    .setFont(fontBold).setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // ── Infos de l'evenement ──
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                    .useAllAvailableWidth().setMarginBottom(20);

            ajouterLignePDF(infoTable, "Evenement", e.getTitre(), fontBold, fontNormal, grisClair);
            ajouterLignePDF(infoTable, "Date",
                    (e.getDateDebut() != null ? e.getDateDebut().format(dateFmt()) : "-")
                            + " -> " + (e.getDateFin() != null ? e.getDateFin().format(dateFmt()) : "-"),
                    fontBold, fontNormal, grisClair);
            ajouterLignePDF(infoTable, "Horaire",
                    (e.getHoraireDebut() != null ? e.getHoraireDebut().format(timeFmt()) : "-")
                            + " - " + (e.getHoraireFin() != null ? e.getHoraireFin().format(timeFmt()) : "-"),
                    fontBold, fontNormal, grisClair);
            ajouterLignePDF(infoTable, "Lieu",
                    (e.getLieu() != null && !e.getLieu().isBlank() ? e.getLieu() : "-")
                            + (e.getAdresse() != null && !e.getAdresse().isBlank() ? " — " + e.getAdresse() : ""),
                    fontBold, fontNormal, grisClair);
            ajouterLignePDF(infoTable, "Organisateur",
                    e.getOrganisateur() != null ? e.getOrganisateur() : "-",
                    fontBold, fontNormal, grisClair);

            doc.add(infoTable);

            // ── Carte du participant principal ──
            ajouterCarteParticipantPDF(doc, code, prenom, nom, "Participant principal",
                    fontBold, fontNormal, vertPrimaire, vertClair, grisClair, e.getTitre());

            // ── Cartes des accompagnants ──
            if (accompagnants != null) {
                for (int i = 0; i < accompagnants.size(); i++) {
                    Accompagnant a = accompagnants.get(i);
                    String accCode = code + "-A" + (i + 1);
                    ajouterCarteParticipantPDF(doc, accCode, a.getPrenom(), a.getNom(),
                            "Accompagnant " + (i + 1), fontBold, fontNormal, vertPrimaire, vertClair, grisClair, e.getTitre());
                }
            }

            // ── Pied de page ──
            doc.add(new Paragraph("Statut : CONFIRME")
                    .setFont(fontBold).setFontSize(12).setFontColor(vertPrimaire)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(15));
            doc.add(new Paragraph("Genere le " + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy a HH:mm")))
                    .setFont(fontNormal).setFontSize(9).setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(10));

            doc.close();

            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.INFORMATION, "Export reussi",
                    "Le ticket a ete exporte avec succes en PDF.\n" + file.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            OutilsInterfaceGraphique.afficherAlerte(Alert.AlertType.ERROR, "Erreur export",
                    "Impossible d'exporter le ticket en PDF :\n" + ex.getMessage());
        }
    }

    private void ajouterLignePDF(Table table, String label, String value,
                                  PdfFont fontBold, PdfFont fontNormal, DeviceRgb separatorColor) {
        Cell cellLabel = new Cell().add(new Paragraph(label).setFont(fontBold).setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(240, 240, 240), 0.5f))
                .setPadding(8);
        Cell cellValue = new Cell().add(new Paragraph(value).setFont(fontNormal).setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(240, 240, 240), 0.5f))
                .setPadding(8);
        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    private void ajouterCarteParticipantPDF(Document doc, String code, String prenom, String nom,
                                            String role, PdfFont fontBold, PdfFont fontNormal,
                                            DeviceRgb headerColor, DeviceRgb badgeColor, DeviceRgb separatorColor) {
        ajouterCarteParticipantPDF(doc, code, prenom, nom, role, fontBold, fontNormal,
                headerColor, badgeColor, separatorColor, null);
    }

    private void ajouterCarteParticipantPDF(Document doc, String code, String prenom, String nom,
                                            String role, PdfFont fontBold, PdfFont fontNormal,
                                            DeviceRgb headerColor, DeviceRgb badgeColor, DeviceRgb separatorColor,
                                            String titreEvenement) {
        // Titre vert avec code
        Table cardTable = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth()
                .setMarginTop(15)
                .setMarginBottom(5);

        Cell headerLeft = new Cell().add(
                        new Paragraph(role).setFont(fontBold).setFontSize(12).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerColor)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);
        Cell headerRight = new Cell().add(
                        new Paragraph(code).setFont(fontBold).setFontSize(11).setFontColor(ColorConstants.WHITE)
                                .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(headerColor)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);
        cardTable.addCell(headerLeft);
        cardTable.addCell(headerRight);
        doc.add(cardTable);

        // Corps : infos + QR Code côte à côte
        String fullName = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");

        Table bodyTable = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth().setMarginBottom(10);

        // Colonne gauche : informations textuelles
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth();
        ajouterLignePDF(infoTable, "Participant", fullName.trim(), fontBold, fontNormal, separatorColor);
        ajouterLignePDF(infoTable, "Code", code, fontBold, fontNormal, separatorColor);

        Cell infoCell = new Cell().add(infoTable)
                .setBorder(Border.NO_BORDER)
                .setPadding(4);
        bodyTable.addCell(infoCell);

        // Colonne droite : QR Code
        Cell qrCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        try {
            String qrContent = QRCodeUtil.construireContenuTicket(code, fullName.trim(),
                    titreEvenement != null ? titreEvenement : "");
            BufferedImage qrBuffered = QRCodeUtil.genererQRCodeBufferedImage(qrContent, 200);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrBuffered, "png", baos);
            ImageData qrImageData = ImageDataFactory.create(baos.toByteArray());
            Image qrImage = new Image(qrImageData).scaleToFit(90, 90);
            qrCell.add(new Paragraph().add(qrImage).setTextAlignment(TextAlignment.CENTER));
            qrCell.add(new Paragraph("Scannez pour valider")
                    .setFont(fontNormal).setFontSize(7)
                    .setFontColor(new DeviceRgb(153, 153, 153))
                    .setTextAlignment(TextAlignment.CENTER));
        } catch (Exception ex) {
            System.err.println("Erreur QR Code PDF : " + ex.getMessage());
            qrCell.add(new Paragraph("[QR Code]").setFont(fontNormal).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));
        }
        bodyTable.addCell(qrCell);

        // Ligne statut sur toute la largeur
        Cell statutCell = new Cell(1, 2).add(
                        new Paragraph("CONFIRME").setFont(fontBold).setFontSize(10)
                                .setFontColor(new DeviceRgb(45, 138, 26))
                                .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(badgeColor)
                .setBorder(Border.NO_BORDER)
                .setPadding(6);
        bodyTable.addCell(statutCell);

        doc.add(bodyTable);
    }
}
