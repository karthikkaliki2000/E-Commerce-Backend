package com.act.ecommerce.controller;

import com.act.ecommerce.configuration.OpenRouterConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/openrouter")
public class OpenRouterController {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouterController.class);

    private final OpenRouterConfig config;
    private final OkHttpClient client = createUnsafeClient();

    public OpenRouterController(OpenRouterConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        logger.debug("API key loaded: {}", config.getApiKey() != null ? "yes" : "no");
    }

    @GetMapping("/ask")
    public ResponseEntity<String> getAnswer(@RequestParam(name = "message") String message) {
        logger.info("Received OpenRouter prompt: {}", message);



        String payload = new org.json.JSONObject()
                .put("model", config.getModel()) // e.g., openai/gpt-4o
                .put("messages", new org.json.JSONArray()
                        .put(new org.json.JSONObject()
                                .put("role", "user")
                                .put("content", message)))
                .toString();

        Request request = new Request.Builder()
                .url(config.getBaseUrl()) // e.g., https://openrouter.ai/api/v1/chat/completions
                .addHeader("Authorization", "Bearer " + "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")
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "http://localhost:9090/") // optional
                .addHeader("X-Title", "E-Commerce") // optional
                .post(RequestBody.create(payload, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : null;

            if (!response.isSuccessful()) {
                logger.error("OpenRouter failed: status={} body={}", response.code(), body);
                return ResponseEntity.status(response.code())
                        .body("{\"error\": \"OpenRouter API call failed.\"}");
            }

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String content = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            logger.info("OpenRouter response: {}", content);
            return ResponseEntity.ok(content);

        } catch (IOException e) {
            logger.error("Error during OpenRouter API call", e);
            return ResponseEntity.status(500).body("{\"error\": \"Internal Server Error\"}");
        }
    }

    private OkHttpClient createUnsafeClient() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String s) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String s) {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAll, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAll[0])
                    .hostnameVerifier((host, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create relaxed SSL client", e);
        }
    }
}
