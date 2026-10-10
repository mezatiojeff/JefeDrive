package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Vehicle;
import com.jefedrive.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.jefedrive.backend.repository.RentalRepository;
import com.jefedrive.backend.repository.RepairRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final RepairRepository repairRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            RepairRepository repairRepository) {

        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.repairRepository = repairRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }


    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with id: " + id
                ));
    }


    public Vehicle createVehicle(Vehicle vehicle) {
        try {
            return vehicleRepository.saveAndFlush(vehicle);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A vehicle with this license plate already exists"
            );
        }
    }


    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(id);


        if (vehicleRepository.existsByLicensePlateAndIdNot(
                vehicleDetails.getLicensePlate(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A vehicle with this license plate already exists"
            );
        }



        vehicle.setBrand(vehicleDetails.getBrand());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setYear(vehicleDetails.getYear());
        vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        vehicle.setColor(vehicleDetails.getColor());
        vehicle.setType(vehicleDetails.getType());
        vehicle.setSeats(vehicleDetails.getSeats());
        vehicle.setFuelType(vehicleDetails.getFuelType());
        vehicle.setTransmission(vehicleDetails.getTransmission());
        vehicle.setMileage(vehicleDetails.getMileage());
        vehicle.setDailyPrice(vehicleDetails.getDailyPrice());
        vehicle.setDepositAmount(vehicleDetails.getDepositAmount());
        vehicle.setStatus(vehicleDetails.getStatus());
        vehicle.setImageUrl(vehicleDetails.getImageUrl());

        return vehicleRepository.save(vehicle);
    }


    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);

        if (rentalRepository.existsByVehicleId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a vehicle that has rental history"
            );
        }

        if (repairRepository.existsByVehicleId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a vehicle that has repair history"
            );
        }

        vehicleRepository.delete(vehicle);
    }


}
