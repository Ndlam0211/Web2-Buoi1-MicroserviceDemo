package com.lamnd.service;

import com.lamnd.client.ProductServiceClient;
import com.lamnd.dto.ProductDTO;
import com.lamnd.model.Order;
import com.lamnd.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order) {
        // Get product information from Product Service
        ProductDTO product = productServiceClient.getProductById(order.getProductId());

        if (product == null) {
            throw new RuntimeException("Product not found with id: " + order.getProductId());
        }

        // Check if product has enough quantity
        if (product.getQuantity() < order.getQuantity()) {
            throw new RuntimeException("Insufficient product quantity. Available: " + product.getQuantity());
        }

        // Calculate total price
        Double totalPrice = product.getPrice() * order.getQuantity();
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");

        // Save order to database
        Order savedOrder = orderRepository.save(order);

        // Update product quantity after successful order creation
        boolean updateSuccess = productServiceClient.updateProductQuantity(order.getProductId(), order.getQuantity());

        if (!updateSuccess) {
            System.err.println("Warning: Failed to update product quantity for product ID: " + order.getProductId());
        }

        return savedOrder;
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order o = order.get();
            o.setProductId(orderDetails.getProductId());
            o.setQuantity(orderDetails.getQuantity());
            o.setTotalPrice(orderDetails.getTotalPrice());
            o.setStatus(orderDetails.getStatus());
            return orderRepository.save(o);
        }
        return null;
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}


