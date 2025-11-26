//package com.act.ecommerce.controller;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.CacheManager;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/ollama")
//public class OllamaController2 {
//    private static final Logger logger = LoggerFactory.getLogger(OllamaController2.class);
//    private final ChatClient chatClient;
//
//    @Autowired
//    private CacheManager cacheManager;
//
//    @Autowired
//    public OllamaController2(OllamaChatModel ollamaChatModel) {
//        this.chatClient = ChatClient.create(ollamaChatModel);
//    }
//
//    @PostMapping("/ask")
//    public ResponseEntity<String> ask(@RequestBody Map<String, Object> payload) {
//        String message = payload != null ? (String) payload.get("message") : null;
//        if (message == null || message.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Message cannot be empty.");
//        }
//        try {
//            String reply = chatClient
//                    .prompt(message)
//                    .call()
//                    .chatResponse()
//                    .getResult()
//                    .getOutput()
//                    .getText()
//                    .trim();
//            return ResponseEntity.ok(reply);
//        } catch (Exception e) {
//            logger.error("General chat failed", e);
//            return ResponseEntity.ok("Sorry, I couldn't process that right now. Please try again.");
//        }
//    }
//}
