package com.lamnd.service;

import com.lamnd.client.ProductServiceClient;
import com.lamnd.client.UserServiceClient;
import com.lamnd.dto.OrderResponse;
import com.lamnd.dto.ProductDTO;
import com.lamnd.dto.UserDTO;
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

    @Autowired
    private UserServiceClient userServiceClient;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public OrderResponse createOrder(Order order) {
        // Get product information from Product Service
        ProductDTO product = productServiceClient.getProductById(order.getProductId());

        if (product == null) {
            throw new RuntimeException("Product not found with id: " + order.getProductId());
        }

        // Get user information from User Service
        UserDTO user = userServiceClient.getUserById(order.getUserId());

        if (user == null) {
            throw new RuntimeException("User not found with id: " + order.getUserId());
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

        // Update product quantity using pessimistic lock to prevent race condition
        boolean updateSuccess = productServiceClient.decrementQuantityWithLock(
            order.getProductId(),
            order.getQuantity()
        );

        if (!updateSuccess) {
            throw new RuntimeException("Failed to decrement product quantity. Please try again.");
        }

        // Build and return OrderResponse with complete information
        return new OrderResponse(
            savedOrder.getId(),
            product,
            user,
            savedOrder.getQuantity(),
            savedOrder.getTotalPrice(),
            savedOrder.getStatus()
        );
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order o = order.get();
            o.setProductId(orderDetails.getProductId());
            o.setUserId(orderDetails.getUserId());
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
