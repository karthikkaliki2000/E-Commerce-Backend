package com.act.ecommerce.dao;


import com.act.ecommerce.entity.OrderDetails;
import com.act.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailsDao extends JpaRepository<OrderDetails,Long> {
    List<OrderDetails> findByUser(User user);
}
