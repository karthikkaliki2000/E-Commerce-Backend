package com.act.ecommerce.controller;

import com.act.ecommerce.entity.OrderRequest;
import com.act.ecommerce.service.OrderDetailsService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsController.class);

    @Autowired
    private OrderDetailsService orderDetailsService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            orderDetailsService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Order placed successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid order request: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unhandled error while placing order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }
}
