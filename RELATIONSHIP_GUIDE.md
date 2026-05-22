# Quan Hệ One-to-Many: Order - OrderDetail

## 📊 Thiết Kế Chuẩn Quan Hệ

Thay vì lưu JSON array, bây giờ sử dụng **quan hệ One-to-Many chuẩn**:

```
Order (1) ---- (N) OrderDetail
```

### Database Tables

#### orders table
```sql
id          | user_id | total_price | status
1           | 1       | 4050.00     | PENDING
2           | 2       | 2100.00     | PENDING
```

#### order_details table
```sql
id  | order_id | product_id | product_name        | price  | quantity | item_total_price
1   | 1        | 1          | Laptop Dell XPS 15  | 1500.0 | 2        | 3000.0
2   | 1        | 2          | Dell UltraSharp 27" | 600.0  | 1        | 600.0
3   | 1        | 3          | Mechanical Keyboard | 150.0  | 3        | 450.0
4   | 2        | 1          | Laptop Dell XPS 15  | 1500.0 | 1        | 1500.0
5   | 2        | 3          | Mechanical Keyboard | 150.0  | 4        | 600.0
```

### Ưu Điểm So Với JSON Column

| Tiêu Chí | JSON Column | One-to-Many |
|---------|------------|------------|
| **Query** | Khó (cần parse JSON) | Dễ (SQL bình thường) |
| **Index** | Không hiệu quả | Có index trên order_id |
| **Update Detail** | Phải cập nhật cả dòng | Cập nhật 1 row |
| **Validation** | Không có constraint | Foreign key constraint |
| **Chuẩn SQL** | Không chuẩn | Chuẩn Normalization |
| **Performance** | Chậm với data lớn | Nhanh |

## 🔄 Models - Mối Quan Hệ

### Order.java (Master)
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    // Quan hệ 1-N: 1 Order có nhiều OrderDetail
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderDetail> orderDetails;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private String status;
}
```

**Giải thích:**
- `@OneToMany`: Order có quan hệ 1-N
- `mappedBy = "order"`: OrderDetail biết Order là master (qua field `order`)
- `cascade = CascadeType.ALL`: Khi xóa Order, xóa tất cả OrderDetails
- `fetch = FetchType.EAGER`: Khi load Order, tự động load OrderDetails

### OrderDetail.java (Detail)
```java
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ N-1: Nhiều OrderDetail thuộc về 1 Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double itemTotalPrice;
}
```

**Giải thích:**
- `@ManyToOne`: N OrderDetails thuộc về 1 Order
- `@JoinColumn(name = "order_id")`: Tạo foreign key `order_id` trong bảng `order_details`
- `fetch = FetchType.LAZY`: Không tự động load Order khi load OrderDetail (giảm performance)

## 🔀 Flow Lưu Dữ Liệu

### 1. Tạo Order
```java
Order order = new Order();
order.setUserId(1);
order.setTotalPrice(4050.0);
order.setStatus("PENDING");

Order savedOrder = orderRepository.save(order);
// Database: INSERT INTO orders (user_id, total_price, status) 
//           VALUES (1, 4050.0, 'PENDING')
// Result: order.id = 1
```

### 2. Tạo OrderDetails
```java
// OrderDetail 1
OrderDetail detail1 = new OrderDetail();
detail1.setOrder(savedOrder);  // Set reference
detail1.setProductId(1);
detail1.setProductName("Laptop");
detail1.setPrice(1500.0);
detail1.setQuantity(2);
detail1.setItemTotalPrice(3000.0);
orderDetailRepository.save(detail1);

// OrderDetail 2
OrderDetail detail2 = new OrderDetail();
detail2.setOrder(savedOrder);
detail2.setProductId(2);
detail2.setProductName("Monitor");
detail2.setPrice(600.0);
detail2.setQuantity(1);
detail2.setItemTotalPrice(600.0);
orderDetailRepository.save(detail2);

// Database:
// INSERT INTO order_details (order_id, product_id, product_name, price, quantity, item_total_price)
// VALUES (1, 1, 'Laptop', 1500.0, 2, 3000.0)
// INSERT INTO order_details (order_id, product_id, product_name, price, quantity, item_total_price)
// VALUES (1, 2, 'Monitor', 600.0, 1, 600.0)
```

## 🔍 Query Examples

### Lấy Order cùng tất cả OrderDetails
```java
// Tự động load vì fetch = FetchType.EAGER
Order order = orderRepository.findById(1);
List<OrderDetail> details = order.getOrderDetails();
// SELECT * FROM orders WHERE id = 1
// SELECT * FROM order_details WHERE order_id = 1
```

### Lấy tất cả OrderDetails của 1 Order
```java
List<OrderDetail> details = orderDetailRepository.findByOrderId(1);
// SELECT * FROM order_details WHERE order_id = 1
```

### Xóa Order (tự động xóa OrderDetails)
```java
orderRepository.deleteById(1);
// DELETE FROM order_details WHERE order_id = 1
// DELETE FROM orders WHERE id = 1
// (Cascade.ALL xử lý tự động)
```

## 📝 OrderService - Cập Nhật Flow

### Trước (JSON)
```java
// Lưu items dưới dạng JSON string
Order order = new Order();
order.setItems("[{\"productId\":1,\"quantity\":2}]");
order.setTotalPrice(3000.0);
orderRepository.save(order);
```

### Sau (One-to-Many)
```java
// Bước 1: Tạo Order
Order order = new Order();
order.setUserId(1);
order.setTotalPrice(3000.0);
order.setStatus("PENDING");
Order savedOrder = orderRepository.save(order);

// Bước 2: Tạo OrderDetails và link với Order
for (OrderDetail detail : orderDetails) {
    detail.setOrder(savedOrder);  // Link
    orderDetailRepository.save(detail);
}
```

## 🧪 Test Example

### Request
```bash
POST http://localhost:8080/api/orders

{
  "userId": 1,
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 2, "quantity": 1}
  ]
}
```

### Database Results

**orders table:**
```
id | user_id | total_price | status
1  | 1       | 3600.00     | PENDING
```

**order_details table:**
```
id | order_id | product_id | product_name        | price  | quantity | item_total_price
1  | 1        | 1          | Laptop Dell XPS 15  | 1500.0 | 2        | 3000.0
2  | 1        | 2          | Dell UltraSharp 27" | 600.0  | 1        | 600.0
```

### Response
```json
{
  "orderId": 1,
  "user": {...},
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
    }
  ],
  "totalPrice": 3600.0,
  "status": "PENDING"
}
```

## 🔐 Cascade Operations

### CascadeType.ALL bao gồm:
- `PERSIST`: Khi save Order → tự động save OrderDetails
- `MERGE`: Khi merge Order → tự động merge OrderDetails
- `REMOVE`: Khi xóa Order → tự động xóa OrderDetails
- `REFRESH`: Khi refresh Order → tự động refresh OrderDetails
- `DETACH`: Khi detach Order → tự động detach OrderDetails

### Ví dụ
```java
// Xóa Order
orderRepository.deleteById(1);

// Hibernate thực hiện:
// 1. DELETE FROM order_details WHERE order_id = 1
// 2. DELETE FROM orders WHERE id = 1
```

## ✅ Lợi Ích

1. **✅ Chuẩn SQL**: Tuân theo Normalization rules
2. **✅ Query Dễ**: Có thể sử dụng SQL bình thường
3. **✅ Index**: Có index trên foreign key
4. **✅ Constraint**: Database tự động validate
5. **✅ Performance**: Nhanh hơn JSON parsing
6. **✅ Scalability**: Dễ mở rộng trong tương lai
7. **✅ JPA Standard**: Mọi JPA framework đều support

## 📚 Repositories

### OrderRepository.java
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

### OrderDetailRepository.java
```java
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);
}
```

## 🎯 Tóm Tắt

| Mục | Chi Tiết |
|-----|---------|
| **Quan Hệ** | Order (1) ← → (N) OrderDetail |
| **Master** | Order |
| **Detail** | OrderDetail |
| **Foreign Key** | order_id trong order_details |
| **Cascade** | CascadeType.ALL (xóa tự động) |
| **Fetch** | EAGER cho Order, LAZY cho OrderDetail |
| **Mapping** | mappedBy = "order" |

Đây là cách tiêu chuẩn và tốt nhất để thiết kế quan hệ 1-N! 🎉

