package com.logintracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime = LocalDateTime.now();
    
    @Column(name = "is_successful", nullable = false)
    private boolean isSuccessful = false;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
