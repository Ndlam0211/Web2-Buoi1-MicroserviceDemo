package com.lamnd.client;

import com.lamnd.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8083/api/users";

    /**
     * Lấy thông tin user từ User Service
     * @param userId ID của user cần lấy
     * @return UserDTO chứa thông tin user, null nếu user không tồn tại
     */
    public UserDTO getUserById(Long userId) {
        try {
            return restTemplate.getForObject(USER_SERVICE_URL + "/" + userId, UserDTO.class);
        } catch (Exception e) {
            System.err.println("Error calling User Service: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra user có tồn tại hay không
     * @param userId ID của user cần kiểm tra
     * @return true nếu user tồn tại, false nếu không
     */
    public boolean userExists(Long userId) {
        try {
            UserDTO user = restTemplate.getForObject(USER_SERVICE_URL + "/" + userId, UserDTO.class);
            return user != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tạo user mới
     * @param user UserDTO chứa thông tin user mới
     * @return UserDTO của user vừa tạo, null nếu tạo thất bại
     */
    public UserDTO createUser(UserDTO user) {
        try {
            return restTemplate.postForObject(USER_SERVICE_URL, user, UserDTO.class);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cập nhật thông tin user
     * @param userId ID của user cần cập nhật
     * @param userDetails UserDTO chứa thông tin mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateUser(Long userId, UserDTO userDetails) {
        try {
            restTemplate.put(USER_SERVICE_URL + "/" + userId, userDetails);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa user
     * @param userId ID của user cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteUser(Long userId) {
        try {
            restTemplate.delete(USER_SERVICE_URL + "/" + userId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}
