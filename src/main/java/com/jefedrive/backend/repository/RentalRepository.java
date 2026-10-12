package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;


public interface RentalRepository extends JpaRepository<Rental, Long> {

    boolean existsByVehicleId(Long vehicleId);

    boolean existsByTenantId(Long tenantId);

    boolean existsByReference(String reference);

    boolean existsByVehicleIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
            Long vehicleId,
            Collection<Rental.Status> statuses,
            LocalDateTime requestedEndDate,
            LocalDateTime requestedStartDate
    );


}
