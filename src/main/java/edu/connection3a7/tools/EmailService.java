package edu.connection3a7.tools;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import edu.connection3a7.entities.Accompagnant;
import edu.connection3a7.entities.Evenement;
import edu.connection3a7.entities.Participation;
import edu.connection3a7.entities.Utilisateur;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Service d'envoi d'emails via Gmail SMTP.
 * <p>
 * Flux en deux étapes :
 * 1) Email de confirmation avec un lien cliquable
 * 2) Après confirmation → email avec ticket PDF en pièce jointe
 * <p>
 * Configuration : appeler {@link #configurer(String, String)} avec votre
 * adresse Gmail et un App Password (pas le mot de passe principal).
 * Pour créer un App Password : https://myaccount.google.com/apppasswords
 */
public class EmailService {

    // ⚠️ Remplacez par votre adresse Gmail et App Password
    private static String EMAIL_FROM = "selminaama73@gmail.com";
    private static String APP_PASSWORD = "ndcf lmfn lhff jgtk";

    private static EmailService instance;
    private Session session;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private EmailService() {
        initSession();
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Configure les identifiants Gmail.
     * À appeler avant toute utilisation (ex: au démarrage de l'app).
     */
    public static void configurer(String email, String appPassword) {
        EMAIL_FROM = email;
        APP_PASSWORD = appPassword;
        instance = null; // Force la réinitialisation
    }

    public boolean isConfigured() {
        return EMAIL_FROM != null && !EMAIL_FROM.isBlank()
                && !EMAIL_FROM.equals("VOTRE_EMAIL@gmail.com")
                && APP_PASSWORD != null && !APP_PASSWORD.isBlank()
                && !APP_PASSWORD.equals("VOTRE_APP_PASSWORD");
    }

    private void initSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });
    }

    // ================================================================
    //  1) EMAIL DE CONFIRMATION
    // ================================================================

    /**
     * Envoie un email de confirmation avec un lien cliquable (asynchrone).
     */
    public CompletableFuture<Boolean> envoyerEmailConfirmation(String destinataire, String prenom,
                                                                Evenement e, String codeParticipation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String confirmUrl = buildConfirmUrl(codeParticipation);
                String html = buildConfirmationHtml(prenom, e, codeParticipation, confirmUrl);

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM, "Firma - Événements"));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
                message.setSubject("Confirmez votre inscription - " + e.getTitre());
                message.setContent(html, "text/html; charset=UTF-8");

                Transport.send(message);
                System.out.println("✉ Email de confirmation envoyé à " + destinataire);
                return true;
            } catch (Exception ex) {
                System.err.println("Erreur envoi email de confirmation : " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        });
    }

    private String buildConfirmUrl(String codeParticipation) {
        TicketServerService tss = TicketServerService.getInstance();
        String baseUrl;
        if (tss.isRunning()) {
            baseUrl = "http://" + tss.getLanAddress() + ":" + tss.getPort();
        } else {
            baseUrl = "http://localhost:8642";
        }
        return baseUrl + "/confirm?code=" + codeParticipation;
    }

    private String buildConfirmationHtml(String prenom, Evenement e, String code, String confirmUrl) {
        String dateStr = e.getDateDebut() != null ? e.getDateDebut().format(DATE_FMT) : "?";
        String lieuStr = e.getLieu() != null ? e.getLieu() : "";

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0; padding:0; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f4;">
              <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; margin-top: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
                
                <!-- Header vert -->
                <div style="background: linear-gradient(135deg, #49ad32, #3d9429); padding: 30px 40px; text-align: center;">
                  <h1 style="color: white; margin: 0; font-size: 24px;">🎫 Firma</h1>
                  <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 14px;">Gestion d'Événements</p>
                </div>
                
                <!-- Corps -->
                <div style="padding: 35px 40px;">
                  <h2 style="color: #333; margin: 0 0 15px; font-size: 20px;">Bonjour %s ! 👋</h2>
                  <p style="color: #555; font-size: 15px; line-height: 1.6;">
                    Votre demande d'inscription à l'événement a bien été enregistrée. 
                    Veuillez confirmer votre participation en cliquant sur le bouton ci-dessous.
                  </p>
                  
                  <!-- Résumé événement -->
                  <div style="background: #f8faf7; border-left: 4px solid #49ad32; padding: 18px 20px; border-radius: 0 8px 8px 0; margin: 20px 0;">
                    <p style="margin: 0 0 8px; font-weight: bold; color: #333; font-size: 16px;">%s</p>
                    <p style="margin: 0; color: #666; font-size: 13px;">📅 %s &nbsp;&nbsp; 📍 %s</p>
                    <p style="margin: 8px 0 0; color: #888; font-size: 12px;">Code : <strong>%s</strong></p>
                  </div>
                  
                  <!-- Bouton de confirmation -->
                  <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #49ad32, #3d9429); color: white; text-decoration: none; padding: 14px 40px; border-radius: 25px; font-size: 16px; font-weight: bold; letter-spacing: 0.5px; box-shadow: 0 4px 12px rgba(73,173,50,0.3);">
                      ✅ Confirmer ma participation
                    </a>
                  </div>
                  
                  <p style="color: #999; font-size: 12px; text-align: center;">
                    Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br>
                    <a href="%s" style="color: #49ad32; word-break: break-all;">%s</a>
                  </p>
                </div>
                
                <!-- Footer -->
                <div style="background: #f8f8f8; padding: 20px 40px; text-align: center; border-top: 1px solid #eee;">
                  <p style="color: #999; font-size: 11px; margin: 0;">
                    Cet email a été envoyé automatiquement par Firma.<br>
                    Si vous n'avez pas demandé cette inscription, ignorez cet email.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(prenom, e.getTitre(), dateStr, lieuStr, code, confirmUrl, confirmUrl, confirmUrl);
    }

    // ================================================================
    //  2) EMAIL AVEC TICKET PDF
    // ================================================================

    /**
     * Envoie un email avec le ticket PDF en pièce jointe (asynchrone).
     */
    public CompletableFuture<Boolean> envoyerEmailTicketPDF(String destinataire, String prenom, String nom,
                                                             Evenement e, Participation participation,
                                                             List<Accompagnant> accompagnants) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Générer le PDF en mémoire
                byte[] pdfBytes = genererTicketPDF(e, participation, prenom, nom, accompagnants);

                // Construire l'email multipart (HTML + pièce jointe)
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM, "Firma - Événements"));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(destinataire));
                message.setSubject("Votre ticket - " + e.getTitre() + " 🎫");

                // Partie HTML
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(buildTicketEmailHtml(prenom, e, participation), "text/html; charset=UTF-8");

                // Pièce jointe PDF
                MimeBodyPart pdfPart = new MimeBodyPart();
                pdfPart.setFileName("ticket_" + participation.getCodeParticipation() + ".pdf");
                pdfPart.setContent(pdfBytes, "application/pdf");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(htmlPart);
                multipart.addBodyPart(pdfPart);

                message.setContent(multipart);
                Transport.send(message);
                System.out.println("✉ Email avec ticket PDF envoyé à " + destinataire);
                return true;
            } catch (Exception ex) {
                System.err.println("Erreur envoi email ticket PDF : " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        });
    }

    private String buildTicketEmailHtml(String prenom, Evenement e, Participation participation) {
        String dateStr = e.getDateDebut() != null ? e.getDateDebut().format(DATE_FMT) : "?";

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0; padding:0; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f4;">
              <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; margin-top: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
                
                <div style="background: linear-gradient(135deg, #49ad32, #3d9429); padding: 30px 40px; text-align: center;">
                  <h1 style="color: white; margin: 0; font-size: 24px;">🎫 Firma</h1>
                  <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 14px;">Votre ticket est prêt !</p>
                </div>
                
                <div style="padding: 35px 40px;">
                  <h2 style="color: #333; margin: 0 0 15px; font-size: 20px;">Félicitations %s ! 🎉</h2>
                  <p style="color: #555; font-size: 15px; line-height: 1.6;">
                    Votre participation à l'événement <strong>%s</strong> est maintenant <span style="color: #49ad32; font-weight: bold;">confirmée</span>.
                  </p>
                  
                  <div style="background: #e8f8e0; border-radius: 10px; padding: 20px; text-align: center; margin: 25px 0;">
                    <p style="margin: 0; color: #2d8a1a; font-size: 16px; font-weight: bold;">✅ PARTICIPATION CONFIRMÉE</p>
                    <p style="margin: 8px 0 0; color: #555; font-size: 13px;">Code : <strong>%s</strong></p>
                  </div>
                  
                  <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    📎 Vous trouverez votre ticket complet en <strong>pièce jointe PDF</strong>.<br>
                    Présentez-le (imprimé ou sur smartphone) le jour de l'événement.
                  </p>
                  
                  <div style="background: #f8faf7; border-left: 4px solid #49ad32; padding: 15px 20px; border-radius: 0 8px 8px 0; margin: 20px 0;">
                    <p style="margin: 0 0 5px; font-weight: bold; color: #333;">📅 %s</p>
                    <p style="margin: 0; color: #666; font-size: 13px;">📍 %s</p>
                  </div>
                </div>
                
                <div style="background: #f8f8f8; padding: 20px 40px; text-align: center; border-top: 1px solid #eee;">
                  <p style="color: #999; font-size: 11px; margin: 0;">
                    Merci d'avoir choisi Firma. À bientôt ! 🎊
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(prenom, e.getTitre(), participation.getCodeParticipation(), dateStr,
                e.getLieu() != null ? e.getLieu() : "");
    }

    // ================================================================
    //  GÉNÉRATION PDF EN MÉMOIRE
    // ================================================================

    /**
     * Génère un ticket PDF en mémoire (byte[]) — même style que l'export PDF existant.
     */
    public byte[] genererTicketPDF(Evenement e, Participation participation,
                                    String prenom, String nom,
                                    List<Accompagnant> accompagnants) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(30, 40, 30, 40);

        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        DeviceRgb vertPrimaire = new DeviceRgb(73, 173, 50);
        DeviceRgb vertClair = new DeviceRgb(232, 248, 224);
        DeviceRgb gris = new DeviceRgb(102, 102, 102);
        DeviceRgb grisClair = new DeviceRgb(240, 236, 224);

        String code = participation.getCodeParticipation();

        // Titre
        doc.add(new Paragraph("TICKET DE PARTICIPATION")
                .setFont(fontBold).setFontSize(22).setFontColor(vertPrimaire)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
        doc.add(new Paragraph(e.getTitre())
                .setFont(fontBold).setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        // Infos événement
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .useAllAvailableWidth().setMarginBottom(20);

        addPdfRow(infoTable, "Evenement", e.getTitre(), fontBold, fontNormal);
        addPdfRow(infoTable, "Date",
                (e.getDateDebut() != null ? e.getDateDebut().format(DATE_FMT) : "-")
                        + " -> " + (e.getDateFin() != null ? e.getDateFin().format(DATE_FMT) : "-"),
                fontBold, fontNormal);
        addPdfRow(infoTable, "Horaire",
                (e.getHoraireDebut() != null ? e.getHoraireDebut().format(TIME_FMT) : "-")
                        + " - " + (e.getHoraireFin() != null ? e.getHoraireFin().format(TIME_FMT) : "-"),
                fontBold, fontNormal);
        addPdfRow(infoTable, "Lieu",
                (e.getLieu() != null ? e.getLieu() : "-")
                        + (e.getAdresse() != null && !e.getAdresse().isBlank() ? " — " + e.getAdresse() : ""),
                fontBold, fontNormal);
        addPdfRow(infoTable, "Organisateur",
                e.getOrganisateur() != null ? e.getOrganisateur() : "-", fontBold, fontNormal);

        doc.add(infoTable);

        // Carte participant principal
        addParticipantCard(doc, code, prenom, nom, "Participant principal",
                fontBold, fontNormal, vertPrimaire, vertClair, e.getTitre());

        // Cartes accompagnants
        if (accompagnants != null) {
            for (int i = 0; i < accompagnants.size(); i++) {
                Accompagnant a = accompagnants.get(i);
                String accCode = code + "-A" + (i + 1);
                addParticipantCard(doc, accCode, a.getPrenom(), a.getNom(),
                        "Accompagnant " + (i + 1), fontBold, fontNormal, vertPrimaire, vertClair, e.getTitre());
            }
        }

        // Footer
        doc.add(new Paragraph("Statut : CONFIRME")
                .setFont(fontBold).setFontSize(12).setFontColor(vertPrimaire)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(15));
        doc.add(new Paragraph("Genere le " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy a HH:mm")))
                .setFont(fontNormal).setFontSize(9).setFontColor(gris)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(10));

        doc.close();
        return baos.toByteArray();
    }

    private void addPdfRow(Table table, String label, String value, PdfFont fontBold, PdfFont fontNormal) {
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

    private void addParticipantCard(Document doc, String code, String prenom, String nom,
                                     String role, PdfFont fontBold, PdfFont fontNormal,
                                     DeviceRgb headerColor, DeviceRgb badgeColor, String titreEvenement) {
        // Header vert avec code
        Table cardHeader = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth().setMarginTop(15).setMarginBottom(5);

        Cell headerLeft = new Cell().add(
                        new Paragraph(role).setFont(fontBold).setFontSize(12).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerColor).setBorder(Border.NO_BORDER).setPadding(10);
        Cell headerRight = new Cell().add(
                        new Paragraph(code).setFont(fontBold).setFontSize(11).setFontColor(ColorConstants.WHITE)
                                .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(headerColor).setBorder(Border.NO_BORDER).setPadding(10);
        cardHeader.addCell(headerLeft);
        cardHeader.addCell(headerRight);
        doc.add(cardHeader);

        String fullName = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");

        // Corps : infos + QR
        Table body = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth().setMarginBottom(10);

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth();
        addPdfRow(infoTable, "Participant", fullName.trim(), fontBold, fontNormal);
        addPdfRow(infoTable, "Code", code, fontBold, fontNormal);

        Cell infoCell = new Cell().add(infoTable).setBorder(Border.NO_BORDER).setPadding(4);
        body.addCell(infoCell);

        // QR Code
        Cell qrCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        try {
            String qrContent = QRCodeUtil.construireContenuTicket(code, fullName.trim(),
                    titreEvenement != null ? titreEvenement : "");
            BufferedImage qrBuffered = QRCodeUtil.genererQRCodeBufferedImage(qrContent, 200);
            ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
            ImageIO.write(qrBuffered, "png", qrBaos);
            ImageData qrImageData = ImageDataFactory.create(qrBaos.toByteArray());
            Image qrImage = new Image(qrImageData).scaleToFit(90, 90);
            qrCell.add(new Paragraph().add(qrImage).setTextAlignment(TextAlignment.CENTER));
            qrCell.add(new Paragraph("Scannez pour valider")
                    .setFont(fontNormal).setFontSize(7)
                    .setFontColor(new DeviceRgb(153, 153, 153))
                    .setTextAlignment(TextAlignment.CENTER));
        } catch (Exception ex) {
            qrCell.add(new Paragraph("[QR Code]").setFont(fontNormal).setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));
        }
        body.addCell(qrCell);

        // Badge confirmé
        Cell statutCell = new Cell(1, 2).add(
                        new Paragraph("CONFIRME").setFont(fontBold).setFontSize(10)
                                .setFontColor(new DeviceRgb(45, 138, 26))
                                .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(badgeColor)
                .setBorder(Border.NO_BORDER).setPadding(6);
        body.addCell(statutCell);

        doc.add(body);
    }
}
