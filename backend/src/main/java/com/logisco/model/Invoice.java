package com.logisco.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String invoiceNumber;

    @OneToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    private Double subtotal;
    private Double taxAmount;
    private Double discount;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDateTime issuedDate = LocalDateTime.now();
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;

    private String notes;

    public enum PaymentStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, ONLINE
    }
}

