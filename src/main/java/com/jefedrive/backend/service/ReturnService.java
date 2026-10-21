package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Rental;
import com.jefedrive.backend.entity.Return;
import com.jefedrive.backend.entity.Vehicle;
import com.jefedrive.backend.entity.Damage;
import com.jefedrive.backend.repository.RentalRepository;
import com.jefedrive.backend.repository.ReturnRepository;
import com.jefedrive.backend.repository.VehicleRepository;
import com.jefedrive.backend.repository.DamageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.math.RoundingMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



@Service
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final DamageRepository damageRepository;

    public ReturnService(
            ReturnRepository returnRepository,
            RentalRepository rentalRepository,
            VehicleRepository vehicleRepository,
            DamageRepository damageRepository){

        this.returnRepository = returnRepository;
        this.rentalRepository = rentalRepository;
        this.vehicleRepository = vehicleRepository;
        this.damageRepository = damageRepository;
    }

    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }

    public Return getReturnById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Return not found with id: " + id
                ));
    }

    @Transactional
    public Return processReturn(
            Long rentalId,
            LocalDateTime actualReturnDate,
            BigDecimal mileage,
            Boolean hasDamage,
            String observations) {

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rental not found with id: " + rentalId
                ));

        if (rental.getStatus() != Rental.Status.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active rentals can be returned"
            );
        }

        if (returnRepository.existsByRentalId(rentalId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A return already exists for this rental"
            );
        }

        if (actualReturnDate == null || actualReturnDate.isBefore(rental.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid actual return date"
            );
        }

        if (mileage == null || mileage.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mileage must be zero or greater"
            );
        }

        if (hasDamage == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Damage status is required"
            );
        }

        BigDecimal lateFee = BigDecimal.ZERO;

        if (actualReturnDate.isAfter(rental.getEndDate())) {
            long lateMinutes = Duration.between(
                    rental.getEndDate(),
                    actualReturnDate
            ).toMinutes();

            BigDecimal lateHours = BigDecimal.valueOf(lateMinutes)
                    .divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);

            lateFee = rental.getDailyPrice()
                    .multiply(new BigDecimal("1.05"))
                    .multiply(lateHours)
                    .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
        }

        // Damage costs will be calculated after the damage-recording module is connected.

        BigDecimal depositRefund = BigDecimal.ZERO;

        Return vehicleReturn = new Return();
        vehicleReturn.setRental(rental);
        vehicleReturn.setActualReturnDate(actualReturnDate);
        vehicleReturn.setMileage(mileage);
        vehicleReturn.setHasDamage(hasDamage);
        vehicleReturn.setObservations(observations);
        vehicleReturn.setDepositRefund(depositRefund);
        vehicleReturn.setCreatedAt(LocalDateTime.now());

        rental.setLateFee(lateFee);
        rental.setUpdatedAt(LocalDateTime.now());

        Vehicle vehicle = rental.getVehicle();
        vehicle.setMileage(mileage);

        rentalRepository.save(rental);

        return returnRepository.save(vehicleReturn);
    }

        @Transactional
    public Damage recordDamage(Long returnId, String description, BigDecimal cost) {
        Return vehicleReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Return not found with id: " + returnId
                ));


            Rental rental = vehicleReturn.getRental();

            if (rental.getStatus() != Rental.Status.ACTIVE) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Damages can only be recorded before the return is completed"
                );
            }


            if (description == null || description.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Damage description is required"
            );
        }

        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Damage cost must be zero or greater"
            );
        }

        Damage damage = new Damage();
        damage.setRentalReturn(vehicleReturn);
        damage.setDescription(description);
        damage.setCost(cost);
        damage.setCreatedAt(LocalDateTime.now());

        Damage savedDamage = damageRepository.save(damage);

        BigDecimal totalDamageCost = damageRepository
                .findByRentalReturnId(returnId)
                .stream()
                .map(Damage::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal lateFee = rental.getLateFee() == null
                    ? BigDecimal.ZERO
                    : rental.getLateFee();

        BigDecimal depositRefund = rental.getDepositAmount()
                    .subtract(lateFee)
                    .subtract(totalDamageCost)
                    .max(BigDecimal.ZERO);


            vehicleReturn.setHasDamage(true);
        vehicleReturn.setDepositRefund(depositRefund);
        rental.setDepositRefund(depositRefund);

        returnRepository.save(vehicleReturn);
        rentalRepository.save(rental);

        return savedDamage;
    }

    @Transactional
    public Return completeReturn(Long returnId) {
        Return vehicleReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Return not found with id: " + returnId
                ));

        Rental rental = vehicleReturn.getRental();

        if (rental.getStatus() != Rental.Status.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active rentals can be completed"
            );
        }

        BigDecimal totalDamageCost = damageRepository
                .findByRentalReturnId(returnId)
                .stream()
                .map(Damage::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lateFee = rental.getLateFee() == null
                ? BigDecimal.ZERO
                : rental.getLateFee();

        BigDecimal depositRefund = rental.getDepositAmount()
                .subtract(lateFee)
                .subtract(totalDamageCost)
                .max(BigDecimal.ZERO);

        vehicleReturn.setHasDamage(totalDamageCost.compareTo(BigDecimal.ZERO) > 0);
        vehicleReturn.setDepositRefund(depositRefund);

        rental.setLateFee(lateFee);
        rental.setDepositRefund(depositRefund);
        rental.setStatus(Rental.Status.COMPLETED);
        rental.setUpdatedAt(LocalDateTime.now());

        Vehicle vehicle = rental.getVehicle();

        if (vehicleReturn.getHasDamage()) {
            vehicle.setStatus(Vehicle.Status.MAINTENANCE);
        } else {
            vehicle.setStatus(Vehicle.Status.AVAILABLE);
        }

        returnRepository.save(vehicleReturn);
        rentalRepository.save(rental);
        vehicleRepository.save(vehicle);

        return vehicleReturn;
    }

}
