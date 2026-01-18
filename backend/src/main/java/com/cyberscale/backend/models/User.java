package com.cyberscale.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 
import jakarta.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore; 

/**
 * Entité représentant un utilisateur de la plateforme.
 */
@Entity
@Table(name = "app_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(nullable = false)
    private String email; 
    
    @Column(nullable = false)
    @JsonIgnore
    private String password; 

    private Integer points = 0;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password; 
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public void setId(Long id) { this.id = id; }

    public void setUsername(String username) { this.username = username; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public Integer getPoints() { return points != null ? points : 0; }
    
    public void setPoints(Integer points) { this.points = points; }

    public void addPoints(int pointsToAdd) {
        this.points = (this.points == null ? 0 : this.points) + pointsToAdd;
    }

}