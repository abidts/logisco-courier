package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Column(unique = true)
    private String paymentId; // Payment gateway transaction ID

    private Double amount;
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentGateway gateway; // RAZORPAY, STRIPE, PAYPAL, COD

    private String gatewayTransactionId;
    private String gatewayResponse;
    private String failureReason;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED, CANCELLED
    }

    public enum PaymentMethod {
        ONLINE, COD, CARD, UPI, NETBANKING, WALLET
    }

    public enum PaymentGateway {
        RAZORPAY, STRIPE, PAYPAL, COD, MANUAL
    }
}

