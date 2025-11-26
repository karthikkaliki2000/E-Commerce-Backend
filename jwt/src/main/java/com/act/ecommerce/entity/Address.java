package com.act.ecommerce.entity;

import jakarta.persistence.*;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label; // e.g., "Home", "Office"
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isDefault;

    private String fullName;
    private String email;
    private String contactNumber;
    private String alternativeContactNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_name")
    private User user;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAlternativeContactNumber() { return alternativeContactNumber; }
    public void setAlternativeContactNumber(String alternativeContactNumber) { this.alternativeContactNumber = alternativeContactNumber; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
