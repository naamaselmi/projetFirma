package marketplace.service;

import marketplace.entities.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quick smoke-test: generates a PDF receipt with fake data and
 * sends it by email to hamza.slimani@esprit.tn.
 *
 * Run with: mvn test -Dtest=EmailReceiptTest
 */
class EmailReceiptTest {

    private static final String TEST_EMAIL = "hamza.slimani@esprit.tn";

    // ── Fake data ─────────────────────────────────────────────────────────────

    private Utilisateur fakeClient() {
        return new Utilisateur(
                99,
                "Slimani",
                "Hamza",
                TEST_EMAIL,
                "***",
                "client");
    }

    private Commande fakeCommande() {
        Commande c = new Commande();
        c.setId(9999);
        c.setUtilisateurId(99);
        c.setNumeroCommande("CMD-TEST-20260225");
        c.setMontantTotal(new BigDecimal("348.50"));
        c.setStatutPaiement(PaymentStatus.PAYE);
        c.setStatutLivraison(DeliveryStatus.EN_ATTENTE);
        c.setAdresseLivraison("12 Rue des Oliviers, Cité El Amal");
        c.setVilleLivraison("Tunis");
        c.setDateCommande(LocalDateTime.of(2026, 2, 25, 6, 7, 0));
        return c;
    }

    private List<DetailCommande> fakeDetails(int commandeId) {
        DetailCommande d1 = new DetailCommande(commandeId, 1, 2, new BigDecimal("129.00"));
        DetailCommande d2 = new DetailCommande(commandeId, 2, 1, new BigDecimal("90.50"));
        return List.of(d1, d2);
    }

    // ── Test ──────────────────────────────────────────────────────────────────

    @Test
    void testSendPaymentConfirmationEmail() throws Exception {
        Utilisateur client = fakeClient();
        Commande commande = fakeCommande();
        List<DetailCommande> details = fakeDetails(commande.getId());

        // 1. Generate PDF
        PdfReceiptService pdfService = new PdfReceiptService();
        File pdf = pdfService.generateReceipt(commande, client, details);

        assertTrue(pdf != null && pdf.exists(),
                "PDF should have been generated at: " + (pdf != null ? pdf.getAbsolutePath() : "null"));
        System.out.println("✅ PDF généré : " + pdf.getAbsolutePath());

        // 2. Send email with PDF attachment
        EmailService emailService = EmailService.getInstance();
        boolean sent = emailService.sendPaymentConfirmation(client, commande, pdf);

        assertTrue(sent, "Email should be sent successfully to " + TEST_EMAIL);
        System.out.println("✅ Email envoyé à : " + TEST_EMAIL);
    }
}
