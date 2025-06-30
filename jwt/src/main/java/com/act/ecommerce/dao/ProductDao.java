package com.act.ecommerce.dao;

import com.act.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDao extends JpaRepository<Product, Long> {
}
