package com.act.ecommerce.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="image_model")
public class ImageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;

    @Column(name = "pic_bytes", length = 50000000)
    private byte[] picBytes;

    public ImageModel() {
        // Default constructor
    }

    public ImageModel(String name, String type, byte[] picBytes) {
        this.name = name;
        this.type = type;
        this.picBytes = picBytes;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public byte[] getPicBytes() {
        return picBytes;
    }
    public void setPicBytes(byte[] picBytes) {
        this.picBytes = picBytes;
    }
    @Override
    public String toString() {
        return "ImageModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", picBytes=" + (picBytes != null ? picBytes.length : 0) +
                '}';
    }
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        uploadedAt = LocalDateTime.now();
    }


}
