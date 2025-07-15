package com.act.ecommerce.controller;

import com.act.ecommerce.entity.Cart;
import com.act.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CartController {

    @Autowired

    private CartService cartService;

    /**
     * Adds a product to the cart for the authenticated user.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/cart/add/{productId}/{quantity}")
    public ResponseEntity<Cart> addToCart(@PathVariable Long productId, @PathVariable int quantity) {
        Cart cart = cartService.addToCart(productId, quantity);
        return ResponseEntity.ok(cart);
    }

    /**
     * Retrieves all cart items for the authenticated user.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/cart/details")
    public ResponseEntity<List<Cart>> getCartDetails() {
        List<Cart> cartDetails = cartService.getCartDetails();
        if (cartDetails == null || cartDetails.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cartDetails);
    }

    /**
     * Removes a cart item by cartId.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/cart/remove/{cartId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartId) {
        cartService.removeCartItem(cartId);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the quantity of a cart item.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/cart/update/{cartId}")
    public ResponseEntity<Cart> updateCartItemQuantity(@PathVariable Long cartId, @RequestBody Map<String, Integer> body) {
        int quantity = body.get("quantity");
        Cart updatedCart = cartService.updateCartItemQuantity(cartId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Clears all cart items for the authenticated user.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/cart/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok().build();
    }
}