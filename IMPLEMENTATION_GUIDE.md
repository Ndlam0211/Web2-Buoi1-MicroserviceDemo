# Microservice Demo - Hướng Dẫn Triển Khai

## Tổng Quan
Project này triển khai 3 microservices: Order Service, Product Service, và User Service với các tính năng:
- RestTemplate để gọi giữa các service
- Pessimistic Lock để xử lý race condition
- **Tạo đơn hàng với nhiều sản phẩm cùng lúc** ⭐
- **Quan hệ One-to-Many giữa Order và OrderDetail** ⭐
- Response đầy đủ thông tin khi đặt hàng

## Cấu Hình Cổng

| Service | Port | Database |
|---------|------|----------|
| Order Service | 8080 | order_microdemo |
| Product Service | 8081 | product_microdemo |
| User Service | 8083 | user_microdemo |

## 📊 Database Schema - Order Service

### Order Table
```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  total_price DOUBLE NOT NULL,
  status VARCHAR(50) NOT NULL
);
```

### OrderDetail Table (Lưu trữ từng sản phẩm trong đơn hàng)
```sql
CREATE TABLE order_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  price DOUBLE NOT NULL,
  quantity INT NOT NULL,
  item_total_price DOUBLE NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
```

### Quan Hệ
```
Order (1) ---- (N) OrderDetail

Ví dụ:
Order #1 (userId: 1, totalPrice: 4050)
├── OrderDetail #1 (productId: 1, name: "Laptop", qty: 2, price: 1500, total: 3000)
├── OrderDetail #2 (productId: 2, name: "Monitor", qty: 1, price: 600, total: 600)
└── OrderDetail #3 (productId: 3, name: "Keyboard", qty: 3, price: 150, total: 450)
```

## Thành Phần Chính

### 1. Product Service (Port 8081)

**Pessimistic Lock Implementation:**
- `ProductRepository.findByIdWithLock()`: Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` để khóa hàng trong database
- `ProductService.decrementQuantityWithLock()`: Giảm số lượng sản phẩm với khóa
- `ProductController.POST /api/products/{id}/decrement-with-lock`: Endpoint xử lý giảm số lượng

### 2. Order Service (Port 8080) - **Hỗ Trợ Nhiều Sản Phẩm** ⭐

**Models:**
- **Order**: Đơn hàng chính (userId, totalPrice, status)
- **OrderDetail**: Chi tiết từng sản phẩm trong đơn hàng (productId, productName, price, quantity, itemTotalPrice)

**DTO Models:**
- `CreateOrderRequest`: Nhận userId và danh sách items
- `OrderItemRequest`: Chứa productId và quantity
- `OrderItemResponse`: Trả về chi tiết sản phẩm
- `OrderResponse`: Chứa orderId, user, danh sách items, totalPrice, status

**Flow tạo đơn hàng:**
```
1. Nhận CreateOrderRequest
2. Kiểm tra userId (gọi User Service)
3. Vòng lặp cho mỗi item:
   - Kiểm tra sản phẩm (gọi Product Service)
   - Kiểm tra số lượng
   - Tính toán giá
   - Tạo OrderDetail object
4. Lưu Order vào database
5. Lưu tất cả OrderDetail objects
6. Vòng lặp giảm quantity với Pessimistic Lock
7. Trả về OrderResponse
```

### 3. User Service (Port 8083)

**Tính năng:** CRUD User

## API Endpoints

### Order Service - Tạo Đơn Hàng Với Nhiều Sản Phẩm

```http
POST /api/orders
Content-Type: application/json

Request Body:
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    },
    {
      "productId": 3,
      "quantity": 3
    }
  ]
}
```

**Response (OrderResponse):**
```json
{
  "orderId": 1,
  "user": {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "a@example.com",
    "phone": "0123456789",
    "address": "123 Street"
  },
  "items": [
    {
      "productId": 1,
      "productName": "Laptop Dell XPS 15",
      "price": 1500.0,
      "quantity": 2,
      "itemTotalPrice": 3000.0
    },
    {
      "productId": 2,
      "productName": "Dell UltraSharp 27\"",
      "price": 600.0,
      "quantity": 1,
      "itemTotalPrice": 600.0
    },
    {
      "productId": 3,
      "productName": "Mechanical Keyboard RGB",
      "price": 150.0,
      "quantity": 3,
      "itemTotalPrice": 450.0
    }
  ],
  "totalPrice": 4050.0,
  "status": "PENDING"
}
```

### Các Endpoint Khác
```
GET /api/orders - Lấy tất cả đơn hàng
GET /api/orders/{id} - Lấy đơn hàng theo ID (kèm OrderDetails)
PUT /api/orders/{id} - Cập nhật đơn hàng
DELETE /api/orders/{id} - Xóa đơn hàng (tự động xóa OrderDetails)
```

## Giải Quyết Race Condition

### Vấn Đề:
```
Sản phẩm A có 10 cái
User 1 mua 7 cái + User 2 mua 5 cái = Lỗi!
Nếu không có lock: Cả 2 thành công → Tồn kho = -2
```

### Giải Pháp - Pessimistic Lock:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Product findByIdWithLock(@Param("id") Long id);
```

**Kết quả:**
- User 1 khóa hàng → 10 - 7 = 3 → Release
- User 2 chờ → Lấy lock → 3 - 5 → LỖI ✅

## Cách Chạy Project

1. **Tạo Database:**
```sql
CREATE DATABASE order_microdemo;
CREATE DATABASE product_microdemo;
CREATE DATABASE user_microdemo;
```

2. **Run các service:**
```bash
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

3. **Test API:**
```
1. Tạo User
2. Tạo 3 Products
3. Tạo Order với Multiple Items
4. Kiểm tra Orders và OrderDetails
```

## Các File Đã Tạo/Cập Nhật

### Order Service - Models ✅
- ✅ Order.java - Cập nhật (quan hệ @OneToMany với OrderDetail)
- ✅ OrderDetail.java - Tạo mới (chi tiết từng sản phẩm)

### Order Service - Repositories ✅
- ✅ OrderRepository.java - Giữ nguyên
- ✅ OrderDetailRepository.java - Tạo mới

### Order Service - Services ✅
- ✅ OrderService.java - Cập nhật (tạo OrderDetail objects)

### Order Service - DTOs ✅
- ✅ CreateOrderRequest.java - Tạo mới
- ✅ OrderItemRequest.java - Tạo mới
- ✅ OrderItemResponse.java - Tạo mới
- ✅ OrderResponse.java - Giữ nguyên

### Documentation ✅
- ✅ IMPLEMENTATION_GUIDE.md - Cập nhật
- ✅ MULTIPLE_ITEMS_ORDER_GUIDE.md - Giữ nguyên
- ✅ Postman_Collection.json - Cập nhật

## Notes

- **Không sử dụng JSON column** - Thay vào đó dùng **quan hệ One-to-Many**
- **Hibernate tự động tạo bảng** với `ddl-auto: update`
- **Cascade.ALL** - Khi xóa Order, tự động xóa OrderDetails
- **fetch = FetchType.EAGER** - Lấy OrderDetails cùng lúc khi query Order
- **mappedBy = "order"** - OrderDetail biết Order là master
