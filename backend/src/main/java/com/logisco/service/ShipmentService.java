package com.logisco.service;

import com.logisco.model.Shipment;
import com.logisco.model.TrackingHistory;
import com.logisco.repository.ShipmentRepository;
import com.logisco.repository.TrackingHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private TrackingHistoryRepository trackingHistoryRepository;

    public Shipment createShipment(Shipment shipment) {
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setStatus(Shipment.ShipmentStatus.PENDING);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());

        // Defaults to avoid null-based errors in pricing
        if (shipment.getShipmentType() == null) {
            shipment.setShipmentType(Shipment.ShipmentType.DOMESTIC);
        }
        if (shipment.getPriority() == null) {
            shipment.setPriority(Shipment.Priority.STANDARD);
        }
        if (shipment.getWeight() != null && shipment.getWeight() < 0) {
            shipment.setWeight(0.0);
        }

        // Calculate price
        calculatePrice(shipment);

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Add initial tracking history
        addTrackingHistory(savedShipment, Shipment.ShipmentStatus.PENDING,
                "Order Created", "Shipment order has been created");

        return savedShipment;
    }

    private String generateTrackingNumber() {
        String prefix = "LOG";
        Random random = new Random();
        long number;
        String trackingNumber;

        do {
            number = 100000000000L + (long)(random.nextDouble() * 900000000000L);
            trackingNumber = prefix + number;
        } while (shipmentRepository.existsByTrackingNumber(trackingNumber));

        return trackingNumber;
    }

    private void calculatePrice(Shipment shipment) {
        double basePrice = 10.0;

        // Calculate based on weight
        if (shipment.getWeight() != null) {
            basePrice += shipment.getWeight() * 2.5;
        }

        // Calculate based on type (null-safe)
        if (shipment.getShipmentType() != null) {
            switch (shipment.getShipmentType()) {
                case INTERNATIONAL:
                    basePrice *= 3;
                    break;
                case EXPRESS:
                    basePrice *= 2;
                    break;
                default:
                    break;
            }
        }

        // Calculate based on priority (null-safe)
        if (shipment.getPriority() != null) {
            switch (shipment.getPriority()) {
                case OVERNIGHT:
                    basePrice *= 2.5;
                    break;
                case EXPRESS:
                    basePrice *= 1.5;
                    break;
                default:
                    break;
            }
        }

        double tax = basePrice * 0.18; // 18% tax
        double total = basePrice + tax;

        shipment.setBasePrice(Math.round(basePrice * 100.0) / 100.0);
        shipment.setTax(Math.round(tax * 100.0) / 100.0);
        shipment.setTotalPrice(Math.round(total * 100.0) / 100.0);
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public List<Shipment> getShipmentsByUser(Long userId) {
        return shipmentRepository.findByUserId(userId);
    }

    public Shipment updateShipmentStatus(Long id, Shipment.ShipmentStatus status,
                                         String location, String description) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        shipment.setStatus(status);
        shipment.setUpdatedAt(LocalDateTime.now());

        if (status == Shipment.ShipmentStatus.DELIVERED) {
            shipment.setActualDelivery(LocalDateTime.now());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        addTrackingHistory(updatedShipment, status, location, description);

        return updatedShipment;
    }

    private void addTrackingHistory(Shipment shipment, Shipment.ShipmentStatus status,
                                    String location, String description) {
        TrackingHistory history = new TrackingHistory();
        history.setShipment(shipment);
        history.setStatus(status);
        history.setLocation(location);
        history.setDescription(description);
        history.setTimestamp(LocalDateTime.now());

        trackingHistoryRepository.save(history);
    }

    public List<TrackingHistory> getTrackingHistory(Long shipmentId) {
        return trackingHistoryRepository.findByShipmentIdOrderByTimestampDesc(shipmentId);
    }

    public Shipment updateShipment(Long id, Shipment shipmentDetails) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        
        // Update fields
        if (shipmentDetails.getAwbNumber() != null) {
            shipment.setAwbNumber(shipmentDetails.getAwbNumber());
        }
        if (shipmentDetails.getStatus() != null) {
            shipment.setStatus(shipmentDetails.getStatus());
        }
        shipment.setUpdatedAt(LocalDateTime.now());
        
        return shipmentRepository.save(shipment);
    }

    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }
}
