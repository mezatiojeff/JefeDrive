
package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Repair;
import com.jefedrive.backend.entity.Vehicle;
import com.jefedrive.backend.repository.RepairRepository;
import com.jefedrive.backend.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepairService {

    private final RepairRepository repairRepository;
    private final VehicleRepository vehicleRepository;

    public RepairService(
            RepairRepository repairRepository,
            VehicleRepository vehicleRepository) {
        this.repairRepository = repairRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Repair createRepair(
            Long vehicleId,
            LocalDate repairDate,
            String description,
            BigDecimal cost,
            String notes) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicle not found"));

        if (repairDate == null || description == null
                || description.isBlank() || cost == null
                || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Repair date, description and a non-negative cost are required");
        }

        Repair repair = new Repair();
        repair.setVehicle(vehicle);
        repair.setRepairDate(repairDate);
        repair.setDescription(description.trim());
        repair.setCost(cost);
        repair.setNotes(notes);
        repair.setStatus(Repair.Status.IN_PROGRESS);
        repair.setCreatedAt(LocalDateTime.now());

        vehicle.setStatus(Vehicle.Status.MAINTENANCE);
        vehicleRepository.save(vehicle);

        return repairRepository.save(repair);
    }

    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }

    public List<Repair> getRepairsByVehicle(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Vehicle not found");
        }

        return repairRepository.findByVehicleIdOrderByRepairDateDesc(
                vehicleId);
    }

    @Transactional
    public Repair completeRepair(Long repairId) {
        Repair repair = repairRepository.findById(repairId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Repair not found"));

        if (repair.getStatus() == Repair.Status.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Repair is already completed");
        }

        repair.setStatus(Repair.Status.COMPLETED);

        Repair savedRepair = repairRepository.save(repair);
        Vehicle vehicle = repair.getVehicle();

        boolean anotherRepairInProgress =
                repairRepository.existsByVehicleIdAndStatus(
                        vehicle.getId(),
                        Repair.Status.IN_PROGRESS);

        if (!anotherRepairInProgress) {
            vehicle.setStatus(Vehicle.Status.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return savedRepair;
    }
}

