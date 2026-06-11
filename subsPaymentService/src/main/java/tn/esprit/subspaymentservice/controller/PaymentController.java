package tn.esprit.subspaymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.subspaymentservice.entity.Payment;
import tn.esprit.subspaymentservice.repository.PaymentRepository;
import tn.esprit.subspaymentservice.service.InvoiceService;
import tn.esprit.subspaymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<String> createPaymentSession(
            @RequestParam Long userId,
            @RequestParam Long planId,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (requesterId == null || !requesterId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        return ResponseEntity.ok(paymentService.createCheckoutSession(userId, planId));
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (requesterId == null || !requesterId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentRepository.findByUserId(userId));
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (requesterId == null || !requesterId.equals(payment.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdf = invoiceService.generateInvoicePdf(payment);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture_coco_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestParam String sessionId) {
        paymentService.confirmPaymentSession(sessionId);
        return ResponseEntity.ok().build();
    }
}
