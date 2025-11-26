package com.act.ecommerce.dao;

import com.act.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser_UserName(String userName);
    Optional<Address> findByUserUserNameAndIsDefaultTrue(String username);

    List<Address> findByUserUserName(String userName);
}
