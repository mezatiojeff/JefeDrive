package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Damage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageRepository extends JpaRepository<Damage, Long> {

    List<Damage> findByRentalReturnId(Long returnId);

}
