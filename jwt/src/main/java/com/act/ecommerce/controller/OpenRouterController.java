package com.act.ecommerce.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;

@RestController
@RequestMapping("/api/openrouter")
public class OpenRouterController {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouterController.class);

    private final OkHttpClient client = createUnsafeClient();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @GetMapping("/ask")
    public ResponseEntity<String> getAnswer(@RequestParam(name = "message") String message) {
        logger.info("Received request: {}", message);

        String apiUrl = "https://openrouter.ai/api/v1/chat/completions";
        String model = "mistralai/mistral-7b-instruct";


        // Format request body as per OpenRouter spec
        String payload = new org.json.JSONObject()
                .put("model", model)
                .put("messages", new org.json.JSONArray()
                        .put(new org.json.JSONObject()
                                .put("role", "user")
                                .put("content", message)))
                .toString();

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "localhost:9090//")
                .addHeader("X-Title", "E-Commerce")
                .post(RequestBody.create(payload, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Request failed with status: {}", response.code());
                return ResponseEntity
                        .status(response.code())
                        .body("{\"error\": \"Failed to get response from OpenRouter API.\"}");
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String content = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            logger.info("Response from model: {}", content);
            return ResponseEntity.ok(content);

        } catch (IOException e) {
            logger.error("Exception occurred while calling OpenRouter API", e);
            return ResponseEntity
                    .status(500)
                    .body("{\"error\": \"Internal Server Error.\"}");
        }
    }


    private OkHttpClient createUnsafeClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
