package marketplace.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfCanvas;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import marketplace.entities.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service de génération de reçus PDF professionnels - Design FIRMA Premium
 */
public class PdfReceiptService {

    // ── Palette FIRMA ─────────────────────────────────────────────────────────
    private static final DeviceRgb PRIMARY_GREEN = new DeviceRgb(73, 173, 50); // #49ad32
    private static final DeviceRgb DARK_GREEN = new DeviceRgb(35, 130, 20); // #238214
    private static final DeviceRgb LIGHT_GREEN = new DeviceRgb(235, 248, 233); // #ebf8e9
    private static final DeviceRgb ACCENT_GREEN = new DeviceRgb(200, 240, 196); // soft accent
    private static final DeviceRgb GRAY_LIGHT = new DeviceRgb(245, 245, 245); // #f5f5f5
    private static final DeviceRgb GRAY_TEXT = new DeviceRgb(120, 120, 120); // #787878
    private static final DeviceRgb DARK_TEXT = new DeviceRgb(40, 40, 40); // #282828
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb ROW_ALT = new DeviceRgb(249, 252, 249);

    // ── Footer handler for page numbers ───────────────────────────────────────
    private static class FooterHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            int totalPages = pdfDoc.getNumberOfPages();
            Rectangle pageSize = page.getPageSize();

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            canvas.beginText()
                    .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 8)
                    .moveText(pageSize.getWidth() / 2 - 20, 25)
                    .showText("Page " + pageNumber + " / " + totalPages)
                    .endText();
            canvas.release();
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ENTRY POINT
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    public File generateReceipt(Commande commande, Utilisateur client,
            List<DetailCommande> details) throws Exception {

        File receiptsDir = new File("receipts");
        if (!receiptsDir.exists())
            receiptsDir.mkdirs();

        String numCmd = commande.getNumeroCommande() != null
                ? commande.getNumeroCommande()
                : "CMD-" + commande.getId();

        String fileName = String.format("receipts/FIRMA_Recu_%s_%s.pdf",
                numCmd,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        PdfWriter writer = new PdfWriter(new File(fileName));
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);

        Document document = new Document(pdf);
        document.setMargins(0, 0, 50, 0); // top margin handled by header block

        // ── 1. Full-width gradient header banner ─────────────────────────────
        addBannerHeader(document, commande, client);

        // ── 2. Info cards row (order details + client details side by side) ──
        document.add(buildInfoSection(commande, client));

        // ── 3. Items table ────────────────────────────────────────────────────
        document.add(buildItemsSection(details));

        // ── 4. Total summary ──────────────────────────────────────────────────
        document.add(buildTotalSection(commande));

        // ── 5. Thank-you block ────────────────────────────────────────────────
        document.add(buildThankYouSection(client));

        // ── 6. Footer note ────────────────────────────────────────────────────
        document.add(buildFooterNote());

        document.close();
        return new File(fileName);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SECTION BUILDERS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Full-width green banner header: logo left, company + receipt info right.
     * Uses a 2-column table spanning the full page width.
     */
    private void addBannerHeader(Document document, Commande commande, Utilisateur client) {
        String numCmd = commande.getNumeroCommande() != null
                ? commande.getNumeroCommande()
                : "CMD-" + commande.getId();
        String dateStr = commande.getDateCommande() != null
                ? commande.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Outer table: full page width, dark-green background
        Table banner = new Table(UnitValue.createPercentArray(new float[] { 1, 2 }));
        banner.setWidth(UnitValue.createPercentValue(100));
        banner.setBackgroundColor(DARK_GREEN);

        // ── Left cell: logo or FIRMA text ────────────────────────────────────
        Cell logoCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(DARK_GREEN)
                .setPadding(28);

        boolean logoAdded = false;
        try {
            URL logoUrl = getClass().getResource("/image/logo.png");
            if (logoUrl != null) {
                Image logo = new Image(ImageDataFactory.create(logoUrl));
                logo.setWidth(72).setHeight(72).setAutoScale(false);
                logoCell.add(logo);
                logoAdded = true;
            }
        } catch (Exception ignored) {
        }

        if (!logoAdded) {
            // Styled fallback text logo
            Paragraph firmaLogo = new Paragraph("FIRMA")
                    .setFontSize(38).setBold().setFontColor(WHITE)
                    .setMarginBottom(0);
            Paragraph tagline = new Paragraph("Marketplace Agricole")
                    .setFontSize(11).setFontColor(ACCENT_GREEN)
                    .setMarginTop(2);
            logoCell.add(firmaLogo).add(tagline);
        }

        banner.addCell(logoCell);

        // ── Right cell: receipt title + key meta ─────────────────────────────
        Cell infoCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(DARK_GREEN)
                .setPaddingTop(28).setPaddingBottom(28)
                .setPaddingRight(32).setPaddingLeft(10)
                .setTextAlignment(TextAlignment.RIGHT);

        infoCell.add(new Paragraph("REÇU DE PAIEMENT")
                .setFontSize(22).setBold().setFontColor(WHITE).setMarginBottom(8));

        infoCell.add(new Paragraph("N° " + numCmd)
                .setFontSize(13).setFontColor(ACCENT_GREEN).setMarginBottom(4));

        infoCell.add(new Paragraph("Date : " + dateStr)
                .setFontSize(10).setFontColor(ACCENT_GREEN).setMarginBottom(4));

        infoCell.add(new Paragraph("Client : " + client.getPrenom() + " " + client.getNom())
                .setFontSize(10).setFontColor(ACCENT_GREEN));

        banner.addCell(infoCell);

        // Thin green accent line below banner
        Table accentLine = new Table(1);
        accentLine.setWidth(UnitValue.createPercentValue(100));
        accentLine.addCell(new Cell().setHeight(4)
                .setBackgroundColor(PRIMARY_GREEN).setBorder(Border.NO_BORDER));

        document.add(banner);
        document.add(accentLine);
        document.add(spacer(14));
    }

    /**
     * Two info cards side-by-side: "Commande" and "Client".
     */
    private Table buildInfoSection(Commande commande, Utilisateur client) {
        String dateStr = commande.getDateCommande() != null
                ? commande.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        Table section = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }));
        section.setWidth(UnitValue.createPercentValue(100));
        section.setMarginLeft(32).setMarginRight(32)
                .setWidth(UnitValue.createPercentValue(90));

        // ── Order card ───────────────────────────────────────────────────────
        Cell orderCard = new Cell().setBorder(Border.NO_BORDER)
                .setPaddingRight(10);
        orderCard.add(sectionTitle("DÉTAILS COMMANDE"));
        orderCard.add(infoRow("N° Commande",
                commande.getNumeroCommande() != null ? commande.getNumeroCommande() : "CMD-" + commande.getId()));
        orderCard.add(infoRow("Date", dateStr));
        orderCard.add(infoRow("Paiement", translatePaymentStatus(commande.getStatutPaiement())));
        orderCard.add(infoRow("Livraison", translateDeliveryStatus(commande.getStatutLivraison())));
        if (commande.getAdresseLivraison() != null && !commande.getAdresseLivraison().isEmpty()
                && !commande.getAdresseLivraison().equals("À confirmer")) {
            orderCard.add(infoRow("Adresse", commande.getAdresseLivraison()));
        }

        // ── Client card ──────────────────────────────────────────────────────
        Cell clientCard = new Cell().setBorder(Border.NO_BORDER)
                .setPaddingLeft(10);
        clientCard.add(sectionTitle("INFORMATIONS CLIENT"));
        clientCard.add(infoRow("Nom", client.getNom() + " " + client.getPrenom()));
        clientCard.add(infoRow("Email", client.getEmail()));

        section.addCell(orderCard);
        section.addCell(clientCard);

        // Wrap in container with side margins
        Table wrapper = new Table(1);
        wrapper.setWidth(UnitValue.createPercentValue(100));
        wrapper.addCell(new Cell().add(section)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(32).setPaddingRight(32));
        return wrapper;
    }

    /**
     * Items table with alternating row colors.
     */
    private Table buildItemsSection(List<DetailCommande> details) {
        // Section title
        Paragraph title = sectionTitle("ARTICLES COMMANDÉS");

        Table wrapper = new Table(1);
        wrapper.setWidth(UnitValue.createPercentValue(100));

        // Title row
        wrapper.addCell(new Cell().add(title)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(32).setPaddingRight(32)
                .setPaddingTop(18).setPaddingBottom(6));

        // Items table
        Table items = new Table(UnitValue.createPercentArray(new float[] { 4, 1.2f, 1.5f, 1.5f }));
        items.setWidth(UnitValue.createPercentValue(100));
        items.setKeepTogether(true);

        // Header row
        items.addHeaderCell(headerCell("Article"));
        items.addHeaderCell(headerCell("Qté"));
        items.addHeaderCell(headerCell("Prix unitaire"));
        items.addHeaderCell(headerCell("Sous-total"));

        // Data rows
        boolean alternate = false;
        if (details == null || details.isEmpty()) {
            Cell empty = new Cell(1, 4)
                    .add(new Paragraph("Détails non disponibles").setFontColor(GRAY_TEXT).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(12).setBorder(Border.NO_BORDER);
            items.addCell(empty);
        } else {
            for (DetailCommande d : details) {
                DeviceRgb rowBg = alternate ? GRAY_LIGHT : WHITE;
                alternate = !alternate;

                items.addCell(dataCell(getProductName(d), TextAlignment.LEFT, rowBg));
                items.addCell(dataCell(String.valueOf(d.getQuantite()), TextAlignment.CENTER, rowBg));
                items.addCell(dataCell(formatPrice(d.getPrixUnitaire()), TextAlignment.RIGHT, rowBg));
                items.addCell(dataCell(formatPrice(d.getSousTotal()), TextAlignment.RIGHT, rowBg));
            }
        }

        Cell tableCell = new Cell().add(items)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(32).setPaddingRight(32);
        wrapper.addCell(tableCell);
        return wrapper;
    }

    /**
     * Total summary block — right-aligned.
     */
    private Table buildTotalSection(Commande commande) {
        Table wrapper = new Table(1);
        wrapper.setWidth(UnitValue.createPercentValue(100));
        wrapper.setKeepTogether(true);

        // Divider
        wrapper.addCell(new Cell().setHeight(1).setBackgroundColor(ACCENT_GREEN)
                .setBorder(Border.NO_BORDER)
                .setMarginLeft(32).setMarginRight(32)
                .setPaddingLeft(32).setPaddingRight(32));

        // Total row
        Table totalRow = new Table(UnitValue.createPercentArray(new float[] { 3, 1.5f }));
        totalRow.setWidth(UnitValue.createPercentValue(100));

        totalRow.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0));

        Cell totalCell = new Cell()
                .add(new Paragraph("TOTAL PAYÉ").setFontSize(9).setFontColor(GRAY_TEXT)
                        .setMarginBottom(2))
                .add(new Paragraph(formatPrice(commande.getMontantTotal()))
                        .setFontSize(22).setBold().setFontColor(DARK_GREEN))
                .setBackgroundColor(LIGHT_GREEN)
                .setBorder(new SolidBorder(PRIMARY_GREEN, 2))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(14)
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));

        totalRow.addCell(new Cell().setBorder(Border.NO_BORDER));
        totalRow.addCell(totalCell);

        Table totalWrapper = new Table(1);
        totalWrapper.setWidth(UnitValue.createPercentValue(100));
        totalWrapper.addCell(new Cell().add(totalRow).setBorder(Border.NO_BORDER)
                .setPaddingLeft(32).setPaddingRight(32).setPaddingTop(10).setPaddingBottom(10));

        wrapper.addCell(new Cell().add(totalWrapper).setBorder(Border.NO_BORDER));
        return wrapper;
    }

    /**
     * Thank-you block — kept together to avoid orphan on new page.
     */
    private Div buildThankYouSection(Utilisateur client) {
        Div box = new Div()
                .setBackgroundColor(LIGHT_GREEN)
                .setBorder(new SolidBorder(PRIMARY_GREEN, 1))
                .setPadding(20)
                .setMarginLeft(32).setMarginRight(32)
                .setMarginTop(18)
                .setKeepTogether(true);

        // Greeting + body in same Paragraph to stay on one page
        box.add(new Paragraph()
                .add(new Text(
                        String.format("Cher(e) %s %s,\n", client.getPrenom(), client.getNom()))
                        .setBold().setFontSize(13).setFontColor(DARK_GREEN))
                .add(new Text(
                        "\nNous vous remercions sincèrement pour votre confiance. " +
                                "Chez FIRMA, nous nous engageons à vous fournir les meilleurs équipements " +
                                "agricoles pour accompagner votre réussite.\n\n" +
                                "Votre satisfaction est notre priorité. N'hésitez pas à nous contacter " +
                                "pour toute question ou assistance.\n\n")
                        .setFontSize(11).setFontColor(DARK_TEXT))
                .add(new Text("Cordialement,\nL'équipe FIRMA 🌾")
                        .setBold().setFontSize(11).setFontColor(DARK_GREEN)));
        return box;
    }

    /**
     * Footer legal note.
     */
    private Div buildFooterNote() {
        Div footer = new Div()
                .setMarginLeft(32).setMarginRight(32).setMarginTop(20);

        footer.add(new Paragraph()
                .add(new Text("Document officiel • ")
                        .setFontSize(8).setFontColor(GRAY_TEXT))
                .add(new Text("FIRMA – contact@firma.tn – Tunis, Tunisie")
                        .setFontSize(8).setFontColor(GRAY_TEXT))
                .setTextAlignment(TextAlignment.CENTER));
        return footer;
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // HELPERS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private Paragraph sectionTitle(String text) {
        return new Paragraph(text)
                .setFontSize(11).setBold()
                .setFontColor(DARK_GREEN)
                .setMarginBottom(6)
                .setBorderBottom(new SolidBorder(PRIMARY_GREEN, 1.5f))
                .setPaddingBottom(4);
    }

    private Paragraph infoRow(String label, String value) {
        return new Paragraph()
                .add(new Text(label + ":  ").setBold().setFontSize(10).setFontColor(DARK_GREEN))
                .add(new Text(value).setFontSize(10).setFontColor(DARK_TEXT))
                .setMarginBottom(4);
    }

    private Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(10).setFontColor(WHITE))
                .setBackgroundColor(DARK_GREEN)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingTop(8).setPaddingBottom(8)
                .setBorder(Border.NO_BORDER);
    }

    private Cell dataCell(String text, TextAlignment align, DeviceRgb bg) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setFontColor(DARK_TEXT))
                .setBackgroundColor(bg)
                .setTextAlignment(align)
                .setPaddingTop(7).setPaddingBottom(7)
                .setPaddingLeft(8).setPaddingRight(8)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ACCENT_GREEN, 0.5f));
    }

    private Paragraph spacer(float height) {
        return new Paragraph(" ").setFontSize(1).setMarginTop(height).setMarginBottom(0);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null)
            return "0.00 DT";
        return String.format("%.2f DT", price);
    }

    private String getProductName(DetailCommande detail) {
        try {
            EquipementService service = new EquipementService();
            Equipement eq = service.getEntities().stream()
                    .filter(e -> e.getId() == detail.getEquipementId())
                    .findFirst().orElse(null);
            return eq != null ? eq.getNom() : "Produit #" + detail.getEquipementId();
        } catch (Exception e) {
            return "Produit #" + detail.getEquipementId();
        }
    }

    private String translatePaymentStatus(PaymentStatus status) {
        if (status == null)
            return "—";
        switch (status) {
            case PAYE:
                return "✓ Payé";
            case EN_ATTENTE:
                return "En attente";
            case ECHOUE:
                return "Échoué";
            case PARTIEL:
                return "Partiel";
            default:
                return status.toString();
        }
    }

    private String translateDeliveryStatus(DeliveryStatus status) {
        if (status == null)
            return "—";
        switch (status) {
            case EN_ATTENTE:
                return "En attente";
            case EN_PREPARATION:
                return "En préparation";
            case EXPEDIE:
                return "Expédié";
            case LIVRE:
                return "✓ Livré";
            case ANNULE:
                return "Annulé";
            default:
                return status.toString();
        }
    }
}
