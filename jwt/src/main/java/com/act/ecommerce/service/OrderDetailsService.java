package com.act.ecommerce.service;

import com.act.ecommerce.configuration.JwtRequestFilter;
import com.act.ecommerce.configuration.RazorpayConfig;
import com.act.ecommerce.constants.OrderStatusConstants;
import com.act.ecommerce.dao.CartDao;
import com.act.ecommerce.dao.OrderDetailsDao;
import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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



    @Autowired
    private RazorpayConfig razorpayConfig;

    private String KEY;
    private String KEY_SECRET;
    private String CURRENCY;

    @PostConstruct
    public void initRazorpayKeys() {
        this.KEY = razorpayConfig.getKey();
        this.KEY_SECRET = razorpayConfig.getSecret();
        this.CURRENCY = razorpayConfig.getCurrency();
        logger.info("Razorpay keys initialized successfully");
    }


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
                OrderStatusConstants.ORDER_PLACED,
                totalOrderPrice,
                productList,
                user,
                orderRequest.getTransactionId()
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

        if (!OrderStatusConstants.ORDER_PLACED.equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Only orders with status PLACED can be cancelled");
        }

        order.setOrderStatus(OrderStatusConstants.ORDER_CANCELLED);
        orderDetailsDao.save(order);
        logger.info("Order with ID {} cancelled", orderId);
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

        if (!OrderStatusConstants.ORDER_SHIPPED.equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Only orders with status SHIPPED can be marked as DELIVERED");
        }

        order.setOrderStatus(OrderStatusConstants.ORDER_DELIVERED);
        orderDetailsDao.save(order);
        logger.info("Order with ID {} marked as Delivered", orderId);
    }

    public void markOrderAsShipped(Long orderId) {
        OrderDetails order = orderDetailsDao.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!OrderStatusConstants.ORDER_PLACED.equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Only orders with status PLACED can be marked as Shipped");
        }

        order.setOrderStatus(OrderStatusConstants.ORDER_SHIPPED);
        orderDetailsDao.save(order);
        logger.info("Order with ID {} marked as Shipped", orderId);
    }


    public TransactionDetails createTransaction(Double amount) {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        if (currentUser == null || currentUser.isBlank()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUser));

        try {
            Map<String, Object> orderOptions = new HashMap<>();
            orderOptions.put("amount", (int) (amount * 100)); // Razorpay expects amount in paise
            orderOptions.put("currency", CURRENCY);
            orderOptions.put("receipt", "receipt#" + System.currentTimeMillis());



            RazorpayClient razorpayClient = new RazorpayClient(KEY, KEY_SECRET);
            Order razorPayOrder = razorpayClient.orders.create(new JSONObject(orderOptions));

            logger.info("Razorpay Order: {}", razorPayOrder.toString());
            logger.info("Transaction created successfully for user: {} with amount: {}", currentUser, amount);

            return prepareTransactionDetails(razorPayOrder);

        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create transaction", e);
        }
    }
    private TransactionDetails prepareTransactionDetails(Order order) {
        String orderId = order.get("id");
        String currency = order.get("currency");
        int amount = order.get("amount");
        String status = order.get("status");
        String receipt = order.get("receipt");



        return new TransactionDetails(orderId, currency, amount, status, receipt,KEY);
    }




}