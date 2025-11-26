package com.act.ecommerce.controller;

import com.act.ecommerce.dao.AddressRepository;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.Address;
import com.act.ecommerce.entity.User;
import com.act.ecommerce.service.AddressService;
import com.act.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/profile")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/addresses")
    public List<Address> getAddresses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findById(username).get();
        if (user == null) {
            // Handle case where user is not found
            throw new RuntimeException("Authenticated user not found in database.");
        }


        return addressService.getAddressesForUser(user.getUserName());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestBody Address address) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findById(username).get();
        if (user == null) {
            // Handle case where user is not found
            throw new RuntimeException("Authenticated user not found in database.");
        }
        System.out.println(user.toString());
        addressService.addAddress(user, address);
        return ResponseEntity.ok("Address added");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Address updated) {
        addressService.updateAddress(id, updated);
        return ResponseEntity.ok("Address updated");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok("Address deleted");
    }

//    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/addresses/{id}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findById(username).get();
        if (user == null) {
            // Handle case where user is not found
            throw new RuntimeException("Authenticated user not found in database.");
        }
        addressService.setDefaultAddress(user.getUserName(), id);
        return ResponseEntity.ok("Default address set");
    }

//    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/addresses/default")
    public ResponseEntity<Address> getDefaultAddress() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findById(username).get();
        if (user == null) {
            // Handle case where user is not found
            throw new RuntimeException("Authenticated user not found in database.");
        }
        Address defaultAddress = addressService.getDefaultAddressForUser(user.getUserName());
        return ResponseEntity.ok(defaultAddress);
    }

}
