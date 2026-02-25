package edu.connection3a7.tools;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("❌ Fichier config.properties introuvable!");
            } else {
                properties.load(input);
                System.out.println("✅ Configuration chargée depuis config.properties");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }
}