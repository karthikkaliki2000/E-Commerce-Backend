package com.act.ecommerce.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
public class OllamaController {

    private static final Logger logger = LoggerFactory.getLogger(OllamaController.class);
    private final ChatClient chatClient;

    @Autowired
    private CacheManager cacheManager;

    public OllamaController(OllamaChatModel ollamaChatModel) {
        this.chatClient = ChatClient.create(ollamaChatModel);
    }

    @PostMapping("/ask")
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) payload.get("history"); // Optional

        logger.info("Received message: {}", message);

        // Check cache
        String cached = cacheManager.getCache("chat") != null
                ? cacheManager.getCache("chat").get(message, String.class)
                : null;
        if (cached != null) {
            logger.info("Returning cached response for message: {}", message);
            return ResponseEntity.ok(cached);
        }

        try {
            // Optionally: Use history to build a more contextual prompt
            String prompt = message;
            if (history != null && !history.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String h : history) {
                    sb.append(h).append("\n");
                }
                sb.append(message);
                prompt = sb.toString();
            }

            String reply = chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();

            // Cache the response
            if (cacheManager.getCache("chat") != null) {
                cacheManager.getCache("chat").put(message, reply.trim());
            }

            return ResponseEntity.ok(reply.trim());
        } catch (Exception e) {
            logger.error("Error during AI interaction", e);
            return ResponseEntity.status(500).body("Oops! Something went wrong.");
        }
    }
}