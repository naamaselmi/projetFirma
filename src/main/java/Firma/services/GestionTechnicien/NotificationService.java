package Firma.services.GestionTechnicien;

import Firma.entities.GestionTechnicien.Demande;
import Firma.entities.GestionTechnicien.Technicien;

public class NotificationService {

    private EmailServiceTechnicien emailService;

    public NotificationService() {
        this.emailService = new EmailServiceTechnicien();
    }

    /**
     * Email de confirmation au client
     */
    public void envoyerConfirmationClient(Demande demande, String emailClient) {
        String sujet = "✅ Confirmation demande #" + demande.getIdDemande();

        String contenu =
                "Bonjour,\n\n" +
                        "Votre demande d'intervention a été enregistrée avec succès.\n\n" +
                        "Détails de votre demande :\n" +
                        "- Type : " + demande.getTypeProbleme() + "\n" +
                        "- Description : " + demande.getDescription() + "\n" +
                        "- Date : " + demande.getDateDemande() + "\n" +
                        "- Statut : " + demande.getStatut() + "\n\n" +
                        "Vous recevrez une notification dès qu'un technicien vous contactera.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FIRMA";

        emailService.envoyerEmail(emailClient, sujet, contenu);
    }

    /**
     * Email au technicien quand une demande lui est assignée
     */
    public void envoyerNotificationTechnicien(Technicien tech, Demande demande) {
        String sujet = "🔔 Nouvelle intervention assignée";

        String contenu =
                "Bonjour " + tech.getPrenom() + " " + tech.getNom() + ",\n\n" +
                        "Une nouvelle intervention vous a été assignée.\n\n" +
                        "Détails de l'intervention :\n" +
                        "- Demande # : " + demande.getIdDemande() + "\n" +
                        "- Type : " + demande.getTypeProbleme() + "\n" +
                        "- Description : " + demande.getDescription() + "\n" +
                        "- Date : " + demande.getDateDemande() + "\n\n" +
                        "Connectez-vous à votre espace pour plus de détails.\n\n" +
                        "Bonne intervention !\n" +
                        "L'équipe FIRMA";

        emailService.envoyerEmail(tech.getEmail(), sujet, contenu);
    }
}