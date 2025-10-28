# 🔐 ERP Authentication System

A secure, production-ready authentication system built with Spring Boot, featuring email verification via OTP, JWT-based authorization, and role-based access control.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Security Features](#security-features)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Future Enhancements](#future-enhancements)

## ✨ Features

- **User Registration** with email verification
- **OTP-based Email Verification** (6-digit code, 5-minute expiration)
- **JWT Authentication** for stateless sessions
- **Role-Based Access Control (RBAC)** with permissions
- **Async Email Sending** using ThreadPoolTaskExecutor
- **In-Memory OTP Cache** using Caffeine for high performance
- **Password Encryption** with BCrypt
- **Global Exception Handling** with custom error responses
- **Request Validation** using Jakarta Bean Validation
- **Comprehensive Logging** with file rotation

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Spring Boot 3.x** | Backend framework |
| **Spring Security** | Authentication & Authorization |
| **JWT (jjwt)** | Token-based authentication |
| **Spring Data JPA** | Database ORM |
| **H2 Database** | In-memory database for development |
| **Caffeine Cache** | High-performance OTP caching |
| **JavaMailSender** | Email functionality |
| **Lombok** | Reduce boilerplate code |
| **Logback** | Logging with file rotation |

## 🏗️ Architecture

### System Flow

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   Client    │─────▶│  REST API    │─────▶│  Services   │
└─────────────┘      └──────────────┘      └─────────────┘
                            │                      │
                            ▼                      ▼
                     ┌──────────────┐      ┌─────────────┐
                     │ JWT Filter   │      │ Cache/Email │
                     └──────────────┘      └─────────────┘
                            │                      │
                            ▼                      ▼
                     ┌──────────────┐      ┌─────────────┐
                     │  Database    │      │  SMTP Mail  │
                     └──────────────┘      └─────────────┘
```

### Authentication Flow

1. **Registration**: User registers → OTP sent via email → OTP cached (5 min)
2. **Verification**: User submits OTP → Account activated → JWT token issued
3. **Login**: User logs in with credentials → JWT token returned
4. **Protected Routes**: JWT validated on each request → Access granted/denied

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Gmail account (for email functionality)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/erp-auth-system.git
cd erp-auth-system
```

2. **Configure Email Settings**

Edit `src/main/resources/application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

> **Note**: Use Gmail App Password, not your regular password. [How to generate App Password](https://support.google.com/accounts/answer/185833)

3. **Run the Application**
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8082`

4. **Access H2 Console** (Optional)
```
URL: http://localhost:8082/h2-console
JDBC URL: jdbc:h2:mem:erpdb
Username: sa
Password: password
```

## 📡 API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Registration successful! Please check your email for OTP verification."
}
```

#### 2. Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "ROLE_USER",
  "message": "Email verified successfully! You are now logged in."
}
```

#### 3. Resend OTP
```http
POST /api/auth/resend-otp?email=john@example.com
```

#### 4. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** (Same as Verify OTP)

### Protected Endpoints (Requires JWT Token)

#### Get User Profile
```http
GET /api/auth/profile
Authorization: Bearer <your-jwt-token>
```

**Response:**
```
john@example.com
```

## 🔒 Security Features

### 1. Password Security
- **BCrypt Hashing** with automatic salt generation
- Minimum 6 characters required
- Passwords never stored in plain text

### 2. JWT Token Security
- **HS256 Algorithm** with 256-bit secret key
- 24-hour token expiration
- Contains: userId, email, role
- Stateless authentication (no server-side sessions)

### 3. OTP Security
- **6-digit random code** using SecureRandom
- **5-minute expiration** via Caffeine cache
- Automatic deletion after successful verification
- Rate limiting via thread pool (max 5 concurrent emails)

### 4. Input Validation
- Email format validation
- Username length constraints (3-50 chars)
- Password confirmation check
- Bean Validation annotations

### 5. Exception Handling
- Custom exceptions for business logic
- Global exception handler
- Proper HTTP status codes
- No sensitive information in error messages

## ⚙️ Configuration

### Key Properties

```properties
# Server
server.port=8082

# JWT Configuration
jwt.secret=your-256-bit-secret-key
jwt.expiration=86400000  # 24 hours in milliseconds

# OTP Configuration
otp.expiration.minutes=5

# Thread Pool (Async Email)
# Core: 2 threads always ready
# Max: 5 threads when busy
# Queue: 100 tasks can wait

# Cache Configuration
# Max 10,000 OTPs
# 5-minute expiration
# Automatic eviction

# Logging
logging.level.com.erp.valid=DEBUG
logging.file.name=logs/app.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=10
```

## 📁 Project Structure

```
src/main/java/com/erp/valid/
├── config/
│   ├── AsyncConfig.java           # Thread pool for async tasks
│   ├── CacheConfig.java            # Caffeine cache configuration
│   ├── DataInitializer.java       # Creates default roles
│   ├── JwtAuthenticationFilter.java # JWT validation filter
│   └── SecurityConfig.java         # Spring Security configuration
├── controller/
│   └── AuthController.java         # REST API endpoints
├── dto/
│   ├── AuthResponse.java           # Login/Verify response
│   ├── LoginRequest.java           # Login payload
│   ├── RegisterRequest.java        # Registration payload
│   ├── UserResponse.java           # Generic user response
│   └── VerifyOtpRequest.java       # OTP verification payload
├── entity/
│   ├── Permission.java             # Permission entity
│   ├── Role.java                   # Role entity (many-to-many with permissions)
│   └── User.java                   # User entity
├── exception/
│   ├── ConflictException.java      # Business logic exceptions
│   ├── GlobalExceptionHandler.java # Centralized error handling
│   └── RoleNotFoundException.java  # Role-specific exception
├── repository/
│   ├── RoleRepository.java         # Role database operations
│   └── UserRepository.java         # User database operations
├── service/
│   ├── EmailService.java           # Async email sending
│   ├── OtpCacheService.java        # OTP cache operations
│   └── UserService.java            # Authentication business logic
└── util/
    ├── JwtUtil.java                # JWT token generation/validation
    └── OtpUtil.java                # OTP generation
```

## 🧪 Testing the Application

### Using cURL

**1. Register a User**
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "confirmPassword": "password123"
  }'
```

**2. Verify OTP** (Check your email for OTP)
```bash
curl -X POST http://localhost:8082/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456"
  }'
```

**3. Access Protected Route**
```bash
curl -X GET http://localhost:8082/api/auth/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Postman

1. Import the collection (create one with above endpoints)
2. Set environment variable `{{baseUrl}}` = `http://localhost:8082`
3. After login/verify, save JWT token to `{{token}}`
4. Use `{{token}}` in Authorization header for protected routes

## 📊 Monitoring & Logging

### Log Files Location
```
logs/
├── app.log                    # Current log file
├── app.log.2024-10-27.0.gz   # Archived logs (compressed)
└── app.log.2024-10-27.1.gz
```

### Log Levels
- **ERROR**: Application errors, failed emails
- **WARN**: Invalid OTP attempts, expired tokens
- **INFO**: Successful operations (registration, login, OTP sent)
- **DEBUG**: Detailed flow for debugging

### Cache Statistics
Check console logs for cache performance:
```
Cache Statistics - Hits: 45, Misses: 5, Hit Rate: 90.00%, Evictions: 2
```

## 🔮 Future Enhancements

- [ ] **Password Reset** functionality
- [ ] **Refresh Token** implementation
- [ ] **Social Login** (Google, Facebook)
- [ ] **Two-Factor Authentication (2FA)**
- [ ] **Rate Limiting** on API endpoints
- [ ] **Account Lockout** after failed attempts
- [ ] **Email Templates** with HTML
- [ ] **PostgreSQL/MySQL** integration for production
- [ ] **Redis Cache** for distributed systems
- [ ] **API Documentation** with Swagger/OpenAPI
- [ ] **Unit & Integration Tests**
- [ ] **Docker Containerization**
- [ ] **CI/CD Pipeline** (GitHub Actions)

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your LinkedIn](https://linkedin.com/in/yourprofile)
- Email: your.email@example.com

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot Documentation
- Baeldung Spring Security Tutorials
- JWT.io for JWT debugging
- Caffeine Cache Documentation

---

**⭐ If you found this project helpful, please give it a star!**