package com.chenghua.service;

import com.chenghua.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(Product product);
    Optional<Product> getProductById(Long id);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String category);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}
