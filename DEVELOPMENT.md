# 🔨 Development & Enhancement Guide

This guide is for developers who want to understand, maintain, or extend the Login Attempts Tracker application.

## 📖 Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Web Layer                             │
│  (AuthController, HTML Templates, CSS)                  │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│             Spring Security Layer                        │
│  (SecurityConfig, CustomAuthenticationProvider)         │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│           Business Logic Layer                           │
│  (UserService, LoginAttemptService)                     │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│             Data Access Layer                            │
│  (UserRepository, LoginAttemptRepository)               │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────────┐
│               Database Layer                             │
│  (H2, MySQL, PostgreSQL - configurable)                 │
└─────────────────────────────────────────────────────────┘
```

## 🔄 Request Flow

### Login Request Flow

```
1. User enters credentials on login.html
                ↓
2. Form POST to /login (AuthController.login())
                ↓
3. Spring Security intercepts (SecurityConfig)
                ↓
4. CustomAuthenticationProvider.authenticate()
                ↓
5. UserService.getUserByUsername()
                ↓
6. Query UserRepository
                ↓
7. Validate password (BCrypt)
                ↓
8. If failed: LoginAttemptService.recordFailedAttempt()
                ↓
9. Check if locked: LoginAttemptService.isAccountLocked()
                ↓
10. If successful: LoginAttemptService.recordSuccessfulAttempt()
                ↓
11. Redirect to /dashboard with authentication
```

## 🛠️ Adding New Features

### 1. Add Email Notifications

**Files to modify/create:**
- Create `service/EmailService.java`
- Update `application.yml` with email config
- Modify `LoginAttemptService.recordFailedAttempt()` to call EmailService

**Example implementation:**

```java
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    
    public void sendLockNotification(User user, long remainingMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account Locked - Login Attempts Tracker");
        message.setText("Your account has been locked for " + remainingMinutes + " minutes...");
        mailSender.send(message);
    }
}
```

### 2. Add Admin Dashboard

**Create new controller:**
- `controller/AdminController.java`

**Create new templates:**
- `templates/admin-dashboard.html`
- `templates/user-management.html`

**Add new repository methods:**
```java
// In UserRepository
List<User> findAllLockedUsers();
List<User> findByCreatedAtAfter(LocalDateTime date);
```

### 3. Add Two-Factor Authentication (2FA)

**Create new entity:**
```java
@Entity
public class TwoFactorAuth {
    @Id
    private Long userId;
    private String secret;
    private boolean enabled;
    private LocalDateTime createdAt;
}
```

**Modify SecurityConfig:**
- Add 2FA check after password verification

### 4. Add Login History Export (CSV)

**Create new controller:**
```java
@GetMapping("/login-attempts/export")
public ResponseEntity<Resource> exportAttempts() {
    // Generate CSV from LoginAttempt records
    // Return as downloadable file
}
```

### 5. Add API Endpoints

**Create new controller:**
- `controller/ApiController.java`

**Example:**
```java
@RestController
@RequestMapping("/api/attempts")
public class ApiController {
    
    @GetMapping("/{username}")
    public List<LoginAttempt> getAttempts(@PathVariable String username) {
        // Return JSON response
    }
    
    @PostMapping("/lock/{username}")
    public void lockAccount(@PathVariable String username) {
        // Lock account manually
    }
}
```

## 🔍 Understanding Key Classes

### CustomAuthenticationProvider

**Purpose:** Authenticates users with attempt tracking

**Key Methods:**
- `authenticate()` - Main authentication logic
- `getClientIpAddress()` - Extracts IP from request

**Extension Points:**
- Add custom validation logic
- Add rate limiting per IP
- Add external authentication (LDAP, OAuth2)

### LoginAttemptService

**Purpose:** Manages attempt tracking and locking

**Key Methods:**
- `recordFailedAttempt()` - Record failed login
- `recordSuccessfulAttempt()` - Record successful login
- `isAccountLocked()` - Check if account is locked
- `getRemainingLockTimeInSeconds()` - Get unlock countdown
- `lockUser()` / `unlockUser()` - Manual lock/unlock

**Extension Points:**
- Add notification logic
- Add IP whitelist/blacklist
- Add progressive delay (exponential backoff)

### AuthController

**Purpose:** Web endpoints for authentication

**Endpoints:**
- `GET /login` - Show login page
- `POST /login` - Process login
- `GET /register` - Show registration page
- `POST /register-user` - Process registration
- `GET /dashboard` - Show user dashboard
- `GET /logout` - Logout user

**Extension Points:**
- Add `POST /api/login` for JSON API
- Add AJAX endpoints for dynamic pages
- Add remember-me functionality

## 📊 Database Optimization

### Indexes to Add

```sql
-- For faster failed attempt queries
CREATE INDEX idx_login_attempts_username_time 
ON login_attempts(username, attempt_time);

-- For faster user lookups
CREATE INDEX idx_users_username 
ON users(username);

-- For checking active locks
CREATE INDEX idx_users_locked 
ON users(is_locked, lock_time);
```

### Query Optimization

**Current Query (LoginAttemptRepository):**
```java
@Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username AND la.isSuccessful = false " +
       "AND la.attemptTime > :afterTime")
int countFailedAttemptsSince(@Param("username") String username, @Param("afterTime") LocalDateTime afterTime);
```

**Why it's optimized:**
- Only counts failed attempts in recent time window
- Uses indexed columns (username, attemptTime)
- Avoids loading all records into memory

## 🧪 Writing Tests

### Unit Test Example

```java
@SpringBootTest
public class LoginAttemptServiceTest {
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testAccountLockingAfterFailedAttempts() {
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);
        
        // Record 5 failed attempts
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailedAttempt("testuser", "127.0.0.1", "Wrong password");
        }
        
        // Assert account is locked
        assertTrue(loginAttemptService.isAccountLocked("testuser"));
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testLoginWithValidCredentials() throws Exception {
        mockMvc.perform(post("/login")
            .param("username", "admin")
            .param("password", "admin123"))
            .andExpect(redirectedUrl("/dashboard"));
    }
}
```

## 🐛 Debugging Guide

### Enable SQL Logging

In `application.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Enable Spring Security Debug

In `SecurityConfig`:
```java
@Override
public void configure(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(...).and().debug();
}
```

### Add Custom Logging

```java
@Service
public class LoginAttemptService {
    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);
    
    public void recordFailedAttempt(String username, String ip, String reason) {
        logger.debug("Recording failed attempt for user: {}, IP: {}, Reason: {}", username, ip, reason);
        // ... implementation
    }
}
```

## 📈 Performance Tuning

### Caching

Add Redis caching for frequently accessed data:

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users", "lockStatus");
    }
}
```

Use in service:
```java
@Cacheable("users")
public Optional<User> getUserByUsername(String username) {
    return userRepository.findByUsername(username);
}
```

### Pagination

For large login attempt histories:

```java
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    Page<LoginAttempt> findByUsername(String username, Pageable pageable);
}
```

## 🔒 Security Best Practices

### Input Validation

```java
private void validateUsername(String username) {
    if (username == null || username.length() < 3 || username.length() > 50) {
        throw new IllegalArgumentException("Invalid username");
    }
    if (!username.matches("^[a-zA-Z0-9_]*$")) {
        throw new IllegalArgumentException("Username contains invalid characters");
    }
}
```

### Prevent SQL Injection

✅ Good (using parameterized queries):
```java
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);
```

❌ Bad (string concatenation):
```java
Query query = em.createQuery("SELECT u FROM User u WHERE u.username = '" + username + "'");
```

### Prevent XSS Attacks

In Thymeleaf templates, use:
```html
<!-- Good - Thymeleaf escapes by default -->
<span th:text="${user.username}"></span>

<!-- Bad - Don't use unescape unless necessary -->
<span th:utext="${user.username}"></span>
```

## 📚 Code Style Guidelines

### Naming Conventions

```java
// Classes
public class LoginAttemptService { }      // PascalCase

// Methods
public void recordFailedAttempt() { }     // camelCase

// Constants
private static final int MAX_ATTEMPTS = 5;  // UPPER_SNAKE_CASE

// Variables
private String ipAddress;                 // camelCase
```

### Documentation

```java
/**
 * Records a failed login attempt for the specified user.
 * 
 * If the number of failed attempts exceeds the maximum threshold,
 * the user account is automatically locked.
 * 
 * @param username The username of the user
 * @param ipAddress The IP address from which login was attempted
 * @param failureReason The reason for the failed attempt
 * @throws IllegalArgumentException if username is empty
 */
public void recordFailedAttempt(String username, String ipAddress, String failureReason) {
    // Implementation
}
```

## 🚀 Deployment Considerations

### Environment-Specific Configuration

Create separate configuration files:
- `application-dev.yml` - Development settings
- `application-prod.yml` - Production settings

### Set active profile:
```bash
java -jar app.jar --spring.profiles.active=prod
```

### Production Checklist

- [ ] Use strong password hashing
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS properly
- [ ] Set up rate limiting
- [ ] Enable logging and monitoring
- [ ] Configure database backups
- [ ] Set up error tracking (Sentry, etc.)
- [ ] Use secrets management (vault, etc.)
- [ ] Configure firewall rules

## 📖 Resources for Learning

### Spring Boot
- https://spring.io/projects/spring-boot
- https://spring.io/guides

### Spring Security
- https://spring.io/projects/spring-security
- https://docs.spring.io/spring-security/

### Thymeleaf
- https://www.thymeleaf.org/doc/

### JPA/Hibernate
- https://docs.jboss.org/hibernate/orm/

## 🤝 Contributing

When adding new features:

1. Create a feature branch: `git checkout -b feature/new-feature`
2. Make changes following code style guidelines
3. Add tests for new functionality
4. Update documentation
5. Commit with clear messages: `git commit -m "Add feature: description"`
6. Push and create pull request

## 📝 Common Tasks

### Add a New Role/Permission

1. Create Role entity
2. Modify User entity to include roles
3. Update SecurityConfig with role checks
4. Add role-based controller endpoints

### Change Database

1. Add new database driver to pom.xml
2. Update application.yml with new connection string
3. Rebuild and test

### Add REST API

1. Create `@RestController` class
2. Add `@GetMapping`, `@PostMapping`, etc.
3. Return objects (automatically converted to JSON)
4. Document with Swagger/SpringFox

---

## 🔗 File Cross-References

- Security logic → `CustomAuthenticationProvider.java`
- Attempt tracking → `LoginAttemptService.java`
- Database queries → `LoginAttemptRepository.java`
- Web endpoints → `AuthController.java`
- Security config → `SecurityConfig.java`

---

**Last Updated:** May 1, 2024  
**Version:** 1.0.0

**Happy Developing! 🚀**
