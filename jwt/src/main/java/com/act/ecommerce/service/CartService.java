package com.act.ecommerce.service;

import com.act.ecommerce.configuration.JwtRequestFilter;
import com.act.ecommerce.dao.CartDao;
import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.Cart;
import com.act.ecommerce.entity.Product;
import com.act.ecommerce.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartDao cartDao;
    @Autowired private ProductDao productDao;
    @Autowired private UserDao userDao;
    @Autowired private JwtRequestFilter jwtRequestFilter;

    public Cart addToCart(Long productId, int quantity) {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        if (currentUser == null || currentUser.isBlank()) {
            throw new IllegalArgumentException("User not authenticated");
        }
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUser));
        Optional<Cart> existingCartOpt = Optional.ofNullable(cartDao.findByUserAndProduct(user, product));
        Cart cart = existingCartOpt.orElse(new Cart(product, user, quantity));
        if (existingCartOpt.isPresent()) {
            cart.setQuantity(quantity);
        }
        return cartDao.save(cart);
    }

    public List<Cart> getCartDetails() {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        if (currentUser == null || currentUser.isBlank()) {
            throw new IllegalArgumentException("User not authenticated");
        }
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUser));
        List<Cart> cartList = cartDao.findByUser(user);
        if (cartList == null || cartList.isEmpty()) {
            return Collections.emptyList();
        }
        return cartList;
    }

    public void removeCartItem(Long cartId) {
        cartDao.deleteById(cartId);
    }

    public Cart updateCartItemQuantity(Long cartId, int quantity) {
        Cart cart = cartDao.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        cart.setQuantity(quantity);
        return cartDao.save(cart);
    }


    @Transactional
    public void clearCart() {
        String currentUser = jwtRequestFilter.CURRENT_USER;
        if (currentUser == null || currentUser.isBlank()) {
            throw new IllegalArgumentException("User not authenticated");
        }
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUser));
        cartDao.deleteByUser(user);
    }
}