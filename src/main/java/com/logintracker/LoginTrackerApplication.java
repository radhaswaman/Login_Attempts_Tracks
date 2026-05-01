package com.logintracker;

import com.logintracker.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LoginTrackerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LoginTrackerApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner initializeUsers(UserService userService) {
        return args -> {
            try {
                // Create sample users for testing
                userService.createUser("admin", "admin123", "admin@example.com", "Administrator");
                userService.createUser("user1", "password123", "user1@example.com", "Test User 1");
                userService.createUser("user2", "password456", "user2@example.com", "Test User 2");
                
                System.out.println("========================================");
                System.out.println("Sample Users Created Successfully");
                System.out.println("========================================");
                System.out.println("Login with:");
                System.out.println("  Username: admin, Password: admin123");
                System.out.println("  Username: user1, Password: password123");
                System.out.println("  Username: user2, Password: password456");
                System.out.println("========================================");
            } catch (IllegalArgumentException e) {
                // Users already exist - ignore
            }
        };
    }
}
