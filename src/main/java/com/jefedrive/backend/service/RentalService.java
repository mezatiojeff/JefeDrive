
package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Rental;
import com.jefedrive.backend.entity.Vehicle;
import com.jefedrive.backend.repository.RentalRepository;
import com.jefedrive.backend.repository.TenantRepository;
import com.jefedrive.backend.repository.UserRepository;
import com.jefedrive.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import java.math.BigDecimal;
import java.time.Duration;

import com.jefedrive.backend.entity.Tenant;
import com.jefedrive.backend.entity.User;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public RentalService(
            RentalRepository rentalRepository,
            VehicleRepository vehicleRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.vehicleRepository = vehicleRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }


    public void validateRentalPeriod(
            Long vehicleId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (startDate == null || endDate == null
                || !endDate.isAfter(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The end date must be after the start date"
            );
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with id: " + vehicleId
                ));

        if (vehicle.getStatus() != Vehicle.Status.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle is not available"
            );
        }

        List<Rental.Status> blockingStatuses = List.of(
                Rental.Status.PENDING,
                Rental.Status.CONFIRMED,
                Rental.Status.ACTIVE
        );

        boolean overlappingRental =
                rentalRepository
                        .existsByVehicleIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                                vehicleId,
                                blockingStatuses,
                                endDate,
                                startDate
                        );

        if (overlappingRental) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle is already booked for the requested period"
            );
        }
    }


    public BigDecimal calculateRentalAmount(
            BigDecimal dailyPrice,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (dailyPrice == null || dailyPrice.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Daily price must be zero or greater"
            );
        }

        if (startDate == null || endDate == null
                || !endDate.isAfter(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The end date must be after the start date"
            );
        }

        long seconds = Duration.between(startDate, endDate).getSeconds();
        long days = (seconds + 86_399) / 86_400;

        if (days < 1) {
            days = 1;
        }

        return dailyPrice.multiply(BigDecimal.valueOf(days));
    }


    public Rental createRental(
            Long userId,
            Long tenantId,
            Long vehicleId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        if (user.getRole() != User.Role.CLIENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only clients can create rentals"
            );
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant not found"
                ));

        validateRentalPeriod(vehicleId, startDate, endDate);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicle not found"
                ));

        String reference;
        do {
            reference = "JD-" + UUID.randomUUID()
                    .toString().substring(0, 12).toUpperCase();
        } while (rentalRepository.existsByReference(reference));

        Rental rental = new Rental();
        rental.setReference(reference);
        rental.setUser(user);
        rental.setTenant(tenant);
        rental.setVehicle(vehicle);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setDailyPrice(vehicle.getDailyPrice());
        rental.setRentalAmount(calculateRentalAmount(
                vehicle.getDailyPrice(), startDate, endDate
        ));
        rental.setDepositAmount(vehicle.getDepositAmount());
        rental.setLateFee(BigDecimal.ZERO);
        rental.setCancellationFee(BigDecimal.ZERO);
        rental.setDepositRefund(BigDecimal.ZERO);
        rental.setStatus(Rental.Status.PENDING);
        rental.setCreatedAt(LocalDateTime.now());
        rental.setUpdatedAt(LocalDateTime.now());

        return rentalRepository.save(rental);
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rental not found with id: " + id
                ));
    }



    @Transactional
    public Rental confirmRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rental not found with id: " + id
                ));

        if (rental.getStatus() != Rental.Status.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending rentals can be confirmed"
            );
        }

        Vehicle vehicle = rental.getVehicle();

        if (vehicle.getStatus() != Vehicle.Status.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle is not available"
            );
        }

        rental.setStatus(Rental.Status.CONFIRMED);
        rental.setUpdatedAt(LocalDateTime.now());

        vehicle.setStatus(Vehicle.Status.RENTED);
        vehicleRepository.save(vehicle);

        return rentalRepository.save(rental);
    }


    @Transactional
    public Rental cancelRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rental not found with id: " + id
                ));

        if (rental.getStatus() != Rental.Status.PENDING
                && rental.getStatus() != Rental.Status.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending or confirmed rentals can be cancelled"
            );
        }

        BigDecimal cancellationFee = rental.getRentalAmount()
                .multiply(new BigDecimal("0.10"))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal depositRefund = rental.getDepositAmount()
                .subtract(cancellationFee)
                .max(BigDecimal.ZERO);

        rental.setCancellationFee(cancellationFee);
        rental.setDepositRefund(depositRefund);
        rental.setStatus(Rental.Status.CANCELLED);
        rental.setUpdatedAt(LocalDateTime.now());

        Rental savedRental = rentalRepository.save(rental);

        Vehicle vehicle = rental.getVehicle();

        List<Rental.Status> blockingStatuses = List.of(
                Rental.Status.CONFIRMED,
                Rental.Status.ACTIVE
        );

        boolean anotherRentalBlocksVehicle =
                rentalRepository
                        .existsByVehicleIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                                vehicle.getId(),
                                blockingStatuses,
                                rental.getEndDate(),
                                rental.getStartDate()
                        );

        if (!anotherRentalBlocksVehicle
                && vehicle.getStatus() == Vehicle.Status.RENTED) {
            vehicle.setStatus(Vehicle.Status.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return savedRental;
    }

    @Transactional
    public Rental handoverRental(long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rental not found with id: " + id
                ));

        if (rental.getStatus() != Rental.Status.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only confirmed rentals can be handed over"
            );
        }

        Vehicle vehicle = rental.getVehicle();

        if (vehicle.getStatus() != Vehicle.Status.RENTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle is not available for handover"
            );
        }

        rental.setStatus(Rental.Status.ACTIVE);
        rental.setUpdatedAt(LocalDateTime.now());

        vehicle.setStatus(Vehicle.Status.RENTED);

        rentalRepository.save(rental);
        vehicleRepository.save(vehicle);

        return rental;
    }

}

