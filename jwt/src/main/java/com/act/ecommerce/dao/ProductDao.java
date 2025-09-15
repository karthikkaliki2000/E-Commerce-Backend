package com.act.ecommerce.dao;

import com.act.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDao extends JpaRepository<Product, Long> {
 public Page<Product> findAll(Pageable pageable);

 Page<Product> findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
         String name, String description, Pageable pageable);


    boolean existsByProductName(String productName);
}
