# Microservice Demo - Product, User & Order Services

## System Architecture

This project implements a small microservice system for products, users and orders with inter-service REST calls.

Services:
- Product Service (port 8081) — manages product catalog and inventory
- User Service (port 8083) — simple user CRUD service
- Order Service (port 8080) — creates and manages orders; uses Product and User services via RestTemplate

## Key Features
- Order creation supports multiple products per order (Order -> OrderDetail relationship)
- Pessimistic locking on product stock updates to avoid race conditions
- Inter-service communication via RestTemplate
- MySQL databases for each service (auto-create / update via Hibernate)
- Postman collection included for testing

## Current Ports and Databases
- Product Service: http://localhost:8081 — database: product_microdemo
- User Service: http://localhost:8083 — database: user_microdemo
- Order Service: http://localhost:8080 — database: order_microdemo

Update connection strings in each service's `application.yml` if your MySQL differs.

## Data Model (Order Service)
- Order (master)
  - id, userId, totalPrice, status
  - One-to-Many relation to OrderDetail
- OrderDetail (detail)
  - id, order_id (FK), productId, productName, price, quantity, itemTotalPrice

This design avoids storing a JSON column for items and follows relational normalization.

## Important Endpoints

Product Service (examples)
- GET  /api/products
- GET  /api/products/{id}
- POST /api/products
- PUT  /api/products/{id}
- DELETE /api/products/{id}
- POST /api/products/{id}/decrement-with-lock — decrement stock using pessimistic lock

User Service (examples)
- GET  /api/users
- GET  /api/users/{id}
- POST /api/users
- PUT  /api/users/{id}
- DELETE /api/users/{id}

Order Service (examples)
- GET  /api/orders
- GET  /api/orders/{id}  (includes order details)
- POST /api/orders  (create order with multiple items)
  Example request body:
  {
    "userId": 1,
    "items": [ {"productId":1, "quantity":2}, {"productId":2, "quantity":1} ]
  }
- PUT  /api/orders/{id}  (update order meta: userId, totalPrice, status)
- DELETE /api/orders/{id}

## How order creation works
1. Order Service validates user via User Service.
2. For each item, Order Service fetches product info from Product Service and verifies stock.
3. Order and OrderDetail rows are created in the Order DB.
4. For each item, Order Service calls Product Service endpoint that decrements stock using a pessimistic DB lock to avoid race conditions.
5. OrderResponse returned includes user info and list of items with per-item totals and order total.

## Running the services
Ensure MySQL is running and update credentials in each `application.yml` if needed.

Build all services:

    mvn clean install

Run each service separately:

    # Product Service
    cd product-service
    mvn spring-boot:run

    # User Service
    cd user-service
    mvn spring-boot:run

    # Order Service
    cd order-service
    mvn spring-boot:run

## Testing
- Import `Postman_Collection.json` from repository root into Postman.
- Sequence: create a user -> create products -> create order (single or multiple items) -> verify product quantities and orders

## Notes and Recommendations
- Current implementation uses pessimistic locking at Product Service to prevent oversell. For high throughput systems consider optimistic locking, distributed locks or a reservation/checkout workflow.
- Order details are stored relationally (OrderDetail) — easier querying and updates.
- Consider adding integration tests and error handling improvements for production readiness.

## Project Structure
(abridged)

```
microservice-demo/
├─ product-service/
├─ user-service/
└─ order-service/
```

## Contact
For development questions, open an issue in the repository or inspect `IMPLEMENTATION_GUIDE.md` and `RELATIONSHIP_GUIDE.md` for implementation details.
