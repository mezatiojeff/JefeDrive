package com.jefedrive.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String model;

    @NotNull
    @Min(1886)
    @Column(nullable = false)
    private Integer year;

    @NotBlank
    @Size(max = 50)
    @Column(name = "license_plate", nullable = false, unique = true, length = 50)
    private String licensePlate;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String color;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String type;

    @NotNull
    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer seats;

    @NotBlank
    @Size(max = 50)
    @Column(name = "fuel_type", nullable = false, length = 50)
    private String fuelType;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String transmission;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mileage;

    @NotNull
    @DecimalMin("5000.00")
    @Column(name = "daily_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyPrice;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        AVAILABLE,
        RENTED,
        MAINTENANCE,
        UNAVAILABLE
    }
}
