# Logisco Courier Service

A comprehensive courier tracking and management system with RESTful API backend and web frontend.

## 🚀 New: Complete Courier Booking System

We've added a full-featured courier booking system with:
- ✅ Multi-step booking wizard
- ✅ Real-time price calculation
- ✅ Serviceability check
- ✅ Multiple courier partner support
- ✅ COD (Cash on Delivery) support
- ✅ Insurance options
- ✅ Complete admin panel

**See [COURIER_BOOKING_SYSTEM.md](./COURIER_BOOKING_SYSTEM.md) for complete documentation.**

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
cd backend
mvn spring-boot:run
```

The application will start on `http://localhost:8090`

- **Frontend**: `http://localhost:8090/`
- **API Base URL**: `http://localhost:8090/api`
- **H2 Console**: `http://localhost:8090/h2-console` (JDBC URL: `jdbc:h2:mem:logisco`)

## 📦 Booking System

### Quick Start - Book a Shipment

1. **Access Booking Form**: Navigate to `http://localhost:8090/booking.html`
2. **Fill Details**: Complete the 5-step wizard
3. **Get Quote**: Real-time price calculation
4. **Confirm Booking**: Create shipment with tracking number

### Booking API Endpoints

- `POST /api/booking/check-serviceability` - Check if pincode is serviceable
- `POST /api/booking/calculate-price` - Calculate shipping price
- `POST /api/booking/create` - Create a new booking

See [COURIER_BOOKING_SYSTEM.md](./COURIER_BOOKING_SYSTEM.md) for detailed API documentation.

---

## 📚 API Documentation

### Base URL
```
http://localhost:8090/api
```

### Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 🔐 Authentication API

### Register User

**Endpoint:** `POST /api/auth/register`

**Public Endpoint** - No authentication required

**Request:**
```bash
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "fullName": "John Doe",
    "role": "USER"
  }'
```

**Response (200 OK):**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

**Roles:** `USER`, `ADMIN`, `STAFF`

---

### Login

**Endpoint:** `POST /api/auth/login`

**Public Endpoint** - No authentication required

**Request:**
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6ImpvaG5fZG9lIiwiaWF0IjoxNzY1MDI3Mjg3LCJleHAiOjE3NjUxMTM2ODd9...",
  "username": "john_doe",
  "role": "USER",
  "userId": 1
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials"
}
```

---

## 📦 Shipments API

### Create Shipment

**Endpoint:** `POST /api/shipments`

**Authentication Required**

**Request:**
```bash
curl -X POST http://localhost:8090/api/shipments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "senderName": "John Doe",
    "senderPhone": "+1234567890",
    "senderEmail": "john@example.com",
    "senderAddress": "123 Main St, New York, NY 10001",
    "receiverName": "Jane Smith",
    "receiverPhone": "+0987654321",
    "receiverEmail": "jane@example.com",
    "receiverAddress": "456 Oak Ave, Los Angeles, CA 90001",
    "packageDescription": "Electronics - Laptop",
    "weight": 3.5,
    "dimensions": "40x30x20",
    "shipmentType": "DOMESTIC",
    "priority": "EXPRESS",
    "pickupDate": "2025-12-10T10:00:00"
  }'
```

**Shipment Types:** `DOMESTIC`, `INTERNATIONAL`, `EXPRESS`

**Priority Levels:** `STANDARD`, `EXPRESS`, `OVERNIGHT`

**Response (200 OK):**
```json
{
  "id": 1,
  "trackingNumber": "TRK123456789",
  "senderName": "John Doe",
  "receiverName": "Jane Smith",
  "status": "PENDING",
  "shipmentType": "DOMESTIC",
  "priority": "EXPRESS",
  "basePrice": 25.00,
  "tax": 2.50,
  "totalPrice": 27.50,
  "createdAt": "2025-12-06T18:30:00",
  "pickupDate": "2025-12-10T10:00:00"
}
```

---

### Get All Shipments

**Endpoint:** `GET /api/shipments`

**Authentication Required**

**Request:**
```bash
curl -X GET http://localhost:8090/api/shipments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "trackingNumber": "TRK123456789",
    "senderName": "John Doe",
    "receiverName": "Jane Smith",
    "status": "PENDING",
    "totalPrice": 27.50
  }
]
```

---

### Get User Shipments

**Endpoint:** `GET /api/shipments/user/{userId}`

**Authentication Required**

**Request:**
```bash
curl -X GET http://localhost:8090/api/shipments/user/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):** Array of shipments for the specified user

---

### Track Shipment (Public)

**Endpoint:** `GET /api/track/{trackingNumber}`

**Public Endpoint** - No authentication required

**Request:**
```bash
curl -X GET http://localhost:8090/api/track/TRK123456789
```

**Response (200 OK):**
```json
{
  "id": 1,
  "trackingNumber": "TRK123456789",
  "status": "IN_TRANSIT",
  "senderName": "John Doe",
  "receiverName": "Jane Smith",
  "currentLocation": "Distribution Center",
  "estimatedDelivery": "2025-12-12T14:00:00"
}
```

**Response (404 Not Found):**
```json
{
  "message": "Shipment not found"
}
```

**Shipment Statuses:** `PENDING`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, `RETURNED`

---

### Get Tracking History

**Endpoint:** `GET /api/shipments/{id}/history`

**Authentication Required**

**Request:**
```bash
curl -X GET http://localhost:8090/api/shipments/1/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "status": "PENDING",
    "location": "Origin",
    "description": "Shipment created",
    "timestamp": "2025-12-06T18:30:00",
    "updatedBy": "system"
  },
  {
    "id": 2,
    "status": "PICKED_UP",
    "location": "New York Hub",
    "description": "Package picked up",
    "timestamp": "2025-12-07T09:00:00",
    "updatedBy": "driver_001"
  }
]
```

---

### Update Shipment Status

**Endpoint:** `PUT /api/shipments/{id}/status`

**Authentication Required**

**Request:**
```bash
curl -X PUT http://localhost:8090/api/shipments/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "IN_TRANSIT",
    "location": "Distribution Center - Chicago",
    "description": "Package in transit to destination"
  }'
```

**Response (200 OK):** Updated shipment object

---

### Delete Shipment

**Endpoint:** `DELETE /api/shipments/{id}`

**Authentication Required**

**Request:**
```bash
curl -X DELETE http://localhost:8090/api/shipments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "message": "Shipment deleted successfully"
}
```

---

## 💰 Invoices API

### Create Invoice for Shipment

**Endpoint:** `POST /api/invoices/shipment/{shipmentId}`

**Authentication Required**

**Request:**
```bash
curl -X POST http://localhost:8090/api/invoices/shipment/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "invoiceNumber": "INV-2025-001",
  "shipment": {
    "id": 1,
    "trackingNumber": "TRK123456789"
  },
  "subtotal": 25.00,
  "taxAmount": 2.50,
  "discount": 0.00,
  "totalAmount": 27.50,
  "paymentStatus": "PENDING",
  "paymentMethod": null,
  "issuedDate": "2025-12-06T18:35:00",
  "dueDate": "2025-12-20T18:35:00"
}
```

---

### Get All Invoices

**Endpoint:** `GET /api/invoices`

**Authentication Required**

**Request:**
```bash
curl -X GET http://localhost:8090/api/invoices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):** Array of all invoices

---

### Get Invoice by Invoice Number

**Endpoint:** `GET /api/invoices/{invoiceNumber}`

**Authentication Required**

**Request:**
```bash
curl -X GET http://localhost:8090/api/invoices/INV-2025-001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):** Invoice object

**Response (404 Not Found):** Empty response

---

### Get Invoice by Shipment ID

**Endpoint:** `GET /api/invoices/shipment/{shipmentId}`

**Authentication Required**

**Request:**
```bash
curl -X GET http://localhost:8090/api/invoices/shipment/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):** Invoice object

---

### Update Payment Status

**Endpoint:** `PUT /api/invoices/{id}/payment`

**Authentication Required**

**Request:**
```bash
curl -X PUT http://localhost:8090/api/invoices/1/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "PAID",
    "method": "ONLINE"
  }'
```

**Payment Statuses:** `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`

**Payment Methods:** `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `ONLINE`

**Response (200 OK):** Updated invoice object

---

## 👥 Admin API

**All admin endpoints require ADMIN role**

### Get All Users

**Endpoint:** `GET /api/admin/users`

**Request:**
```bash
curl -X GET http://localhost:8090/api/admin/users \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response (200 OK):** Array of all users

---

### Update User

**Endpoint:** `PUT /api/admin/users/{id}`

**Request:**
```bash
curl -X PUT http://localhost:8090/api/admin/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{
    "email": "newemail@example.com",
    "phoneNumber": "+1234567890",
    "fullName": "Updated Name",
    "active": true
  }'
```

**Response (200 OK):** Updated user object

---

### Delete User

**Endpoint:** `DELETE /api/admin/users/{id}`

**Request:**
```bash
curl -X DELETE http://localhost:8090/api/admin/users/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "message": "User deleted successfully"
}
```

---

### Get All Settings

**Endpoint:** `GET /api/admin/settings`

**Request:**
```bash
curl -X GET http://localhost:8090/api/admin/settings \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response (200 OK):** Array of system settings

---

### Create or Update Setting

**Endpoint:** `POST /api/admin/settings`

**Request:**
```bash
curl -X POST http://localhost:8090/api/admin/settings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{
    "settingKey": "MAINTENANCE_MODE",
    "settingValue": "false",
    "enabled": true,
    "description": "Enable/disable maintenance mode",
    "type": "BOOLEAN"
  }'
```

**Setting Types:** `BOOLEAN`, `STRING`, `NUMBER`, `EMAIL`

**Response (200 OK):** Created/updated setting object

---

### Get Setting by Key

**Endpoint:** `GET /api/admin/settings/{key}`

**Request:**
```bash
curl -X GET http://localhost:8090/api/admin/settings/MAINTENANCE_MODE \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response (200 OK):** Setting object

---

### Delete Setting

**Endpoint:** `DELETE /api/admin/settings/{id}`

**Request:**
```bash
curl -X DELETE http://localhost:8090/api/admin/settings/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response (200 OK):**
```json
{
  "message": "Setting deleted successfully"
}
```

---

## 📮 Postman Collection

### Importing to Postman

1. **Create a new collection** in Postman named "Logisco Courier API"
2. **Set collection variables:**
   - `base_url`: `http://localhost:8090`
   - `token`: (leave empty, will be set after login)

3. **Add Pre-request Script** to collection:
```javascript
// Auto-add Authorization header if token exists
if (pm.collectionVariables.get("token")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.collectionVariables.get("token")
    });
}
```

4. **Create requests** using the curl examples above, or use the following Postman format:

### Postman Request Examples

#### Register User
- **Method:** POST
- **URL:** `{{base_url}}/api/auth/register`
- **Body (raw JSON):**
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "fullName": "Test User",
  "role": "USER"
}
```

#### Login
- **Method:** POST
- **URL:** `{{base_url}}/api/auth/login`
- **Body (raw JSON):**
```json
{
  "username": "testuser",
  "password": "password123"
}
```
- **Tests (to save token):**
```javascript
var jsonData = pm.response.json();
if (jsonData.token) {
    pm.collectionVariables.set("token", jsonData.token);
}
```

#### Get Shipments (Protected)
- **Method:** GET
- **URL:** `{{base_url}}/api/shipments`
- **Headers:** Authorization header added automatically by pre-request script

### Postman Environment Variables

Create an environment with:
- `base_url`: `http://localhost:8090`
- `token`: (auto-populated after login)
- `user_id`: (optional, for testing user-specific endpoints)

---

## 🔒 Security

### Public Endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/track/{trackingNumber}`
- `GET /` (frontend)
- `GET /*.html`, `/css/**`, `/js/**` (static files)

### Protected Endpoints
All other endpoints require JWT authentication:
```
Authorization: Bearer <token>
```

### Admin-Only Endpoints
- `/api/admin/**` - Requires `ADMIN` role

### Error Responses

**401 Unauthorized:**
```json
{
  "message": "Invalid credentials"
}
```

**403 Forbidden:**
```json
{
  "message": "Access denied"
}
```

**400 Bad Request:**
```json
{
  "message": "Error: <error details>"
}
```

---

## 🗄️ Database

- **Type:** H2 In-Memory Database
- **Console:** `http://localhost:8090/h2-console`
- **JDBC URL:** `jdbc:h2:mem:logisco`
- **Username:** `sa`
- **Password:** (empty)

**Note:** Data is reset when the application restarts (in-memory database).

---

## 🛠️ Technology Stack

- **Backend:** Spring Boot 3.1.5
- **Security:** Spring Security 6 with JWT
- **Database:** H2 (in-memory)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **Java Version:** 17

---

## 📝 API Response Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## 🧪 Testing Examples

### Complete Workflow Example

```bash
# 1. Register a user
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123","email":"demo@example.com","fullName":"Demo User","role":"USER"}'

# 2. Login and save token
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}' | jq -r '.token')

# 3. Create a shipment
curl -X POST http://localhost:8090/api/shipments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": 1,
    "senderName": "John Doe",
    "senderEmail": "john@example.com",
    "senderAddress": "123 Main St",
    "receiverName": "Jane Smith",
    "receiverEmail": "jane@example.com",
    "receiverAddress": "456 Oak Ave",
    "packageDescription": "Package",
    "weight": 2.5,
    "shipmentType": "DOMESTIC",
    "priority": "STANDARD"
  }'

# 4. Get all shipments
curl -X GET http://localhost:8090/api/shipments \
  -H "Authorization: Bearer $TOKEN"

# 5. Create invoice for shipment
curl -X POST http://localhost:8090/api/invoices/shipment/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📞 Support

For issues or questions, please refer to the backend README or check the application logs.

---

## 📄 License

This project is part of the Logisco Courier Service system.

