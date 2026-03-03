package Firma.tools.GestionTechnicien;

public class GoogleMapsConfig {

    private static String apiKey = null;

    public static String getApiKey() {
        if (apiKey == null) {
            apiKey = ConfigLoader.get("google.maps.api.key", "");
        }
        return apiKey;
    }

    public static void setApiKey(String key) {
        apiKey = key;
    }
}
