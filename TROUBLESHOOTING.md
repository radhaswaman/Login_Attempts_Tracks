# 🔧 Troubleshooting Guide

This guide covers common issues and their solutions.

## Build Issues

### ❌ BUILD FAILURE: cannot find symbol

**Error Message:**
```
[ERROR] Cannot find symbol
[ERROR]   symbol:   class User
[ERROR]   location: package com.logintracker.entity
```

**Causes:**
- Java version mismatch
- Corrupted Maven cache
- IDE not recognizing changes

**Solutions:**
1. Check Java version (must be 17+):
   ```bash
   java -version
   ```
   If wrong version, install Java 17+

2. Clear Maven cache:
   ```bash
   mvn clean install -U
   ```

3. If using IDE:
   - IntelliJ: File → Invalidate Caches → Restart
   - Eclipse: Project → Clean All

### ❌ BUILD FAILURE: Compilation error

**Solutions:**
1. Verify all Java files are properly formatted
2. Check for missing imports
3. Rebuild from scratch:
   ```bash
   mvn clean
   rm -rf ~/.m2/repository/com/logintracker/  # Linux/Mac
   rmdir %USERPROFILE%\.m2\repository\com\logintracker /s  # Windows
   mvn install
   ```

### ⚠️ Dependency download takes too long

**Cause:** Large files being downloaded

**Solutions:**
1. Wait patiently (can take 2-3 minutes on first run)
2. Use faster internet connection
3. Check internet connection: `ping google.com`

---

## Runtime Issues

### ❌ "Port 8080 already in use"

**Error Message:**
```
Address already in use: bind
```

**Solutions:**

**Option 1: Change port in application.yml**
```yaml
server:
  port: 8081
```

**Option 2: Find and stop other application using port 8080**

Windows:
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

Linux/Mac:
```bash
lsof -i :8080
kill -9 <PID>
```

**Option 3: Use different port in command line**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### ❌ "Java command not found"

**Error:**
```
'java' is not recognized as an internal or external command
```

**Cause:** Java not installed or not in PATH

**Solutions:**

**Windows:**
1. Download JDK 17: https://www.oracle.com/java/technologies/downloads/
2. Run installer
3. During installation, note the install path
4. Set JAVA_HOME:
   - Control Panel → System → Advanced System Settings
   - Click "Environment Variables"
   - New System Variable: `JAVA_HOME` = `C:\Program Files\Java\jdk-17.x.x`
   - Edit PATH, add: `%JAVA_HOME%\bin`
5. Restart Command Prompt
6. Verify: `java -version`

**Mac:**
```bash
/usr/libexec/java_home -V  # List Java installations
# If nothing found, install with Homebrew:
brew install openjdk@17
# Set JAVA_HOME:
export JAVA_HOME=$(brew --prefix openjdk@17)
export PATH=$JAVA_HOME/bin:$PATH
```

**Linux:**
```bash
sudo apt-get install openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### ❌ "Maven command not found"

**Error:**
```
'mvn' is not recognized as an internal or external command
```

**Cause:** Maven not installed or not in PATH

**Solutions:**

**Windows:**
1. Download Maven: https://maven.apache.org/download.cgi
2. Extract to folder (e.g., `C:\Maven`)
3. Set MAVEN_HOME:
   - Control Panel → System → Advanced System Settings
   - Click "Environment Variables"
   - New System Variable: `MAVEN_HOME` = `C:\Maven`
   - Edit PATH, add: `%MAVEN_HOME%\bin`
4. Restart Command Prompt
5. Verify: `mvn --version`

**Mac:**
```bash
brew install maven
# Verify:
mvn --version
```

**Linux:**
```bash
sudo apt-get install maven
# Verify:
mvn --version
```

### ❌ Application won't start - "Connection refused"

**Error:**
```
org.springframework.beans.factory.BeanCreationException
Caused by: java.sql.SQLException: Cannot connect to database
```

**Cause:** Database issue

**Solutions:**
1. Check if application.yml has correct database settings
2. If using H2 (default), delete any leftover H2 files:
   ```bash
   rm -f *.mv.db  # Linux/Mac
   del *.mv.db    # Windows
   ```
3. Restart application:
   ```bash
   mvn spring-boot:run
   ```

---

## Web Interface Issues

### ❌ "Page not loading" / Shows blank page

**Solutions:**
1. Check URL: Should be exactly `http://localhost:8080/login`
2. Verify application is running (check terminal)
3. Clear browser cache:
   - Chrome: Ctrl+Shift+Delete
   - Firefox: Ctrl+Shift+Delete
   - Edge: Ctrl+Shift+Delete
   - Safari: Cmd+Shift+Delete
4. Try different browser
5. Check browser console for errors: Press F12

### ❌ CSS not loading / Page looks ugly

**Cause:** Static resources not being served

**Solutions:**
1. Verify `src/main/resources/static/css/style.css` exists
2. Clear browser cache (Ctrl+Shift+Delete)
3. Restart application
4. Check if correct port is configured

### ❌ Login page shows "Resource not found" error

**Solutions:**
1. Verify correct URL: `http://localhost:8080/login`
2. Check if AuthController exists and has `/login` endpoint
3. Restart application

---

## Database Issues

### ❌ "User already exists" on registration

**Cause:** User records already exist in database

**Solutions:**
1. Use different username
2. For H2 (default), database resets on restart:
   - Stop application (Ctrl+C)
   - Start application: `mvn spring-boot:run`

### ❌ Cannot access H2 Console

**Error:** `http://localhost:8080/h2-console` shows "Not found"

**Solutions:**
1. Verify H2 console is enabled in `application.yml`:
   ```yaml
   spring:
     h2:
       console:
         enabled: true
   ```
2. Restart application
3. Access with correct port (check server.port in config)

### ❌ "Table not found" error

**Cause:** Database tables not created

**Solutions:**
1. Verify `spring.jpa.hibernate.ddl-auto=create-drop` in `application.yml`
2. Restart application to auto-create tables
3. Check H2 console to verify tables exist

---

## Login & Security Issues

### ❌ "Invalid credentials" for demo account

**Cause:** Demo users not created or wrong password

**Solutions:**
1. Use correct credentials:
   - Username: `admin`
   - Password: `admin123`
2. Try registering new user instead
3. Check application startup logs for user creation messages

### ❌ Account locked when shouldn't be

**Cause:** Failed attempts counter didn't reset

**Solutions:**
1. Wait for lock duration (30 minutes by default) for auto-unlock
2. Change lock duration for testing:
   ```yaml
   app:
     security:
       lock-duration-minutes: 1
   ```
3. Stop and restart application (H2 database resets)

### ❌ Can't login after registration

**Solutions:**
1. Verify email and username don't already exist
2. Use exact username and password from registration
3. Passwords are case-sensitive
4. Check for leading/trailing spaces in input

---

## Performance Issues

### ❌ Application startup is very slow

**Solutions:**
1. Increase heap size:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"
   ```
2. Use faster machine or close other applications
3. Check internet connection (for dependency download)

### ❌ Login process is slow

**Solutions:**
1. Normal login takes <100ms, if slower:
2. Check system resources (CPU, memory usage)
3. Check MySQL connection if using MySQL instead of H2
4. Restart application

---

## IDE-Specific Issues

### IntelliJ IDEA
- **Issue:** Cannot find Spring Boot configuration
  - Solution: File → Project Structure → Project SDK → Select Java 17
- **Issue:** "pom.xml" file not recognized
  - Solution: Right-click pom.xml → Add as Maven Project

### Eclipse
- **Issue:** Project won't build
  - Solution: Project → Clean All, then Project → Build All
- **Issue:** Server won't start
  - Solution: Servers → Stop all, delete previous launch configuration

### VS Code
- **Issue:** Debugging not working
  - Solution: Install "Extension Pack for Java"
  - Command Palette (Ctrl+Shift+P) → Java: Configure Runtime
  - Select Java 17+

---

## Advanced Debugging

### Enable Debug Logging

Edit `application.yml`:
```yaml
logging:
  level:
    root: DEBUG
    com.logintracker: TRACE
```

Then check console output for detailed logs.

### Enable SQL Logging

Edit `application.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

This shows all database queries in console.

### Check Application Logs

Logs print to console by default. To save to file:

Edit `application.yml`:
```yaml
logging:
  file:
    name: logs/application.log
    max-size: 10MB
```

---

## Still Having Issues?

1. **Read the full README.md** - May have more detailed information
2. **Check Spring Boot Documentation** - https://spring.io/projects/spring-boot
3. **Check Spring Security Documentation** - https://spring.io/projects/spring-security
4. **Stack Overflow** - Search your error message
5. **Project GitHub Issues** - If applicable

---

## Quick Help Commands

```bash
# Check Java version
java -version

# Check Maven version
mvn --version

# Clean rebuild
mvn clean install

# Run with debug output
mvn clean install -X

# Run specific port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Check if port is in use (Windows)
netstat -ano | findstr :8080

# Check if port is in use (Mac/Linux)
lsof -i :8080

# List Maven repositories
mvn dependency:tree
```

---

**Last Updated:** May 1, 2024  
**Version:** 1.0.0
