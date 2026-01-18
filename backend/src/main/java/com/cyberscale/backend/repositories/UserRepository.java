package com.cyberscale.backend.repositories;

import com.cyberscale.backend.models.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface d'acces aux donnees pour l'entite User (Table "app_user").
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);   
    boolean existsByUsername(String username);
}