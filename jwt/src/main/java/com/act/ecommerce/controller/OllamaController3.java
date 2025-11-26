//package com.act.ecommerce.controller;
//
//import com.act.ecommerce.service.SqlQueryExecutor;
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
//public class OllamaController3 {
//    private static final Logger logger = LoggerFactory.getLogger(OllamaController3.class);
//    private final ChatClient chatClient;
//
//    @Autowired
//    private CacheManager cacheManager;
//
//    @Autowired
//     private SqlQueryExecutor sqlQueryExecutor;
//
//    @Autowired
//    public OllamaController3(OllamaChatModel ollamaChatModel) {
//        this.chatClient = ChatClient.create(ollamaChatModel);
//    }
//
//    @PostMapping("/ask")
//    public ResponseEntity<String> ask(@RequestBody Map<String, Object> payload) {
//        String message = payload != null ? (String) payload.get("message") : null;
//        if (message == null || message.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Message cannot be empty.");
//        }
//        System.out.println("=================================>"+message+"<==================================");
//
//        String startPrompt = "Hi, you are an ecommerce bot. You will write SQL queries to read the data and respond to customer queries related to their questions. " +
//                "Write a SQL query using SQLITE3 to get rows from tables. The tables have the following list of columns:\n" +
//                "CATALOG SCHEMA (use EXACT column names below):\n" +
//                "Table: product\n" +
//                "Columns:\n" +
//                "  product_id bigint AI PK\n" +
//                "  created_at datetime(6)\n" +
//                "  product_actual_price double\n" +
//                "  product_description varchar(2000)\n" +
//                "  product_discounted_price double\n" +
//                "  product_name varchar(255)\n" +
//                "  updated_at datetime(6)\n" +
//                "Table: order_item\n" +
//                "Columns:\n" +
//                "  id bigint AI PK\n" +
//                "  quantity int\n" +
//                "  unit_price double\n" +
//                "  order_order_id bigint\n" +
//                "  product_product_id bigint\n" +
//                "Table: order_products\n" +
//                "Columns:\n" +
//                "  order_id bigint\n" +
//                "  product_id bigint\n" +
//                "Table: product\n" +
//                "Columns:\n" +
//                "  product_id bigint AI PK\n" +
//                "  created_at datetime(6)\n" +
//                "  product_actual_price double\n" +
//                "  product_description varchar(2000)\n" +
//                "  product_discounted_price double\n" +
//                "  product_name varchar(255)\n" +
//                "  updated_at datetime(6)\n" +
//                "While responding, only return the raw query statement. Response should be plain text, do not format it (e.g., select * from table_name). " +
//                "If you're not able to query, respond with 'NO_SQL_QUERY'. {user_query}" +
//
//                "Make sure you're not including any comments or explanation text";
//
//        try {
//            String sqlQuery = chatClient
//                    .prompt(startPrompt)
//                    .call()
//                    .chatResponse()
//                    .getResult()
//                    .getOutput()
//                    .getText()
//                    .trim();
//            System.out.println("=================================>"+sqlQuery+"<==================================");
//            String queryResult=sqlQueryExecutor.runDynamicQuery(sqlQuery);
//            System.out.println("=================================>"+queryResult+"<==================================");
//
//            return ResponseEntity.ok(queryResult);
//        } catch (Exception e) {
//            logger.error("General chat failed", e);
//            return ResponseEntity.ok("Sorry, I couldn't process that right now. Please try again.");
//        }
//    }
//}
