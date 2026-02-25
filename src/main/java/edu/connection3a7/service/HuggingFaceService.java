package edu.connection3a7.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.connection3a7.tools.ConfigLoader;
import okhttp3.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HuggingFaceService {

    private static final String API_URL = "https://api-inference.huggingface.co/models/";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiToken;

    public HuggingFaceService() {
        // ✅ Charge le token depuis ConfigLoader
        this.apiToken = ConfigLoader.get("huggingface.api.token");

        if (apiToken == null || apiToken.isEmpty() || apiToken.equals("hf_votre_token_ici")) {
            System.err.println("⚠️ Token HuggingFace non configuré dans config.properties");
            System.err.println("📝 Ajoutez: huggingface.api.token=hf_votre_token");
        } else {
            System.out.println("✅ Token HuggingFace chargé");
        }

        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String query(String modelName, String prompt) throws IOException {
        if (apiToken == null || apiToken.isEmpty() || apiToken.equals("hf_votre_token_ici")) {
            throw new IOException("Token HuggingFace non configuré. Ajoutez-le dans config.properties");
        }

        String url = API_URL + modelName;

        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", prompt);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiToken)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API: " + response.code());
            }
            return response.body().string();
        }
    }
}