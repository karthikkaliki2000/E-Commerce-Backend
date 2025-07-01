package com.act.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String orderFullName;
    private String orderFullAddress;
    private String orderEmail;
    private String orderPhoneNumber;
    private String orderAlternativePhoneNumber;
    private String orderStatus;
    private Double orderTotalPrice;

    @ManyToOne
    @JoinColumn(name = "product_product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_user_name")
    private User user;

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
    public OrderDetails() {}

    public OrderDetails(String orderFullName, String orderFullAddress, String orderEmail,
                        String orderPhoneNumber, String orderAlternativePhoneNumber,
                        String orderStatus, Double orderTotalPrice,
                        Product product, User user) {
        this.orderFullName = orderFullName;
        this.orderFullAddress = orderFullAddress;
        this.orderEmail = orderEmail;
        this.orderPhoneNumber = orderPhoneNumber;
        this.orderAlternativePhoneNumber = orderAlternativePhoneNumber;
        this.orderStatus = orderStatus;
        this.orderTotalPrice = orderTotalPrice;
        this.product = product;
        this.user = user;
    }

    public OrderDetails(Long orderId, String orderFullName, String orderFullAddress, String orderEmail,
                        String orderPhoneNumber, String orderAlternativePhoneNumber,
                        String orderStatus, Double orderTotalPrice,
                        Product product, User user) {
        this(orderFullName, orderFullAddress, orderEmail, orderPhoneNumber,
                orderAlternativePhoneNumber, orderStatus, orderTotalPrice, product, user);
        this.orderId = orderId;
    }

    // Getters and Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderFullName() {
        return orderFullName;
    }

    public void setOrderFullName(String orderFullName) {
        this.orderFullName = orderFullName;
    }

    public String getOrderFullAddress() {
        return orderFullAddress;
    }

    public void setOrderFullAddress(String orderFullAddress) {
        this.orderFullAddress = orderFullAddress;
    }

    public String getOrderEmail() {
        return orderEmail;
    }

    public void setOrderEmail(String orderEmail) {
        this.orderEmail = orderEmail;
    }

    public String getOrderPhoneNumber() {
        return orderPhoneNumber;
    }

    public void setOrderPhoneNumber(String orderPhoneNumber) {
        this.orderPhoneNumber = orderPhoneNumber;
    }

    public String getOrderAlternativePhoneNumber() {
        return orderAlternativePhoneNumber;
    }

    public void setOrderAlternativePhoneNumber(String orderAlternativePhoneNumber) {
        this.orderAlternativePhoneNumber = orderAlternativePhoneNumber;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getOrderTotalPrice() {
        return orderTotalPrice;
    }

    public void setOrderTotalPrice(Double orderTotalPrice) {
        this.orderTotalPrice = orderTotalPrice;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "orderId=" + orderId +
                ", orderFullName='" + orderFullName + '\'' +
                ", orderFullAddress='" + orderFullAddress + '\'' +
                ", orderEmail='" + orderEmail + '\'' +
                ", orderPhoneNumber='" + orderPhoneNumber + '\'' +
                ", orderAlternativePhoneNumber='" + orderAlternativePhoneNumber + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderTotalPrice=" + orderTotalPrice +
                ", product=" + product +
                ", user=" + user +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
