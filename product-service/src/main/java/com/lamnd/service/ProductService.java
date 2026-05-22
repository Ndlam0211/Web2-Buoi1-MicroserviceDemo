package com.lamnd.service;

import com.lamnd.model.Product;
import com.lamnd.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            p.setName(productDetails.getName());
            p.setDescription(productDetails.getDescription());
            p.setPrice(productDetails.getPrice());
            p.setQuantity(productDetails.getQuantity());
            return productRepository.save(p);
        }
        return null;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public boolean decrementQuantityWithLock(Long productId, Integer quantityToDeduct) {
        try {
            // Sử dụng pessimistic lock để tránh race condition
            Product product = productRepository.findByIdWithLock(productId);

            if (product == null) {
                return false;
            }

            if (product.getQuantity() < quantityToDeduct) {
                return false;
            }

            product.setQuantity(product.getQuantity() - quantityToDeduct);
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            System.err.println("Error decrementing quantity with lock: " + e.getMessage());
            return false;
        }
    }
}
