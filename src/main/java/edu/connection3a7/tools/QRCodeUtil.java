package edu.connection3a7.tools;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour la génération de QR Codes.
 * Utilisé pour les tickets de participation (JavaFX + PDF).
 */
public class QRCodeUtil {

    private QRCodeUtil() {
        // Classe utilitaire, pas d'instanciation
    }

    /**
     * Génère un QR Code sous forme d'ImageView JavaFX.
     *
     * @param contenu Le texte à encoder dans le QR Code
     * @param taille  La taille en pixels (largeur = hauteur)
     * @return Un ImageView contenant le QR Code
     */
    public static ImageView genererQRCodeImageView(String contenu, int taille) {
        try {
            BitMatrix bitMatrix = creerBitMatrix(contenu, taille);
            WritableImage image = new WritableImage(taille, taille);
            PixelWriter writer = image.getPixelWriter();

            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    writer.setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(taille);
            imageView.setFitHeight(taille);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (WriterException e) {
            System.err.println("Erreur génération QR Code JavaFX : " + e.getMessage());
            return new ImageView(); // Image vide en cas d'erreur
        }
    }

    /**
     * Génère un QR Code sous forme de BufferedImage (pour l'export PDF via iText).
     *
     * @param contenu Le texte à encoder dans le QR Code
     * @param taille  La taille en pixels (largeur = hauteur)
     * @return Un BufferedImage contenant le QR Code
     */
    public static BufferedImage genererQRCodeBufferedImage(String contenu, int taille) {
        try {
            BitMatrix bitMatrix = creerBitMatrix(contenu, taille);
            BufferedImage image = new BufferedImage(taille, taille, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < taille; y++) {
                for (int x = 0; x < taille; x++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            return image;
        } catch (WriterException e) {
            System.err.println("Erreur génération QR Code BufferedImage : " + e.getMessage());
            return new BufferedImage(taille, taille, BufferedImage.TYPE_INT_RGB);
        }
    }

    /**
     * Crée la matrice de bits du QR Code.
     */
    private static BitMatrix creerBitMatrix(String contenu, int taille) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Haute correction d'erreur
        hints.put(EncodeHintType.MARGIN, 1); // Marge minimale

        QRCodeWriter qrWriter = new QRCodeWriter();
        return qrWriter.encode(contenu, BarcodeFormat.QR_CODE, taille, taille, hints);
    }

    /**
     * Construit le contenu encodé dans le QR Code d'un ticket.
     * Si le serveur de tickets est actif, retourne une URL accessible sur mobile.
     * Sinon, retourne un texte formaté.
     *
     * @param codeParticipation Le code unique (ex: PART-AB12C ou PART-AB12C-A1)
     * @param nomComplet        Nom complet du participant
     * @param titreEvenement    Titre de l'événement
     * @return L'URL du ticket ou le texte formaté
     */
    public static String construireContenuTicket(String codeParticipation, String nomComplet, String titreEvenement) {
        // Si le serveur de tickets est actif, encoder une URL
        TicketServerService ticketServer = TicketServerService.getInstance();
        if (ticketServer.isRunning()) {
            return ticketServer.getTicketURL(codeParticipation);
        }

        // Fallback : texte brut
        return "FIRMA-TICKET\n" +
               "Code: " + codeParticipation + "\n" +
               "Participant: " + nomComplet + "\n" +
               "Evenement: " + titreEvenement;
    }
}
