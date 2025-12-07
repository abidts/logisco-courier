package com.logisco.controller;

import com.logisco.model.Payment;
import com.logisco.model.Shipment;
import com.logisco.repository.PaymentRepository;
import com.logisco.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody Map<String, Object> request) {
        try {
            Long shipmentId = Long.parseLong(request.get("shipmentId").toString());
            Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
            if (shipmentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Shipment not found"));
            }

            Shipment shipment = shipmentOpt.get();
            Double amount = shipment.getTotalPrice();
            String paymentId = generatePaymentId();

            Payment payment = new Payment();
            payment.setShipment(shipment);
            payment.setPaymentId(paymentId);
            payment.setAmount(amount);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setMethod(Payment.PaymentMethod.ONLINE);
            payment.setGateway(Payment.PaymentGateway.MANUAL);
            paymentRepository.save(payment);

            String redirectUrl = "http://localhost:8090/payments/" + paymentId + "/checkout";
            return ResponseEntity.ok(Map.of(
                    "paymentId", paymentId,
                    "amount", amount,
                    "currency", payment.getCurrency(),
                    "redirectUrl", redirectUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private String generatePaymentId() {
        Random random = new Random();
        return "PAY" + System.currentTimeMillis() + random.nextInt(1000);
    }
}
