package com.lamnd.service;

import com.lamnd.client.ProductServiceClient;
import com.lamnd.client.UserServiceClient;
import com.lamnd.dto.*;
import com.lamnd.model.Order;
import com.lamnd.model.OrderDetail;
import com.lamnd.repository.OrderRepository;
import com.lamnd.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

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

    /**
     * Tạo đơn hàng với nhiều sản phẩm cùng lúc
     * @param request CreateOrderRequest chứa userId và danh sách sản phẩm
     * @return OrderResponse chứa thông tin đầy đủ về đơn hàng
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Validate request
        if (request.getUserId() == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("User ID and items list are required");
        }

        // Get user information from User Service
        UserDTO user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + request.getUserId());
        }

        // Process each item in the order
        List<OrderItemResponse> orderItems = new ArrayList<>();
        List<OrderDetail> orderDetails = new ArrayList<>();
        Double totalPrice = 0.0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // Get product information from Product Service
            ProductDTO product = productServiceClient.getProductById(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found with id: " + itemRequest.getProductId());
            }

            // Check if product has enough quantity
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient product quantity for product '" + product.getName()
                    + "'. Available: " + product.getQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            // Calculate item total price
            Double itemTotalPrice = product.getPrice() * itemRequest.getQuantity();
            totalPrice += itemTotalPrice;

            // Create OrderItemResponse
            OrderItemResponse itemResponse = new OrderItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                itemRequest.getQuantity(),
                itemTotalPrice
            );
            orderItems.add(itemResponse);

            // Create OrderDetail object (will be saved after Order is created)
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(product.getId());
            orderDetail.setProductName(product.getName());
            orderDetail.setPrice(product.getPrice());
            orderDetail.setQuantity(itemRequest.getQuantity());
            orderDetail.setItemTotalPrice(itemTotalPrice);
            orderDetails.add(orderDetail);
        }

        // Create and save order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);

        // Set order reference for each OrderDetail and save
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }

        // Update product quantities using pessimistic lock
        for (OrderItemRequest itemRequest : request.getItems()) {
            boolean updateSuccess = productServiceClient.decrementQuantityWithLock(
                itemRequest.getProductId(),
                itemRequest.getQuantity()
            );

            if (!updateSuccess) {
                throw new RuntimeException("Failed to decrement quantity for product id: "
                    + itemRequest.getProductId() + ". Please try again.");
            }
        }

        // Build and return OrderResponse with complete information
        return new OrderResponse(
            savedOrder.getId(),
            user,
            orderItems,
            savedOrder.getTotalPrice(),
            savedOrder.getStatus()
        );
    }

    /**
     * Cập nhật đơn hàng
     */
    public Order updateOrder(Long id, Order orderDetails) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order o = order.get();
            o.setUserId(orderDetails.getUserId());
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
