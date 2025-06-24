package com.act.ecommerce.service;

import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    public String getAllProductSummaries() {
        List<Product> products = productDao.findAll();

        if (products.isEmpty()) {
            return "No products found in the catalog.";
        }

        return products.stream()
                .map(product ->
                        "- " + product.getProductName() + ": " +
                                product.getProductDescription() +
                                " (Price: ₹" + product.getProductActualPrice() +
                                ", Discounted Price: ₹" + product.getProductDiscountedPrice() + ")"
                )
                .collect(Collectors.joining("\n"));
    }






    public Product addNewProduct(Product product) {
        // Logic to add a new product
        // This could involve saving the product to a database, etc.
        return productDao.save(product);


    }

    public List<Product> getAllProducts() {
        // Logic to retrieve all products
        // This could involve fetching products from a database, etc.
        return productDao.findAll();
    }

    public Product getProductById(Integer id) {
        // Logic to retrieve a product by its ID
        // This could involve fetching the product from a database, etc.
        return productDao.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public void deleteProductById(Integer id) {
        // Logic to delete a product by its ID
        // This could involve removing the product from a database, etc.
        Product product = getProductById(id);
        if (product != null) {
            productDao.delete(product);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    public Product updateProductById(Integer id, Product product) {
        // Logic to update a product by its ID
        // This could involve updating the product in a database, etc.
        Product existingProduct = getProductById(id);
        if (existingProduct != null) {
            existingProduct.setProductName(product.getProductName());
            existingProduct.setProductDescription(product.getProductDescription());
            existingProduct.setProductActualPrice(product.getProductActualPrice());
            existingProduct.setProductDiscountedPrice(product.getProductDiscountedPrice());
            return productDao.save(existingProduct);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }
}
