package com.act.ecommerce.entity;

public class TransactionDetails {
    private String orderId;
    private String currency;
    private Integer amount;

    private String receipt;
    private String status;

    private String key;

    //constructor
    public TransactionDetails() {
    }
    public TransactionDetails(String orderId, String currency, Integer amount, String receipt, String status) {
        this.orderId = orderId;
        this.currency = currency;
        this.amount = amount;

        this.receipt = receipt;
        this.status = status;
    }

    public TransactionDetails(String orderId, String currency, Integer amount, String receipt, String status, String key) {
        this.orderId = orderId;
        this.currency = currency;
        this.amount = amount;

        this.receipt = receipt;
        this.status = status;
        this.key = key;
    }
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public Integer getAmount() {
        return amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getReceipt() {
        return receipt;
    }
    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    @Override
    public String toString() {
        return "TransactionDetails{" +
                "orderId='" + orderId + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +

                ", receipt='" + receipt + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionDetails)) return false;

        TransactionDetails that = (TransactionDetails) o;

        if (!orderId.equals(that.orderId)) return false;
        if (!currency.equals(that.currency)) return false;
        if (!amount.equals(that.amount)) return false;

        if (!receipt.equals(that.receipt)) return false;
        return status.equals(that.status);
    }


}
