# Logisco Courier Backend

Spring Boot REST API service for authentication, shipments, invoices, and admin settings.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
cd backend
mvn spring-boot:run
```

- **Base URL:** `http://localhost:8090`
- **API Base:** `http://localhost:8090/api`
- **Frontend:** `http://localhost:8090/`
- **H2 Console:** `http://localhost:8090/h2-console`
  - JDBC URL: `jdbc:h2:mem:logisco`
  - Username: `sa`
  - Password: (empty)

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.1.5
- **Security:** Spring Security 6 with JWT authentication
- **Database:** H2 (in-memory)
- **ORM:** Spring Data JPA / Hibernate 6.2.13
- **Build Tool:** Maven
- **Java Version:** 17

## 🔒 Security Configuration

### Public Endpoints (No Authentication)
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/track/{trackingNumber}` - Public tracking
- `GET /` - Frontend root
- `GET /*.html`, `/css/**`, `/js/**` - Static files
- `GET /h2-console/**` - H2 database console

### Protected Endpoints (JWT Required)
All other `/api/**` endpoints require:
```
Authorization: Bearer <jwt-token>
```

### Admin-Only Endpoints
- `/api/admin/**` - Requires `ADMIN` role in JWT token

## 🔐 Authentication API

### Register User

**Endpoint:** `POST /api/auth/register`

**Curl Example:**
```bash
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "P@ssw0rd",
    "email": "alice@example.com",
    "phoneNumber": "+1234567890",
    "fullName": "Alice Smith",
    "role": "USER"
  }'
```

**Request Body:**
```json
{
  "username": "alice",
  "password": "P@ssw0rd",
  "email": "alice@example.com",
  "phoneNumber": "+1234567890",
  "fullName": "Alice Smith",
  "role": "USER"
}
```

**Response (200 OK):**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Username already exists"
}
```

**Available Roles:** `USER`, `ADMIN`, `STAFF`

---

### Login

**Endpoint:** `POST /api/auth/login`

**Curl Example:**
```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "P@ssw0rd"
  }'
```

**Request Body:**
```json
{
  "username": "alice",
  "password": "P@ssw0rd"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6ImFsaWNlIiwiaWF0IjoxNzY1MDI3Mjg3LCJleHAiOjE3NjUxMTM2ODd9...",
  "username": "alice",
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

**Note:** Save the `token` from the response to use in subsequent API calls.

## 📦 Shipments API

### Create Shipment

**Endpoint:** `POST /api/shipments`

**Curl Example:**
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

**Response:** Shipment with auto-generated `trackingNumber`, calculated `basePrice`, `tax`, `totalPrice`, status `PENDING`

---

### Get All Shipments

**Endpoint:** `GET /api/shipments`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/shipments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Array of all shipments

---

### Get User Shipments

**Endpoint:** `GET /api/shipments/user/{userId}`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/shipments/user/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Array of shipments for the specified user

---

### Track Shipment (Public)

**Endpoint:** `GET /api/track/{trackingNumber}`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/track/TRK123456789
```

**Response (200 OK):** Shipment object

**Response (404 Not Found):** Empty response

**Shipment Statuses:** `PENDING`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, `RETURNED`

---

### Get Tracking History

**Endpoint:** `GET /api/shipments/{id}/history`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/shipments/1/history \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Array of tracking history entries (ordered by timestamp descending)

---

### Update Shipment Status

**Endpoint:** `PUT /api/shipments/{id}/status`

**Curl Example:**
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

**Request Body:**
```json
{
  "status": "IN_TRANSIT",
  "location": "Hub A",
  "description": "Departed"
}
```

**Response:** Updated shipment object

---

### Delete Shipment

**Endpoint:** `DELETE /api/shipments/{id}`

**Curl Example:**
```bash
curl -X DELETE http://localhost:8090/api/shipments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "Shipment deleted successfully"
}
```

## 💰 Invoices API

### Create Invoice for Shipment

**Endpoint:** `POST /api/invoices/shipment/{shipmentId}`

**Curl Example:**
```bash
curl -X POST http://localhost:8090/api/invoices/shipment/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Created invoice with auto-generated `invoiceNumber`, calculated amounts, `paymentStatus: PENDING`

---

### Get All Invoices

**Endpoint:** `GET /api/invoices`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/invoices \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:** Array of all invoices

---

### Get Invoice by Invoice Number

**Endpoint:** `GET /api/invoices/{invoiceNumber}`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/invoices/INV-2025-001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):** Invoice object

**Response (404 Not Found):** Empty response

---

### Get Invoice by Shipment ID

**Endpoint:** `GET /api/invoices/shipment/{shipmentId}`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/invoices/shipment/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (200 OK):** Invoice object

**Response (404 Not Found):** Empty response

---

### Update Payment Status

**Endpoint:** `PUT /api/invoices/{id}/payment`

**Curl Example:**
```bash
curl -X PUT http://localhost:8090/api/invoices/1/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "PAID",
    "method": "ONLINE"
  }'
```

**Request Body:**
```json
{
  "status": "PAID",
  "method": "ONLINE"
}
```

**Payment Statuses:** `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`

**Payment Methods:** `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `ONLINE`

**Response:** Updated invoice object. When status is `PAID`, the associated shipment is marked as `paid=true`

## 👥 Admin API

**All admin endpoints require ADMIN role in JWT token**

### Get All Users

**Endpoint:** `GET /api/admin/users`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/admin/users \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:** Array of all users (including password hashes)

---

### Update User

**Endpoint:** `PUT /api/admin/users/{id}`

**Curl Example:**
```bash
curl -X PUT http://localhost:8090/api/admin/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{
    "email": "new@example.com",
    "phoneNumber": "+1234567890",
    "fullName": "Updated Name",
    "active": true
  }'
```

**Request Body (partial update):**
```json
{
  "email": "new@example.com",
  "phoneNumber": "+1234567890",
  "fullName": "Updated Name",
  "active": true
}
```

**Response:** Updated user object

---

### Delete User

**Endpoint:** `DELETE /api/admin/users/{id}`

**Curl Example:**
```bash
curl -X DELETE http://localhost:8090/api/admin/users/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "User deleted successfully"
}
```

---

### Get All Settings

**Endpoint:** `GET /api/admin/settings`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/admin/settings \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:** Array of all system settings

---

### Create or Update Setting

**Endpoint:** `POST /api/admin/settings`

**Curl Example:**
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

**Request Body:**
```json
{
  "settingKey": "FEATURE_X",
  "settingValue": "true",
  "enabled": true,
  "description": "Feature description",
  "type": "BOOLEAN"
}
```

**Setting Types:** `BOOLEAN`, `STRING`, `NUMBER`, `EMAIL`

**Response:** Created/updated setting object

---

### Get Setting by Key

**Endpoint:** `GET /api/admin/settings/{key}`

**Curl Example:**
```bash
curl -X GET http://localhost:8090/api/admin/settings/MAINTENANCE_MODE \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response (200 OK):** Setting object

**Response (404 Not Found):** Empty response

---

### Delete Setting

**Endpoint:** `DELETE /api/admin/settings/{id}`

**Curl Example:**
```bash
curl -X DELETE http://localhost:8090/api/admin/settings/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "Setting deleted successfully"
}
```

## 📋 Data Models

### Shipment
- Sender/Receiver information (name, phone, email, address)
- Package details (description, weight, dimensions)
- `shipmentType`: `DOMESTIC`, `INTERNATIONAL`, `EXPRESS`
- `priority`: `STANDARD`, `EXPRESS`, `OVERNIGHT`
- `status`: `PENDING`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, `RETURNED`
- Auto-generated `trackingNumber`
- Calculated prices (`basePrice`, `tax`, `totalPrice`)
- Dates (`createdAt`, `pickupDate`, `estimatedDelivery`, `actualDelivery`)

### Invoice
- Auto-generated `invoiceNumber`
- Amounts (`subtotal`, `taxAmount`, `discount`, `totalAmount`)
- `paymentStatus`: `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`
- `paymentMethod`: `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `ONLINE`
- Dates (`issuedDate`, `dueDate`, `paidDate`)
- Optional `notes`

### User
- `username` (unique), `password` (hashed), `email`
- `role`: `ADMIN`, `USER`, `STAFF`
- `active` (boolean)
- `fullName`, `phoneNumber`
- `createdAt` timestamp

### SystemSettings
- `settingKey` (unique)
- `settingValue`
- `type`: `BOOLEAN`, `STRING`, `NUMBER`, `EMAIL`
- `enabled` (boolean)
- `description`

## ⚙️ Configuration

### Application Properties
Location: `src/main/resources/application.properties`

- **Server Port:** `8090`
- **JWT Secret:** Configured in properties
- **JWT Expiration:** `86400000` ms (24 hours)
- **Database:** H2 in-memory (`jdbc:h2:mem:logisco`)
- **CORS:** All origins and methods allowed
- **H2 Console:** Enabled at `/h2-console`

### Security Configuration
- JWT-based stateless authentication
- Password encoding: BCrypt
- Public endpoints configured in `SecurityConfig.java`
- JWT filter validates tokens on protected endpoints

## 📮 Postman Collection Setup

### Quick Setup
1. Create a new Postman collection
2. Add collection variables:
   - `base_url`: `http://localhost:8090`
   - `token`: (empty initially)

3. Add pre-request script to collection:
```javascript
if (pm.collectionVariables.get("token")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.collectionVariables.get("token")
    });
}
```

4. In login request, add test script to save token:
```javascript
var jsonData = pm.response.json();
if (jsonData.token) {
    pm.collectionVariables.set("token", jsonData.token);
}
```

### Example Workflow
1. Register user → `POST /api/auth/register`
2. Login → `POST /api/auth/login` (saves token automatically)
3. Create shipment → `POST /api/shipments` (uses saved token)
4. Get shipments → `GET /api/shipments` (uses saved token)

## 🧪 Testing

### Complete Test Workflow
```bash
# 1. Register
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123","email":"test@example.com","fullName":"Test User","role":"USER"}'

# 2. Login and save token
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}' | jq -r '.token')

# 3. Create shipment
curl -X POST http://localhost:8090/api/shipments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"userId":1,"senderName":"John","senderEmail":"john@example.com","senderAddress":"123 St","receiverName":"Jane","receiverEmail":"jane@example.com","receiverAddress":"456 Ave","packageDescription":"Package","weight":2.5,"shipmentType":"DOMESTIC","priority":"STANDARD"}'

# 4. Get shipments
curl -X GET http://localhost:8090/api/shipments \
  -H "Authorization: Bearer $TOKEN"
```

## 📝 Notes

- Database is in-memory (H2), data resets on application restart
- JWT tokens expire after 24 hours (configurable)
- All passwords are hashed using BCrypt
- CORS is enabled for all origins (configure for production)
- Frontend files are served from the `frontend` directory
