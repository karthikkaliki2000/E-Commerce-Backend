package com.act.ecommerce.controller;

import com.act.ecommerce.entity.User;
import com.act.ecommerce.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // Centralizing API paths for better organization
public class UserController {

    @Autowired
    private UserService userService;

    @PostConstruct
    public void initRolesAndUsers() {
        // Initialize roles and users at startup if necessary
        userService.initRolesAndUsers();
    }

    @PostMapping("/register")
    public User registerNewUser(@RequestBody User user) {
        // Enhanced validation for null values and role assignments
        if (user.getUserName() == null || user.getUserPassword() == null) {
            throw new IllegalArgumentException("Username, password, and roles must not be null or empty.");
        }

        return userService.registerNewUser(user);
    }

    @GetMapping("/forUser")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String forUser() {
        return "This is restricted to users with ROLE_USER.";
    }

    @GetMapping("/forAdmin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String forAdmin() {

        return "This is restricted to administrators with ROLE_ADMIN.";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String forAllUsers() {
        return "Accessible by both ROLE_USER and ROLE_ADMIN.";
    }

}
