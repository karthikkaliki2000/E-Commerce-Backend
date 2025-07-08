package com.act.ecommerce.service;

import com.act.ecommerce.configuration.JwtRequestFilter;
import com.act.ecommerce.dao.OrderDetailsDao;
import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsService.class);
    private static final String ORDER_PLACED_TEMPLATE = "Order placed successfully with %d products.";

    @Autowired private OrderDetailsDao orderDetailsDao;
    @Autowired private ProductDao productDao;
    @Autowired private UserDao userDao;
    @Autowired private JwtRequestFilter jwtRequestFilter;

    /**
     * Places one or more product-based orders for the authenticated user.
     */
    public void placeOrder(OrderRequest orderRequest) {
        validateOrderRequest(orderRequest);

        String currentUser = jwtRequestFilter.CURRENT_USER;
        if (currentUser == null || currentUser.isBlank()) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + currentUser));

        List<OrderProductQuantity> productQuantityList = orderRequest.getOrderProductQuantities();
        if (productQuantityList == null || productQuantityList.isEmpty()) {
            throw new IllegalArgumentException("Order must include at least one product");
        }

        for (OrderProductQuantity item : productQuantityList) {
          validateProductItem(item);

            Product product = productDao.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + item.getProductId()));

            double totalPrice = product.getProductDiscountedPrice() * item.getQuantity();

            List<Product> productList = Collections.singletonList(product);
            OrderDetails order = new OrderDetails(
                    orderRequest.getFullName(),
                    orderRequest.getFullAddress(),
                    orderRequest.getEmail(),
                    orderRequest.getContactNumber(),
                    orderRequest.getAlternativeContactNumber(),
                    String.format(ORDER_PLACED_TEMPLATE, item.getQuantity()),
                    totalPrice,
                    productList,
                    user
            );


            orderDetailsDao.save(order);
            logger.info("Saved order for product '{}' with quantity {}", product.getProductName(), item.getQuantity());
        }

        logger.info("Order placed successfully with {} items by user '{}'", productQuantityList.size(), currentUser);
    }

    /**
     * Ensures basic user and order fields are present.
     */
    private void validateOrderRequest(OrderRequest orderRequest) {
        if (orderRequest == null) throw new IllegalArgumentException("Order request is null");

        if (isEmpty(orderRequest.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (isEmpty(orderRequest.getFullAddress())) {
            throw new IllegalArgumentException("Address is required");
        }
        if (isEmpty(orderRequest.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        if (isEmpty(orderRequest.getContactNumber())) {
            throw new IllegalArgumentException("Primary contact number is required");
        }
    }

    /**
     * Validates each product ID and quantity in the order.
     */
    private void validateProductItem(OrderProductQuantity item) {
        if (item == null) throw new IllegalArgumentException("Order item is null");
        if (item.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0 for product ID: " + item.getProductId());
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Retrieves all orders placed by the authenticated user.
     */
    public List<OrderResponse> getOrderHistory() {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<OrderDetails> orders = orderDetailsDao.findByUser(user);

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId) {
        OrderDetails order = orderDetailsDao.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(OrderDetails order) {
        List<ProductSummary> productSummaries = order.getProducts().stream()
                .map(product -> new ProductSummary(
                        product.getProductId(),
                        product.getProductName(),
                        1, // Replace with real quantity if available
                        product.getProductDiscountedPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getOrderFullName(),
                order.getOrderFullAddress(),
                order.getOrderPhoneNumber(),
                order.getOrderAlternativePhoneNumber(),
                order.getOrderEmail(),
                order.getOrderStatus(),
                order.getOrderTotalPrice(),
                order.getCreatedAt(),
                productSummaries
        );
    }

}
