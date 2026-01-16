package com.cyberscale.backend.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Entité de liaison représentant la réussite d'un Challenge par un Utilisateur.
 * Agit comme une preuve de "Flag validé". 
 */
@Entity
@Table(name = "user_challenges", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "challenge_id"})
})
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    private LocalDateTime solvedAt;

    public UserChallenge() {}

    public UserChallenge(User user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
        this.solvedAt = LocalDateTime.now();
    }

    public Challenge getChallenge() { return challenge; }
}