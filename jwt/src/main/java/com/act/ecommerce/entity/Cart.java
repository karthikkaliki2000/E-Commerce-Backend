package com.act.ecommerce.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @OneToOne
    private Product product;

    @OneToOne
    private User user;

    private int quantity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Cart() {}
    public Cart(Product product, User user, int quantity) {
        this.product = product;
        this.user = user;
        this.quantity = quantity;
    }
    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }
    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", product=" + product +
                ", user=" + user +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


}
