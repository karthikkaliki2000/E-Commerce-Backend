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

@Service
public class CartService {

    @Autowired
    CartDao cartDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Autowired
    UserDao userDao;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CartService.class);

    public Cart addToCart(Long productId, int quantity) {

        //if cart is not exists then create a new cart


        Product product = productDao.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        logger.info("Adding product to cart: " + product.getProductName());
        String currentUser = jwtRequestFilter.CURRENT_USER;
        if (currentUser == null || currentUser.isEmpty()) {
            throw new IllegalArgumentException("Current user is not authenticated");
        }
        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + currentUser));


        Cart existingCart = cartDao.findByUserAndProduct(user, product);
        if (existingCart != null) {
            // If the product already exists in the cart, update the quantity
            existingCart.setQuantity(quantity);
            return cartDao.save(existingCart);
        }
        // Logic to add the product to the user's cart
        if(product!=null && user!=null) {
            // Assuming CartDao has a method to add product to user's cart


            Cart cart=new Cart(
                    product,
                    user,
                    quantity
            );


            return cartDao.save(cart);
        } else {
            throw new IllegalArgumentException("Invalid product or user");
        }





    }
}
