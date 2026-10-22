package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    boolean existsByVehicleId(Long vehicleId);

    List<Repair> findByVehicleIdOrderByRepairDateDesc(Long vehicleId);

    boolean existsByVehicleIdAndStatus(
            Long vehicleId,
            Repair.Status status
    );
}
