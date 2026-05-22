# Hướng Dẫn Cập Nhật: Tạo Đơn Hàng Với Nhiều Sản Phẩm

## 🎯 Tóm Tắt Các Thay Đổi

Bạn vừa cập nhật OrderService để **hỗ trợ tạo đơn hàng với nhiều sản phẩm cùng lúc** thay vì chỉ một sản phẩm.

## 📦 Các DTO Mới Được Tạo

### 1. OrderItemRequest.java
```java
{
  "productId": 1,
  "quantity": 2
}
```
- Chứa thông tin một sản phẩm cần mua
- productId: ID sản phẩm
- quantity: Số lượng mu���n mua

### 2. CreateOrderRequest.java
```java
{
  "userId": 1,
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 2, "quantity": 1},
    {"productId": 3, "quantity": 3}
  ]
}
```
- Yêu cầu tạo đơn hàng
- userId: ID người mua
- items: Danh sách sản phẩm cần mua

### 3. OrderItemResponse.java
```java
{
  "productId": 1,
  "productName": "Laptop Dell XPS 15",
  "price": 1500.0,
  "quantity": 2,
  "itemTotalPrice": 3000.0
}
```
- Chi tiết từng sản phẩm trong phản hồi
- Chứa tên sản phẩm, giá, số lượng, và tổng tiền cho item này

### 4. OrderResponse.java (Cập Nhật)
```java
{
  "orderId": 1,
  "user": {...},
  "items": [...],
  "totalPrice": 4050.0,
  "status": "PENDING"
}
```
- Thay vì chứa `ProductDTO + quantity` giờ chứa danh sách `OrderItemResponse`

## 📊 Cập Nhật Order Model

### Trước (Lưu một sản phẩm):
```java
@Column(nullable = false)
private Long productId;

@Column(nullable = false)
private Long userId;

@Column(nullable = false)
private Integer quantity;

@Column(nullable = false)
private Double totalPrice;

@Column(nullable = false)
private String status;
```

### Sau (Lưu nhiều sản phẩm):
```java
@Column(nullable = false)
private Long userId;

@Column(nullable = false, columnDefinition = "JSON")
private String items; // JSON: [{"productId": 1, "quantity": 2}, ...]

@Column(nullable = false)
private Double totalPrice;

@Column(nullable = false)
private String status;
```

**Lý do**: 
- Thay vì lưu productId và quantity riêng lẻ
- Giờ lưu toàn bộ danh sách items dưới dạng JSON string
- MySQL hỗ trợ kiểu dữ liệu JSON, dễ query và lưu trữ

## 🔄 Flow Tạo Đơn Hàng Với Nhiều Sản Phẩm

```
1. Client gửi CreateOrderRequest
   {
     "userId": 1,
     "items": [
       {"productId": 1, "quantity": 2},
       {"productId": 2, "quantity": 1}
     ]
   }
   ↓
2. OrderService.createOrder() nhận yêu cầu
   ├─ Kiểm tra userId (gọi User Service)
   ├─ Vòng lặp cho mỗi item:
   │  ├─ Lấy thông tin sản phẩm (gọi Product Service)
   │  ├─ Kiểm tra quantity có đủ không
   │  └─ Tính toán giá: price * quantity
   ├─ Tính tổng tiền: 3000 + 600 = 3600
   ├─ Lưu Order vào DB:
   │  - userId: 1
   │  - items: JSON string
   │  - totalPrice: 3600
   │  - status: PENDING
   ├─ Vòng lặp untuk mỗi item:
   │  └─ Giảm quantity với Pessimistic Lock (→ Product Service)
   └─ Return OrderResponse với đầy đủ thông tin
      ↓
3. Client nhận OrderResponse
```

## 📝 API Request/Response Examples

### Request - Tạo đơn hàng với 3 sản phẩm
```bash
POST http://localhost:8080/api/orders
Content-Type: application/json

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

### Response - OrderResponse
```json
{
  "orderId": 1,
  "user": {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "phone": "0123456789",
    "address": "123 Đường ABC, Quận 1, TP HCM"
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

## 🧪 Cách Test Với Postman

### Bước 1: Tạo User
```
POST http://localhost:8083/api/users
{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "phone": "0123456789",
  "address": "123 Đường ABC, Quận 1, TP HCM"
}
```
**Lưu ý userId = 1** (hoặc ID trả về)

### Bước 2: Tạo 3 Sản Phẩm
```
POST http://localhost:8081/api/products

Request 1:
{
  "name": "Laptop Dell XPS 15",
  "description": "High performance laptop",
  "price": 1500.00,
  "quantity": 20
}

Request 2:
{
  "name": "Dell UltraSharp 27\"",
  "description": "4K Monitor",
  "price": 600.00,
  "quantity": 15
}

Request 3:
{
  "name": "Mechanical Keyboard RGB",
  "description": "Cherry MX Switches",
  "price": 150.00,
  "quantity": 30
}
```
**Lưu ý productIds = 1, 2, 3** (hoặc IDs trả về)

### Bước 3: Tạo Đơn Hàng V��i Nhiều Sản Phẩm
```
POST http://localhost:8080/api/orders

Body:
{
  "userId": 1,
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 2, "quantity": 1},
    {"productId": 3, "quantity": 3}
  ]
}
```

### Bước 4: Kiểm Tra Kết Quả
```
GET http://localhost:8080/api/orders
GET http://localhost:8080/api/orders/1

GET http://localhost:8081/api/products
Xem quantity của từng sản phẩm đã giảm:
- Product 1: 20 - 2 = 18
- Product 2: 15 - 1 = 14
- Product 3: 30 - 3 = 27
```

## 🔒 Pessimistic Lock - Xử Lý Race Condition

### Khi tạo đơn hàng:
```java
for (OrderItemRequest itemRequest : request.getItems()) {
    // Giảm quantity với Pessimistic Lock
    boolean updateSuccess = productServiceClient.decrementQuantityWithLock(
        itemRequest.getProductId(),
        itemRequest.getQuantity()
    );
    
    if (!updateSuccess) {
        throw new RuntimeException("Không đủ hàng hoặc lỗi");
    }
}
```

### Ví dụ: Xử lý Race Condition
```
Tình huống: 
- Sản phẩm A có 10 cái
- User 1 mua 7 cái cùng lúc với User 2 mua 5 cái

Với Pessimistic Lock:
1. User 1 request → Khóa sản phẩm A (SELECT ... FOR UPDATE)
2. User 2 request → Chờ (đợi lock từ User 1)
3. User 1 → 10 - 7 = 3 → Hoàn thành → Release lock
4. User 2 → Lấy lock → Thấy còn 3 → 3 - 5 = -2 (LỖI!)
   → Trả về false → Đơn hàng User 2 bị huỷ

KẾT QUẢ: An toàn, không bao giờ âm số lượng
```

## 📂 Các File Được Tạo/Cập Nhật

| File | Trạng Thái | Mô Tả |
|------|-----------|-------|
| OrderItemRequest.java | ✅ Tạo mới | DTO cho từng sản phẩm |
| CreateOrderRequest.java | ✅ Tạo mới | DTO nhận request đơn hàng |
| OrderItemResponse.java | �� Tạo mới | DTO chi tiết sản phẩm trong response |
| OrderResponse.java | ✅ Cập nhật | Chứa danh sách items thay vì 1 product |
| Order.java | ✅ Cập nhật | Lưu items JSON thay vì productId |
| OrderService.java | ✅ Cập nhật | Xử lý nhiều sản phẩm với loop |
| OrderController.java | ✅ Cập nhật | Nhận CreateOrderRequest |
| Postman_Collection.json | ✅ Cập nhật | Thêm endpoint Single + Multiple Items |
| IMPLEMENTATION_GUIDE.md | ✅ Cập nhật | Chi tiết về chức năng mới |

## ⚠️ Các Lỗi Validate

OrderService sẽ throw RuntimeException nếu:

1. **userId = null hoặc items trống**
   ```
   "User ID and items list are required"
   ```

2. **User không tồn tại**
   ```
   "User not found with id: 999"
   ```

3. **Sản phẩm không tồn tại**
   ```
   "Product not found with id: 999"
   ```

4. **Không đủ số lượng**
   ```
   "Insufficient product quantity for product 'Laptop'. Available: 5, Requested: 10"
   ```

5. **Lỗi khi giảm quantity**
   ```
   "Failed to decrement quantity for product id: 1. Please try again."
   ```

## 🚀 Performance Notes

- **Mỗi lần tạo đơn hàng với N sản phẩm:**
  - Gọi User Service: 1 lần (GET)
  - Gọi Product Service: 2N lần
    - N lần để lấy thông tin (GET)
    - N lần để giảm quantity (POST decrement-with-lock)

- **Ví dụ với 3 sản phẩm:**
  - User Service: 1 request
  - Product Service: 6 requests (3 GET + 3 POST)
  - **Tổng: 7 requests micro-service**

## 💡 Cải Tiến Trong Tương Lai

1. **Optimize API calls**: Cache product info để giảm số lần gọi GET
2. **Batch operations**: Gọi 1 lần POST để giảm quantity cho N sản phẩm
3. **Saga Pattern**: Xử lý distributed transaction nếu 1 service lỗi
4. **Event-driven**: Sử dụng message queue (RabbitMQ, Kafka) thay v�� sync call

## 📌 Tóm Tắt

✅ **Đã triển khai:**
- Tạo đơn hàng với **nhiều sản phẩm cùng lúc**
- Tính toán **tổng tiền** cho cả đơn hàng
- Sử dụng **Pessimistic Lock** cho mỗi sản phẩm
- Trả về **OrderResponse đầy đủ thông tin**
- Collection Postman với **Single + Multiple Items**

🎉 **Sẵn sàng để test!**

