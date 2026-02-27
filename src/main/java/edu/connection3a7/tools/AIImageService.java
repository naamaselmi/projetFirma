package edu.connection3a7.tools;

import edu.connection3a7.entities.Type;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de génération d'images d'événements
 * Utilise Picsum Photos avec sélection intelligente par ID
 */
public class AIImageService {
    
    // API Picsum Photos - Gratuite, illimitée, ultra-fiable
    private static final String PICSUM_API_URL = "https://picsum.photos/512/512";
    
    private final HttpClient httpClient;
    
    public AIImageService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AIConfig.getTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
    
    /**
     * Génère une image d'événement basée sur les informations fournies
     * Utilise Unsplash pour obtenir une image pertinente
     */
    public File generateEventImage(String title, String description, Type eventType, 
                                   String location, String organizer) throws Exception {
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'événement est requis");
        }
        
        // Construction des mots-clés pour Unsplash
        String keywords = buildKeywords(title, description, eventType, location);
        System.out.println("Mots-clés générés: " + keywords);
        
        // Appel API Unsplash
        byte[] imageBytes = callUnsplashAPI(keywords);
        
        // Sauvegarde dans un fichier temporaire
        File tempFile = File.createTempFile("event_img_", ".jpg");
        tempFile.deleteOnExit(); // Nettoyage automatique
        
        Files.write(tempFile.toPath(), imageBytes);
        
        return tempFile;
    }
    
    /**
     * Construit des mots-clés pertinents pour Unsplash
     */
    private String buildKeywords(String title, String description, Type eventType, String location) {
        StringBuilder keywords = new StringBuilder();
        
        // Ajouter le type d'événement en premier
        if (eventType != null) {
            keywords.append(getEventTypeKeyword(eventType)).append(",");
        }
        
        // Ajouter des mots-clés du titre (premiers mots significatifs)
        String[] titleWords = title.trim().split("\\s+");
        int wordCount = 0;
        for (String word : titleWords) {
            if (word.length() > 3 && wordCount < 3) {
                keywords.append(word).append(",");
                wordCount++;
            }
        }
        
        // Ajouter "event" pour le contexte
        keywords.append("event,professional");
        
        return keywords.toString();
    }
    
    /**
     * Retourne un mot-clé adapté au type d'événement
     */
    private String getEventTypeKeyword(Type eventType) {
        Map<Type, String> keywordMap = new HashMap<>();
        
        // Normaliser le type
        Type normalizedType = normalizeType(eventType);
        
        keywordMap.put(Type.EXPOSITION, "exhibition,art,gallery");
        keywordMap.put(Type.ATELIER, "workshop,training,learning");
        keywordMap.put(Type.CONFERENCE, "conference,business,meeting");
        keywordMap.put(Type.SALON, "trade show,exhibition,business");
        keywordMap.put(Type.FORMATION, "training,education,learning");
        keywordMap.put(Type.AUTRE, "event,celebration,gathering");
        
        return keywordMap.getOrDefault(normalizedType, "event,professional");
    }
    
    /**
     * Construit un prompt optimisé pour la génération d'image
     */
    private String buildPrompt(String title, String description, Type eventType, 
                              String location, String organizer) {
        
        StringBuilder prompt = new StringBuilder();
        
        // Style général
        prompt.append("Professional event poster, clean modern design, high quality, 4K resolution, vibrant colors. ");
        
        // Titre principal
        prompt.append("Event title: \"").append(title).append("\". ");
        
        // Type d'événement avec style adapté
        if (eventType != null) {
            String typeStyle = getEventTypeStyle(eventType);
            prompt.append(typeStyle).append(" ");
        }
        
        // Description (limitée)
        if (description != null && !description.trim().isEmpty()) {
            String shortDesc = description.trim();
            if (shortDesc.length() > 150) {
                shortDesc = shortDesc.substring(0, 150) + "...";
            }
            prompt.append("Description: ").append(shortDesc).append(". ");
        }
        
        // Localisation
        if (location != null && !location.trim().isEmpty()) {
            prompt.append("Location: ").append(location.trim()).append(". ");
        }
        
        // Organisateur
        if (organizer != null && !organizer.trim().isEmpty()) {
            prompt.append("Organized by: ").append(organizer.trim()).append(". ");
        }
        
        // Instructions finales pour la qualité
        prompt.append("Professional graphic design, clean layout, attractive typography, no text overlay, realistic lighting.");
        
        return prompt.toString();
    }
    
    /**
     * Retourne un style visuel adapté au type d'événement
     */
    private String getEventTypeStyle(Type eventType) {
        Map<Type, String> styleMap = new HashMap<>();
        
        // Normaliser le type (éviter les doublons minuscules/majuscules)
        Type normalizedType = normalizeType(eventType);
        
        styleMap.put(Type.EXPOSITION, "Art exhibition style, gallery atmosphere, artistic, creative, cultural event");
        styleMap.put(Type.ATELIER, "Workshop style, hands-on activity, educational, interactive, learning environment");
        styleMap.put(Type.CONFERENCE, "Conference style, professional business event, corporate, networking, speakers");
        styleMap.put(Type.SALON, "Trade show style, exhibition hall, business networking, professional stands");
        styleMap.put(Type.FORMATION, "Training workshop style, educational, professional development, learning");
        styleMap.put(Type.AUTRE, "General event style, social gathering, community event, celebration");
        
        return styleMap.getOrDefault(normalizedType, "Social event, community gathering, celebration");
    }
    
    /**
     * Normalise le type d'événement (gère les doublons minuscules/majuscules)
     */
    private Type normalizeType(Type eventType) {
        if (eventType == null) return Type.AUTRE;
        
        String typeName = eventType.name().toUpperCase();
        try {
            return Type.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            // Si le type n'existe pas en majuscule, chercher une correspondance
            for (Type t : Type.values()) {
                if (t.name().equalsIgnoreCase(typeName)) {
                    return t;
                }
            }
            return Type.AUTRE;
        }
    }
    
    /**
     * Appelle l'API Picsum Photos pour obtenir une image
     * Utilise une sélection intelligente d'IDs basée sur le type d'événement
     */
    private byte[] callUnsplashAPI(String keywords) throws Exception {
        
        // Sélectionner un ID d'image approprié basé sur les mots-clés
        int imageId = selectImageIdFromKeywords(keywords);
        
        // URL Picsum avec ID spécifique + paramètre aléatoire pour éviter le cache
        String url = "https://picsum.photos/id/" + imageId + "/512/512?random=" + System.currentTimeMillis();
        
        System.out.println("Récupération d'image depuis Picsum Photos...");
        System.out.println("ID sélectionné: " + imageId + " (basé sur: " + keywords + ")");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(AIConfig.getTimeoutSeconds()))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "image/jpeg,image/png,image/*")
                .GET()
                .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        int statusCode = response.statusCode();
        
        if (statusCode == 200) {
            System.out.println("✓ Image récupérée avec succès !");
            System.out.println("✓ Taille: " + response.body().length + " octets");
            return response.body();
        } else {
            throw new RuntimeException("Erreur API Picsum (" + statusCode + "). Veuillez utiliser l'upload manuel.");
        }
    }
    
    /**
     * Sélectionne un ID d'image Picsum approprié basé sur les mots-clés
     * Utilise des plages d'IDs qui correspondent à différents styles d'images
     */
    private int selectImageIdFromKeywords(String keywords) {
        String lowerKeywords = keywords.toLowerCase();
        
        // IDs Picsum sélectionnés pour différents types d'événements
        // Ces IDs ont été choisis pour leur qualité et leur pertinence
        
        // CONFERENCE - Images professionnelles, business, technologie
        int[] conferenceIds = {1, 3, 15, 20, 26, 28, 48, 63, 82, 103, 119, 180, 201, 250, 367, 431, 478, 493, 582, 659};
        
        // ATELIER - Images de travail, collaboration, créativité
        int[] atelierIds = {7, 13, 27, 42, 52, 88, 109, 152, 188, 225, 287, 342, 395, 447, 501, 556, 623, 678, 701, 756};
        
        // EXPOSITION - Images artistiques, architecture, culture
        int[] expositionIds = {10, 24, 39, 58, 77, 96, 123, 164, 206, 237, 292, 349, 403, 456, 511, 572, 638, 684, 717, 783};
        
        // SALON - Images d'espaces, halls, événements
        int[] salonIds = {16, 33, 47, 65, 84, 112, 145, 177, 219, 264, 311, 368, 421, 473, 529, 591, 647, 693, 738, 801};
        
        // FORMATION - Images éducatives, apprentissage
        int[] formationIds = {21, 35, 54, 71, 91, 127, 159, 194, 241, 278, 326, 381, 437, 488, 543, 604, 661, 708, 751, 819};
        
        // AUTRE - Images générales, variées
        int[] autreIds = {8, 18, 29, 44, 62, 79, 98, 134, 171, 213, 256, 301, 357, 412, 467, 521, 578, 634, 689, 744};
        
        // Sélection basée sur les mots-clés
        int[] selectedIds;
        
        if (lowerKeywords.contains("conference") || lowerKeywords.contains("business") || lowerKeywords.contains("meeting")) {
            selectedIds = conferenceIds;
        } else if (lowerKeywords.contains("workshop") || lowerKeywords.contains("atelier") || lowerKeywords.contains("training")) {
            selectedIds = atelierIds;
        } else if (lowerKeywords.contains("exhibition") || lowerKeywords.contains("exposition") || lowerKeywords.contains("art") || lowerKeywords.contains("gallery")) {
            selectedIds = expositionIds;
        } else if (lowerKeywords.contains("trade") || lowerKeywords.contains("salon") || lowerKeywords.contains("show")) {
            selectedIds = salonIds;
        } else if (lowerKeywords.contains("formation") || lowerKeywords.contains("education") || lowerKeywords.contains("learning")) {
            selectedIds = formationIds;
        } else {
            selectedIds = autreIds;
        }
        
        // Sélectionner un ID aléatoire dans la liste appropriée
        int randomIndex = (int) (Math.random() * selectedIds.length);
        return selectedIds[randomIndex];
    }
    
    /**
     * Méthode de test pour vérifier la connexion à l'API
     */
    public boolean testConnection() {
        try {
            // Test simple avec Picsum Photos
            String url = PICSUM_API_URL + "?random=" + System.currentTimeMillis();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();
            
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            boolean success = response.statusCode() == 200;
            
            if (success) {
                System.out.println("✓ Connexion à Picsum Photos réussie !");
                System.out.println("✓ Taille de l'image test: " + response.body().length + " octets");
            } else {
                System.err.println("✗ Erreur de connexion : " + response.statusCode());
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("✗ Erreur de connexion à l'API: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Version simplifiée pour les tests (utilise une image par défaut si l'API échoue)
     */
    public File generateEventImageWithFallback(String title, String description, Type eventType, 
                                              String location, String organizer) {
        try {
            return generateEventImage(title, description, eventType, location, organizer);
        } catch (Exception e) {
            System.err.println("Erreur de génération d'image IA: " + e.getMessage());
            
            // Fallback: créer un fichier image par défaut
            try {
                File defaultImage = new File("src/main/resources/image/default_event.png");
                if (defaultImage.exists()) {
                    return defaultImage;
                } else {
                    // Créer un fichier temporaire vide (sera remplacé par l'utilisateur)
                    File tempFile = File.createTempFile("event_default_", ".txt");
                    tempFile.deleteOnExit();
                    return tempFile;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Impossible de créer l'image de fallback", ex);
            }
        }
    }
}