package com.act.ecommerce.service;

import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public Product getProductById(Long id) {
        // Logic to retrieve a product by its ID
        // This could involve fetching the product from a database, etc.
        return productDao.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public void deleteProductById(Long id) {
        // Logic to delete a product by its ID
        // This could involve removing the product from a database, etc.
        Product product = getProductById(id);
        if (product != null) {
            productDao.delete(product);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    public Product updateProductById(Long id, Product product) {
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

    public List<Product> getProductDetails(boolean isSingleProductCheckOut, Long productId){
        if (isSingleProductCheckOut) {
         //we are going to buy a single product
            List<Product> list=new ArrayList<>();
            Product product = getProductById(productId);
            if (product != null) {
                list.add(product);
                System.out.println("Product Details: " + product);
            } else {
                System.out.println("Product not found with id: " + productId);
            }
            return list;
        } else {
           //we are going to checkout entire cart
        }
        return new ArrayList<>(); // Return an empty list if no products are found
    }
}
