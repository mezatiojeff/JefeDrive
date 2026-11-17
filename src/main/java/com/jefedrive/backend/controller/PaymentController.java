
package com.jefedrive.backend.controller;

import com.jefedrive.backend.entity.Payment;
import com.jefedrive.backend.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment createPayment(
            @RequestParam Long rentalId,
            @RequestParam BigDecimal paidAmount,
            @RequestParam String paidCurrency,
            @RequestParam Payment.PaymentMethod paymentMethod,
            @RequestParam(required = false) String transactionReference) {

        return paymentService.createPayment(
                rentalId,
                paidAmount,
                paidCurrency,
                paymentMethod,
                transactionReference);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{paymentId}")
    public Payment getPaymentById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping("/rental/{rentalId}")
    public List<Payment> getPaymentsByRental(
            @PathVariable Long rentalId) {
        return paymentService.getPaymentsByRental(rentalId);
    }

    @PutMapping("/{paymentId}/status")
    public Payment updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam Payment.Status status) {
        return paymentService.updatePaymentStatus(paymentId, status);
    }

    @PutMapping("/{paymentId}/refund/confirm")
    public Payment confirmRefund(@PathVariable Long paymentId) {
        return paymentService.confirmRefund(paymentId);
    }
}
