package com.act.ecommerce.service;

import com.act.ecommerce.dao.RoleDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.Role;
import com.act.ecommerce.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(User user) {
        validateUser(user); // Centralized validation

        // Assign default ROLE_USER if no role is set
        Role defaultRole = roleDao.findById("ROLE_USER")
                .orElseThrow(() -> new IllegalArgumentException("Role ROLE_USER does not exist."));
        user.setRoles(Set.of(defaultRole));

        // Check if the user already exists
        if (userDao.existsById(user.getUserName())) {
            throw new IllegalArgumentException("User with username " + user.getUserName() + " already exists.");
        }

        // Encode password before saving
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));

        logger.info("Registering new user: {}", user.getUserName());
        return userDao.save(user);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User object must not be null.");
        }

        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty.");
        }

        if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty.");
        }

        if (user.getUserFirstName() == null || user.getUserFirstName().isEmpty()) {
            throw new IllegalArgumentException("User first name must not be null or empty.");
        }

        if (user.getUserLastName() == null || user.getUserLastName().isEmpty()) {
            throw new IllegalArgumentException("User last name must not be null or empty.");
        }
    }

    public void initRolesAndUsers() {
        logger.info("Initializing roles and users...");

        Role adminRole = saveRole("ROLE_ADMIN", "Administrator role with full access");
        Role userRole = saveRole("ROLE_USER", "Regular user role with limited access");

        User adminUser = new User();
        adminUser.setUserName("admin");
        adminUser.setUserFirstName("Admin");
        adminUser.setUserLastName("User");
        adminUser.setUserPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(Set.of(adminRole));

        if (!userDao.existsById(adminUser.getUserName())) {
            userDao.save(adminUser);
            logger.info("Admin user created successfully.");
        } else {
            logger.warn("Admin user already exists, skipping creation.");
        }
    }

    private Role saveRole(String roleName, String description) {
        return roleDao.findById(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRole_name(roleName);
                    role.setRole_description(description);
                    return roleDao.save(role);
                });
    }
}
