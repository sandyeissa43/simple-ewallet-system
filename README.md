# Simple E-Wallet System

A simple RESTful E-Wallet System developed using Spring Boot.

## Technologies Used

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT Authentication
- H2 Database
- Maven
- OpenAPI / Swagger
- JUnit 5
- Mockito
- Postman

## Features

- User registration and management
- User authentication using JWT
- Password encryption using BCrypt
- Wallet management
- Deposit money
- Withdraw money
- Transfer money between wallets
- Transaction history
- Input validation
- Global exception handling
- Pagination
- API documentation using OpenAPI

## Database

The project uses an H2 in-memory database.

Database URL:

`jdbc:h2:mem:ewalletdb`

H2 Console:

`http://localhost:8080/h2-console`

Username: `sa`

Password: empty

## How to Run

### 1. Clone the repository

```bash
git clone https://github.com/sandyeissa43/simple-ewallet-system.git
