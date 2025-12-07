package com.logisco.service;

import com.logisco.model.Invoice;
import com.logisco.model.Shipment;
import com.logisco.repository.InvoiceRepository;
import com.logisco.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
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

    public byte[] generateInvoicePdf(String invoiceNumber) {
        Invoice invoice = findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Invoice"));
            document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Issued Date: " + invoice.getIssuedDate()));
            document.add(new Paragraph("Due Date: " + invoice.getDueDate()));
            if (invoice.getShipment() != null) {
                Shipment s = invoice.getShipment();
                document.add(new Paragraph("Shipment Tracking: " + s.getTrackingNumber()));
                document.add(new Paragraph("Receiver: " + s.getReceiverName()));
                document.add(new Paragraph("Sender: " + s.getSenderName()));
            }
            document.add(new Paragraph("Subtotal: " + formatAmount(invoice.getSubtotal())));
            document.add(new Paragraph("Tax: " + formatAmount(invoice.getTaxAmount())));
            document.add(new Paragraph("Discount: " + formatAmount(invoice.getDiscount())));
            document.add(new Paragraph("Total: " + formatAmount(invoice.getTotalAmount())));
            document.add(new Paragraph("Payment Status: " + invoice.getPaymentStatus()));
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF");
        }
    }

    private String formatAmount(Double amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }
}
