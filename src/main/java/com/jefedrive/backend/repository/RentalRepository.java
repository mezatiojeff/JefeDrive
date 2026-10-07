package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
}
