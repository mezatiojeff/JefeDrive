
package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Payment;
import com.jefedrive.backend.entity.Rental;
import com.jefedrive.backend.repository.PaymentRepository;
import com.jefedrive.backend.repository.RentalRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.jefedrive.backend.entity.Vehicle;
import com.jefedrive.backend.repository.VehicleRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final ExchangeRateService exchangeRateService;
    private final VehicleRepository vehicleRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            RentalRepository rentalRepository,
            ExchangeRateService exchangeRateService,
            VehicleRepository vehicleRepository) {
        this.paymentRepository = paymentRepository;
        this.rentalRepository = rentalRepository;
        this.exchangeRateService = exchangeRateService;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Payment createPayment(
            Long rentalId,
            BigDecimal paidAmount,
            String paidCurrency,
            Payment.PaymentMethod paymentMethod,
            String transactionReference) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Rental not found"));

        String currency = paidCurrency == null
                ? ""
                : paidCurrency.trim().toUpperCase(Locale.ROOT);

        if (paidAmount == null
                || paidAmount.compareTo(BigDecimal.ZERO) <= 0
                || (!currency.equals("XAF") && !currency.equals("CNY"))
                || paymentMethod == null ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment information");
        }

        if (rental.getStatus() != Rental.Status.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending rentals can receive a payment");
        }

        if (paymentRepository.existsByRentalId(rentalId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A payment already exists for this rental");
        }

        BigDecimal exchangeRate;

        if (currency.equals("XAF")) {
            exchangeRate = BigDecimal.ONE;
        } else {
            exchangeRate = exchangeRateService.getCnyRate().rate();
        }

        BigDecimal rentalAmount = rental.getRentalAmount();
        BigDecimal depositAmount = rental.getDepositAmount();

        BigDecimal totalAmount = rentalAmount.add(depositAmount)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal paidAmountInXaf = paidAmount
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        if (paidAmountInXaf.compareTo(totalAmount) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment must equal the rental amount plus the deposit in XAF");
        }

        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setRentalAmount(rentalAmount);
        payment.setDepositAmount(depositAmount);
        payment.setLateFeeAmount(BigDecimal.ZERO);
        payment.setBaseCurrency("XAF");
        payment.setExchangeRate(exchangeRate);
        payment.setPaidAmount(paidAmount);
        payment.setPaidCurrency(currency);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payment.Status.PENDING);
        payment.setTransactionReference(
                transactionReference == null
                        || transactionReference.isBlank()
                        ? null : transactionReference.trim());
        payment.setCreatedAt(LocalDateTime.now());

        payment.setRefundAmount(BigDecimal.ZERO);

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }

    public List<Payment> getPaymentsByRental(Long rentalId) {
        if (!rentalRepository.existsById(rentalId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Rental not found");
        }

        return paymentRepository.findByRentalIdOrderByCreatedAtDesc(rentalId);
    }

    @Transactional
    public Payment updatePaymentStatus(
            Long paymentId, Payment.Status status) {

        Payment payment = getPaymentById(paymentId);

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        if (payment.getStatus() != Payment.Status.PENDING
                || (status != Payment.Status.PAID
                && status != Payment.Status.FAILED)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid payment status transition");
        }

        if (status == Payment.Status.PAID) {
            Rental rental = rentalRepository.findById(
                            payment.getRental().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Rental not found"));

            if (rental.getStatus() != Rental.Status.PENDING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Only pending rentals can be confirmed");
            }

            Vehicle vehicle = vehicleRepository.findById(
                            rental.getVehicle().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Vehicle not found"));

            if (vehicle.getStatus() != Vehicle.Status.AVAILABLE) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Vehicle is no longer available");
            }

            vehicle.setStatus(Vehicle.Status.RENTED);
            rental.setStatus(Rental.Status.CONFIRMED);
            payment.setPaymentDate(LocalDateTime.now());
        }

        payment.setStatus(status);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment requestRefund(Long rentalId, BigDecimal cancellationFee) {

        List<Payment> payments =
                paymentRepository.findByRentalIdAndStatus(
                        rentalId,
                        Payment.Status.PAID
                );

        if (payments.isEmpty()) {
            return null;
        }

        Payment payment = payments.get(0);

        // Convert cancellation fee from XAF to the paid currency
        BigDecimal refundAmount = payment.getPaidAmount()
                .subtract(
                        cancellationFee.divide(
                                payment.getExchangeRate(),
                                2,
                                RoundingMode.HALF_UP
                        )
                )
                .setScale(2, RoundingMode.HALF_UP);

        payment.setRefundAmount(refundAmount);
        payment.setStatus(Payment.Status.REFUND_PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void cancelPendingPayments(Long rentalId) {

        List<Payment> payments =
                paymentRepository.findByRentalIdAndStatus(
                        rentalId,
                        Payment.Status.PENDING
                );

        for (Payment payment : payments) {
            payment.setStatus(Payment.Status.CANCELLED);
            paymentRepository.save(payment);
        }
    }

    @Transactional
    public Payment confirmRefund(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != Payment.Status.REFUND_PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment is not awaiting a refund"
            );
        }

        payment.setStatus(Payment.Status.REFUNDED);
        payment.setRefundDate(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
}
