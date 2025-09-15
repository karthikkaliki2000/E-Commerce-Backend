package com.act.ecommerce.dao;


import com.act.ecommerce.entity.OrderDetails;
import com.act.ecommerce.entity.User;
import com.razorpay.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailsDao extends JpaRepository<OrderDetails,Long> {
    List<OrderDetails> findByUser(User user);
    List<OrderDetails> findByUserAndOrderStatusIgnoreCase(User user, String orderStatus);
    List<OrderDetails> findByOrderStatusIgnoreCase(String orderStatus);

    List<OrderDetails> findByOrderStatus(String orderStatus);
}
