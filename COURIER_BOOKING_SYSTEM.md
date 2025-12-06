# Complete Courier Booking System Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Database Schema](#database-schema)
3. [API Endpoints](#api-endpoints)
4. [Booking Flow](#booking-flow)
5. [Integration Guide](#integration-guide)
6. [Admin Panel](#admin-panel)
7. [Frontend Implementation](#frontend-implementation)

---

## System Overview

The Logisco Courier Booking System is a comprehensive solution for managing courier shipments with features similar to major courier booking platforms like Delhivery, DTDC, FedEx, etc.

### Key Features
- **Multi-step Booking Wizard**: Step-by-step form for easy booking
- **Serviceability Check**: Validates pickup and delivery locations
- **Dynamic Pricing**: Real-time price calculation based on weight, distance, and service type
- **Multiple Courier Partners**: Support for multiple courier service providers
- **COD Support**: Cash on Delivery option
- **Insurance**: Optional insurance for valuable packages
- **Real-time Tracking**: Track shipments with AWB numbers
- **Admin Panel**: Complete management interface

---

## Database Schema

### 1. Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    full_name VARCHAR(255),
    role VARCHAR(255) CHECK (role IN ('ADMIN','USER','STAFF')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Shipments Table (Enhanced)
```sql
CREATE TABLE shipments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_number VARCHAR(255) UNIQUE NOT NULL,
    booking_id VARCHAR(255),
    awb_number VARCHAR(255),
    user_id BIGINT,
    courier_partner_id BIGINT,
    
    -- Pickup Details
    sender_name VARCHAR(255),
    sender_phone VARCHAR(255),
    sender_email VARCHAR(255),
    sender_address VARCHAR(500),
    sender_city VARCHAR(255),
    sender_state VARCHAR(255),
    sender_country VARCHAR(255) DEFAULT 'India',
    sender_pincode VARCHAR(10),
    preferred_pickup_date TIMESTAMP,
    preferred_pickup_time_slot VARCHAR(50),
    
    -- Delivery Details
    receiver_name VARCHAR(255),
    receiver_phone VARCHAR(255),
    receiver_email VARCHAR(255),
    receiver_address VARCHAR(500),
    receiver_city VARCHAR(255),
    receiver_state VARCHAR(255),
    receiver_country VARCHAR(255) DEFAULT 'India',
    receiver_pincode VARCHAR(10),
    
    -- Package Details
    package_description VARCHAR(500),
    package_type VARCHAR(50) CHECK (package_type IN ('DOCUMENT','PARCEL','FRAGILE','ELECTRONICS','FOOD','LIQUID','HAZARDOUS','OTHERS')),
    weight DOUBLE,
    length DOUBLE,
    width DOUBLE,
    height DOUBLE,
    volumetric_weight DOUBLE,
    number_of_packages INT DEFAULT 1,
    declared_value DOUBLE,
    insurance_required BOOLEAN DEFAULT FALSE,
    special_handling_instructions VARCHAR(1000),
    
    -- Shipping Options
    shipment_type VARCHAR(50) CHECK (shipment_type IN ('DOMESTIC','INTERNATIONAL','EXPRESS')),
    delivery_type VARCHAR(50) CHECK (delivery_type IN ('STANDARD','EXPRESS','SAME_DAY','OVERNIGHT')),
    status VARCHAR(50) CHECK (status IN ('PENDING','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','CANCELLED','RETURNED')),
    priority VARCHAR(50) CHECK (priority IN ('STANDARD','EXPRESS','OVERNIGHT')),
    
    -- Financial
    base_price DOUBLE,
    tax DOUBLE,
    total_price DOUBLE,
    cod_amount DOUBLE,
    cod_enabled BOOLEAN DEFAULT FALSE,
    paid BOOLEAN DEFAULT FALSE,
    distance DOUBLE,
    
    -- Notification Preferences
    email_notification BOOLEAN DEFAULT TRUE,
    sms_notification BOOLEAN DEFAULT TRUE,
    whatsapp_notification BOOLEAN DEFAULT FALSE,
    
    -- Documents
    invoice_document_url VARCHAR(500),
    supporting_documents_url VARCHAR(500),
    
    -- Dates
    pickup_date TIMESTAMP,
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (courier_partner_id) REFERENCES courier_partners(id)
);
```

### 3. Courier Partners Table
```sql
CREATE TABLE courier_partners (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    code VARCHAR(255) UNIQUE NOT NULL,
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    api_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    integrated BOOLEAN DEFAULT FALSE,
    base_rate DOUBLE,
    fuel_surcharge DOUBLE,
    service_tax DOUBLE,
    min_charge DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. Pricing Rules Table
```sql
CREATE TABLE pricing_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    courier_partner_id BIGINT,
    delivery_type VARCHAR(50) CHECK (delivery_type IN ('STANDARD','EXPRESS','SAME_DAY','OVERNIGHT')),
    package_type VARCHAR(50) CHECK (package_type IN ('DOCUMENT','PARCEL','FRAGILE','ELECTRONICS','FOOD','LIQUID','HAZARDOUS','OTHERS')),
    min_weight DOUBLE,
    max_weight DOUBLE,
    rate_per_kg DOUBLE,
    fixed_charge DOUBLE,
    distance_multiplier DOUBLE,
    cod_charge DOUBLE,
    fragile_charge DOUBLE,
    insurance_charge DOUBLE,
    fuel_surcharge DOUBLE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (courier_partner_id) REFERENCES courier_partners(id)
);
```

### 5. Serviceability Table
```sql
CREATE TABLE serviceability (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    courier_partner_id BIGINT,
    pincode VARCHAR(10),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255) DEFAULT 'India',
    status VARCHAR(50) CHECK (status IN ('SERVICEABLE','NON_SERVICEABLE','PARTIAL')),
    estimated_days INT,
    cod_available BOOLEAN DEFAULT FALSE,
    reverse_pickup_available BOOLEAN DEFAULT FALSE,
    last_checked TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (courier_partner_id) REFERENCES courier_partners(id)
);
```

### 6. Payments Table
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT UNIQUE,
    payment_id VARCHAR(255) UNIQUE,
    amount DOUBLE,
    currency VARCHAR(10) DEFAULT 'INR',
    status VARCHAR(50) CHECK (status IN ('PENDING','PROCESSING','SUCCESS','FAILED','REFUNDED','CANCELLED')),
    method VARCHAR(50) CHECK (method IN ('ONLINE','COD','CARD','UPI','NETBANKING','WALLET')),
    gateway VARCHAR(50) CHECK (gateway IN ('RAZORPAY','STRIPE','PAYPAL','COD','MANUAL')),
    gateway_transaction_id VARCHAR(255),
    gateway_response TEXT,
    failure_reason VARCHAR(500),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id)
);
```

### 7. Invoices Table (Existing)
```sql
CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(255) UNIQUE,
    shipment_id BIGINT UNIQUE,
    subtotal DOUBLE,
    tax_amount DOUBLE,
    discount DOUBLE,
    total_amount DOUBLE,
    payment_status VARCHAR(50) CHECK (payment_status IN ('PENDING','PAID','OVERDUE','CANCELLED')),
    payment_method VARCHAR(50) CHECK (payment_method IN ('CASH','CREDIT_CARD','DEBIT_CARD','BANK_TRANSFER','ONLINE')),
    issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    paid_date TIMESTAMP,
    notes VARCHAR(500),
    FOREIGN KEY (shipment_id) REFERENCES shipments(id)
);
```

### 8. Tracking History Table (Existing)
```sql
CREATE TABLE tracking_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shipment_id BIGINT,
    status VARCHAR(50),
    location VARCHAR(255),
    description VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    FOREIGN KEY (shipment_id) REFERENCES shipments(id)
);
```

---

## API Endpoints

### Booking API

#### 1. Check Serviceability
**Endpoint:** `POST /api/booking/check-serviceability`

**Request:**
```json
{
  "pickupPincode": "110001",
  "deliveryPincode": "400001",
  "courierPartnerId": 1  // Optional
}
```

**Response:**
```json
{
  "pickupServiceable": true,
  "deliveryServiceable": true,
  "serviceable": true,
  "pickupCodAvailable": true,
  "deliveryCodAvailable": true,
  "estimatedDays": 3
}
```

#### 2. Calculate Price
**Endpoint:** `POST /api/booking/calculate-price`

**Request:**
```json
{
  "weight": 2.5,
  "length": 30,
  "width": 20,
  "height": 15,
  "packageType": "PARCEL",
  "deliveryType": "EXPRESS",
  "pickupPincode": "110001",
  "deliveryPincode": "400001",
  "codEnabled": false,
  "codAmount": 0,
  "insuranceRequired": true,
  "declaredValue": 5000,
  "courierPartnerId": 1  // Optional - if not provided, returns prices for all partners
}
```

**Response (Single Partner):**
```json
{
  "basePrice": 150.00,
  "codCharge": 0.00,
  "fragileCharge": 0.00,
  "insuranceCharge": 50.00,
  "fuelSurcharge": 15.00,
  "serviceTax": 27.00,
  "subtotal": 215.00,
  "totalPrice": 242.00,
  "chargeableWeight": 2.5,
  "volumetricWeight": 1.8,
  "courierPartner": "Delhivery",
  "estimatedDays": 2
}
```

**Response (All Partners):**
```json
[
  {
    "courierPartnerId": 1,
    "courierPartnerCode": "DELHIVERY",
    "courierPartner": "Delhivery",
    "totalPrice": 242.00,
    "estimatedDays": 2
  },
  {
    "courierPartnerId": 2,
    "courierPartnerCode": "DTDC",
    "courierPartner": "DTDC",
    "totalPrice": 280.00,
    "estimatedDays": 3
  }
]
```

#### 3. Create Booking
**Endpoint:** `POST /api/booking/create`

**Request:**
```json
{
  "senderName": "John Doe",
  "senderEmail": "john@example.com",
  "senderPhone": "+1234567890",
  "senderAddress": "123 Main St",
  "senderCity": "New Delhi",
  "senderState": "Delhi",
  "senderCountry": "India",
  "senderPincode": "110001",
  "preferredPickupDate": "2025-12-10T10:00:00",
  "preferredPickupTimeSlot": "10:00-12:00",
  
  "receiverName": "Jane Smith",
  "receiverEmail": "jane@example.com",
  "receiverPhone": "+0987654321",
  "receiverAddress": "456 Oak Ave",
  "receiverCity": "Mumbai",
  "receiverState": "Maharashtra",
  "receiverCountry": "India",
  "receiverPincode": "400001",
  
  "packageDescription": "Electronics",
  "packageType": "ELECTRONICS",
  "weight": 2.5,
  "length": 30,
  "width": 20,
  "height": 15,
  "numberOfPackages": 1,
  "declaredValue": 5000,
  "insuranceRequired": true,
  "specialHandlingInstructions": "Handle with care",
  
  "deliveryType": "EXPRESS",
  "courierPartnerId": 1,
  
  "codEnabled": false,
  "codAmount": 0,
  
  "emailNotification": true,
  "smsNotification": true,
  "whatsappNotification": false
}
```

**Response:**
```json
{
  "bookingId": "BK1733507287000",
  "trackingNumber": "TRK12345678",
  "awbNumber": "AWB17335072870001",
  "shipmentId": 1,
  "totalPrice": 242.00,
  "estimatedDelivery": 2,
  "message": "Booking created successfully"
}
```

---

## Booking Flow

### Step-by-Step Process

1. **User enters pickup details**
   - Validates pincode format
   - Checks serviceability (optional)

2. **User enters delivery details**
   - Validates pincode format
   - Checks serviceability

3. **User enters package details**
   - Weight, dimensions
   - Package type
   - Insurance options

4. **System calculates pricing**
   - Calculates volumetric weight
   - Determines chargeable weight
   - Applies pricing rules
   - Shows multiple courier options (if available)

5. **User selects courier partner**
   - Views price comparison
   - Selects preferred partner

6. **User reviews and confirms**
   - Reviews all details
   - Confirms booking

7. **System creates booking**
   - Generates booking ID
   - Generates tracking number
   - Creates AWB number (via courier API)
   - Sends confirmation

### Pricing Calculation Logic

1. **Volumetric Weight Calculation**
   ```
   Volumetric Weight = (Length × Width × Height) / 5000
   ```

2. **Chargeable Weight**
   ```
   Chargeable Weight = Max(Actual Weight, Volumetric Weight)
   ```

3. **Base Price**
   ```
   Base Price = Fixed Charge + (Chargeable Weight × Rate per kg) + (Distance × Distance Multiplier)
   ```

4. **Additional Charges**
   - COD Charge: Percentage or fixed amount
   - Fragile Charge: Fixed amount (if package type is FRAGILE)
   - Insurance Charge: Percentage of declared value
   - Fuel Surcharge: Percentage of base price

5. **Taxes**
   ```
   Service Tax = Base Price × Tax Rate
   ```

6. **Total Price**
   ```
   Total = Base Price + Additional Charges + Fuel Surcharge + Service Tax
   ```

---

## Integration Guide

### 1. Payment Gateway Integration

#### Razorpay Integration Example

```java
@Service
public class PaymentService {
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    public Map<String, Object> createPaymentOrder(Double amount, String orderId) {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", orderId);
        
        Order order = razorpay.Orders.create(orderRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", amount);
        response.put("keyId", razorpayKeyId);
        
        return response;
    }
}
```

#### Payment Webhook Handler

```java
@PostMapping("/api/payment/webhook")
public ResponseEntity<?> handlePaymentWebhook(@RequestBody String payload) {
    // Verify webhook signature
    // Update payment status
    // Update shipment status
    return ResponseEntity.ok().build();
}
```

### 2. Courier Partner API Integration

#### Delhivery API Integration Example

```java
@Service
public class DelhiveryIntegrationService {
    
    @Value("${delhivery.api.key}")
    private String apiKey;
    
    @Value("${delhivery.api.url}")
    private String apiUrl;
    
    public String createShipment(Shipment shipment) {
        // Prepare request
        Map<String, Object> request = new HashMap<>();
        request.put("name", shipment.getReceiverName());
        request.put("phone", shipment.getReceiverPhone());
        request.put("add", shipment.getReceiverAddress());
        request.put("pin", shipment.getReceiverPincode());
        request.put("payment_mode", shipment.getCodEnabled() ? "COD" : "Prepaid");
        request.put("amount", shipment.getCodAmount());
        
        // Call Delhivery API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            apiUrl + "/api/cmu/create.json", entity, Map.class);
        
        // Extract AWB number
        Map<String, Object> result = response.getBody();
        return (String) result.get("waybill");
    }
    
    public Map<String, Object> trackShipment(String awbNumber) {
        // Call tracking API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + apiKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            apiUrl + "/api/packages/json/?waybill=" + awbNumber,
            HttpMethod.GET, entity, Map.class);
        
        return response.getBody();
    }
}
```

### 3. Google Maps API Integration

#### Distance Calculation

```java
@Service
public class GoogleMapsService {
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    public Double calculateDistance(String origin, String destination) {
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
            "?origins=" + origin +
            "&destinations=" + destination +
            "&key=" + apiKey;
        
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
        // Extract distance from response
        List<Map<String, Object>> rows = (List<Map<String, Object>>) response.get("rows");
        Map<String, Object> elements = (Map<String, Object>) rows.get(0).get("elements");
        Map<String, Object> distance = (Map<String, Object>) elements.get(0).get("distance");
        
        return ((Number) distance.get("value")).doubleValue() / 1000.0; // Convert to km
    }
}
```

### 4. SMS/Email Notification Integration

#### Email Service

```java
@Service
public class NotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendBookingConfirmation(Shipment shipment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(shipment.getSenderEmail());
        message.setSubject("Booking Confirmation - " + shipment.getBookingId());
        message.setText("Your shipment has been booked successfully.\n" +
            "Tracking Number: " + shipment.getTrackingNumber() + "\n" +
            "AWB Number: " + shipment.getAwbNumber());
        
        mailSender.send(message);
    }
}
```

---

## Admin Panel

### Features

1. **Shipment Management**
   - View all shipments
   - Filter by status, date, courier partner
   - Update shipment status
   - Print labels and manifests

2. **Courier Partner Management**
   - Add/Edit courier partners
   - Configure API credentials
   - Set base rates and charges

3. **Pricing Management**
   - Create/Edit pricing rules
   - Set weight-based rates
   - Configure additional charges

4. **Serviceability Management**
   - Manage serviceable pincodes
   - Update serviceability status
   - Bulk import pincodes

5. **Reports & Analytics**
   - Booking statistics
   - Revenue reports
   - Courier partner performance
   - Delivery performance

### Admin API Endpoints

#### Get All Shipments
```
GET /api/admin/shipments?status=PENDING&page=0&size=20
```

#### Update Shipment Status
```
PUT /api/admin/shipments/{id}/status
{
  "status": "PICKED_UP",
  "location": "Origin Hub",
  "description": "Package picked up"
}
```

#### Manage Courier Partners
```
POST /api/admin/courier-partners
PUT /api/admin/courier-partners/{id}
DELETE /api/admin/courier-partners/{id}
```

#### Manage Pricing Rules
```
POST /api/admin/pricing-rules
PUT /api/admin/pricing-rules/{id}
DELETE /api/admin/pricing-rules/{id}
```

---

## Frontend Implementation

### Booking Form Structure

The booking form is a 5-step wizard:

1. **Step 1: Pickup Details**
   - Sender information
   - Pickup address
   - Preferred pickup date/time

2. **Step 2: Delivery Details**
   - Receiver information
   - Delivery address

3. **Step 3: Package Details**
   - Package type
   - Weight and dimensions
   - Insurance options

4. **Step 4: Shipping Options**
   - Delivery type selection
   - Courier partner selection
   - COD options
   - Real-time price display

5. **Step 5: Review & Pay**
   - Review all details
   - Final price confirmation
   - Submit booking

### Key JavaScript Functions

- `nextStep()`: Validates and moves to next step
- `prevStep()`: Moves to previous step
- `calculatePrice()`: Calls API to calculate price
- `checkServiceability()`: Validates pincode serviceability
- `selectCourier()`: Selects courier partner
- Form submission: Creates booking

---

## Testing

### Test Scenarios

1. **Serviceability Check**
   - Valid pincodes
   - Invalid pincodes
   - Non-serviceable areas

2. **Price Calculation**
   - Different weights
   - Different package types
   - COD enabled/disabled
   - Insurance enabled/disabled

3. **Booking Creation**
   - Complete booking flow
   - Missing required fields
   - Invalid data

4. **Payment Processing**
   - Successful payment
   - Failed payment
   - Payment webhook

---

## Deployment

### Environment Variables

```properties
# Server
server.port=8090

# Database
spring.datasource.url=jdbc:h2:mem:logisco
spring.datasource.username=sa
spring.datasource.password=

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Payment Gateway
razorpay.key.id=your-razorpay-key
razorpay.key.secret=your-razorpay-secret

# Courier APIs
delhivery.api.key=your-delhivery-key
delhivery.api.url=https://staging-express.delhivery.com

# Google Maps
google.maps.api.key=your-google-maps-key

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email
spring.mail.password=your-password
```

---

## Future Enhancements

1. **Multi-currency Support**
2. **International Shipping**
3. **Bulk Booking**
4. **API Rate Limiting**
5. **Advanced Analytics Dashboard**
6. **Mobile App Integration**
7. **WhatsApp Bot Integration**
8. **Automated Label Printing**
9. **Return Management**
10. **Customer Support Chat**

---

## Support

For issues or questions, please refer to the main README or contact the development team.

