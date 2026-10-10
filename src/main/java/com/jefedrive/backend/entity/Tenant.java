package com.jefedrive.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String phone;

    @NotBlank
    @Size(max = 100)
    @Column(name = "national_id", nullable = false, length = 100)
    private String nationalId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "driver_license", nullable = false, length = 100)
    private String driverLicense;

    @NotNull
    @Column(name = "driver_license_expiry", nullable = false)
    private LocalDate driverLicenseExpiry;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;


    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
