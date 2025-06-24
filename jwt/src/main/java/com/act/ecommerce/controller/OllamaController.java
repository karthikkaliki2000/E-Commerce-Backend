package com.act.ecommerce.controller;

import com.act.ecommerce.service.ProductService;
import com.act.ecommerce.service.SqlQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ollama")
public class OllamaController {

    private static final Logger logger = LoggerFactory.getLogger(OllamaController.class);
    private final ChatClient chatClient;

    public OllamaController(OllamaChatModel ollamaChatModel) {
        this.chatClient = ChatClient.create(ollamaChatModel);
    }

    @GetMapping("/ask")
    public ResponseEntity<String> chat(@RequestParam String message) {
        logger.info("Received message: {}", message);

        try {
            String reply = chatClient.prompt(message).call().chatResponse().getResult().getOutput().getText();
            return ResponseEntity.ok(reply.trim());
        } catch (Exception e) {
            logger.error("Error during AI interaction", e);
            return ResponseEntity.status(500).body("Oops! Something went wrong.");
        }
    }
}
