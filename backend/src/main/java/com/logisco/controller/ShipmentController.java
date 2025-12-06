package com.logisco.controller;

import com.logisco.model.Shipment;
import com.logisco.model.TrackingHistory;
import com.logisco.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @PostMapping("/shipments")
    public ResponseEntity<?> createShipment(@RequestBody Shipment shipment) {
        try {
            Shipment created = shipmentService.createShipment(shipment);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/shipments")
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/shipments/user/{userId}")
    public ResponseEntity<List<Shipment>> getUserShipments(@PathVariable Long userId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByUser(userId));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<?> trackShipment(@PathVariable String trackingNumber) {
        return shipmentService.findByTrackingNumber(trackingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/shipments/{id}/history")
    public ResponseEntity<List<TrackingHistory>> getTrackingHistory(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getTrackingHistory(id));
    }

    @PutMapping("/shipments/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            Shipment.ShipmentStatus status = Shipment.ShipmentStatus
                    .valueOf(statusUpdate.get("status"));
            String location = statusUpdate.get("location");
            String description = statusUpdate.get("description");

            Shipment updated = shipmentService.updateShipmentStatus(
                    id, status, location, description);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/shipments/{id}")
    public ResponseEntity<?> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.ok(Map.of("message", "Shipment deleted successfully"));
    }
}

