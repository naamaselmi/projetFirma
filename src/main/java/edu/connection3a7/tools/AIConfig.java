package edu.connection3a7.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration de l'API d'IA
 */
public class AIConfig {
    
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = AIConfig.class.getClassLoader().getResourceAsStream("ai_config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                // Valeurs par défaut
                properties.setProperty("ai.model", "picsum-smart");
                properties.setProperty("ai.max_retries", "3");
                properties.setProperty("ai.timeout_seconds", "30");
                properties.setProperty("ai.max_image_size", "512");
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la configuration IA: " + e.getMessage());
            // Valeurs par défaut
            properties.setProperty("ai.model", "picsum-smart");
            properties.setProperty("ai.max_retries", "3");
            properties.setProperty("ai.timeout_seconds", "30");
            properties.setProperty("ai.max_image_size", "512");
        }
    }
    
    public static String getApiKey() {
        // Pollinations.AI ne nécessite pas de clé API
        return ""; // Pas de clé nécessaire
    }
    
    public static String getModel() {
        return properties.getProperty("ai.model", "picsum-smart");
    }
    
    public static int getMaxRetries() {
        try {
            return Integer.parseInt(properties.getProperty("ai.max_retries", "3"));
        } catch (NumberFormatException e) {
            return 3;
        }
    }
    
    public static int getTimeoutSeconds() {
        try {
            return Integer.parseInt(properties.getProperty("ai.timeout_seconds", "60"));
        } catch (NumberFormatException e) {
            return 60;
        }
    }
    
    public static int getMaxImageSize() {
        try {
            return Integer.parseInt(properties.getProperty("ai.max_image_size", "1024"));
        } catch (NumberFormatException e) {
            return 1024;
        }
    }
    
    public static String getApiUrl() {
        // Picsum Photos - Gratuite, illimitée, ultra-fiable
        return "https://picsum.photos/512/512";
    }
    
    public static boolean isConfigured() {
        // Pollinations.AI ne nécessite pas de configuration
        return true; // Toujours configuré
    }
}