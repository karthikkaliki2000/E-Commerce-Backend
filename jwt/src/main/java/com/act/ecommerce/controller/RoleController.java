package com.act.ecommerce.controller;

import com.act.ecommerce.entity.Role;
import com.act.ecommerce.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;


    @PostMapping({"/createNewRole"})
    public Role createNewRole(@RequestBody Role role) {

        // Validate the role object
        if (role == null || role.getRole_name() == null || role.getRole_description() == null) {
            throw new IllegalArgumentException("Role name and description must not be null");
        }

        // Create and return the new role
        return roleService.createNewRole(role);

    }
}
