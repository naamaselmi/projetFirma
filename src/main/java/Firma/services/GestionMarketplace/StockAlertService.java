package Firma.services.GestionMarketplace;

import Firma.entities.GestionMarketplace.Equipement;
import Firma.entities.GestionMarketplace.Fournisseur;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des alertes de stock
 * Vérifie automatiquement les niveaux de stock et envoie des notifications
 */
public class StockAlertService {

    private static StockAlertService instance;
    
    private final EquipementService equipementService;
    private final FournisseurService fournisseurService;
    private final EmailService emailService;
    
    // Email de l'administrateur
    private static final String ADMIN_EMAIL = "hamza.slimani@esprit.tn";
    
    private StockAlertService() {
        this.equipementService = new EquipementService();
        this.fournisseurService = new FournisseurService();
        this.emailService = EmailService.getInstance();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized StockAlertService getInstance() {
        if (instance == null) {
            instance = new StockAlertService();
        }
        return instance;
    }
    
    /**
     * Vérifie tous les équipements et envoie des alertes pour ceux en rupture de stock
     * @return Nombre d'alertes envoyées
     */
    public int checkAndSendAlerts() {
        int alertsSent = 0;
        
        try {
            List<Equipement> lowStockEquipements = equipementService.getLowStockEquipements();
            
            if (lowStockEquipements.isEmpty()) {
                System.out.println("[StockAlert] Aucun équipement en rupture de stock");
                return 0;
            }
            
            System.out.println("[StockAlert] " + lowStockEquipements.size() + " équipement(s) en alerte");
            
            // Envoyer une alerte pour chaque équipement
            for (Equipement equipement : lowStockEquipements) {
                boolean sent = sendStockAlert(equipement);
                if (sent) {
                    alertsSent++;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[StockAlert] Erreur lors de la vérification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return alertsSent;
    }
    
    /**
     * Vérifie un équipement spécifique et envoie une alerte si nécessaire
     * @param equipement L'équipement à vérifier
     * @return true si une alerte a été envoyée
     */
    public boolean checkEquipementAndAlert(Equipement equipement) {
        if (equipement == null) return false;
        
        // Vérifier si le stock est en dessous du seuil d'alerte
        if (equipement.getQuantiteStock() >= equipement.getSeuilAlerte()) {
            return false; // Pas d'alerte nécessaire
        }
        
        try {
            return sendStockAlert(equipement);
            
        } catch (Exception e) {
            System.err.println("[StockAlert] Erreur lors de l'envoi d'alerte: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envoie un email d'alerte de stock à l'administrateur
     */
    private boolean sendStockAlert(Equipement equipement) {
        try {
            // Récupérer les informations du fournisseur
            Fournisseur fournisseur = getFournisseurById(equipement.getFournisseurId());
            
            // Construire et envoyer l'email
            String subject = "🚨 ALERTE STOCK - " + equipement.getNom();
            String htmlContent = buildStockAlertEmail(equipement, fournisseur);
            
            return emailService.sendStockAlert(ADMIN_EMAIL, subject, htmlContent);
            
        } catch (Exception e) {
            System.err.println("[StockAlert] Erreur lors de l'envoi d'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Construit le contenu HTML de l'email d'alerte
     */
    private String buildStockAlertEmail(Equipement equipement, Fournisseur fournisseur) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("</head><body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>");
        
        // Container principal
        html.append("<div style='max-width:650px;margin:30px auto;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);'>");
        
        // Header avec alerte rouge
        html.append("<div style='background:linear-gradient(135deg,#ef5350 0%,#e53935 100%);padding:30px 25px;text-align:center;'>");
        html.append("<div style='font-size:50px;margin-bottom:10px;'>🚨</div>");
        html.append("<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:bold;'>ALERTE STOCK CRITIQUE</h1>");
        html.append("<p style='color:#ef5350;margin:8px 0 0 0;font-size:14px;'>Intervention requise immédiatement</p>");
        html.append("</div>");
        
        // Bande d'urgence
        html.append("<div style='background-color:#ffebee;padding:15px 25px;border-left:5px solid #ef5350;'>");
        html.append("<p style='margin:0;color:#c62828;font-size:15px;font-weight:bold;'>");
        html.append("⚠️ Le produit suivant est en rupture de stock ou en dessous du seuil d'alerte");
        html.append("</p></div>");
        
        // Section produit
        html.append("<div style='padding:30px 25px;'>");
        
        // Titre produit
        html.append("<div style='background-color:#f8f9fa;padding:20px;border-radius:8px;border-left:4px solid #49ad32;margin-bottom:25px;'>");
        html.append("<h2 style='margin:0 0 8px 0;color:#333;font-size:22px;'>");
        html.append(equipement.getNom());
        html.append("</h2>");
        if (equipement.getDescription() != null && !equipement.getDescription().isEmpty()) {
            html.append("<p style='margin:0;color:#666;font-size:14px;line-height:1.5;'>");
            html.append(equipement.getDescription());
            html.append("</p>");
        }
        html.append("</div>");
        
        // Informations du produit - Grid
        html.append("<div style='background-color:#fff;border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;margin-bottom:25px;'>");
        
        // Header du tableau
        html.append("<div style='background-color:#49ad32;padding:12px 20px;'>");
        html.append("<h3 style='margin:0;color:#ffffff;font-size:16px;font-weight:bold;'>📦 INFORMATIONS PRODUIT</h3>");
        html.append("</div>");
        
        // Lignes d'information
        addInfoRow(html, "ID Produit", "#" + equipement.getId(), false);
        addInfoRow(html, "Stock actuel", 
            "<span style='color:#ef5350;font-weight:bold;font-size:18px;'>" + 
            equipement.getQuantiteStock() + " unités</span>", true);
        addInfoRow(html, "Seuil d'alerte", equipement.getSeuilAlerte() + " unités", false);
        addInfoRow(html, "Prix d'achat", equipement.getPrixAchat() + " DT", true);
        addInfoRow(html, "Prix de vente", equipement.getPrixVente() + " DT", false);
        addInfoRow(html, "Statut", 
            equipement.isDisponible() ? 
            "<span style='color:#4caf50;'>✓ Disponible</span>" : 
            "<span style='color:#ef5350;'>✗ Indisponible</span>", true);
        
        html.append("</div>");
        
        // Informations fournisseur
        if (fournisseur != null) {
            html.append("<div style='background-color:#fff;border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;margin-bottom:25px;'>");
            
            // Header
            html.append("<div style='background-color:#2196f3;padding:12px 20px;'>");
            html.append("<h3 style='margin:0;color:#ffffff;font-size:16px;font-weight:bold;'>🏢 FOURNISSEUR</h3>");
            html.append("</div>");
            
            // Info fournisseur
            addInfoRow(html, "Entreprise", fournisseur.getNomEntreprise(), false);
            if (fournisseur.getContactNom() != null && !fournisseur.getContactNom().isEmpty()) {
                addInfoRow(html, "Contact", fournisseur.getContactNom(), true);
            }
            if (fournisseur.getTelephone() != null && !fournisseur.getTelephone().isEmpty()) {
                addInfoRow(html, "Téléphone", 
                    "<a href='tel:" + fournisseur.getTelephone() + "' style='color:#2196f3;text-decoration:none;'>" + 
                    fournisseur.getTelephone() + "</a>", false);
            }
            if (fournisseur.getEmail() != null && !fournisseur.getEmail().isEmpty()) {
                addInfoRow(html, "Email", 
                    "<a href='mailto:" + fournisseur.getEmail() + "' style='color:#2196f3;text-decoration:none;'>" + 
                    fournisseur.getEmail() + "</a>", true);
            }
            if (fournisseur.getVille() != null && !fournisseur.getVille().isEmpty()) {
                addInfoRow(html, "Ville", fournisseur.getVille(), false);
            }
            
            html.append("</div>");
        }
        
        // Actions recommandées
        html.append("<div style='background-color:#fff3e0;padding:20px;border-radius:8px;border-left:4px solid #ffa726;margin-bottom:25px;'>");
        html.append("<h3 style='margin:0 0 12px 0;color:#e65100;font-size:16px;font-weight:bold;'>💡 ACTIONS RECOMMANDÉES</h3>");
        html.append("<ul style='margin:0;padding-left:20px;color:#666;line-height:1.8;'>");
        html.append("<li>Contacter immédiatement le fournisseur pour passer une commande</li>");
        html.append("<li>Vérifier les délais de livraison et la disponibilité</li>");
        html.append("<li>Mettre à jour le statut du produit si nécessaire</li>");
        html.append("<li>Informer l'équipe commerciale de la situation</li>");
        html.append("</ul>");
        html.append("</div>");
        
        // Call to action
        html.append("<div style='text-align:center;margin:30px 0;'>");
        html.append("<a href='#' style='display:inline-block;background-color:#49ad32;color:#ffffff;");
        html.append("padding:14px 32px;text-decoration:none;border-radius:6px;font-weight:bold;");
        html.append("font-size:15px;box-shadow:0 3px 8px rgba(73,173,50,0.3);'>Gérer le stock</a>");
        html.append("</div>");
        
        html.append("</div>"); // Fin padding principal
        
        // Footer
        html.append("<div style='background-color:#f8f9fa;padding:20px 25px;border-top:1px solid #e0e0e0;text-align:center;'>");
        html.append("<p style='margin:0 0 8px 0;color:#666;font-size:13px;'>");
        html.append("Cet email a été généré automatiquement par le système FIRMA");
        html.append("</p>");
        html.append("<p style='margin:0;color:#999;font-size:12px;'>");
        html.append("🌾 FIRMA Marketplace - Votre partenaire agricole de confiance");
        html.append("</p>");
        html.append("<p style='margin:8px 0 0 0;color:#999;font-size:11px;'>");
        html.append("contact@firma.tn • Tunis, Tunisie");
        html.append("</p>");
        html.append("</div>");
        
        html.append("</div>"); // Fin container
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Ajoute une ligne d'information au tableau
     */
    private void addInfoRow(StringBuilder html, String label, String value, boolean alternate) {
        String bgColor = alternate ? "#f8f9fa" : "#ffffff";
        html.append("<div style='display:flex;padding:12px 20px;border-bottom:1px solid #e0e0e0;background-color:")
            .append(bgColor).append(";'>");
        html.append("<div style='flex:0 0 140px;font-weight:bold;color:#555;font-size:14px;'>")
            .append(label).append(":</div>");
        html.append("<div style='flex:1;color:#333;font-size:14px;'>")
            .append(value).append("</div>");
        html.append("</div>");
    }
    
    /**
     * Récupère un fournisseur par son ID
     */
    private Fournisseur getFournisseurById(int fournisseurId) {
        try {
            List<Fournisseur> fournisseurs = fournisseurService.getEntities();
            for (Fournisseur f : fournisseurs) {
                if (f.getId() == fournisseurId) {
                    return f;
                }
            }
        } catch (SQLException e) {
            System.err.println("[StockAlert] Erreur lors de la récupération du fournisseur: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Récupère la liste des équipements en alerte
     */
    public List<Equipement> getLowStockEquipements() {
        try {
            return equipementService.getLowStockEquipements();
        } catch (SQLException e) {
            System.err.println("[StockAlert] Erreur lors de la récupération des équipements: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
