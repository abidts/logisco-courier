package com.logisco.service;

import com.logisco.model.Invoice;
import com.logisco.model.Shipment;
import com.logisco.repository.InvoiceRepository;
import com.logisco.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    public Invoice createInvoice(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setShipment(shipment);
        invoice.setSubtotal(shipment.getBasePrice());
        invoice.setTaxAmount(shipment.getTax());
        invoice.setDiscount(0.0);
        invoice.setTotalAmount(shipment.getTotalPrice());
        invoice.setPaymentStatus(Invoice.PaymentStatus.PENDING);
        invoice.setIssuedDate(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(7));

        return invoiceRepository.save(invoice);
    }

    private String generateInvoiceNumber() {
        String prefix = "INV";
        Random random = new Random();
        int number = 10000 + random.nextInt(90000);
        String year = String.valueOf(LocalDateTime.now().getYear());
        return prefix + year + number;
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    public Optional<Invoice> findByShipmentId(Long shipmentId) {
        return invoiceRepository.findByShipmentId(shipmentId);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice updatePaymentStatus(Long id, Invoice.PaymentStatus status,
                                       Invoice.PaymentMethod method) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setPaymentStatus(status);
        invoice.setPaymentMethod(method);

        if (status == Invoice.PaymentStatus.PAID) {
            invoice.setPaidDate(LocalDateTime.now());

            // Update shipment paid status
            Shipment shipment = invoice.getShipment();
            shipment.setPaid(true);
            shipmentRepository.save(shipment);
        }

        return invoiceRepository.save(invoice);
    }
}

