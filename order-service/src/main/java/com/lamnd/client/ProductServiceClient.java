package com.lamnd.client;

import com.lamnd.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081/api/products";

    public ProductDTO getProductById(Long productId) {
        try {
            return restTemplate.getForObject(PRODUCT_SERVICE_URL + "/" + productId, ProductDTO.class);
        } catch (Exception e) {
            System.err.println("Error calling Product Service: " + e.getMessage());
            return null;
        }
    }

    public boolean updateProductQuantity(Long productId, Integer quantityToDeduct) {
        try {
            ProductDTO product = getProductById(productId);
            if (product == null) {
                return false;
            }

            // Calculate new quantity
            Integer newQuantity = product.getQuantity() - quantityToDeduct;
            product.setQuantity(newQuantity);

            // Update product via PUT request
            restTemplate.put(PRODUCT_SERVICE_URL + "/" + productId, product);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating product quantity: " + e.getMessage());
            return false;
        }
    }
}