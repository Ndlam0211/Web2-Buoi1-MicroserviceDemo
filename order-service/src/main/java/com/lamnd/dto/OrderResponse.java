package com.lamnd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private ProductDTO product;
    private UserDTO user;
    private Integer quantity;
    private Double totalPrice;
    private String status;
}

