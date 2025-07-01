package com.act.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;

    @Column(length = 2000)
    private String productDescription;

    private double productDiscountedPrice;
    private double productActualPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @OrderBy("id ASC")
    private List<ImageModel> productImages;

    // Optional: Bidirectional mapping (if needed)
     @OneToMany(mappedBy = "product")
     private List<OrderDetails> orders;

    public Product() {
        // Default constructor
    }

    public Product(Long productId, String productName, String productDescription,
                   double productDiscountedPrice, double productActualPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productDiscountedPrice = productDiscountedPrice;
        this.productActualPrice = productActualPrice;
    }

    // Getters and Setters

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getProductDiscountedPrice() {
        return productDiscountedPrice;
    }

    public void setProductDiscountedPrice(double productDiscountedPrice) {
        this.productDiscountedPrice = productDiscountedPrice;
    }

    public double getProductActualPrice() {
        return productActualPrice;
    }

    public void setProductActualPrice(double productActualPrice) {
        this.productActualPrice = productActualPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ImageModel> getProductImages() {
        return productImages;
    }

    public void setProductImages(List<ImageModel> productImages) {
        this.productImages = productImages;
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

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", productDiscountedPrice=" + productDiscountedPrice +
                ", productActualPrice=" + productActualPrice +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", productImages=" + productImages +
                '}';
    }
}
