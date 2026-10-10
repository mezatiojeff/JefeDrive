package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    boolean existsByVehicleId(Long vehicleId);
}
