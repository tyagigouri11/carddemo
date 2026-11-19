# CardDemo Phase 1 & Phase 2 - Complete Setup Instructions

**Status**: All code ready, tools need to be installed

---

## Quick Start (3 Steps)

### Option A: Automatic Installation (Recommended)

1. **Open PowerShell as Administrator**
   - Right-click PowerShell → Run as Administrator

2. **Run the installation script:**
   ```powershell
   cd c:\Gouri\document
   powershell -ExecutionPolicy Bypass -File INSTALL_ALL.ps1
   ```
   - This will download and install:
     - Java 17 JDK (~180 MB)
     - Apache Maven 3.9.6 (~10 MB)
     - Node.js 20 LTS (~50 MB)
   - **Time: 10-15 minutes** (depending on internet speed)

3. **After installation, restart PowerShell or your computer** (important!)

4. **Build and run both Phase 1 & Phase 2:**
   ```powershell
   cd c:\Gouri\document
   powershell -ExecutionPolicy Bypass -File RUN_CARDDEMO.ps1
   ```
   - This will:
     - Build Phase 1: `mvn clean package` (2-3 minutes)
     - Install Phase 2 deps: `npm install` (2-3 minutes)

---

## Option B: Manual Installation

If you prefer to install tools manually:

### 1. Install Java 17 JDK

- Download: https://www.oracle.com/java/technologies/downloads/#java17
  - Choose: **Windows x64 Installer**
- Run installer, accept defaults
- Verify:
  ```bash
  java -version
  ```
  Should show: `java version "17.x.x"`

### 2. Install Maven 3.8+

- Download: https://maven.apache.org/download.cgi
  - Get: **Binary zip archive** (apache-maven-3.9.x-bin.zip)
- Extract to: `C:\apache-maven-3.9.6`
- Add to PATH:
  - Right-click "This PC" → Properties → Advanced system settings
  - Environment Variables → New User Variable:
    - Name: `MAVEN_HOME`
    - Value: `C:\apache-maven-3.9.6`
  - Edit PATH, add: `%MAVEN_HOME%\bin`
- Verify:
  ```bash
  mvn --version
  ```

### 3. Install Node.js 18+ LTS

- Download: https://nodejs.org/ (LTS version)
- Run installer, accept defaults
- Verify:
  ```bash
  node --version
  npm --version
  ```
  Should show: v18+ or v20+

### 4. Restart PowerShell/Command Prompt

### 5. Build Phase 1
```bash
cd c:\Gouri\document\carddemo-modernization
mvn clean package
```

### 6. Install Phase 2 Dependencies
```bash
cd c:\Gouri\document\carddemo-react
npm install
```

---

## Running Both Services

### Terminal 1: Start Phase 1 Backend

```bash
cd c:\Gouri\document\carddemo-modernization
java -jar target/carddemo-1.0.0.jar --spring.profiles.active=dev
```

**Expected output:**
```
Started CarddemoApplication in 3.123 seconds
Tomcat started on port(s): 8080 (http)
```

**Keep this terminal running!**

### Terminal 2: Start Phase 2 Frontend

```bash
cd c:\Gouri\document\carddemo-react
npm run dev
```

**Expected output:**
```
VITE v5.0.7  ready in 234 ms

➜  Local:   http://localhost:5173/
```

---

## Verify Integration

### Option 1: Access Frontend
- Open browser: **http://localhost:5173**
- Login with any email/password
- Dashboard should display with data from backend

### Option 2: Check Backend API
- Open browser: **http://localhost:8080/swagger-ui.html**
- See all REST APIs documented

### Option 3: Monitor Network
- In frontend browser (http://localhost:5173)
- Press F12 → Network tab
- Perform any action
- Verify API calls going to `localhost:8080/api/*`

---

## Troubleshooting

### "java: command not found"
- **Cause**: Java not installed or not in PATH
- **Solution**: 
  1. Install Java 17 JDK from https://www.oracle.com/java/technologies/downloads/#java17
  2. Restart PowerShell
  3. Verify: `java -version`

### "mvn: command not found"
- **Cause**: Maven not installed or not in PATH
- **Solution**:
  1. Install Maven from https://maven.apache.org/download.cgi
  2. Add `C:\apache-maven-3.9.6\bin` to PATH
  3. Restart PowerShell
  4. Verify: `mvn --version`

### "npm: command not found"
- **Cause**: Node.js not installed
- **Solution**:
  1. Install Node.js from https://nodejs.org/
  2. Restart PowerShell
  3. Verify: `node --version && npm --version`

### "ECONNREFUSED localhost:8080"
- **Cause**: Phase 1 backend not running
- **Solution**: Start Terminal 1 with Phase 1 backend first

### "Cannot connect to localhost:5173"
- **Cause**: Phase 2 frontend not running
- **Solution**: Start Terminal 2 with Phase 2 frontend

### "Port 8080 already in use"
- **Cause**: Another application using port 8080
- **Solution**: Change backend port:
  ```bash
  java -jar target/carddemo-1.0.0.jar --server.port=8081
  ```

### "Port 5173 already in use"
- **Cause**: Another application using port 5173
- **Solution**: Change frontend port:
  ```bash
  npm run dev -- --port 5174
  ```

### "mvn clean package" takes too long
- This is normal for first build
- Maven downloads 200+ dependencies (~500 MB total)
- Subsequent builds are much faster (cached)
- Total time: 2-3 minutes first time

### "npm install" downloads slowly
- Normal behavior
- Node.js packages total ~300 MB
- Time: 2-3 minutes on average internet speed
- Only needs to run once

---

## What Each Script Does

### INSTALL_ALL.ps1
- Downloads Java 17 JDK (~180 MB)
- Downloads Apache Maven 3.9.6 (~10 MB)
- Downloads Node.js 20 LTS (~50 MB)
- Installs all three tools
- Adds to system PATH
- Verifies installations
- **Time: 10-15 minutes**

### RUN_CARDDEMO.ps1
- Verifies tools are installed
- Builds Phase 1: `mvn clean package`
- Installs Phase 2: `npm install`
- Displays instructions for running services
- **Time: 5-8 minutes**

### START_CARDDEMO.bat
- Double-click to run both scripts automatically
- Simplest option for first-time setup

---

## File Locations

| Component | Location |
|-----------|----------|
| Phase 1 Backend | `c:\Gouri\document\carddemo-modernization\` |
| Phase 2 Frontend | `c:\Gouri\document\carddemo-react\` |
| Installation Scripts | `c:\Gouri\document\` |
| Phase 1 JAR (after build) | `carddemo-modernization\target\carddemo-1.0.0.jar` |
| Phase 2 Source | `carddemo-react\src\` |

---

## Services Ports

| Service | Port | URL |
|---------|------|-----|
| Phase 1 Backend | 8080 | http://localhost:8080 |
| Phase 1 Swagger API | 8080 | http://localhost:8080/swagger-ui.html |
| Phase 1 H2 Console | 8080 | http://localhost:8080/h2-console |
| Phase 2 Frontend (dev) | 5173 | http://localhost:5173 |
| Phase 2 Frontend (prod) | 80 | http://localhost |

---

## Quick Reference Commands

```bash
# Build Phase 1
mvn clean package              # in carddemo-modernization/

# Run Phase 1
java -jar target/carddemo-1.0.0.jar --spring.profiles.active=dev

# Install Phase 2 deps
npm install                    # in carddemo-react/

# Run Phase 2
npm run dev                    # in carddemo-react/

# Build Phase 2 for production
npm run build                  # in carddemo-react/

# Test Phase 1
mvn test                       # in carddemo-modernization/

# Test Phase 2
npm test                       # in carddemo-react/
```

---

## Complete Timeline

| Task | Time |
|------|------|
| Install Java 17 | 2-3 min |
| Install Maven | 1 min |
| Install Node.js | 2-3 min |
| Build Phase 1 (mvn clean package) | 2-3 min |
| Install Phase 2 (npm install) | 2-3 min |
| **Total** | **10-15 min** |

---

## Next Steps After Setup

1. ✅ Both services running
2. ✅ Frontend accessible at http://localhost:5173
3. ✅ Backend API at http://localhost:8080/swagger-ui.html
4. ✅ Data flowing between frontend and backend
5. ✅ Ready for development or deployment

---

## Support

For issues, check:
1. **SETUP_INSTRUCTIONS.md** (this file) - Troubleshooting section
2. **carddemo-modernization/README.md** - Phase 1 documentation
3. **carddemo-react/README.md** - Phase 2 documentation

---

**Ready to begin? Run: `powershell -ExecutionPolicy Bypass -File INSTALL_ALL.ps1`**

