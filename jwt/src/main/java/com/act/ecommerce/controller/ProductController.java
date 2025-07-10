package com.act.ecommerce.controller;

import com.act.ecommerce.entity.ImageModel;
import com.act.ecommerce.entity.Product;
import com.act.ecommerce.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.act.ecommerce.dao.ProductDao productDao;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = {"/product/add"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Product addNewProduct(@RequestPart("product") Product product, @RequestPart("imagefiles") MultipartFile[] imageFiles) {
        // Convert uploaded files to List<ImageModel>
        List<ImageModel> imageModels = uploadImageFiles(imageFiles);
        product.setProductImages(imageModels);

        if (imageModels.isEmpty()) {
            throw new RuntimeException("No images uploaded for the product");
        }
        if (product.getProductName() == null || product.getProductName().isEmpty()) {
            throw new RuntimeException("Product name cannot be empty");
        }
        return productService.addNewProduct(product);
    }

    public List<ImageModel> uploadImageFiles(MultipartFile[] imageFiles) {
        List<ImageModel> imageModels = new ArrayList<>();
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

    @GetMapping("/product/all")
    public List<Product> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(defaultValue = "") String searchKey) {


        return productService.getAllProducts(pageNumber,pageSize, searchKey);
    }


    @GetMapping({"/product/{productId}"})
    public Product getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping({"/product/delete/{id}"})
    public void deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = {"/product/update/{id}"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Product updateProductById(
            @PathVariable Long id,
            @RequestPart("product") Product product,
            @RequestPart(value = "imagefiles", required = false) MultipartFile[] imageFiles) {

        if (imageFiles != null && imageFiles.length > 0) {
            List<ImageModel> imageModels = uploadImageFiles(imageFiles);
            product.setProductImages(imageModels);
        }
        return productService.updateProductById(id, product);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/product/details")
    public List<Product> getProductDetails(
            @RequestParam boolean isSingleProductCheckOut,
            @RequestParam(required = false) Long productId) {




        return productService.getProductDetails(isSingleProductCheckOut, productId);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/api/product/{productId}/main-image")
    public ResponseEntity<byte[]> getMainProductImage(@PathVariable Long productId) {
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        if (product.getProductImages() == null || product.getProductImages().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ImageModel mainImage = product.getProductImages().get(0);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // or detect type
                .body(mainImage.getPicBytes());
    }

}