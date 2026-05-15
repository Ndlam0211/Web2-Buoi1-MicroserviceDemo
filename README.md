# Microservice Demo - Product & Order Service

## System Architecture

This is a microservice-based system for managing products and orders. It consists of two main services:

### 1. Product Service (Port: 8081)

- Provides APIs to manage products in the inventory
- Database: MySQL (product database)
- Handles CRUD operations for products

### 2. Order Service (Port: 8082)

- Provides APIs to manage customer orders
- Database: MySQL (order database)
- Communicates with Product Service via REST API
- Validates product availability before creating orders

## Database Configuration

Both services use MySQL with automatic database creation:

- **Product Service**: `jdbc:mysql://localhost:3306/product?createDatabaseIfNotExist=true`
- **Order Service**: `jdbc:mysql://localhost:3306/order?createDatabaseIfNotExist=true`

### Prerequisites

- MySQL Server running on localhost:3306
- Default credentials: username=root, password=root

If your MySQL setup is different, update the `application.yml` files in each service.

## Project Structure

```
microservice-demo/
├── product-service/
│   ├── src/main/java/com/lamnd/
│   │   ├── ProductServiceApplication.java
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   ├── service/
│   │   │   └── ProductService.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   └── model/
│   │       └── Product.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── order-service/
│   ├── src/main/java/com/lamnd/
│   │   ├── OrderServiceApplication.java
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── service/
│   │   │   └── OrderService.java
│   │   ├── repository/
│   │   │   └── OrderRepository.java
│   │   ├── client/
│   │   │   └── ProductServiceClient.java
│   │   ├── config/
│   │   │   └── RestTemplateConfig.java
│   │   ├── dto/
│   │   │   └── ProductDTO.java
│   │   └── model/
│   │       └── Order.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
└── pom.xml (parent)
```

## Dependencies

- Spring Boot 4.0.6
- Spring Data JPA
- Spring Web
- MySQL Connector J
- Lombok (for reducing boilerplate code)

## Product Service APIs

### GET /api/products

Get all products

**Response:**

```json
[
  {
    "id": 1,
    "name": "Product Name",
    "description": "Product Description",
    "price": 99.99,
    "quantity": 50
  }
]
```

### GET /api/products/{id}

Get product by ID

### POST /api/products

Create a new product

**Request:**

```json
{
  "name": "Product Name",
  "description": "Product Description",
  "price": 99.99,
  "quantity": 50
}
```

### PUT /api/products/{id}

Update product by ID

### DELETE /api/products/{id}

Delete product by ID

## Order Service APIs

### GET /api/orders

Get all orders

**Response:**

```json
[
  {
    "id": 1,
    "productId": 1,
    "quantity": 5,
    "totalPrice": 499.95,
    "status": "PENDING"
  }
]
```

### GET /api/orders/{id}

Get order by ID

### POST /api/orders

Create a new order

**Request:**

```json
{
  "productId": 1,
  "quantity": 5
}
```

**Features:**

- Automatically validates product availability via Product Service
- Calculates total price based on product price and quantity
- Sets order status to "PENDING"
- Returns error if product not found or insufficient quantity

### PUT /api/orders/{id}

Update order by ID

### DELETE /api/orders/{id}

Delete order by ID

## How Order Service Communicates with Product Service

The `OrderService` uses `ProductServiceClient` to:

1. Fetch product details from Product Service using `RestTemplate`
2. Validate if the product exists
3. Check if enough quantity is available
4. Calculate the total price
5. Create the order only if validation passes

## Building the Project

### Build all services:

```bash
cd microservice-demo
mvn clean install
```

### Build individual service:

```bash
# Product Service
cd product-service
mvn clean install

# Order Service
cd order-service
mvn clean install
```

## Running the Services

### Run Product Service:

```bash
cd product-service
mvn spring-boot:run
```

Service will be available at: `http://localhost:8081`

### Run Order Service:

```bash
cd order-service
mvn spring-boot:run
```

Service will be available at: `http://localhost:8082`

## Testing the Services

### 1. Create a Product

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High performance laptop",
    "price": 1200.00,
    "quantity": 10
  }'
```

### 2. Create an Order

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

### 3. Get All Orders

```bash
curl http://localhost:8082/api/orders
```

### 4. Get Order Details

```bash
curl http://localhost:8082/api/orders/1
```

## Technology Stack

- **Framework**: Spring Boot 4.0.6
- **Database**: MySQL
- **ORM**: Hibernate (via Spring Data JPA)
- **Build Tool**: Maven
- **Java Version**: 17
- **REST Client**: RestTemplate
- **Lombok**: For annotations (@Data, @NoArgsConstructor, @AllArgsConstructor)

## Features Implemented

✅ Product Service with full CRUD operations
✅ Order Service with full CRUD operations
✅ Inter-service communication (Order Service → Product Service)
✅ Product validation before order creation
✅ Inventory quantity checking
✅ Automatic total price calculation
✅ MySQL database integration with auto-creation
✅ Spring Data JPA for data persistence
✅ RESTful API design
✅ Proper error handling

## Future Enhancements

- Add authentication and authorization
- Implement circuit breaker pattern for resilient service calls
- Add caching for product information
- Implement event-driven architecture with message queues
- Add service discovery (Eureka/Consul)
- Implement API Gateway
- Add comprehensive logging and monitoring
- Add unit and integration tests
- Implement rate limiting
