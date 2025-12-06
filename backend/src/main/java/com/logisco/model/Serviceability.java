package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "serviceability")
@Data
public class Serviceability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "courier_partner_id")
    private CourierPartner courierPartner;

    private String pincode;
    private String city;
    private String state;
    private String country = "India";

    @Enumerated(EnumType.STRING)
    private ServiceStatus status; // SERVICEABLE, NON_SERVICEABLE, PARTIAL

    private Integer estimatedDays; // Estimated delivery days
    private Boolean codAvailable = false;
    private Boolean reversePickupAvailable = false;

    private LocalDateTime lastChecked = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ServiceStatus {
        SERVICEABLE, NON_SERVICEABLE, PARTIAL
    }
}

