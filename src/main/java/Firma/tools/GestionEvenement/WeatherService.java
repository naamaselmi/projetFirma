package Firma.tools.GestionEvenement;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service météo utilisant l'API Open-Meteo (100% gratuit, aucune clé API).
 * https://open-meteo.com/
 *
 * Fonctionnalités :
 *  - Géocodage automatique (nom de lieu → coordonnées GPS)
 *  - Prévision jusqu'à 16 jours
 *  - Météo actuelle pour les événements passés ou lointains
 *  - Cache en mémoire pour éviter les appels redondants
 *  - Appels asynchrones pour ne pas bloquer l'UI
 *  - Parsing JSON léger (sans bibliothèque externe)
 */
public class WeatherService {

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    private static WeatherService instance;
    private final HttpClient httpClient;

    // Cache : clé = "lieu|date" → résultat météo
    private final ConcurrentHashMap<String, WeatherResult> cache = new ConcurrentHashMap<>();
    // Cache geocoding : lieu → "lat,lon"
    private final ConcurrentHashMap<String, String> geoCache = new ConcurrentHashMap<>();

    private WeatherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public static synchronized WeatherService getInstance() {
        if (instance == null) {
            instance = new WeatherService();
        }
        return instance;
    }

    /**
     * Open-Meteo est toujours disponible (pas de clé API).
     */
    public boolean isConfigured() {
        return true;
    }

    /**
     * Récupère la météo pour un lieu et une date donnée (asynchrone).
     *
     * @param lieu Le lieu (ville, adresse, nom d'un endroit)
     * @param date La date de l'événement
     * @return CompletableFuture contenant le résultat météo
     */
    public CompletableFuture<WeatherResult> getMeteo(String lieu, LocalDate date) {
        return getMeteo(lieu, null, date);
    }

    /**
     * Récupère la météo pour un lieu, une adresse et une date donnée (asynchrone).
     * Tente d'abord le lieu, puis extrait la ville de l'adresse en fallback.
     *
     * @param lieu    Le nom du lieu (ex: "Palais des Congrès")
     * @param adresse L'adresse complète (ex: "1 Place de la Porte Maillot, 75017 Paris")
     * @param date    La date de l'événement
     * @return CompletableFuture contenant le résultat météo
     */
    public CompletableFuture<WeatherResult> getMeteo(String lieu, String adresse, LocalDate date) {
        String effectifLieu = (lieu != null && !lieu.isBlank()) ? lieu.trim() : null;
        String effectifAdresse = (adresse != null && !adresse.isBlank()) ? adresse.trim() : null;

        if (effectifLieu == null && effectifAdresse == null) {
            return CompletableFuture.completedFuture(null);
        }

        String cacheKey = (effectifLieu != null ? effectifLieu.toLowerCase() : "") + "|" + date;
        WeatherResult cached = cache.get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Construire la liste des termes à essayer pour le géocodage
        // 1) lieu original, 2) ville extraite de l'adresse, 3) adresse brute
        java.util.List<String> candidates = new java.util.ArrayList<>();
        if (effectifLieu != null) candidates.add(effectifLieu);
        if (effectifAdresse != null) {
            String ville = extraireVilleDeAdresse(effectifAdresse);
            if (ville != null && !ville.equalsIgnoreCase(effectifLieu)) {
                candidates.add(ville);
            }
        }

        // Essayer chaque candidat séquentiellement
        return tryGeocode(candidates, 0)
                .thenCompose(coords -> {
                    if (coords == null) {
                        System.err.println("Géocodage échoué pour : " + candidates);
                        return CompletableFuture.completedFuture((WeatherResult) null);
                    }
                    return fetchMeteo(coords, date);
                })
                .thenApply(result -> {
                    if (result != null) {
                        cache.put(cacheKey, result);
                    }
                    return result;
                })
                .exceptionally(ex -> {
                    System.err.println("Erreur météo : " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Essaie de géocoder chaque candidat de la liste, retourne les coords du premier qui fonctionne.
     */
    private CompletableFuture<double[]> tryGeocode(java.util.List<String> candidates, int index) {
        if (index >= candidates.size()) {
            return CompletableFuture.completedFuture(null);
        }
        return geocoder(candidates.get(index))
                .thenCompose(coords -> {
                    if (coords != null) return CompletableFuture.completedFuture(coords);
                    return tryGeocode(candidates, index + 1);
                });
    }

    /**
     * Extrait le nom de la ville d'une adresse française.
     * Ex: "1 Place de la Porte Maillot, 75017 Paris" → "Paris"
     * Ex: "20 Rue Jean Jaurès, 92800 Puteaux" → "Puteaux"
     */
    private String extraireVilleDeAdresse(String adresse) {
        if (adresse == null) return null;
        // Pattern typique : ..., XXXXX Ville  ou  ..., Ville
        // Chercher après la dernière virgule
        int lastComma = adresse.lastIndexOf(',');
        String segment = (lastComma >= 0) ? adresse.substring(lastComma + 1).trim() : adresse.trim();

        // Supprimer le code postal (5 chiffres) s'il est présent
        segment = segment.replaceAll("^\\d{4,5}\\s+", "").trim();

        if (!segment.isEmpty()) {
            return segment;
        }
        return null;
    }

    // ================================================================
    //  Géocodage (nom de lieu → coordonnées)
    // ================================================================

    private CompletableFuture<double[]> geocoder(String lieu) {
        String key = lieu.toLowerCase().trim();
        String cachedCoords = geoCache.get(key);
        if (cachedCoords != null) {
            String[] parts = cachedCoords.split(",");
            return CompletableFuture.completedFuture(
                    new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])});
        }

        String url = GEOCODING_URL + "?name=" + encode(lieu) + "&count=1&language=fr";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("Geocoding erreur " + response.statusCode());
                        return null;
                    }
                    String json = response.body();
                    // Chercher "latitude" et "longitude" dans le premier résultat
                    double lat = extractDoubleValue(json, 0, "\"latitude\"");
                    double lon = extractDoubleValue(json, 0, "\"longitude\"");

                    if (lat == 0 && lon == 0) {
                        System.err.println("Lieu non trouvé pour le géocodage : " + lieu);
                        return null;
                    }

                    geoCache.put(key, lat + "," + lon);
                    return new double[]{lat, lon};
                });
    }

    // ================================================================
    //  Appel météo Open-Meteo
    // ================================================================

    private CompletableFuture<WeatherResult> fetchMeteo(double[] coords, LocalDate date) {
        double lat = coords[0];
        double lon = coords[1];
        long joursJusquA = ChronoUnit.DAYS.between(LocalDate.now(), date);

        // Open-Meteo supporte les prévisions jusqu'à 16 jours
        // On demande la date cible ou la météo actuelle si hors portée
        String url;
        boolean useForecast;

        if (joursJusquA >= 0 && joursJusquA <= 16) {
            url = FORECAST_URL + "?latitude=" + lat + "&longitude=" + lon
                    + "&daily=temperature_2m_max,temperature_2m_min,weathercode"
                    + "&current_weather=true"
                    + "&timezone=auto"
                    + "&start_date=" + date + "&end_date=" + date;
            useForecast = true;
        } else {
            // Hors portée des prévisions : utiliser la météo actuelle comme indication
            url = FORECAST_URL + "?latitude=" + lat + "&longitude=" + lon
                    + "&current_weather=true&timezone=auto";
            useForecast = false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        final boolean forecast = useForecast;
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("Open-Meteo erreur " + response.statusCode() + ": " + response.body());
                        return null;
                    }
                    return parseOpenMeteo(response.body(), forecast, joursJusquA < 0);
                });
    }

    // ================================================================
    //  Parsing de la réponse Open-Meteo
    // ================================================================

    private WeatherResult parseOpenMeteo(String json, boolean hasDailyForecast, boolean isPast) {
        try {
            WeatherResult result = new WeatherResult();

            if (hasDailyForecast) {
                // Extraire la section "daily":{...} (pas "daily_units"!)
                String dailySection = extractJsonSection(json, "daily");
                if (dailySection == null) {
                    System.err.println("Open-Meteo : section 'daily' introuvable");
                    return null;
                }

                double tempMax = extractDoubleFromArray(dailySection, "\"temperature_2m_max\"");
                double tempMin = extractDoubleFromArray(dailySection, "\"temperature_2m_min\"");
                int weatherCode = (int) extractDoubleFromArray(dailySection, "\"weathercode\"");

                result.tempMax = Math.round(tempMax);
                result.tempMin = Math.round(tempMin);
                result.temperature = Math.round((tempMax + tempMin) / 2.0);
                result.weatherCode = weatherCode;
                result.description = weatherCodeToDescription(weatherCode);
                result.isForecast = true;
            } else {
                // Extraire la section "current_weather":{...} (pas "current_weather_units"!)
                String cwSection = extractJsonSection(json, "current_weather");
                if (cwSection == null) {
                    System.err.println("Open-Meteo : section 'current_weather' introuvable");
                    return null;
                }

                double temp = extractDoubleValue(cwSection, 0, "\"temperature\"");
                int weatherCode = (int) extractDoubleValue(cwSection, 0, "\"weathercode\"");

                result.temperature = Math.round(temp);
                result.tempMin = result.temperature;
                result.tempMax = result.temperature;
                result.weatherCode = weatherCode;
                result.description = weatherCodeToDescription(weatherCode);
                result.isForecast = false;
            }

            result.isPast = isPast;
            return result;
        } catch (Exception e) {
            System.err.println("Erreur parsing Open-Meteo : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrait une section JSON par clé exacte : "key":{...}
     * Gère les clés ambiguës (ex: "daily" vs "daily_units", "current_weather" vs "current_weather_units").
     * Retourne la sous-chaîne entre accolades { ... } incluses.
     */
    private String extractJsonSection(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = 0;
        while (idx < json.length()) {
            idx = json.indexOf(pattern, idx);
            if (idx < 0) return null;

            int afterKey = idx + pattern.length();
            // Sauter les espaces puis vérifier qu'on a ':'
            int pos = afterKey;
            while (pos < json.length() && json.charAt(pos) == ' ') pos++;
            if (pos >= json.length() || json.charAt(pos) != ':') {
                idx = afterKey;
                continue;
            }
            pos++; // Sauter le ':'
            while (pos < json.length() && json.charAt(pos) == ' ') pos++;

            if (pos < json.length() && json.charAt(pos) == '{') {
                // Trouver l'accolade fermante correspondante
                int braceCount = 1;
                int start = pos;
                int end = pos + 1;
                while (end < json.length() && braceCount > 0) {
                    char c = json.charAt(end);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                    end++;
                }
                return json.substring(start, end);
            }
            idx = afterKey;
        }
        return null;
    }

    /**
     * Extrait la première valeur numérique d'un tableau JSON associé à une clé.
     * Ex: "temperature_2m_max":[15.3] → 15.3
     */
    private double extractDoubleFromArray(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return 0;
        int bracketStart = json.indexOf("[", idx);
        if (bracketStart < 0) return 0;

        int start = bracketStart + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }

        if (end > start) {
            try {
                return Double.parseDouble(json.substring(start, end));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    // ================================================================
    //  WMO Weather Code → description en français + emoji
    //  https://open-meteo.com/en/docs#weathervariables
    // ================================================================

    private String weatherCodeToDescription(int code) {
        return switch (code) {
            case 0 -> "Ciel dégagé";
            case 1 -> "Principalement dégagé";
            case 2 -> "Partiellement nuageux";
            case 3 -> "Couvert";
            case 45, 48 -> "Brouillard";
            case 51 -> "Bruine légère";
            case 53 -> "Bruine modérée";
            case 55 -> "Bruine dense";
            case 56, 57 -> "Bruine verglaçante";
            case 61 -> "Pluie légère";
            case 63 -> "Pluie modérée";
            case 65 -> "Pluie forte";
            case 66, 67 -> "Pluie verglaçante";
            case 71 -> "Neige légère";
            case 73 -> "Neige modérée";
            case 75 -> "Neige forte";
            case 77 -> "Grains de neige";
            case 80 -> "Averses légères";
            case 81 -> "Averses modérées";
            case 82 -> "Averses violentes";
            case 85, 86 -> "Averses de neige";
            case 95 -> "Orage";
            case 96, 99 -> "Orage avec grêle";
            default -> "Variable";
        };
    }

    // ================================================================
    //  Utilitaires JSON
    // ================================================================

    private double extractDoubleValue(String json, int searchFrom, String key) {
        if (searchFrom < 0) searchFrom = 0;
        int idx = json.indexOf(key, searchFrom);
        if (idx < 0) return 0;
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx < 0) return 0;

        int start = colonIdx + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }

        if (end > start) {
            try {
                return Double.parseDouble(json.substring(start, end));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private String encode(String s) {
        return URLEncoder.encode(s.trim(), StandardCharsets.UTF_8);
    }

    // ================================================================
    //  Résultat météo
    // ================================================================

    /**
     * Données météo retournées par le service.
     */
    public static class WeatherResult {
        public double temperature;    // Température moyenne en °C
        public double tempMin;        // Température minimale
        public double tempMax;        // Température maximale
        public String description;    // Ex: "Ciel dégagé", "Pluie légère"
        public int weatherCode;       // WMO Weather interpretation code
        public boolean isForecast;    // true = prévision, false = météo actuelle
        public boolean isPast;        // true = événement passé

        /**
         * Retourne l'emoji correspondant au code météo WMO.
         */
        public String getEmoji() {
            return switch (weatherCode) {
                case 0 -> "☀️";
                case 1 -> "🌤";
                case 2 -> "⛅";
                case 3 -> "☁️";
                case 45, 48 -> "🌫";
                case 51, 53, 55, 56, 57 -> "🌧";
                case 61, 63, 80, 81 -> "🌦";
                case 65, 82 -> "🌧";
                case 66, 67 -> "🌧❄";
                case 71, 73, 75, 77, 85, 86 -> "🌨";
                case 95, 96, 99 -> "⛈";
                default -> "🌡";
            };
        }

        /**
         * Ligne résumé courte pour affichage dans une carte.
         */
        public String getResumeCourt() {
            String emoji = getEmoji();
            String temp = Math.round(temperature) + "°C";
            return emoji + " " + temp + " · " + (description != null ? description : "");
        }

        /**
         * Indication de fiabilité.
         */
        public String getFiabilite() {
            if (isPast) return "Météo actuelle (événement passé)";
            if (isForecast) return "Prévision fiable";
            return "Météo indicative (> 16 jours)";
        }
    }
}
