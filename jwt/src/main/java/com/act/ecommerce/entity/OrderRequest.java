package com.act.ecommerce.entity;

import java.util.List;

public class OrderRequest {

    private String fullName;

    private String fullAddress;

    private String contactNumber;

    private String alternativeContactNumber;

    private String email;


    private List<OrderProductQuantity> orderProductQuantities;

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

    public OrderRequest() {
        // Default constructor
    }
    public OrderRequest(String fullName, String fullAddress, String contactNumber, String alternativeContactNumber, String email, List<OrderProductQuantity> orderProductQuantities) {
        this.fullName = fullName;
        this.fullAddress = fullAddress;
        this.contactNumber = contactNumber;
        this.alternativeContactNumber = alternativeContactNumber;
        this.email = email;
        this.orderProductQuantities = orderProductQuantities;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderRequest)) return false;

        OrderRequest that = (OrderRequest) o;

        if (!fullName.equals(that.fullName)) return false;
        if (!fullAddress.equals(that.fullAddress)) return false;
        if (!contactNumber.equals(that.contactNumber)) return false;
        if (!alternativeContactNumber.equals(that.alternativeContactNumber)) return false;
        if (!email.equals(that.email)) return false;
        return orderProductQuantities.equals(that.orderProductQuantities);
    }




}
