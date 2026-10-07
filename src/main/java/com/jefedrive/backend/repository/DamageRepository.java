package com.jefedrive.backend.repository;

import com.jefedrive.backend.entity.Damage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DamageRepository extends JpaRepository<Damage, Long> {
}
