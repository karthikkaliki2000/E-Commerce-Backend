package com.act.ecommerce.dao;

import com.act.ecommerce.entity.Cart;
import com.act.ecommerce.entity.Product;
import com.act.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDao extends JpaRepository<Cart, Long> {
    Cart findByUserAndProduct(User user, Product product);
    List<Cart> findByUser(User user);
    void deleteByUser(User user);
}
