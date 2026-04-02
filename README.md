# 📐 Quantity Measurement App

---

## 🧭 Overview

The **Quantity Measurement App** is a backend application that started as a simple problem — comparing units like feet and inches — and gradually evolved into a **scalable, secure, real-world system**.

Over multiple use cases (UC1 → UC18), this project demonstrates how basic logic can grow into a production-style backend using:

- Object-Oriented Design
- Generic Programming
- REST APIs
- Layered Architecture
- Secure Authentication (JWT + Google OAuth)

---

## 💡 What Problem Does It Solve?

It allows users to:

- Compare quantities (e.g., 1 foot == 12 inches)
- Convert units (feet, inches, liters, kg, etc.)
- Perform operations (addition, subtraction, division)
- Securely access APIs using authentication

---

## 🧠 How the Project Evolved

### 🔹 Phase 1: Core Logic (UC1–UC4)
- Started with basic equality checks
- Introduced unit conversion (feet ↔ inches)
- Learned object equality and clean class design

---

### 🔹 Phase 2: Scalable Design (UC5–UC10)
- Created a generic `Quantity` system
- Introduced enums for units
- Applied DRY and OOP principles
- Supported multiple domains (Length, Weight, Volume)

---

### 🔹 Phase 3: Operations & Abstraction (UC11–UC14)
- Added arithmetic operations (add, subtract, divide)
- Centralized logic using reusable patterns
- Improved code maintainability

---

### 🔹 Phase 4: Backend Architecture (UC15–UC17)
- Converted project into a Spring Boot application
- Introduced layered architecture:
  - Controller → Service → Repository
- Used DTOs for clean API communication
- Built REST APIs

---

### 🔹 Phase 5: Security (UC18)
- Implemented authentication using:
  - JWT (JSON Web Token)
  - Google OAuth 2.0
- Secured APIs using Spring Security

---

## 🔐 Authentication Explained (Simple)

### 1. JWT Authentication

When a user logs in:
- The system verifies credentials
- Generates a **JWT token**
- This token is used for future requests

👉 No need to log in again and again

---

### 2. Google Authentication (OAuth 2.0)

Instead of creating an account:
- User logs in via Google
- Backend receives user details
- JWT token is generated automatically

👉 Faster and more secure login

---

## ⚙️ How It Works (Flow)

### Normal API Flow
> Client → Controller → Service → Repository → Database


### Secured Flow (JWT)
> Client → JWT Filter → Controller → Service → Database


### Google Login Flow
> Client → Google → Backend → JWT → Client

##  Example
> * Input: 1 foot and 12 inches
> *  Output: true


---

##  Key Concepts Used

- **OOP (Object-Oriented Programming)** → Clean and modular design  
- **TDD (Test-Driven Development)** → Writing tests before logic  
- **DTO Pattern** → Clean API input/output  
- **JWT** → Stateless authentication  
- **OAuth 2.0** → Third-party login (Google)  
- **Spring Boot** → Backend framework  
- **Spring Security** → API protection  

---

## How to Run

```bash
mvn spring-boot:run
