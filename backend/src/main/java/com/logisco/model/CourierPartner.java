package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "courier_partners")
@Data
public class CourierPartner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "DELHIVERY", "DTDC", "FEDEX"

    private String apiKey;
    private String apiSecret;
    private String apiUrl;
    private Boolean active = true;
    private Boolean integrated = false;

    // Pricing configuration
    private Double baseRate; // Base rate per kg
    private Double fuelSurcharge; // Percentage
    private Double serviceTax; // Percentage
    private Double minCharge; // Minimum charge

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

