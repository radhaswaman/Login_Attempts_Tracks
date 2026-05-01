# 💻 Setup Guide for Different Laptop

This guide will help you set up the Login Attempts Tracker application on any Windows, Mac, or Linux computer.

## 🔍 System Requirements Check

Before starting, verify your system has the required software. Open a terminal/command prompt and check:

### Check Java Version
```bash
java -version
```

Expected output should show Java 17 or higher.

**If not installed or version is wrong:**
- Download JDK 17: https://www.oracle.com/java/technologies/downloads/
- Install it (use default settings)
- Restart your terminal/command prompt
- Run `java -version` again to verify

### Check Maven Version
```bash
mvn --version
```

Expected output should show Maven 3.6 or higher.

**If not installed or version is wrong:**
- Download Maven: https://maven.apache.org/download.cgi
- Extract to a folder (e.g., `C:\Maven` or `/opt/maven`)
- Add Maven to your system PATH:
  
  **Windows:**
  - Go to: Control Panel → System → Advanced System Settings → Environment Variables
  - Click "New" under System Variables
  - Variable name: `MAVEN_HOME`
  - Variable value: Path to Maven folder (e.g., `C:\Maven`)
  - Click OK
  - In "Path" variable, add: `%MAVEN_HOME%\bin`
  - Restart terminal and verify: `mvn --version`
  
  **Mac/Linux:**
  ```bash
  export MAVEN_HOME=/path/to/maven
  export PATH=$MAVEN_HOME/bin:$PATH
  ```

## 📂 Step 1: Copy Project Files

### Option A: From USB/Cloud Storage
1. Copy the entire `Login_Attempts_Tracker` folder to your preferred location
2. Remember the full path to this folder

### Option B: Clone from Repository
```bash
git clone <repository-url>
cd Login_Attempts_Tracker
```

### Option C: Manual Download
1. Download the project files as ZIP
2. Extract to your preferred location
3. Navigate into the extracted folder

## 🔧 Step 2: Initial Setup (One-Time)

Open terminal/command prompt and navigate to the project folder:

**Windows:**
```cmd
cd C:\Users\YourUsername\Documents\Login_Attempts_Tracker
```

**Mac/Linux:**
```bash
cd ~/Documents/Login_Attempts_Tracker
```

Or use `cd` to navigate to wherever you saved the project.

### Verify You're in the Right Folder

Check that you see these files in the current directory:
```bash
ls    # Mac/Linux
dir   # Windows
```

You should see: `pom.xml`, `README.md`, `src/` folder, etc.

## 🚀 Step 3: Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies (may take 2-3 minutes on first run)
- Compile the source code
- Create the JAR file

**Expected output at the end:**
```
[INFO] BUILD SUCCESS
```

**If you see BUILD FAILURE:**
1. Verify Java version: `java -version` (should be 17+)
2. Verify Maven version: `mvn --version` (should be 3.6+)
3. Try again: `mvn clean install -U`

## ▶️ Step 4: Run the Application

```bash
mvn spring-boot:run
```

**You should see output like:**
```
Started LoginTrackerApplication in 5.123 seconds
```

This means the application is running successfully!

**Note:** The first build will take longer (2-3 minutes) because it downloads dependencies.

## 🌐 Step 5: Access the Application

1. Open your web browser (Chrome, Firefox, Edge, Safari, etc.)
2. Type this in the address bar:
   ```
   http://localhost:8080/login
   ```

3. You should see the login page!

## 🔑 Step 6: Login with Demo Account

Use these credentials:
- **Username:** admin
- **Password:** admin123

Or click "Register here" to create a new account.

## 🛑 Stopping the Application

To stop the running application:
1. In the terminal/command prompt where it's running
2. Press `Ctrl+C`
3. Type `Y` and press Enter to confirm

## 🔄 Running Again

After stopping, to run the application again:
```bash
mvn spring-boot:run
```

Or, if you prefer to run the JAR file directly:
```bash
java -jar target/login-attempts-tracker-1.0.0.jar
```

## ⚙️ Customizing Configuration

### Change Server Port

If port 8080 is already in use:

Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 8081  # Change to any available port
```

Then rebuild and run:
```bash
mvn clean install
mvn spring-boot:run
```

Access it at: `http://localhost:8081/login`

### Change Lock Settings (for Testing)

Edit `src/main/resources/application.yml`:
```yaml
app:
  security:
    max-failed-attempts: 3           # Lock after 3 attempts instead of 5
    lock-duration-minutes: 1         # Lock for 1 minute instead of 30
```

This makes testing the lock feature faster!

## 📚 Project Structure

```
Login_Attempts_Tracker/
├── src/
│   └── main/
│       ├── java/com/logintracker/     → Java source code
│       └── resources/
│           ├── templates/              → HTML pages (login, dashboard, etc.)
│           ├── static/css/             → Stylesheets
│           └── application.yml         → Configuration file
├── pom.xml                             → Maven configuration
├── README.md                           → Full documentation
├── QUICK_START.md                      → Quick start guide
└── target/                             → Compiled files (created after build)
```

## 🐛 Common Issues & Solutions

### Issue 1: "Port 8080 already in use"
**Solution:** 
- Change port in `application.yml` (see section above)
- Or stop other applications using port 8080

### Issue 2: "Java command not found"
**Solution:**
- Verify Java is installed: `java -version`
- If not installed, download and install JDK 17+
- Restart terminal after installation

### Issue 3: "Maven command not found"
**Solution:**
- Verify Maven is installed: `mvn --version`
- If not installed, download and install Maven
- Add Maven to PATH (see system requirements section)
- Restart terminal after setup

### Issue 4: "BUILD FAILURE - cannot find symbol"
**Solution:**
- Run: `mvn clean install -U`
- This clears cache and updates dependencies

### Issue 5: Login page shows blank/styling is off
**Solution:**
- Clear browser cache: `Ctrl+Shift+Delete` (Windows) or `Cmd+Shift+Delete` (Mac)
- Try a different browser
- Restart the application

### Issue 6: Can't create new users - "Username already exists"
**Solution:**
- The demo users are created on first run
- Either use existing users or delete the database:
  - Stop the application
  - The H2 database is in-memory, so it resets when you restart
  - Run the application again

## 🌍 Using the Application

### Dashboard Features

After logging in, you'll see:

1. **Account Information**
   - View your username, email, and name
   - See when your account was created

2. **Security Status**
   - Check if your account is locked
   - View number of failed login attempts
   - See countdown timer when locked

3. **Login Attempt History**
   - View recent login attempts
   - See if each attempt was successful or failed
   - View IP addresses used for login attempts

### Testing Security Features

1. **Test Account Lock:**
   - Try logging in 5 times with wrong password
   - After 5 failures, account should be locked
   - Wait 30 minutes (or 1 minute if you changed the config) for auto-unlock

2. **Test Registration:**
   - Click "Register here" on login page
   - Fill in the form with new credentials
   - Log in with your new account

## 📝 Important Notes

- **Database:** Uses H2 in-memory database by default (resets when app restarts)
- **Data Loss:** Login attempts are lost when app restarts
- **For Production:** Set up MySQL or PostgreSQL with persistent storage
- **Security:** Change demo passwords before deploying

## 📞 Need Help?

1. Check the **README.md** for detailed documentation
2. Check the **Troubleshooting** section above
3. Read the **QUICK_START.md** for a quick overview
4. Review the source code comments in the Java files

## ✅ Verification Checklist

Before considering setup complete:

- [ ] Java 17+ installed and working
- [ ] Maven 3.6+ installed and working
- [ ] Project files copied/cloned
- [ ] `mvn clean install` completed successfully
- [ ] `mvn spring-boot:run` starts without errors
- [ ] Can access http://localhost:8080/login
- [ ] Can login with admin/admin123
- [ ] Dashboard shows account information
- [ ] Can see login attempt history

## 🎉 You're Ready!

Congratulations! Your Login Attempts Tracker application is now set up and running on your computer.

You can now:
- Explore the application
- Test the security features
- Modify configurations
- Deploy to other computers using this same guide
- Deploy to production servers

---

**Troubleshooting Questions?** Refer to the README.md file for more detailed information.

**Happy Coding! 🚀**
