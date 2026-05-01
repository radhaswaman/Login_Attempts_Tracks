package com.logintracker.controller;

import com.logintracker.entity.User;
import com.logintracker.service.LoginAttemptService;
import com.logintracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid credentials or account locked. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register-user")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String fullName,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        try {
            // Validate inputs
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "Username is required");
                return "register";
            }
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return "register";
            }
            if (password == null || password.length() < 4) {
                model.addAttribute("error", "Password must be at least 4 characters");
                return "register";
            }
            
            userService.createUser(username, password, email, fullName);
            redirectAttributes.addAttribute("success", "true");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            if (fullName != null) {
                model.addAttribute("fullName", fullName);
            }
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);
            
            // Get login attempt history
            var attemptHistory = loginAttemptService.getAttemptHistory(username, 10);
            model.addAttribute("attempts", attemptHistory);
            
            // Get failed attempt count
            int failedAttempts = loginAttemptService.getFailedAttemptCount(username);
            model.addAttribute("failedAttempts", failedAttempts);
            
            // Check if locked and get remaining time
            boolean isLocked = loginAttemptService.isAccountLocked(username);
            if (isLocked) {
                long remainingSeconds = loginAttemptService.getRemainingLockTimeInSeconds(username);
                model.addAttribute("isLocked", true);
                model.addAttribute("remainingSeconds", remainingSeconds);
            } else {
                model.addAttribute("isLocked", false);
            }
        }
        
        return "dashboard";
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}
