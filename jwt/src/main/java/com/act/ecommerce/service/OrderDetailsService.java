package com.act.ecommerce.service;

import com.act.ecommerce.configuration.JwtRequestFilter;
import com.act.ecommerce.dao.CartDao;
import com.act.ecommerce.dao.OrderDetailsDao;
import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsService.class);

    @Autowired private OrderDetailsDao orderDetailsDao;
    @Autowired private ProductDao productDao;
    @Autowired private UserDao userDao;
    @Autowired private CartDao cartDao;
    @Autowired private JwtRequestFilter jwtRequestFilter;

    @Transactional
    public void placeOrder(OrderRequest orderRequest, boolean isSingleProductCheckout) {
        validateOrderRequest(orderRequest);

        String currentUser = jwtRequestFilter.CURRENT_USER;
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUser));

        List<OrderProductQuantity> productQuantityList = orderRequest.getOrderProductQuantities();
        if (productQuantityList == null || productQuantityList.isEmpty()) {
            throw new IllegalArgumentException("No products included in the order");
        }

        double totalOrderPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Product> productList = new ArrayList<>();

        for (OrderProductQuantity item : productQuantityList) {
            validateProductItem(item);

            Product product = productDao.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + item.getProductId()));

            double lineTotal = product.getProductDiscountedPrice() * item.getQuantity();
            totalOrderPrice += lineTotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getProductDiscountedPrice());
            orderItems.add(orderItem);
            productList.add(product);
        }

        OrderDetails order = new OrderDetails(
                orderRequest.getFullName(),
                orderRequest.getFullAddress(),
                orderRequest.getEmail(),
                orderRequest.getContactNumber(),
                orderRequest.getAlternativeContactNumber(),
                "Order Placed",
                totalOrderPrice,
                productList,
                user
        );
        order.setItems(orderItems);
        orderItems.forEach(item -> item.setOrder(order));

        orderDetailsDao.save(order);

        if (isSingleProductCheckout) {
            cartDao.deleteByUser(user);
        } else {
            clearCheckedOutItemsFromCart(user, productQuantityList);
        }

        logger.info("Order placed with {} items by '{}'. Order ID: {}", productQuantityList.size(), currentUser, order.getOrderId());
    }

    private void clearCheckedOutItemsFromCart(User user, List<OrderProductQuantity> items) {
        items.forEach(item -> {
            Product product = productDao.findById(item.getProductId()).orElse(null);
            if (product != null) {
                Cart cart = cartDao.findByUserAndProduct(user, product);
                if (cart != null) cartDao.deleteById(cart.getCartId());
            }
        });
    }

    public List<OrderResponse> getOrderDetails(Long orderId) {
        if (orderId != null) {
            return Collections.singletonList(getOrderById(orderId));
        } else {
            return getOrderHistory();
        }
    }

    public OrderResponse getOrderById(Long orderId) {
        OrderDetails order = orderDetailsDao.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrderHistory() {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<OrderDetails> orders = orderDetailsDao.findByUser(user);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    // NEW: Filter user orders by status
    public List<OrderResponse> getOrderHistoryByStatus(String status) {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<OrderDetails> orders = orderDetailsDao.findByUserAndOrderStatusIgnoreCase(user, status);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    // NEW: Filter all orders (admin) by status
    public List<OrderResponse> getAllOrdersByStatus(String status) {
        List<OrderDetails> orders = orderDetailsDao.findByOrderStatusIgnoreCase(status);
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(OrderDetails order) {
        List<ProductSummary> productSummaries = order.getItems().stream()
                .map(item -> new ProductSummary(
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
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

    private void validateOrderRequest(OrderRequest orderRequest) {
        if (orderRequest == null) throw new IllegalArgumentException("Order request is null");

        if (isEmpty(orderRequest.getFullName())) throw new IllegalArgumentException("Full name is required");
        if (isEmpty(orderRequest.getFullAddress())) throw new IllegalArgumentException("Address is required");
        if (isEmpty(orderRequest.getEmail())) throw new IllegalArgumentException("Email is required");
        if (isEmpty(orderRequest.getContactNumber())) throw new IllegalArgumentException("Contact number is required");
    }

    private void validateProductItem(OrderProductQuantity item) {
        if (item == null) throw new IllegalArgumentException("Order item is null");
        if (item.getProductId() == null) throw new IllegalArgumentException("Product ID cannot be null");
        if (item.getQuantity() == null || item.getQuantity() <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    private boolean isEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }

    public void cancelOrder(Long orderId) {
        OrderDetails order = orderDetailsDao.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        if (!order.getOrderStatus().toLowerCase().equals("order placed")) {
            throw new IllegalStateException("Only orders with status PLACED can be cancelled");
        }
        order.setOrderStatus("Order Cancelled");
        orderDetailsDao.save(order);
    }

    public List<OrderResponse> getAllOrders() {
        List<OrderDetails> orders = orderDetailsDao.findAll();
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public void markOrderAsDelivered(Long orderId) {
        OrderDetails order = orderDetailsDao.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getOrderStatus().toLowerCase().equals("order shipped")) {
            throw new IllegalStateException("Only orders with status Shipped can be marked as DELIVERED");
        }
        order.setOrderStatus("Order Delivered");
        orderDetailsDao.save(order);
        logger.info("Order with ID {} marked as delivered", orderId);

    }

    public void markOrderAsShipped(Long orderId) {
        OrderDetails order = orderDetailsDao.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getOrderStatus().toLowerCase().equals("order placed")) {
            throw new IllegalStateException("Only orders with status PLACED can be marked as Shipped");
        }
        order.setOrderStatus("Order Shipped");
        orderDetailsDao.save(order);
        logger.info("Order with ID {} marked as Shipped", orderId);

    }
}