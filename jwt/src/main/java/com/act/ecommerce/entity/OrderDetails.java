package com.act.ecommerce.entity;

import jakarta.persistence.*;

@Entity
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderId;

    private String orderFullName;

    private String orderFullAddress;

    private String orderEmail;

    private String orderPhoneNumber;
    private String orderAlternativePhoneNumber;

    private String orderStatus;

    private Double orderTotalPrice;

    @OneToOne
    private Product product;

    @OneToOne
    private User user;



   //constructor
    public OrderDetails() {
    }

    public OrderDetails(String orderFullName, String orderFullAddress, String orderEmail,
                        String orderPhoneNumber, String orderAlternativePhoneNumber,
                        String orderStatus, Double orderTotalPrice) {
        this.orderFullName = orderFullName;
        this.orderFullAddress = orderFullAddress;
        this.orderEmail = orderEmail;
        this.orderPhoneNumber = orderPhoneNumber;
        this.orderAlternativePhoneNumber = orderAlternativePhoneNumber;
        this.orderStatus = orderStatus;
        this.orderTotalPrice = orderTotalPrice;
    }

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

}
