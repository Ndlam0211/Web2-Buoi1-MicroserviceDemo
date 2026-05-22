# Microservice Demo - Hướng Dẫn Triển Khai

## Tổng Quan
Project này triển khai 3 microservices: Order Service, Product Service, và User Service với các tính năng:
- RestTemplate để gọi giữa các service
- Pessimistic Lock để xử lý race condition
- Response đầy đủ thông tin khi đặt hàng

## Cấu Hình Cổng

| Service | Port | Database |
|---------|------|----------|
| Order Service | 8080 | order_microdemo |
| Product Service | 8081 | product_microdemo |
| User Service | 8083 | user_microdemo |

## Thành Phần Chính

### 1. Product Service (Port 8081)

**Pessimistic Lock Implementation:**
- `ProductRepository.findByIdWithLock()`: Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` để khóa hàng trong database
- `ProductService.decrementQuantityWithLock()`: Giảm số lượng sản phẩm với khóa
- `ProductController.POST /api/products/{id}/decrement-with-lock`: Endpoint xử lý giảm số lượng

**Cách hoạt động Pessimistic Lock:**
```
Khi nhiều request cùng lúc muốn update một sản phẩm:
1. Request 1 khóa hàng (PESSIMISTIC_WRITE)
2. Request 2 phải chờ cho đến khi Request 1 hoàn thành
3. Đảm bảo không có race condition
4. Số lượng sản phẩm luôn chính xác
```

### 2. Order Service (Port 8080)

**Tính năng:**
- Gọi Product Service để lấy thông tin sản phẩm
- Gọi User Service để lấy thông tin người dùng
- Sử dụng pessimistic lock khi giảm số lượng sản phẩm
- Trả về `OrderResponse` chứa đầy đủ thông tin

**Flow tạo đơn hàng:**
```
1. Kiểm tra sản phẩm tồn tại (gọi Product Service)
2. Kiểm tra người dùng tồn tại (gọi User Service)
3. Kiểm tra số lượng có đủ
4. Lưu đơn hàng vào database
5. Giảm số lượng sản phẩm với pessimistic lock
6. Trả về OrderResponse với thông tin đầy đủ
```

### 3. User Service (Port 8083)

**Tính năng:**
- CRUD User
- API endpoints: GET, POST, PUT, DELETE

## API Endpoints

### Order Service
```
POST /api/orders
Request Body:
{
  "productId": 1,
  "userId": 1,
  "quantity": 2
}

Response (OrderResponse):
{
  "orderId": 1,
  "product": {
    "id": 1,
    "name": "Laptop",
    "price": 1000.0,
    "quantity": 8,
    "description": "High performance laptop"
  },
  "user": {
    "id": 1,
    "name": "John",
    "email": "john@example.com",
    "phone": "0123456789",
    "address": "123 Street"
  },
  "quantity": 2,
  "totalPrice": 2000.0,
  "status": "PENDING"
}
```

### Product Service
```
GET /api/products/{id} - Lấy thông tin sản phẩm
POST /api/products/{id}/decrement-with-lock - Giảm số lượng với lock
Request Body: quantity (Integer)
```

### User Service
```
GET /api/users/{id} - Lấy thông tin user
POST /api/users - Tạo user
PUT /api/users/{id} - Cập nhật user
DELETE /api/users/{id} - Xóa user
```

## Giải Quyết Race Condition

### Vấn Đề:
Khi có 2 người dùng cùng mua một sản phẩm có 10 cái:
- User 1 mua 7 cái
- User 2 mua 5 cái
- Mà không có lock thì cả 2 đều thành công (sai, còn -2 cái)

### Giải Pháp - Pessimistic Lock:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Product findByIdWithLock(@Param("id") Long id);
```

**Ưu điểm:**
- ✅ Đơn giản dễ hiểu
- ✅ 100% đảm bảo tính nhất quán
- ✅ Phù hợp cho hệ thống có throughput trung bình

**Nhược điểm:**
- ❌ Có thể gây deadlock
- ❌ Performance giảm với traffic cao
- ❌ Khóa lâu → throughput thấp

### Các giải pháp khác:
1. **Optimistic Lock** - Sử dụng version field, phù hợp khi xung đột ít
2. **Distributed Lock** - Sử dụng Redis, phù hợp cho hệ thống phân tán lớn
3. **Event Sourcing** - Lưu lại tất cả sự thay đổi, phù hợp khi cần audit trail

## Cách Chạy Project

1. **Tạo Database:**
```sql
CREATE DATABASE order_microdemo;
CREATE DATABASE product_microdemo;
CREATE DATABASE user_microdemo;
```

2. **Run các service:**
```
# Terminal 1 - Product Service
cd product-service
mvn spring-boot:run

# Terminal 2 - User Service
cd user-service
mvn spring-boot:run

# Terminal 3 - Order Service
cd order-service
mvn spring-boot:run
```

3. **Test API với Postman:**
```
1. Tạo User: POST http://localhost:8083/api/users
2. Tạo Product: POST http://localhost:8081/api/products
3. Tạo Order: POST http://localhost:8080/api/orders
```

## Các File Đã Sửa/Tạo

### Order Service
- ✅ ProductServiceClient.java - Sửa lỗi BOM, thêm decrementQuantityWithLock()
- ✅ UserServiceClient.java - Tạo mới
- ✅ UserDTO.java - Tạo mới
- ✅ OrderResponse.java - Tạo mới
- ✅ Order.java - Thêm userId
- ✅ OrderService.java - Cập nhật createOrder() với pessimistic lock
- ✅ OrderController.java - Cập nhật trả về OrderResponse
- ✅ application.yml - Cập nhật port thành 8080

### Product Service
- ✅ ProductRepository.java - Thêm findByIdWithLock()
- ✅ ProductService.java - Thêm decrementQuantityWithLock()
- ✅ ProductController.java - Thêm endpoint decrement-with-lock

### User Service
- ✅ User.java - Tạo mới
- ✅ UserRepository.java - Tạo mới
- ✅ UserService.java - Tạo mới
- ✅ UserController.java - Tạo mới
- ✅ RestTemplateConfig.java - Tạo mới

## Notes

- Tất cả services sử dụng MySQL database
- Hibernage tự động tạo bảng với `ddl-auto: update`
- RestTemplate được cấu hình để gọi giữa các service
- Pessimistic lock được triển khai để tránh race condition

