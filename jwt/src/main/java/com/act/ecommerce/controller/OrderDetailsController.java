package com.act.ecommerce.controller;

import com.act.ecommerce.entity.OrderRequest;
import com.act.ecommerce.entity.OrderResponse;
import com.act.ecommerce.service.OrderDetailsService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsController.class);

    @Autowired
    private OrderDetailsService orderDetailsService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/place")
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            orderDetailsService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("message", "Order placed successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Invalid order request: " + e.getMessage()));
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unhandled error while placing order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred"));
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/history")
    public ResponseEntity<List<OrderResponse>> getOrderHistory() {
        List<OrderResponse> response = orderDetailsService.getOrderHistory();
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderDetailsService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }
}
