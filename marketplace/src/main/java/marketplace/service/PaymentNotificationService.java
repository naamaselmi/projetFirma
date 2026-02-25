package marketplace.service;

import java.io.File;
import java.util.List;

import marketplace.entities.Commande;
import marketplace.entities.DetailCommande;
import marketplace.entities.Utilisateur;

/**
 * Service orchestrateur pour les notifications de paiement
 * Génère le PDF et envoie l'email automatiquement
 */
public class PaymentNotificationService {

    private static PaymentNotificationService instance;

    private final PdfReceiptService pdfService;
    private final EmailService emailService;
    private final DetailCommandeService detailService;
    private final UtilisateurService userService;

    private PaymentNotificationService() {
        this.pdfService = new PdfReceiptService();
        this.emailService = EmailService.getInstance();
        this.detailService = new DetailCommandeService();
        this.userService = new UtilisateurService();
    }

    public static synchronized PaymentNotificationService getInstance() {
        if (instance == null) {
            instance = new PaymentNotificationService();
        }
        return instance;
    }

    /**
     * Traite la notification complète après un paiement réussi
     * 
     * @param commande La commande payée
     * @return true si tout s'est bien passé, false sinon
     */
    public boolean processPaymentNotification(Commande commande) {
        try {
            System.out.println("🔔 Traitement de la notification de paiement...");

            // 1. Récupérer les informations du client
            Utilisateur client = userService.getById(commande.getUtilisateurId());
            if (client == null) {
                System.err.println("❌ Client introuvable: " + commande.getUtilisateurId());
                return false;
            }

            // 2. Récupérer les détails de la commande
            List<DetailCommande> details = detailService.getDetailsByCommande(
                    commande.getId());
            if (details.isEmpty()) {
                System.err.println("⚠️ Aucun détail de commande trouvé pour la commande " + commande.getId()
                        + " - envoi email sans détails");
            }

            // 3. Générer le PDF de reçu
            System.out.println("📄 Génération du reçu PDF...");
            File pdfReceipt = pdfService.generateReceipt(commande, client, details);
            System.out.println("✓ PDF généré: " + pdfReceipt.getAbsolutePath());

            // 4. Envoyer l'email avec le PDF
            System.out.println("📧 Envoi de l'email à: " + client.getEmail());
            boolean emailSent = emailService.sendPaymentConfirmation(
                    client, commande, pdfReceipt);

            if (emailSent) {
                System.out.println("✓ Email envoyé avec succès!");
                return true;
            } else {
                System.err.println("❌ Échec de l'envoi de l'email");
                // Le PDF est quand même généré et sauvegardé
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère uniquement le PDF sans envoyer d'email
     * Utile pour les tests ou si l'email est désactivé
     */
    public File generateReceiptOnly(Commande commande) {
        try {
            Utilisateur client = userService.getById(commande.getUtilisateurId());
            List<DetailCommande> details = detailService.getDetailsByCommande(
                    commande.getId());

            return pdfService.generateReceipt(commande, client, details);

        } catch (Exception e) {
            System.err.println("Erreur génération PDF: " + e.getMessage());
            return null;
        }
    }

    /**
     * Vérifie si le service de notification est prêt
     */
    public boolean isReady() {
        return emailService.isConfigured();
    }

    /**
     * Envoie un email de test
     */
    public boolean sendTestNotification(String testEmail) {
        return emailService.sendTestEmail(testEmail);
    }
}
