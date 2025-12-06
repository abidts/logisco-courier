package com.logisco.controller;

import com.logisco.model.Shipment;
import com.logisco.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private ServiceabilityService serviceabilityService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private DistanceCalculationService distanceService;

    @Autowired
    private ShipmentService shipmentService;

    /**
     * Check serviceability for pickup and delivery pincodes
     */
    @PostMapping("/check-serviceability")
    public ResponseEntity<?> checkServiceability(@RequestBody Map<String, String> request) {
        try {
            String pickupPincode = request.get("pickupPincode");
            String deliveryPincode = request.get("deliveryPincode");
            Long courierPartnerId = request.get("courierPartnerId") != null ?
                Long.parseLong(request.get("courierPartnerId")) : null;

            if (pickupPincode == null || deliveryPincode == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Pickup and delivery pincodes are required"));
            }

            if (courierPartnerId != null) {
                // Check for specific partner
                Map<String, Object> result = serviceabilityService.validateServiceability(
                    pickupPincode, deliveryPincode, courierPartnerId
                );
                return ResponseEntity.ok(result);
            } else {
                // Check for all partners
                return ResponseEntity.ok(serviceabilityService.checkServiceabilityForAllPartners(deliveryPincode));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error checking serviceability: " + e.getMessage()));
        }
    }

    /**
     * Calculate price for a shipment
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<?> calculatePrice(@RequestBody Map<String, Object> request) {
        try {
            Shipment shipment = new Shipment();
            
            // Set package details
            shipment.setWeight(((Number) request.get("weight")).doubleValue());
            shipment.setLength(request.get("length") != null ? 
                ((Number) request.get("length")).doubleValue() : null);
            shipment.setWidth(request.get("width") != null ? 
                ((Number) request.get("width")).doubleValue() : null);
            shipment.setHeight(request.get("height") != null ? 
                ((Number) request.get("height")).doubleValue() : null);
            
            shipment.setPackageType(Shipment.PackageType.valueOf(
                request.get("packageType").toString().toUpperCase()));
            shipment.setDeliveryType(Shipment.DeliveryType.valueOf(
                request.get("deliveryType").toString().toUpperCase()));
            
            shipment.setCodEnabled(request.get("codEnabled") != null && 
                Boolean.parseBoolean(request.get("codEnabled").toString()));
            if (shipment.getCodEnabled() && request.get("codAmount") != null) {
                shipment.setCodAmount(((Number) request.get("codAmount")).doubleValue());
            }
            
            shipment.setInsuranceRequired(request.get("insuranceRequired") != null && 
                Boolean.parseBoolean(request.get("insuranceRequired").toString()));
            if (shipment.getInsuranceRequired() && request.get("declaredValue") != null) {
                shipment.setDeclaredValue(((Number) request.get("declaredValue")).doubleValue());
            }

            // Calculate distance
            String pickupPincode = request.get("pickupPincode").toString();
            String deliveryPincode = request.get("deliveryPincode").toString();
            Double distance = distanceService.calculateDistance(pickupPincode, deliveryPincode);
            shipment.setDistance(distance);

            Long courierPartnerId = request.get("courierPartnerId") != null ?
                Long.parseLong(request.get("courierPartnerId").toString()) : null;

            if (courierPartnerId != null) {
                // Calculate for specific partner
                Map<String, Object> price = pricingService.calculatePrice(shipment, courierPartnerId);
                return ResponseEntity.ok(price);
            } else {
                // Calculate for all partners
                return ResponseEntity.ok(pricingService.calculatePricesForAllPartners(shipment));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error calculating price: " + e.getMessage()));
        }
    }

    /**
     * Create a booking
     */
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request) {
        try {
            Shipment shipment = new Shipment();
            
            // Pickup details
            shipment.setSenderName(request.get("senderName").toString());
            shipment.setSenderEmail(request.get("senderEmail").toString());
            shipment.setSenderPhone(request.get("senderPhone").toString());
            shipment.setSenderAddress(request.get("senderAddress").toString());
            shipment.setSenderCity(request.get("senderCity").toString());
            shipment.setSenderState(request.get("senderState").toString());
            shipment.setSenderCountry(request.get("senderCountry") != null ? 
                request.get("senderCountry").toString() : "India");
            shipment.setSenderPincode(request.get("senderPincode").toString());
            
            if (request.get("preferredPickupDate") != null) {
                shipment.setPreferredPickupDate(java.time.LocalDateTime.parse(
                    request.get("preferredPickupDate").toString()));
            }
            if (request.get("preferredPickupTimeSlot") != null) {
                shipment.setPreferredPickupTimeSlot(request.get("preferredPickupTimeSlot").toString());
            }

            // Delivery details
            shipment.setReceiverName(request.get("receiverName").toString());
            shipment.setReceiverPhone(request.get("receiverPhone").toString());
            if (request.get("receiverEmail") != null) {
                shipment.setReceiverEmail(request.get("receiverEmail").toString());
            }
            shipment.setReceiverAddress(request.get("receiverAddress").toString());
            shipment.setReceiverCity(request.get("receiverCity").toString());
            shipment.setReceiverState(request.get("receiverState").toString());
            shipment.setReceiverCountry(request.get("receiverCountry") != null ? 
                request.get("receiverCountry").toString() : "India");
            shipment.setReceiverPincode(request.get("receiverPincode").toString());

            // Package details
            shipment.setPackageDescription(request.get("packageDescription") != null ? 
                request.get("packageDescription").toString() : "");
            shipment.setPackageType(Shipment.PackageType.valueOf(
                request.get("packageType").toString().toUpperCase()));
            shipment.setWeight(((Number) request.get("weight")).doubleValue());
            shipment.setLength(request.get("length") != null ? 
                ((Number) request.get("length")).doubleValue() : null);
            shipment.setWidth(request.get("width") != null ? 
                ((Number) request.get("width")).doubleValue() : null);
            shipment.setHeight(request.get("height") != null ? 
                ((Number) request.get("height")).doubleValue() : null);
            shipment.setNumberOfPackages(request.get("numberOfPackages") != null ? 
                ((Number) request.get("numberOfPackages")).intValue() : 1);
            
            if (request.get("declaredValue") != null) {
                shipment.setDeclaredValue(((Number) request.get("declaredValue")).doubleValue());
            }
            shipment.setInsuranceRequired(request.get("insuranceRequired") != null && 
                Boolean.parseBoolean(request.get("insuranceRequired").toString()));
            
            if (request.get("specialHandlingInstructions") != null) {
                shipment.setSpecialHandlingInstructions(request.get("specialHandlingInstructions").toString());
            }

            // Shipping options
            shipment.setDeliveryType(Shipment.DeliveryType.valueOf(
                request.get("deliveryType").toString().toUpperCase()));
            
            // Calculate distance
            Double distance = distanceService.calculateDistance(
                shipment.getSenderPincode(), shipment.getReceiverPincode());
            shipment.setDistance(distance);

            // Courier partner
            Long courierPartnerId = Long.parseLong(request.get("courierPartnerId").toString());
            shipment.setCourierPartner(new com.logisco.model.CourierPartner());
            shipment.getCourierPartner().setId(courierPartnerId);

            // Calculate price
            Map<String, Object> price = pricingService.calculatePrice(shipment, courierPartnerId);
            shipment.setBasePrice(((Number) price.get("basePrice")).doubleValue());
            shipment.setTax(((Number) price.get("serviceTax")).doubleValue());
            shipment.setTotalPrice(((Number) price.get("totalPrice")).doubleValue());

            // COD
            shipment.setCodEnabled(request.get("codEnabled") != null && 
                Boolean.parseBoolean(request.get("codEnabled").toString()));
            if (shipment.getCodEnabled() && request.get("codAmount") != null) {
                shipment.setCodAmount(((Number) request.get("codAmount")).doubleValue());
            }

            // Notification preferences
            shipment.setEmailNotification(request.get("emailNotification") == null || 
                Boolean.parseBoolean(request.get("emailNotification").toString()));
            shipment.setSmsNotification(request.get("smsNotification") == null || 
                Boolean.parseBoolean(request.get("smsNotification").toString()));
            shipment.setWhatsappNotification(request.get("whatsappNotification") != null && 
                Boolean.parseBoolean(request.get("whatsappNotification").toString()));

            // Generate booking ID and tracking number
            String bookingId = "BK" + System.currentTimeMillis();
            String trackingNumber = "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            shipment.setBookingId(bookingId);
            shipment.setTrackingNumber(trackingNumber);
            shipment.setStatus(Shipment.ShipmentStatus.PENDING);

            // Set user (would get from authentication context in production)
            // For now, create a default user or get from request
            // shipment.setUser(user);

            // Save shipment
            Shipment savedShipment = shipmentService.createShipment(shipment);

            // Generate AWB (would integrate with courier partner API)
            String awbNumber = generateAWB(savedShipment);
            savedShipment.setAwbNumber(awbNumber);
            savedShipment = shipmentService.updateShipment(savedShipment.getId(), savedShipment);

            return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "trackingNumber", trackingNumber,
                "awbNumber", awbNumber,
                "shipmentId", savedShipment.getId(),
                "totalPrice", savedShipment.getTotalPrice(),
                "estimatedDelivery", price.get("estimatedDays"),
                "message", "Booking created successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error creating booking: " + e.getMessage()));
        }
    }

    /**
     * Generate AWB number (would integrate with courier partner API)
     */
    private String generateAWB(Shipment shipment) {
        // In production, this would call the courier partner's API to generate AWB
        return "AWB" + System.currentTimeMillis() + shipment.getId();
    }
}

