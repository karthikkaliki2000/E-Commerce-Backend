 package com.act.ecommerce.controller;

 import com.act.ecommerce.service.SqlQueryExecutor;
 import net.sf.jsqlparser.parser.CCJSqlParserUtil;
 import net.sf.jsqlparser.statement.Statement;
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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 @RestController
 @RequestMapping("/api/ollama")
 public class OllamaController {

     private static final Logger logger = LoggerFactory.getLogger(OllamaController.class);
     private final ChatClient chatClient;

     @Autowired
     private CacheManager cacheManager;

     @Autowired
     private SqlQueryExecutor sqlQueryExecutor;

     // --- 1. Intent Classification Prompt ---
     private static final String INTENT_CLASSIFICATION_PROMPT = """
         You are a routing engine. Classify the user's query into one of two categories: 'SQL' or 'CHAT'.
         - Use 'SQL' for any request that needs data from the catalog or orders: availability ("do we have"), pricing ("price", "cost", "how much"), counts ("how many", "count"), lists ("list", "show"), price filters ("under", "less than", "more than", "between"), popularity ("most ordered", "bestseller", "popular"), or order/account status.
         - Use 'CHAT' only for general small talk or policy questions that do not require database lookup.

         Ambiguous product/price/availability/count/list queries MUST be classified as 'SQL'.

         *** OUTPUT ONLY THE CLASSIFICATION WORD (SQL or CHAT) AND NOTHING ELSE. ***
         """;

     // --- 2. SQL Generation Prompt (STRICT CODE ONLY) ---
     private static final String SQL_PROMPT = """
You are an ecommerce SQL bot. Your task is to convert customer queries into valid, read-only SQL SELECT statements using SQLITE3.

CATALOG SCHEMA (use EXACT column names below):
- product:
    - product_id bigint AI PK
    - created_at datetime(6)
    - product_actual_price double
    - product_description varchar(2000)
    - product_discounted_price double
    - product_name varchar(255)
    - updated_at datetime(6)

- order_item:
    - id bigint AI PK
    - quantity int
    - unit_price double
    - order_order_id bigint
    - product_product_id bigint

- order_products:
    - order_id bigint
    - product_id bigint

RULES:
- Output ONLY the raw SQL query. No explanations, no comments, no formatting.
- If no valid query can be generated, respond with: NO_SQL_QUERY
- Always use product_discounted_price for price filters.
- Do NOT multiply prices by 100 — prices are already stored in rupees.
- For name filters, use: LOWER(product_name) LIKE LOWER('%keyword%')
- For counts: SELECT COUNT(*) AS total FROM ...
- For lists without explicit limit: LIMIT 10
- Normalize user inputs: interpret "30k" as 30000; ignore thousands separators like ","
- For bestseller lists: JOIN order_products op ON op.product_id = p.product_id, GROUP BY p.product_name, ORDER BY SUM(op.quantity) DESC

EXAMPLES:
-- Count products under 20000
SELECT COUNT(*) AS total FROM product WHERE product_actual_price <= 20000;

-- Check if a Toshiba 108 cm TV exists and get price
SELECT product_name, product_actual_price FROM product WHERE LOWER(product_name) LIKE LOWER('%toshiba%108 cm%tv%') LIMIT 3;

-- List TVs under 30000
SELECT product_name, product_actual_price FROM product WHERE LOWER(product_name) LIKE LOWER('%tv%') AND product_actual_price <= 30000 LIMIT 10;

-- Top 5 most ordered products (bestsellers)
SELECT p.product_name, SUM(op.quantity) AS total_ordered_quantity
FROM product p
JOIN order_products op ON p.product_id = op.product_id
GROUP BY p.product_name
ORDER BY total_ordered_quantity DESC
LIMIT 5;

Customer query: {user_query}
Note: Remember only provide raw sql query no other texts
""";


     // --- 3. General Conversation Prompt (STRICTLY FRIENDLY + NO HALLUCINATIONS) ---
     private static final String GENERAL_PROMPT = """
         You are a friendly, concise e-commerce assistant named 'Aura'.
         Be brief, answer directly, and avoid filler.
         Never invent product availability, prices, counts, or bestsellers.
         If the user asks for such info without a database result, say: "Let me check our database for that." If no data is found, say you couldn't find matching products.
         """;


     public OllamaController(OllamaChatModel ollamaChatModel) {
         this.chatClient = ChatClient.create(ollamaChatModel);
     }

     @PostMapping("/ask")
     public ResponseEntity<String> chat(@RequestBody Map<String, Object> payload) {
         String message = (String) payload.get("message");
         @SuppressWarnings("unchecked")
         List<String> history = (List<String>) payload.get("history");

         if (message == null || message.trim().isEmpty()) {
             return ResponseEntity.badRequest().body("Message cannot be empty.");
         }

         logger.info("Received message: {}", message);

         // Normalize shorthand like 30k -> 30000 to help SQL generation
         if (message != null) {
             Pattern kPattern = Pattern.compile("(?i)\\b(\\d{1,3})\\s*k\\b");
             Matcher kMatcher = kPattern.matcher(message);
             StringBuffer normalized = new StringBuffer();
             while (kMatcher.find()) {
                 int thousands = Integer.parseInt(kMatcher.group(1)) * 1000;
                 kMatcher.appendReplacement(normalized, String.valueOf(thousands));
             }
             kMatcher.appendTail(normalized);
             message = normalized.toString();
         }

         String lowerMessage = message.toLowerCase().trim();

         // Conversation-scoped cache key derived from history
         String conversationKey = "conv_" + (history != null ? String.valueOf(history.toString().hashCode()) : "0");

         // Follow-up handling for "which ones / what product" after a count answer
         String[] followUpKeywords = new String[]{
                 "what product", "which product", "which products", "which ones", "show them", "list them", "show products", "list products", "what are they", "what are those"
         };
         boolean isFollowUp = false;
         for (String fkw : followUpKeywords) {
             if (lowerMessage.contains(fkw)) { isFollowUp = true; break; }
         }
         if (isFollowUp) {
             String detailsSqlKey = "details_sql_" + conversationKey;
             String detailsSql = cacheManager.getCache("chat") != null
                     ? cacheManager.getCache("chat").get(detailsSqlKey, String.class)
                     : null;
             // Fallback to a more general last-details key if conversation-scoped key misses
             if (detailsSql == null && cacheManager.getCache("chat") != null) {
                 detailsSql = cacheManager.getCache("chat").get("details_sql_last", String.class);
             }
             if (detailsSql != null && detailsSql.toLowerCase().startsWith("select")) {
                 try {
                     String raw = sqlQueryExecutor.runDynamicQuery(detailsSql);
                     if ("[]".equals(raw)) {
                         return ResponseEntity.ok("I couldn't find any matching products.");
                     }
                     String pp = String.format(
                             "You are a friendly e-commerce assistant. The database returned the following JSON data: %s. Please summarize the products with names and prices clearly, maximum 10 items. Do NOT mention SQL, JSON, or database.",
                             raw
                     );
                     String out = chatClient.prompt(pp).call().chatResponse().getResult().getOutput().getText().trim();
                     if (cacheManager.getCache("chat") != null) {
                         cacheManager.getCache("chat").put(String.valueOf((pp).hashCode()), out);
                     }
                     return ResponseEntity.ok(out);
                 } catch (Exception e) {
                     logger.error("Follow-up details query failed", e);
                     return ResponseEntity.ok("Sorry, I couldn't fetch the product list right now.");
                 }
             }
         }

         // 1. Handle simple greetings/simple conversation instantly (Mode 1: Static)
         if (lowerMessage.equals("hi") || lowerMessage.equals("hello") || lowerMessage.equals("hey") || lowerMessage.contains("how are you")) {
             return ResponseEntity.ok("Hi there! I'm Aura, ready to assist you. How can I help with your shopping today? 😊");
         }

         // 2. Deterministic pre-routing for product/price/count/list queries
         String[] forceSqlKeywords = new String[]{
                 "price", "price of", "how much", "cost", "cost of", "how many", "count", "under ", "less than", "more than",
                 "list", "show", "available", "availability", "do we have", "in stock", "order status",
                 "bestseller", "best seller", "most ordered", "popular", "what product", "which ones", "what are they"
         };
         boolean shouldForceSql = false;
         for (String kw : forceSqlKeywords) {
             if (lowerMessage.contains(kw)) {
                 shouldForceSql = true; break;
             }
         }

         // 3. Intent Classification Call (LLM) only if not forced
         String intentPrompt = INTENT_CLASSIFICATION_PROMPT + "\nQuery: " + message;

         String intentCacheKey = "intent_" + String.valueOf(intentPrompt.hashCode());
         String cachedIntent = cacheManager.getCache("chat") != null
                 ? cacheManager.getCache("chat").get(intentCacheKey, String.class)
                 : null;

         String intent = shouldForceSql ? "SQL" : cachedIntent;
         if (intent == null) {
             try {
                 intent = chatClient.prompt(intentPrompt).call().chatResponse().getResult().getOutput().getText().trim().toUpperCase();
                 if (cacheManager.getCache("chat") != null) {
                     cacheManager.getCache("chat").put(intentCacheKey, intent);
                 }
             } catch (Exception e) {
                 logger.error("Intent classification failed", e);
                 intent = "CHAT"; // Default to safe chat mode on failure
             }
         }
         logger.info("Classified Intent: {}", intent);

         // 3. Determine Final System Prompt
         String systemPrompt = intent.equals("SQL") ? SQL_PROMPT : GENERAL_PROMPT;

         StringBuilder promptBuilder = new StringBuilder(systemPrompt).append("\nUser: ");
         if (history != null && !history.isEmpty()) {
             for (String h : history) {
                 promptBuilder.append(h).append("\n");
             }
         }
         promptBuilder.append(message);
         String finalPrompt = promptBuilder.toString();

         // 4. Cache lookup for the final full prompt
         String finalCacheKey = String.valueOf(finalPrompt.hashCode());
         String finalCached = cacheManager.getCache("chat") != null
                 ? cacheManager.getCache("chat").get(finalCacheKey, String.class)
                 : null;

         if (finalCached != null) {
             logger.info("Returning cached final response for hash: {}", finalCacheKey);
             return ResponseEntity.ok(finalCached);
         }

         try {
             // 5. Final LLM Call based on the classified intent
             String reply = chatClient.prompt(finalPrompt).call().chatResponse().getResult().getOutput().getText().trim();
             logger.info("Final LLM raw reply: {}", reply);
             reply = reply.replace("```sql", "")
                     .replace("```", "")
                     .trim();

             logger.info("Final LLM raw reply (cleaned): {}", reply);

             try {
                 Statement statement = CCJSqlParserUtil.parse(reply);
                 reply = statement.toString(); // normalized SQL
             } catch (Exception e) {
                 logger.error("Failed to parse SQL: {}", reply, e);
             }
             logger.info("Final Parsed raw Query: {}", reply);
             // 6. Handle SQL Mode (Mode 2: Data Retrieval)
             if (intent.equals("SQL")) {
                 if (reply.toLowerCase().startsWith("select")) {

                     // A. Execute the SQL Query
                     String rawQueryResult = sqlQueryExecutor.runDynamicQuery(reply);
                        System.out.println("Raw Query Result--------------"+rawQueryResult+"------------");
                     // B. Short-circuit on errors (with a retry using camelCase column names)
                     if (rawQueryResult != null && rawQueryResult.contains("\"error\"")) {
                         logger.warn("SQL error encountered. Attempting camelCase retry. Error: {}", rawQueryResult);
                         try {
                             String retrySql = reply
                                     .replace("product_name", "productName")
                                     .replace("product_actual_price", "productActualPrice")
                                     .replace("product_discounted_price", "productDiscountedPrice")
                                     .replace("product_id", "productId")
                                     .replace("order_total_price", "orderTotalPrice")
                                     .replace("order_status", "orderStatus");
                             if (!retrySql.equals(reply)) {
                                 String retryResult = sqlQueryExecutor.runDynamicQuery(retrySql);
                                 if (retryResult != null && !retryResult.contains("\"error\"")) {
                                     rawQueryResult = retryResult; // proceed with normal flow below
                                 } else {
                                     logger.warn("Retry SQL also failed: {}", retryResult);
                                     return ResponseEntity.ok("Sorry, I couldn't retrieve that right now. Please try again.");
                                 }
                             } else {
                                 return ResponseEntity.ok("Sorry, I couldn't retrieve that right now. Please try again.");
                             }
                         } catch (Exception ex) {
                             logger.warn("CamelCase retry attempt failed unexpectedly", ex);
                             return ResponseEntity.ok("Sorry, I couldn't retrieve that right now. Please try again.");
                         }
                     }

                     // C. Handle scalar counts directly without a second LLM call
                     if (rawQueryResult != null && rawQueryResult.startsWith("{")) {
                         // Try to extract count from keys 'total' or 'count'
                         String count = null;
                         if (rawQueryResult.contains("\"total\"")) {
                             count = rawQueryResult.replaceAll(".*\\\"total\\\"\\s*:\\s*\\\"?(\\d+)\\\"?.*", "$1");
                         } else if (rawQueryResult.contains("\"count\"")) {
                             count = rawQueryResult.replaceAll(".*\\\"count\\\"\\s*:\\s*\\\"?(\\d+)\\\"?.*", "$1");
                         }
                         if (count != null && count.matches("\\d+")) {
                             // Build and cache a follow-up details SQL if the original SQL was a count on product
                             String countSql = reply;
                             String whereClause = "";
                             try {
                                 // Extract WHERE clause from the count SQL
                                 String lc = countSql.toLowerCase();
                                 if (lc.contains(" from product")) {
                                     int fromIdx = lc.indexOf(" from product");
                                     String tail = countSql.substring(fromIdx + " from product".length());
                                     // Find where and optional limit
                                     int whereIdx = tail.toLowerCase().indexOf(" where ");
                                     if (whereIdx >= 0) {
                                         whereClause = tail.substring(whereIdx, tail.length());
                                         // Trim after any ORDER/LIMIT/; if present
                                         int endIdx = whereClause.toLowerCase().indexOf(" limit ");
                                         if (endIdx < 0) endIdx = whereClause.toLowerCase().indexOf(" order by ");
                                         if (endIdx < 0) endIdx = whereClause.indexOf(';');
                                         if (endIdx > 0) whereClause = whereClause.substring(0, endIdx);
                                     }
                                 }
                             } catch (Exception ex) {
                                 logger.warn("Failed to derive WHERE clause from count SQL", ex);
                             }

                             String detailsSql = ("SELECT product_name, product_actual_price FROM product " + whereClause + " LIMIT 10").trim();
                             if (cacheManager.getCache("chat") != null) {
                                 String detailsSqlKey = "details_sql_" + conversationKey;
                                 cacheManager.getCache("chat").put(detailsSqlKey, detailsSql);
                                 cacheManager.getCache("chat").put("details_sql_last", detailsSql);
                             }

                             // If the original user message asks both for count and list, fetch details now and combine
                             boolean wantsListToo = lowerMessage.contains("what") || lowerMessage.contains("which") || lowerMessage.contains("list") || lowerMessage.contains("show");
                             if (wantsListToo) {
                                 try {
                                     String detailsJson = sqlQueryExecutor.runDynamicQuery(detailsSql);
                                     if (detailsJson == null || detailsJson.equals("[]")) {
                                         String combined = String.format("There are %s products matching your request, but I couldn't list them right now.", count);
                                         if (cacheManager.getCache("chat") != null) {
                                             cacheManager.getCache("chat").put(finalCacheKey, combined);
                                         }
                                         return ResponseEntity.ok(combined);
                                     }
                                     String pp = String.format(
                                             "You are a helpful e-commerce assistant. We have a total of %s matching products. Based on this JSON: %s, present up to 10 product names with prices in a concise bullet list. Do NOT mention SQL, JSON, or database.",
                                             count, detailsJson
                                     );
                                     String out = chatClient.prompt(pp).call().chatResponse().getResult().getOutput().getText().trim();
                                     if (cacheManager.getCache("chat") != null) {
                                         cacheManager.getCache("chat").put(finalCacheKey, out);
                                     }
                                     return ResponseEntity.ok(out);
                                 } catch (Exception ex) {
                                     logger.warn("Combined count+list flow failed; falling back to count only", ex);
                                 }
                             }

                             String direct = String.format("There are %s products matching your request.", count);
                             if (cacheManager.getCache("chat") != null) {
                                 cacheManager.getCache("chat").put(finalCacheKey, direct);
                             }
                             return ResponseEntity.ok(direct);
                         }
                     }

                     // D. If array is empty, reply clearly
                     if ("[]".equals(rawQueryResult)) {
                         String notFound = "I couldn't find any products matching your request.";
                         if (cacheManager.getCache("chat") != null) {
                             cacheManager.getCache("chat").put(finalCacheKey, notFound);
                         }
                         return ResponseEntity.ok(notFound);
                     }

                     System.out.println("**************>"+rawQueryResult+"<**************");
                     // E. Post-process the tabular/structured result into human-readable text (fallback to LLM)
                     String postProcessPrompt = String.format(
                             "You are a friendly e-commerce assistant. The database returned the following JSON data: %s. Please rephrase this data into a friendly, concise response for the customer. Mention product names and their prices clearly where applicable and prices should be in rupees only. If the data are rankings (order_count), mention top items. Do NOT mention SQL, JSON, or database.",
                             rawQueryResult
                     );

                     String friendlyResponse = chatClient.prompt(postProcessPrompt).call().chatResponse().getResult().getOutput().getText().trim();
                     System.out.println("####################>"+friendlyResponse+"<#################");

                     // F. Cache and return the final friendly response
                     if (cacheManager.getCache("chat") != null) {
                         cacheManager.getCache("chat").put(finalCacheKey, friendlyResponse);
                     }
                     return ResponseEntity.ok(friendlyResponse);

                 } else {
                     // D. LLM failed to generate SQL (Mode 3: Complex Intent Failure)
                     logger.error("LLM FAILED TO GENERATE SQL in SQL mode. Output: {}", reply);
                     // Fallback: This catch prevents the abnormal scripts from reaching the customer
                     return ResponseEntity.ok("I apologize, I'm having trouble accessing our database for that specific request. Could you please try phrasing your question about products or orders differently?");
                 }

             } else {
                 // 7. Handle General Chat Mode (Mode 4: General Chat)

                 // Final safety check for general chat mode
                 if (reply.toLowerCase().contains("script") || reply.toLowerCase().contains("sql assistant") || reply.contains("Me:") || reply.contains("Aura:") || reply.contains("User:")) {
                     logger.warn("LLM returned unexpected scripted/role-playing response in general mode: {}", reply);
                     return ResponseEntity.ok("I'm sorry, I seem to have gotten confused! Could you please try asking your question again?");
                 }

                 // Cache and return the general response
                 if (cacheManager.getCache("chat") != null) {
                     cacheManager.getCache("chat").put(finalCacheKey, reply);
                 }
                 return ResponseEntity.ok(reply);
             }

         } catch (Exception e) {
             logger.error("Error during AI interaction or SQL execution", e);
             return ResponseEntity.status(500).body("Oops! Something went wrong with the AI service. Please check the logs for details.");
         }
     }
 }

