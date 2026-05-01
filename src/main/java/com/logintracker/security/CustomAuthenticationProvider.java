package com.logintracker.security;

import com.logintracker.entity.User;
import com.logintracker.repository.UserRepository;
import com.logintracker.service.LoginAttemptService;
import com.logintracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        String ipAddress = getClientIpAddress();
        
        // Check if account is locked
        if (loginAttemptService.isAccountLocked(username)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTimeInSeconds(username);
            int remainingMinutes = (int) (remainingSeconds / 60);
            loginAttemptService.recordFailedAttempt(username, ipAddress, "Account locked");
            throw new BadCredentialsException(
                "Account is locked. Please try again in " + remainingMinutes + " minutes."
            );
        }
        
        // Verify user exists and password matches
        var user = userService.getUserByUsername(username)
            .orElseThrow(() -> {
                loginAttemptService.recordFailedAttempt(username, ipAddress, "User not found");
                return new BadCredentialsException("Invalid username or password");
            });
        
        if (!userService.verifyPassword(password, user.getPassword())) {
            loginAttemptService.recordFailedAttempt(username, ipAddress, "Invalid password");
            
            int failedCount = loginAttemptService.getFailedAttemptCount(username);
            int maxAttempts = 5; // Default value, can be made configurable
            
            throw new BadCredentialsException(
                "Invalid username or password. " +
                "Failed attempts: " + failedCount + "/" + maxAttempts
            );
        }
        
        // Check if user is enabled
        if (!user.isEnabled()) {
            loginAttemptService.recordFailedAttempt(username, ipAddress, "User disabled");
            throw new BadCredentialsException("User account is disabled");
        }
        
        // Authentication successful
        loginAttemptService.recordSuccessfulAttempt(username, ipAddress);
        
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        return new UsernamePasswordAuthenticationToken(username, password, authorities);
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
    
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            return "UNKNOWN";
        }
        return "UNKNOWN";
    }
}
