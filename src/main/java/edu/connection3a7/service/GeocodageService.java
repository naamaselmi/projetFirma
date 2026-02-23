package edu.connection3a7.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.connection3a7.entities.Coordonnees;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public class GeocodageService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeocodageService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Optional<Coordonnees> geocoderAdresse(String adresse) {
        if (adresse == null || adresse.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            String url = NOMINATIM_URL + URLEncoder.encode(adresse.trim(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("User-Agent", "projetFirma/1.0 (desktop app)")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("⚠️ Géocodage impossible (HTTP " + response.statusCode() + ")");
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.isArray() || root.isEmpty()) {
                return Optional.empty();
            }

            JsonNode premier = root.get(0);
            double latitude = premier.path("lat").asDouble(Double.NaN);
            double longitude = premier.path("lon").asDouble(Double.NaN);
            String adresseNormalisee = premier.path("display_name").asText(adresse.trim());

            if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
                return Optional.empty();
            }

            return Optional.of(new Coordonnees(latitude, longitude, adresseNormalisee));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.out.println("⚠️ Erreur géocodage: " + e.getMessage());
            return Optional.empty();
        }
    }
}