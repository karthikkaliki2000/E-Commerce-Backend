package com.act.ecommerce.service;

import com.act.ecommerce.dao.RoleDao;
import com.act.ecommerce.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleDao roleDao;


    public Role createNewRole(Role role) {
        // Check if the role already exists
        if (roleDao.existsById(role.getRole_name())) {
            throw new IllegalArgumentException("Role with name " + role.getRole_name() + " already exists.");
        }
        // Save the new role to the database
       return roleDao.save(role);

    }
}
