package com.cyberscale.backend.repositories;

import com.cyberscale.backend.models.DetectedThreat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectedThreatRepository extends JpaRepository<DetectedThreat, Long> {
}