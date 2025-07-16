package com.act.ecommerce.entity;

import java.util.List;
import java.util.Objects;

public class OrderRequest {

    private String fullName;
    private String fullAddress;
    private String contactNumber;
    private String alternativeContactNumber;
    private String email;
    private String transactionId;
    private List<OrderProductQuantity> orderProductQuantities;

    // Constructors
    public OrderRequest() {}

    public OrderRequest(String fullName, String fullAddress, String contactNumber,
                        String alternativeContactNumber, String email,
                        List<OrderProductQuantity> orderProductQuantities) {
        this.fullName = fullName;
        this.fullAddress = fullAddress;
        this.contactNumber = contactNumber;
        this.alternativeContactNumber = alternativeContactNumber;
        this.email = email;
        this.orderProductQuantities = orderProductQuantities;
    }

    public OrderRequest(String fullName, String fullAddress, String contactNumber,
                        String alternativeContactNumber, String email,
                        List<OrderProductQuantity> orderProductQuantities, String transactionId) {
        this(fullName, fullAddress, contactNumber, alternativeContactNumber, email, orderProductQuantities);
        this.transactionId = transactionId;
    }

    // Getters and Setters

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAlternativeContactNumber() {
        return alternativeContactNumber;
    }

    public void setAlternativeContactNumber(String alternativeContactNumber) {
        this.alternativeContactNumber = alternativeContactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrderProductQuantity> getOrderProductQuantities() {
        return orderProductQuantities;
    }

    public void setOrderProductQuantities(List<OrderProductQuantity> orderProductQuantities) {
        this.orderProductQuantities = orderProductQuantities;
    }

    // Object Overrides
    @Override
    public String toString() {
        return "OrderRequest{" +
                "fullName='" + fullName + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", alternativeContactNumber='" + alternativeContactNumber + '\'' +
                ", email='" + email + '\'' +
                ", orderProductQuantities=" + orderProductQuantities +
                '}';
    }



    @Override
    public int hashCode() {
        return Objects.hash(fullName, fullAddress, contactNumber, alternativeContactNumber, email, orderProductQuantities);
    }
}
