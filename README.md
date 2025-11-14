# 📚 BookBazaar – Online Bookstore Backend (Spring Boot + MySQL)

BookBazaar is a production-ready backend system for managing books, users, and orders in an online bookstore.  
It supports stock management, secure order placement with transactions, cancellation handling, search, pagination, and concurrency safety.

---

## 🚀 Features

### 📘 Book Management
- Browse books with pagination
- Search by keyword (title/author)
- Filter by category
- Track price and stock

### 🛒 Order Management
- Place orders with stock validation
- Prevent overselling (transactional safety)
- Calculate:
  - ✔ Total Price  
  - ✔ 5% GST  
  - ✔ 10% discount on orders > ₹1000
- Restore stock on order cancellation
- View order history (paginated)

### 👥 User Management
- Manage and view user order history

---

## 🏗️ Tech Stack

| Layer | Technology |
|------|------------|
| Backend | Spring Boot 3.x, Java 17 |
| Database | MySQL 8 |
| ORM | Spring Data JPA (Hibernate) |
| Build Tool | Maven |
| Utility | Lombok |
| Testing | JUnit 5 |

---

## 📂 Project Structure
```
src/main/java/com/bookbazaar
│
├── controller
├── service
├── repository
├── domain
├── dto
└── exception
```

## 🧪 Postman API Endpoints

```
📘 Browse Books:
GET /books?page=0&size=10

🔍 Search Books:
GET /books/search?keyword=java&category=programming&page=0&size=10

🛒 Place Order:
POST /orders
Content-Type: application/json
{
  "userId": 1,
  "items": [
    { "bookId": 2, "quantity": 3 },
    { "bookId": 5, "quantity": 1 }
  ]
}

❌ Cancel Order:
PUT /orders/{id}/cancel

📦 View User Order History:
GET /orders/user/{userId}?page=0&size=5
```

Test Scenarios:
```
| Scenario                      | Expected Outcome                               |
| ----------------------------- | ---------------------------------------------- |
| Place order with enough stock | Order placed, stock decreases                  |
| Place order with low stock    | Error: *Insufficient stock*                    |
| Cancel order                  | Stock restored correctly                       |
| Two users ordering same book  | Stock never goes negative (transaction safety) |
| Fetch user orders             | Paginated list with nested items               |
| Search books                  | Filters applied correctly                      |
```
---
