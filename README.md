# 🔐 Quantity Measurement App – UC18 (Google Authentication)

This project is a backend application that not only performs quantity operations (like unit conversion and arithmetic) but also focuses on **secure user authentication**.

In this stage (UC18), the main goal is to allow users to log in **securely using Google instead of manually creating accounts**.

---

## 💡 What is Authentication?

Authentication means verifying **who the user is**.

There are two ways used here:

### 1. JWT Authentication
When a user logs in with email/password, the system generates a **JWT (JSON Web Token)**.

👉 Think of it like a digital pass:
- You log in once
- You receive a token
- You use that token to access protected APIs

---

### 2. Google Authentication (OAuth 2.0)

Instead of creating a new account, users can log in using Google.

👉 Flow:
1. User clicks “Login with Google”
2. Google verifies the user
3. Backend receives user details (email, name)
4. A JWT token is generated
5. User is now authenticated

---

## ⚙️ How It Works Internally

- **Spring Security** protects APIs
- **JWT Filter** checks every request for a valid token
- **OAuth2 Service** handles Google login
- **Success Handler** generates JWT after Google login

---

## 🧪 Example

### If a user logs in via Google:

> * Input → Google Login
> * Output → JWT Token


### Now this token is used for all future API requests.

---

## 📌 Key Terms

- **JWT**: A secure token used instead of sessions  
- **OAuth 2.0**: Protocol for third-party login (Google, GitHub)  
- **Spring Security**: Framework to secure APIs  

---

This project demonstrates how modern applications handle **secure, scalable authentication** in real-world systems.