package com.logintracker.repository;

import com.logintracker.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username AND la.isSuccessful = false " +
           "AND la.attemptTime > :afterTime ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findFailedAttemptsSince(@Param("username") String username, 
                                                 @Param("afterTime") LocalDateTime afterTime);
    
    List<LoginAttempt> findByUsername(String username);
    
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username AND la.isSuccessful = false " +
           "AND la.attemptTime > :afterTime")
    int countFailedAttemptsSince(@Param("username") String username, 
                                  @Param("afterTime") LocalDateTime afterTime);
}
