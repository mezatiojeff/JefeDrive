package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByRentalIdOrderByCreatedAtDesc(Long rentalId);

    boolean existsByRentalId(Long rentalId);

    List<Payment> findByRentalIdAndStatus(Long rentalId, Payment.Status status);
}