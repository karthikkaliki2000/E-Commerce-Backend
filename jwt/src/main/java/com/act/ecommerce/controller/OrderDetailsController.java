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
    @PostMapping("/place/{isSingleProductCheckout}")
    public ResponseEntity<Map<String, String>> placeOrder(
            @PathVariable(name = "isSingleProductCheckout") boolean isSingleProductCheckout,
            @RequestBody OrderRequest orderRequest) {
        try {
            orderDetailsService.placeOrder(orderRequest, isSingleProductCheckout);
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
    public ResponseEntity<List<OrderResponse>> getOrderHistory(
            @RequestParam(value = "status", required = false) String status) {
        List<OrderResponse> response = (status == null || status.isEmpty())
                ? orderDetailsService.getOrderHistory()
                : orderDetailsService.getOrderHistoryByStatus(status);
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

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping({"getOrderDetails", "getOrderDetails/{orderId}"})
    public ResponseEntity<List<OrderResponse>> getOrderDetails(
            @PathVariable(required = false) Long orderId,
            @RequestParam(value = "status", required = false) String status) {
        List<OrderResponse> response;
        if (orderId != null) {
            OrderResponse orderResponse = orderDetailsService.getOrderById(orderId);
            response = Collections.singletonList(orderResponse);
        } else if (status != null && !status.isEmpty()) {
            response = orderDetailsService.getOrderHistoryByStatus(status);
        } else {
            response = orderDetailsService.getOrderHistory();
        }
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    // all my orders
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/myOrders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestParam(value = "status", required = false) String status) {
        List<OrderResponse> response = (status == null || status.isEmpty())
                ? orderDetailsService.getOrderHistory()
                : orderDetailsService.getOrderHistoryByStatus(status);
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/allOrders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(value = "status", required = false) String status) {
        List<OrderResponse> response = (status == null || status.isEmpty())
                ? orderDetailsService.getAllOrders()
                : orderDetailsService.getAllOrdersByStatus(status);
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/cancelOrder/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        orderDetailsService.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/markOrderAsDelivered/{orderId}")
    public ResponseEntity<?> markOrderAsDelivered(@PathVariable Long orderId) {
        orderDetailsService.markOrderAsDelivered(orderId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Order marked as delivered successfully"));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/markOrderAsShipped/{orderId}")
    public ResponseEntity<?> markOrderAsShipped(@PathVariable Long orderId) {
        orderDetailsService.markOrderAsShipped(orderId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Order marked as Shipped successfully"));
    }
}