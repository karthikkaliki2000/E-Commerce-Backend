package com.act.ecommerce.service;

import com.act.ecommerce.configuration.JwtRequestFilter;
import com.act.ecommerce.dao.CartDao;
import com.act.ecommerce.dao.ProductDao;
import com.act.ecommerce.dao.UserDao;
import com.act.ecommerce.entity.Cart;
import com.act.ecommerce.entity.Product;
import com.act.ecommerce.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private CartDao cartDao;

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

    public List<Product> getProductDetails(boolean isSingleProductCheckOut, Long productId) {
        if (isSingleProductCheckOut && productId != null && productId > 0L) {

            return getSingleProductDetails(productId);
        }
        else{

            return getCartProductDetails();
        }

    }

    private List<Product> getSingleProductDetails(Long productId) {


        Product product = getProductById(productId);
        if (Objects.nonNull(product)) {

            List<Product> productList = new ArrayList<>();
            productList.add(product);
            return productList;
        }


        return Collections.emptyList();
    }

    private List<Product> getCartProductDetails() {
        String currentUser = jwtRequestFilter.CURRENT_USER;

        if (currentUser == null || currentUser.isBlank()) {

            throw new IllegalArgumentException("Current user is not authenticated 172");
        }

        User user = userDao.findById(currentUser)
                .orElseThrow(() -> new RuntimeException("User not found with username:  176" + currentUser));

        List<Cart> cartList = cartDao.findByUser(user);

        if (cartList == null || cartList.isEmpty()) {

            return Collections.emptyList();
        }


        return cartList.stream()
                .map(Cart::getProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
