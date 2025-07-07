package com.act.ecommerce.controller;

import com.act.ecommerce.entity.Cart;
import com.act.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CartController.class);


    // Define endpoints for cart operations here
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/cart/add/{productId}/{quantity}")
    public Cart addToCart(@PathVariable(name = "productId") Long productId, @PathVariable (name = "quantity") int quantity) {
        // Logic to add item to cart
        logger.info("Adding product with ID: " + productId + " to cart with quantity: " + quantity);
        return cartService.addToCart(productId, quantity);
    }

}
