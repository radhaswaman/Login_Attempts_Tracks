package com.logintracker.service;

import com.logintracker.entity.LoginAttempt;
import com.logintracker.entity.User;
import com.logintracker.repository.LoginAttemptRepository;
import com.logintracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;
    
    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;
    
    @Value("${app.security.lock-duration-minutes:30}")
    private int lockDurationMinutes;
    
    /**
     * Record a failed login attempt
     */
    public void recordFailedAttempt(String username, String ipAddress, String failureReason) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setSuccessful(false);
        attempt.setIpAddress(ipAddress);
        attempt.setFailureReason(failureReason);
        if (user != null) {
            attempt.setUser(user);
        }
        
        loginAttemptRepository.save(attempt);
        
        // Check if account should be locked
        if (user != null) {
            int failedCount = getFailedAttemptCount(username);
            if (failedCount >= maxFailedAttempts) {
                lockUser(user);
            }
        }
    }
    
    /**
     * Record a successful login attempt
     */
    public void recordSuccessfulAttempt(String username, String ipAddress) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setSuccessful(true);
        attempt.setIpAddress(ipAddress);
        if (user != null) {
            attempt.setUser(user);
        }
        
        loginAttemptRepository.save(attempt);
        
        // Unlock user if locked
        if (user != null && user.isLocked()) {
            unlockUser(user);
        }
    }
    
    /**
     * Get the count of failed login attempts in the last lock duration
     */
    public int getFailedAttemptCount(String username) {
        LocalDateTime checkTime = LocalDateTime.now().minusMinutes(lockDurationMinutes);
        return loginAttemptRepository.countFailedAttemptsSince(username, checkTime);
    }
    
    /**
     * Check if user should be locked based on failed attempts
     */
    public boolean isAccountLocked(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return false;
        }
        
        if (!user.isLocked()) {
            return false;
        }
        
        // Check if lock time has expired
        if (user.getLockTime() != null) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                unlockUser(user);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get remaining lock time in seconds
     */
    public long getRemainingLockTimeInSeconds(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null || !user.isLocked() || user.getLockTime() == null) {
            return 0;
        }
        
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
        long remainingSeconds = java.time.temporal.ChronoUnit.SECONDS.between(LocalDateTime.now(), unlockTime);
        
        return Math.max(0, remainingSeconds);
    }
    
    /**
     * Lock user account
     */
    public void lockUser(User user) {
        user.setLocked(true);
        user.setLockTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Unlock user account
     */
    public void unlockUser(User user) {
        user.setLocked(false);
        user.setLockTime(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Get login attempt history for a user
     */
    public List<LoginAttempt> getAttemptHistory(String username, int limit) {
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsername(username);
        return attempts.stream().limit(limit).toList();
    }
    
    /**
     * Clear old login attempts (older than specified days)
     */
    public void clearOldAttempts(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        // Implementation depends on your database requirements
    }
}
