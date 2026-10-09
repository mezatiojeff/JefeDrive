package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Vehicle;
import com.jefedrive.backend.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }


    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(id);

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
        vehicleRepository.delete(vehicle);
    }
}
