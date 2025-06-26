package com.act.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Role {

    @Id
    private String role_name;

    private  String role_description;

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public String getRole_description() {
        return role_description;
    }

    public void setRole_description(String role_description) {
        this.role_description = role_description;
    }

    @Override
    public String toString() {
        return "Role{" +
                "role_name='" + role_name + '\'' +
                ", role_description='" + role_description + '\'' +
                '}';
    }


}
