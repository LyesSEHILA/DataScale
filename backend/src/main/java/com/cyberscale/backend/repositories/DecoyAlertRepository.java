package com.cyberscale.backend.repositories;

import com.cyberscale.backend.models.DecoyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecoyAlertRepository extends JpaRepository<DecoyAlert, Long> {
    List<DecoyAlert> findTop20ByOrderByDetectedAtDesc();
}
