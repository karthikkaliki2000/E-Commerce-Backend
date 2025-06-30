package com.act.ecommerce.controller;

import com.act.ecommerce.entity.ImageModel;
import com.act.ecommerce.entity.Product;
import com.act.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = {"/product/add"},consumes={
        MediaType.MULTIPART_FORM_DATA_VALUE})
    public Product addNewProduct(@RequestPart("product") Product product, @RequestPart("imagefiles") MultipartFile[] imageFiles ) {
        // Logic to add a new product
        // This could involve saving the product to a database, etc.
        Set<ImageModel> imageModels = uploadImageFiles(imageFiles);
        product.setProductImages(imageModels);
        // Assuming productService has a method to handle the product addition
        if (imageModels.isEmpty()) {
            throw new RuntimeException("No images uploaded for the product");
        }
        if (product.getProductName() == null || product.getProductName().isEmpty()) {
            throw new RuntimeException("Product name cannot be empty");
        }
        return productService.addNewProduct(product);

    }


    public Set<ImageModel> uploadImageFiles(MultipartFile[] imageFiles) {
        // Logic to handle image file uploads
        // This could involve saving the files to a server or cloud storage
        Set<ImageModel> imageModels = new HashSet<>();
        for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                ImageModel imageModel = new ImageModel();
                imageModel.setName(file.getOriginalFilename());
                imageModel.setType(file.getContentType());
                try {
                    imageModel.setPicBytes(file.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageModels.add(imageModel);
            }

        }
        return imageModels;
    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping({"/product/all"})
    public List<Product> getAllProducts() {
        // Logic to retrieve all products
        // This could involve fetching products from a database
        return productService.getAllProducts();
    }


    @GetMapping({"/product/{productId}"})
    public Product getProductById(@PathVariable Integer productId) {
        // Logic to retrieve a product by its ID
        // This could involve fetching the product from a database
        return productService.getProductById(productId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping({"/product/delete/{id}"})
    public void deleteProductById(@PathVariable Integer id) {
        // Logic to delete a product by its ID
        // This could involve removing the product from a database
        productService.deleteProductById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping({"/product/update/{id}"})
    public Product updateProductById(@PathVariable Integer id, @RequestBody Product product) {
        // Logic to update a product by its ID
        // This could involve updating the product in a database
        return productService.updateProductById(id, product);
    }

}
