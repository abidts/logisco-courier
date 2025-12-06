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

    // Sender Information
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    @Column(length = 500)
    private String senderAddress;

    // Receiver Information
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    @Column(length = 500)
    private String receiverAddress;

    // Shipment Details
    private String packageDescription;
    private Double weight;
    private String dimensions;

    @Enumerated(EnumType.STRING)
    private ShipmentType shipmentType;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Financial
    private Double basePrice;
    private Double tax;
    private Double totalPrice;
    private Boolean paid = false;

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
}

