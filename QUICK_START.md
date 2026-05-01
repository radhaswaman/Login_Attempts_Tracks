# 🚀 Quick Start Guide

Get the Login Attempts Tracker application running in 2 minutes!

## Prerequisites
- **Java 17+**: Download from https://www.oracle.com/java/technologies/downloads/
- **Maven 3.6+**: Download from https://maven.apache.org/

## 3-Step Setup

### Step 1: Build
```bash
cd Login_Attempts_Tracker
mvn clean install
```

### Step 2: Run
```bash
mvn spring-boot:run
```

### Step 3: Open Browser
```
http://localhost:8080/login
```

## Demo Credentials

Try logging in with:
- **Username**: admin
- **Password**: admin123

Or register a new account!

## What Happens Next?

1. **Login Page** - Sign in or register
2. **Dashboard** - View your account status and login history
3. **Try Failed Attempts** - Login 5 times with wrong password to see account locking in action
4. **Wait 30 minutes** - Or modify the config to test unlock faster

## Modify Lock Settings

Edit `src/main/resources/application.yml`:

```yaml
app:
  security:
    max-failed-attempts: 5        # Change to 3 for quicker testing
    lock-duration-minutes: 30     # Change to 1 for quicker testing
```

Then rebuild and restart the application.

## Database Console (Optional)

View the database:
```
http://localhost:8080/h2-console
```

## Troubleshooting

**Port 8080 in use?**
Edit `application.yml`:
```yaml
server:
  port: 8081
```

**Build fails?**
```bash
mvn clean install -U
java -version  # Verify Java 17+
```

## Files Overview

| File | Purpose |
|------|---------|
| `pom.xml` | Dependencies & build config |
| `application.yml` | App settings |
| `src/main/java/com/logintracker/*` | Java source code |
| `src/main/resources/templates/*` | HTML pages |
| `README.md` | Full documentation |

## Project Features

✅ Track failed login attempts  
✅ Auto-lock accounts after 5 failed attempts  
✅ Auto-unlock after 30 minutes  
✅ Record IP addresses  
✅ View login history  
✅ User registration  
✅ Responsive web UI  

## Next Steps

1. Explore the dashboard
2. Test the security features
3. Review the code structure
4. Modify configurations as needed
5. Deploy to production when ready

---

**Questions?** See the full README.md for detailed documentation.
