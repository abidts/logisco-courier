package com.logisco.service;

import com.logisco.model.CourierPartner;
import com.logisco.model.PricingRule;
import com.logisco.model.Shipment;
import com.logisco.repository.CourierPartnerRepository;
import com.logisco.repository.PricingRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {

    @Autowired
    private CourierPartnerRepository courierPartnerRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private DistanceCalculationService distanceService;

    /**
     * Calculate volumetric weight
     * Formula: (Length × Width × Height) / 5000 (for cm to kg conversion)
     */
    public Double calculateVolumetricWeight(Double length, Double width, Double height) {
        if (length == null || width == null || height == null) {
            return 0.0;
        }
        return (length * width * height) / 5000.0;
    }

    /**
     * Get chargeable weight (higher of actual or volumetric)
     */
    public Double getChargeableWeight(Double actualWeight, Double volumetricWeight) {
        return Math.max(actualWeight != null ? actualWeight : 0.0, 
                       volumetricWeight != null ? volumetricWeight : 0.0);
    }

    /**
     * Calculate price for a shipment
     */
    public Map<String, Object> calculatePrice(Shipment shipment, Long courierPartnerId) {
        Map<String, Object> result = new HashMap<>();
        
        CourierPartner partner = courierPartnerRepository.findById(courierPartnerId)
                .orElseThrow(() -> new RuntimeException("Courier partner not found"));

        // Calculate volumetric weight
        Double volumetricWeight = calculateVolumetricWeight(
            shipment.getLength(), shipment.getWidth(), shipment.getHeight()
        );
        shipment.setVolumetricWeight(volumetricWeight);

        // Get chargeable weight
        Double chargeableWeight = getChargeableWeight(shipment.getWeight(), volumetricWeight);

        // Convert Shipment enums to PricingRule enums
        PricingRule.DeliveryType deliveryType = convertDeliveryType(shipment.getDeliveryType());
        PricingRule.PackageType packageType = convertPackageType(shipment.getPackageType());
        
        // Find applicable pricing rules
        List<PricingRule> rules = pricingRuleRepository.findApplicableRules(
            courierPartnerId,
            deliveryType,
            packageType,
            chargeableWeight
        );

        if (rules.isEmpty()) {
            // Use default pricing if no specific rule found
            return calculateDefaultPrice(partner, shipment, chargeableWeight);
        }

        PricingRule rule = rules.get(0); // Use first matching rule

        // Base price calculation
        Double basePrice = rule.getFixedCharge() != null ? rule.getFixedCharge() : 0.0;
        
        // Weight-based charge
        if (rule.getRatePerKg() != null && chargeableWeight > 0) {
            basePrice += (chargeableWeight * rule.getRatePerKg());
        }

        // Distance-based charge
        if (shipment.getDistance() != null && rule.getDistanceMultiplier() != null) {
            basePrice += (shipment.getDistance() * rule.getDistanceMultiplier());
        }

        // Additional charges
        Double codCharge = 0.0;
        if (shipment.getCodEnabled() && rule.getCodCharge() != null) {
            if (rule.getCodCharge() < 1.0) {
                // Percentage
                codCharge = shipment.getCodAmount() * rule.getCodCharge();
            } else {
                // Fixed amount
                codCharge = rule.getCodCharge();
            }
        }

        Double fragileCharge = 0.0;
        if (packageType == PricingRule.PackageType.FRAGILE && rule.getFragileCharge() != null) {
            fragileCharge = rule.getFragileCharge();
        }

        Double insuranceCharge = 0.0;
        if (shipment.getInsuranceRequired() && shipment.getDeclaredValue() != null && rule.getInsuranceCharge() != null) {
            insuranceCharge = shipment.getDeclaredValue() * (rule.getInsuranceCharge() / 100.0);
        }

        // Fuel surcharge
        Double fuelSurcharge = 0.0;
        if (rule.getFuelSurcharge() != null) {
            fuelSurcharge = basePrice * (rule.getFuelSurcharge() / 100.0);
        } else if (partner.getFuelSurcharge() != null) {
            fuelSurcharge = basePrice * (partner.getFuelSurcharge() / 100.0);
        }

        // Service tax
        Double serviceTax = 0.0;
        if (rule.getServiceTax() != null) {
            serviceTax = basePrice * (rule.getServiceTax() / 100.0);
        } else if (partner.getServiceTax() != null) {
            serviceTax = basePrice * (partner.getServiceTax() / 100.0);
        }

        // Total calculation
        Double subtotal = basePrice + codCharge + fragileCharge + insuranceCharge + fuelSurcharge;
        Double totalPrice = subtotal + serviceTax;

        // Apply minimum charge
        if (partner.getMinCharge() != null && totalPrice < partner.getMinCharge()) {
            totalPrice = partner.getMinCharge();
        }

        result.put("basePrice", basePrice);
        result.put("codCharge", codCharge);
        result.put("fragileCharge", fragileCharge);
        result.put("insuranceCharge", insuranceCharge);
        result.put("fuelSurcharge", fuelSurcharge);
        result.put("serviceTax", serviceTax);
        result.put("subtotal", subtotal);
        result.put("totalPrice", totalPrice);
        result.put("chargeableWeight", chargeableWeight);
        result.put("volumetricWeight", volumetricWeight);
        result.put("courierPartner", partner.getName());
        result.put("estimatedDays", calculateEstimatedDays(shipment.getDeliveryType(), shipment.getDistance()));

        return result;
    }

    /**
     * Calculate price for multiple courier partners
     */
    public List<Map<String, Object>> calculatePricesForAllPartners(Shipment shipment) {
        List<Map<String, Object>> prices = new ArrayList<>();
        
        List<CourierPartner> partners = courierPartnerRepository.findAll();
        
        for (CourierPartner partner : partners) {
            if (partner.getActive()) {
                try {
                    Map<String, Object> price = calculatePrice(shipment, partner.getId());
                    price.put("courierPartnerId", partner.getId());
                    price.put("courierPartnerCode", partner.getCode());
                    prices.add(price);
                } catch (Exception e) {
                    // Skip if pricing calculation fails for this partner
                }
            }
        }
        
        return prices;
    }

    /**
     * Fallback pricing when no courier partners are configured (self-service)
     */
    public Map<String, Object> calculateSelfPrice(Shipment shipment) {
        Map<String, Object> result = new HashMap<>();

        // Volumetric weight
        Double volumetricWeight = calculateVolumetricWeight(
                shipment.getLength(), shipment.getWidth(), shipment.getHeight());
        shipment.setVolumetricWeight(volumetricWeight);

        // Chargeable weight
        Double chargeableWeight = getChargeableWeight(shipment.getWeight(), volumetricWeight);

        // Base price similar to ShipmentService fallback
        double basePrice = 10.0;
        if (chargeableWeight != null) {
            basePrice += chargeableWeight * 2.5;
        }

        // Simple tax (18%)
        double serviceTax = basePrice * 0.18;
        double totalPrice = basePrice + serviceTax;

        result.put("basePrice", basePrice);
        result.put("fuelSurcharge", 0.0);
        result.put("serviceTax", serviceTax);
        result.put("totalPrice", totalPrice);
        result.put("chargeableWeight", chargeableWeight);
        result.put("volumetricWeight", volumetricWeight);
        result.put("courierPartner", "Self");
        result.put("estimatedDays", calculateEstimatedDays(shipment.getDeliveryType(), shipment.getDistance()));
        return result;
    }

    private Map<String, Object> calculateDefaultPrice(CourierPartner partner, Shipment shipment, Double chargeableWeight) {
        Map<String, Object> result = new HashMap<>();
        
        Double basePrice = partner.getBaseRate() != null ? 
            (chargeableWeight * partner.getBaseRate()) : 0.0;
        
        if (partner.getMinCharge() != null && basePrice < partner.getMinCharge()) {
            basePrice = partner.getMinCharge();
        }

        Double fuelSurcharge = partner.getFuelSurcharge() != null ?
            basePrice * (partner.getFuelSurcharge() / 100.0) : 0.0;
        
        Double serviceTax = partner.getServiceTax() != null ?
            basePrice * (partner.getServiceTax() / 100.0) : 0.0;

        Double totalPrice = basePrice + fuelSurcharge + serviceTax;

        result.put("basePrice", basePrice);
        result.put("fuelSurcharge", fuelSurcharge);
        result.put("serviceTax", serviceTax);
        result.put("totalPrice", totalPrice);
        result.put("chargeableWeight", chargeableWeight);
        result.put("courierPartner", partner.getName());
        result.put("estimatedDays", calculateEstimatedDays(shipment.getDeliveryType(), shipment.getDistance()));

        return result;
    }

    private Integer calculateEstimatedDays(Shipment.DeliveryType deliveryType, Double distance) {
        if (deliveryType == null) {
            return 5; // Default
        }

        switch (deliveryType) {
            case SAME_DAY:
                return 1;
            case OVERNIGHT:
                return 1;
            case EXPRESS:
                return 2;
            case STANDARD:
            default:
                if (distance != null) {
                    if (distance < 100) return 2;
                    if (distance < 500) return 3;
                    if (distance < 1000) return 4;
                    return 5;
                }
                return 5;
        }
    }

    /**
     * Convert Shipment.DeliveryType to PricingRule.DeliveryType
     */
    private PricingRule.DeliveryType convertDeliveryType(Shipment.DeliveryType shipmentType) {
        if (shipmentType == null) {
            return PricingRule.DeliveryType.STANDARD;
        }
        try {
            return PricingRule.DeliveryType.valueOf(shipmentType.name());
        } catch (IllegalArgumentException e) {
            return PricingRule.DeliveryType.STANDARD;
        }
    }

    /**
     * Convert Shipment.PackageType to PricingRule.PackageType
     */
    private PricingRule.PackageType convertPackageType(Shipment.PackageType shipmentType) {
        if (shipmentType == null) {
            return PricingRule.PackageType.PARCEL;
        }
        try {
            return PricingRule.PackageType.valueOf(shipmentType.name());
        } catch (IllegalArgumentException e) {
            return PricingRule.PackageType.PARCEL;
        }
    }
}
