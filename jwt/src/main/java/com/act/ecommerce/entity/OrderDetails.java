package com.act.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "user_user_name")
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OrderDetails(String fullName, String fullAddress, String email, String contactNumber, String alternativeContactNumber, String placed, double totalOrderPrice, User user) {
        this.orderFullName = fullName;
        this.orderFullAddress = fullAddress;
        this.orderEmail = email;
        this.orderPhoneNumber = contactNumber;
        this.orderAlternativePhoneNumber = alternativeContactNumber;
        this.orderStatus = placed;
        this.orderTotalPrice = totalOrderPrice;
        this.user = user;
    }

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


    public OrderDetails() {
    }
    public OrderDetails(String orderFullName, String orderFullAddress, String orderEmail,
                        String orderPhoneNumber, String orderAlternativePhoneNumber, String orderStatus,
                        Double orderTotalPrice, List<Product> products, User user) {
        this.orderFullName = orderFullName;
        this.orderFullAddress = orderFullAddress;
        this.orderEmail = orderEmail;
        this.orderPhoneNumber = orderPhoneNumber;
        this.orderAlternativePhoneNumber = orderAlternativePhoneNumber;
        this.orderStatus = orderStatus;
        this.orderTotalPrice = orderTotalPrice;
        this.products = products;
        this.user = user;
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


    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
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

    public List<OrderItem> getItems() {
        return items;
    }
    public void setItems(List<OrderItem> items) {
        this.items = items;
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
                ", products=" + products +
                ", user=" + user +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
