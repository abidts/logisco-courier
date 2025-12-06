package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
@Data
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Pickup Details
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    @Column(length = 500)
    private String senderAddress;
    private String senderCity;
    private String senderState;
    private String senderCountry = "India";
    private String senderPincode;
    private LocalDateTime preferredPickupDate;
    private String preferredPickupTimeSlot; // e.g., "10:00-12:00"

    // Delivery Details
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    @Column(length = 500)
    private String receiverAddress;
    private String receiverCity;
    private String receiverState;
    private String receiverCountry = "India";
    private String receiverPincode;

    // Package Details
    private String packageDescription;
    
    @Enumerated(EnumType.STRING)
    private PackageType packageType;
    
    private Double weight; // Actual weight in kg
    private Double length; // cm
    private Double width; // cm
    private Double height; // cm
    private Double volumetricWeight; // Auto-calculated
    private Integer numberOfPackages = 1;
    private Double declaredValue; // Value of goods for insurance
    private Boolean insuranceRequired = false;
    private String specialHandlingInstructions;
    
    // Old dimensions field (kept for backward compatibility)
    private String dimensions;

    @Enumerated(EnumType.STRING)
    private ShipmentType shipmentType;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType; // STANDARD, EXPRESS, SAME_DAY, OVERNIGHT

    @Enumerated(EnumType.STRING)
    private Priority priority; // Kept for backward compatibility

    // Financial
    private Double basePrice;
    private Double tax;
    private Double totalPrice;
    private Double codAmount; // Cash on Delivery amount
    private Boolean codEnabled = false;
    private Boolean paid = false;
    
    // Courier Partner
    @ManyToOne
    @JoinColumn(name = "courier_partner_id")
    private CourierPartner courierPartner;
    
    private String awbNumber; // Airway Bill Number from courier partner
    private String bookingId; // Internal booking ID
    private Double distance; // Distance in km (calculated)
    
    // Notification Preferences
    private Boolean emailNotification = true;
    private Boolean smsNotification = true;
    private Boolean whatsappNotification = false;
    
    // Document uploads
    private String invoiceDocumentUrl;
    private String supportingDocumentsUrl;

    // Dates
    private LocalDateTime pickupDate;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<TrackingHistory> trackingHistory = new ArrayList<>();

    public enum ShipmentType {
        DOMESTIC, INTERNATIONAL, EXPRESS
    }

    public enum ShipmentStatus {
        PENDING, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY,
        DELIVERED, CANCELLED, RETURNED
    }

    public enum Priority {
        STANDARD, EXPRESS, OVERNIGHT
    }
    
    public enum DeliveryType {
        STANDARD, EXPRESS, SAME_DAY, OVERNIGHT
    }
    
    public enum PackageType {
        DOCUMENT, PARCEL, FRAGILE, ELECTRONICS, FOOD, LIQUID, HAZARDOUS, OTHERS
    }
}

