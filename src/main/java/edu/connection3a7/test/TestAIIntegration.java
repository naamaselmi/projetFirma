package edu.connection3a7.test;

import edu.connection3a7.entities.Type;
import edu.connection3a7.tools.AIConfig;
import edu.connection3a7.tools.AIImageService;
import java.io.File;

/**
 * Test d'intégration de l'IA
 */
public class TestAIIntegration {
    
    public static void main(String[] args) {
        System.out.println("=== Test d'intégration de l'IA ===");
        
        // Test de la configuration
        System.out.println("\n1. Test de configuration :");
        System.out.println("API configurée : " + AIConfig.isConfigured());
        System.out.println("Modèle : " + AIConfig.getModel());
        System.out.println("URL API : " + AIConfig.getApiUrl());
        System.out.println("Timeout : " + AIConfig.getTimeoutSeconds() + "s");
        
        // Test de connexion
        System.out.println("\n2. Test de connexion à l'API :");
        AIImageService aiService = new AIImageService();
        boolean connected = aiService.testConnection();
        System.out.println("Connexion API : " + (connected ? "✓ OK" : "✗ ÉCHEC"));
        
        if (!connected) {
            System.out.println("\n⚠️  IMPORTANT : L'API Picsum Photos n'est pas accessible.");
            System.out.println("Vérifiez votre connexion Internet et réessayez.");
            return;
        }
        
        // Test de génération d'image
        System.out.println("\n3. Test de génération d'image :");
        try {
            System.out.println("Génération d'une image de test...");
            
            File image = aiService.generateEventImage(
                "Conférence sur l'IA",
                "Une conférence passionnante sur les dernières avancées en intelligence artificielle",
                Type.CONFERENCE,
                "Paris, France",
                "FIRMA Events"
            );
            
            System.out.println("✓ Image générée avec succès !");
            System.out.println("Emplacement : " + image.getAbsolutePath());
            System.out.println("Taille : " + image.length() + " octets");
            System.out.println("Existe : " + image.exists());
            
        } catch (Exception e) {
            System.out.println("✗ Erreur lors de la génération : " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test terminé ===");
    }
}