package com.act.ecommerce.service;

import com.act.ecommerce.entity.Address;
import com.act.ecommerce.entity.User;
import com.act.ecommerce.dao.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    /**
     * Get all addresses for a given user
     */
    public List<Address> getAddressesForUser(String userName) {
        return addressRepository.findByUser_UserName(userName);
    }

    /**
     * Add a new address for the user
     */
    public Address addAddress(User user, Address address) {
        List<Address> existing = getAllAddressesByUserName(user.getUserName());

        // If no addresses exist, make this one default
        if (existing.isEmpty()) {
            address.setDefault(true);
        } else {
            address.setDefault(false); // optional, for clarity
        }

        address.setUser(user);
        return addressRepository.save(address);
    }


    public List<Address> getAllAddressesByUserName(String userName) {
        return addressRepository.findByUserUserName(userName);
    }

    /**
     * Update an existing address
     */
    public Address updateAddress(Long id, Address updated) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));

        // Save the existing default status before updating
        boolean currentDefaultStatus = existing.isDefault();

        existing.setLabel(updated.getLabel());
        existing.setStreet(updated.getStreet());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        existing.setPostalCode(updated.getPostalCode());
        existing.setCountry(updated.getCountry());

        // **CRITICAL FIX**: DO NOT update isDefault from the PUT/form data.
        // It must be managed ONLY by the dedicated setDefaultAddress PATCH endpoint.
        existing.setDefault(currentDefaultStatus); // Revert to the existing status

        // New fields
        existing.setFullName(updated.getFullName());
        existing.setEmail(updated.getEmail());
        existing.setContactNumber(updated.getContactNumber());
        existing.setAlternativeContactNumber(updated.getAlternativeContactNumber());

        return addressRepository.save(existing);
    }

    /**
     * Delete an address by ID
     */
    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new RuntimeException("Address not found with id: " + id);
        }
        addressRepository.deleteById(id);
    }

    @Transactional
    public void setDefaultAddress(String userName, Long addressId) {
        // This method correctly unsets all others and sets the target one.
        List<Address> addresses = addressRepository.findByUser_UserName(userName);
        for (Address addr : addresses) {
            addr.setDefault(addr.getId().equals(addressId));
        }
        addressRepository.saveAll(addresses);
    }


    public Optional<Address> getDefaultAddress(String userName) {
        return addressRepository.findByUser_UserName(userName).stream()
                .filter(Address::isDefault)
                .findFirst();
    }

    public Optional<Address> getAddressByIdForUser(Long id, String userName) {
        return addressRepository.findById(id)
                .filter(addr -> addr.getUser().getUserName().equals(userName));
    }

    public Address getDefaultAddressForUser(String username) {
        return addressRepository.findByUserUserNameAndIsDefaultTrue(username)
                .orElseThrow(() -> new RuntimeException("No default address found"));
    }


}