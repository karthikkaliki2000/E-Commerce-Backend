package com.act.ecommerce.schedulers;

import com.act.ecommerce.configuration.JwtRequestFilter;
import com.act.ecommerce.dao.CartDao;
import com.act.ecommerce.dao.OrderDetailsDao;
import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.OrderDetails;
import com.act.ecommerce.service.OrderDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsService.class);

    @Autowired
    private OrderDetailsDao orderDetailsDao;
    @Autowired private ProductDao productDao;
    @Autowired private UserDao userDao;
    @Autowired private CartDao cartDao;
    @Autowired private JwtRequestFilter jwtRequestFilter;

    //   @Scheduled(initialDelay = 10000,fixedRate = 10000)
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minute
    public void processPlacedOrdersForShipping() {
        List<OrderDetails> placedOrders = orderDetailsDao.findByOrderStatus("Order Placed");

        if (placedOrders.isEmpty()) {
            logger.info("No 'Order Placed' orders found at {}", LocalDateTime.now());
            return;
        }

        placedOrders.forEach(order -> {
            try {
                if ("Order Placed".equalsIgnoreCase(order.getOrderStatus())) {
                    order.setOrderStatus("Order Shipped");
                    orderDetailsDao.save(order);
                    logger.info("Order ID {} marked as 'Order Shipped'", order.getOrderId());
                } else {
                    logger.warn("Order ID {} skipped: current status is '{}'", order.getOrderId(), order.getOrderStatus());
                }
            } catch (Exception e) {
                logger.error("Error updating order ID {}: {}", order.getOrderId(), e.getMessage(), e);
            }
        });

        logger.info("Shipping task completed. Total processed: {}", placedOrders.size());
    }


    @Scheduled(cron = "0 */10 * * * *") // Every 10 minutes
    public void processShippedOrdersForDelivery() {
        List<OrderDetails> shippedOrders = orderDetailsDao.findByOrderStatus("Order Shipped");

        if (shippedOrders.isEmpty()) {
            logger.info("No 'Order Shipped' orders found at {}", LocalDateTime.now());
            return;
        }

        shippedOrders.forEach(order -> {
            try {
                if ("Order Shipped".equalsIgnoreCase(order.getOrderStatus())) {
                    order.setOrderStatus("Order Delivered");
                    orderDetailsDao.save(order);
                    logger.info("Order ID {} marked as 'Order Delivered'", order.getOrderId());
                } else {
                    logger.warn("Order ID {} skipped: current status is '{}'", order.getOrderId(), order.getOrderStatus());
                }
            } catch (Exception e) {
                logger.error("Error delivering order ID {}: {}", order.getOrderId(), e.getMessage(), e);
            }
        });

        logger.info("Delivery task completed. Total processed: {}", shippedOrders.size());
    }

}
