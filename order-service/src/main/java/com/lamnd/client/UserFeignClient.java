package com.lamnd.client;

import com.lamnd.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("http://localhost:8083/api/users")
public interface UserFeignClient {

    @GetMapping("/{id}")
    ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id);
}
