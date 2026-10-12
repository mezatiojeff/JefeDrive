
package com.jefedrive.backend.controller;

import com.jefedrive.backend.entity.Rental;
import com.jefedrive.backend.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public Rental createRental(
            @RequestParam Long userId,
            @RequestParam Long tenantId,
            @RequestParam Long vehicleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        return rentalService.createRental(
                userId, tenantId, vehicleId, startDate, endDate
        );
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.getAllRentals();
    }

    @GetMapping("/{id}")
    public Rental getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }
}

