package edu.connection3a7.tools;

import java.io.InputStream;
import java.util.Properties;

public class GoogleMapsConfig {

    private static String API_KEY;

    static {
        try (InputStream input = GoogleMapsConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            Properties prop = new Properties();
            prop.load(input);
            API_KEY = prop.getProperty("");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement clé Google Maps: " + e.getMessage());
            API_KEY = "";
        }
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static boolean isConfigured() {
        return API_KEY != null && !API_KEY.isEmpty()
                && !API_KEY.equals("");
    }
}