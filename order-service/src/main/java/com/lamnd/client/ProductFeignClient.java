package com.lamnd.client;

import com.lamnd.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("http://localhost:8081/api/products")
public interface ProductFeignClient {

    @GetMapping("/{productId}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable("productId") Long productId);

    @PostMapping("/{productId}/decrement-with-lock")
    ResponseEntity<Boolean> decrementQuantityWithLock(@PathVariable("productId") Long productId,@RequestBody Integer quantityToDeduct);
}
