# QuantityMeasurementApp
# UC15 – JSON Support for Quantity Measurement REST API

## Overview
UC15 introduces **JSON support** to the Quantity Measurement Application.  
The application can now **accept measurement data in JSON format and return responses in JSON**, making the API easier to integrate with web applications, mobile apps, and external systems.

This enhancement improves interoperability and follows modern **REST API communication standards**.

---

## Objective
The objective of this use case is to:

- Accept measurement data through a **JSON request body**
- Convert units using the existing **business logic**
- Return the **conversion result in JSON format**
- Enable easy API testing through tools like **Postman or curl**

---
## Technology Stack
- Java
- Spring Boot
- REST API
- Jackson (for JSON serialization/deserialization)
- Maven

---

## API Endpoint

### Convert Quantity

**Endpoint**

```
POST /quantity/convert
```

**Description**

Converts a quantity from one unit to another unit within the same measurement category.

---

## Request Body (JSON)

Example request:

```json
{
  "value": 1,
  "fromUnit": "FEET",
  "toUnit": "INCH"
}
```

---

## Response Body (JSON)

Example response:

```json
{
  "convertedValue": 12
}
```

This means **1 foot = 12 inches**.

---

## How It Works

1. The client sends a **JSON request** containing:
   - value
   - source unit
   - target unit

2. The **Controller Layer** receives the request.

3. The request is passed to the **Service Layer** where conversion logic is applied.

4. The application calculates the result.

5. The result is returned as a **JSON response**.

---

## Example Using cURL

```
curl -X POST http://localhost:8080/quantity/convert \
-H "Content-Type: application/json" \
-d '{"value":1,"fromUnit":"FEET","toUnit":"INCH"}'
```

---

## Expected Output

```
{
  "convertedValue": 12
}
```

---

## Benefits of UC15

- Enables **standard JSON communication**
- Makes the API easier to integrate with **frontend applications**
- Improves **testing using API tools**
- Follows **RESTful design principles**

---

## Future Enhancements

- Support multiple measurement types (length, volume, temperature)
- Add validation for incompatible unit conversions
- Provide detailed error responses
- Add API documentation using **Swagger/OpenAPI**

---

## Conclusion

UC15 enhances the Quantity Measurement Application by enabling **JSON-based REST communication**. This makes the service more flexible, scalable, and compatible with modern applications.