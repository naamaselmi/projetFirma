package edu.connection3a7.service;

import edu.connection3a7.tools.EmailConfig;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private static final String EMAIL_EXPEDITEUR = "firma@support.com";
    private static final String NOM_EXPEDITEUR = "FIRMA Support";

    public boolean envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            Session session = EmailConfig.getSession();
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR, NOM_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            Transport.send(message);
            System.out.println("✅ Email envoyé à: " + destinataire);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        }
    }
}