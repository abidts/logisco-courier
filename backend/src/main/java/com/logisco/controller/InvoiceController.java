package com.logisco.controller;

import com.logisco.model.Invoice;
import com.logisco.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/shipment/{shipmentId}")
    public ResponseEntity<?> createInvoice(@PathVariable Long shipmentId) {
        try {
            Invoice invoice = invoiceService.createInvoice(shipmentId);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<?> getInvoice(@PathVariable String invoiceNumber) {
        return invoiceService.findByInvoiceNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{invoiceNumber}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable String invoiceNumber) {
        byte[] pdf = invoiceService.generateInvoicePdf(invoiceNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoiceNumber + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<?> getInvoiceByShipment(@PathVariable Long shipmentId) {
        return invoiceService.findByShipmentId(shipmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<?> updatePayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payment) {
        try {
            Invoice.PaymentStatus status = Invoice.PaymentStatus
                    .valueOf(payment.get("status"));
            Invoice.PaymentMethod method = Invoice.PaymentMethod
                    .valueOf(payment.get("method"));

            Invoice updated = invoiceService.updatePaymentStatus(id, status, method);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
