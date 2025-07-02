package com.act.ecommerce.service;

import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public List<Product> getAllProducts(int pageNumber, int pageSize, String searchKey) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Product> productPage;
        List<Product> products;

        boolean hasSearch = searchKey != null && !searchKey.trim().isEmpty();

        if (hasSearch) {
            String search = searchKey.trim().toLowerCase();
            productPage = productDao.findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
                    search, search, pageable);
        } else {
            productPage = productDao.findAll(pageable);
        }

        products = productPage.getContent();
       // logPaginationMetadata(productPage);
        System.out.println("Response from ProductService: " + products.toArray().length);
        return products;
    }

    private void logPaginationMetadata(Page<Product> page) {
        if (page.isEmpty()) {
            System.out.println("No products found in the catalog.");
        } else {
            System.out.printf("📦 Total: %d | Pages: %d | Current: %d | PageSize: %d%n",
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.getNumber(),
                    page.getSize());
        }
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
        Product existingProduct = getProductById(id);
        if (existingProduct != null) {
            existingProduct.setProductName(product.getProductName());
            existingProduct.setProductDescription(product.getProductDescription());
            existingProduct.setProductActualPrice(product.getProductActualPrice());
            existingProduct.setProductDiscountedPrice(product.getProductDiscountedPrice());

            // Handle images only if provided in the update request
            if (product.getProductImages() != null) {

                // Clear existing images if new ones are provided
            if (existingProduct.getProductImages() != null) {
                existingProduct.getProductImages().clear();
            }
                existingProduct.getProductImages().addAll(product.getProductImages());
            }
            // If product.getProductImages() is null, do not change images
                System.out.println("Updating product with ID: in Service--------------> " + product.getProductImages());
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
