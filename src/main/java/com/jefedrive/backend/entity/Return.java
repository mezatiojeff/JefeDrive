package com.jefedrive.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns")
@Getter
@Setter
@NoArgsConstructor
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_id", nullable = false, unique = true)
    private Rental rental;

    @Column(name = "actual_return_date", nullable = false)
    private LocalDateTime actualReturnDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mileage;

    @Column(name = "has_damage", nullable = false)
    private Boolean hasDamage = false;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(name = "deposit_refund", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositRefund;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
