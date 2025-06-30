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

import java.util.List;

@Service
public class OrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsService.class);
    private static final String ORDER_PLACED_SUCCESSFULLY = "Order placed successfully with %d products.";

    @Autowired
    private OrderDetailsDao orderDetailsDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private UserDao userDao;

    public void placeOrder(OrderRequest orderRequest) {
        validateOrderRequest(orderRequest);

        String currentUser = jwtRequestFilter.CURRENT_USER;
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + currentUser));

        List<OrderProductQuantity> orderProductQuantityList = orderRequest.getOrderProductQuantities();

        if (orderProductQuantityList == null || orderProductQuantityList.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product");
        }

        for (OrderProductQuantity item : orderProductQuantityList) {
            validateProductQuantity(item);

            Product product = productDao.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + item.getProductId()));

            double totalPrice = product.getProductDiscountedPrice() * item.getQuantity();

            OrderDetails order = new OrderDetails(
                    orderRequest.getFullName(),
                    orderRequest.getFullAddress(),
                    orderRequest.getEmail(),
                    orderRequest.getContactNumber(),
                    orderRequest.getAlternativeContactNumber(),
                    String.format(ORDER_PLACED_SUCCESSFULLY, item.getQuantity()),
                    totalPrice,
                    product,
                    user
            );

            orderDetailsDao.save(order);
            logger.info("Saved order for product ID: {} with quantity: {}", item.getProductId(), item.getQuantity());
        }

        logger.info("Order placed successfully with {} products for user '{}'.", orderProductQuantityList.size(), currentUser);
    }

    private void validateOrderRequest(OrderRequest orderRequest) {
        if (orderRequest == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }

        if (isEmpty(orderRequest.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (isEmpty(orderRequest.getFullAddress())) {
            throw new IllegalArgumentException("Full address is required");
        }

        if (isEmpty(orderRequest.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }

        if (isEmpty(orderRequest.getContactNumber())) {
            throw new IllegalArgumentException("Contact number is required");
        }
    }

    private void validateProductQuantity(OrderProductQuantity item) {
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
}
