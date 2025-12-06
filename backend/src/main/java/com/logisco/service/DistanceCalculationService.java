package com.logisco.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceCalculationService {

    /**
     * Calculate distance between two pincodes
     * In production, this would integrate with Google Maps API or similar
     */
    public Double calculateDistance(String pickupPincode, String deliveryPincode) {
        // TODO: Integrate with Google Maps Distance Matrix API
        // For now, return a mock distance based on pincode difference
        
        if (pickupPincode == null || deliveryPincode == null) {
            return 100.0; // Default distance
        }

        try {
            // Simple mock calculation based on pincode difference
            int pickup = Integer.parseInt(pickupPincode.substring(0, Math.min(3, pickupPincode.length())));
            int delivery = Integer.parseInt(deliveryPincode.substring(0, Math.min(3, deliveryPincode.length())));
            
            int diff = Math.abs(pickup - delivery);
            return Math.max(10.0, diff * 0.5); // Minimum 10km, scale by difference
        } catch (Exception e) {
            return 100.0; // Default distance
        }
    }

    /**
     * Calculate distance between two addresses
     * This would use Google Maps Geocoding and Distance Matrix API
     */
    public Double calculateDistanceByAddress(String pickupAddress, String deliveryAddress) {
        // TODO: Integrate with Google Maps API
        // Example: Use Distance Matrix API to get actual distance
        return 100.0; // Placeholder
    }
}

