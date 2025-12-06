package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rules")
@Data
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "courier_partner_id")
    private CourierPartner courierPartner;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    private PackageType packageType;

    private Double minWeight; // kg
    private Double maxWeight; // kg
    private Double ratePerKg;
    private Double fixedCharge;
    private Double distanceMultiplier; // Rate per km

    // Additional charges
    private Double codCharge; // Percentage or fixed
    private Double fragileCharge;
    private Double insuranceCharge; // Percentage of declared value
    private Double fuelSurcharge; // Percentage
    private Double serviceTax; // Percentage

    private Boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum DeliveryType {
        STANDARD, EXPRESS, SAME_DAY, OVERNIGHT
    }

    public enum PackageType {
        DOCUMENT, PARCEL, FRAGILE, ELECTRONICS, FOOD, LIQUID, HAZARDOUS, OTHERS
    }
}

