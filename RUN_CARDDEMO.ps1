# CardDemo Phase 1 & Phase 2 - Build & Run Script
# Make sure INSTALL_ALL.ps1 has been run first

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "CardDemo Phase 1 & Phase 2 - Build & Run" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Verify tools are installed
Write-Host "Verifying tools..." -ForegroundColor Yellow

$javaCheck = cmd /c java -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Java 17 not found. Please run INSTALL_ALL.ps1 first and restart." -ForegroundColor Red
    exit 1
}

$mvnCheck = cmd /c mvn --version 2>&1 | Select-Object -First 1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Maven not found. Please run INSTALL_ALL.ps1 first and restart." -ForegroundColor Red
    exit 1
}

$nodeCheck = cmd /c node --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Node.js not found. Please run INSTALL_ALL.ps1 first and restart." -ForegroundColor Red
    exit 1
}

Write-Host "✓ All tools verified!" -ForegroundColor Green
Write-Host ""

# ============= BUILD PHASE 1 =============
Write-Host "Step 1: Building Phase 1 (Java Backend)..." -ForegroundColor Yellow

cd "c:\Gouri\document\carddemo-modernization"

Write-Host "  Running: mvn clean package"
Write-Host "  (This takes 2-3 minutes, please wait...)"
Write-Host ""

mvn clean package

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "  ✓ Phase 1 built successfully!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  ✗ Phase 1 build failed!" -ForegroundColor Red
    exit 1
}

# ============= INSTALL PHASE 2 DEPENDENCIES =============
Write-Host ""
Write-Host "Step 2: Installing Phase 2 (React) Dependencies..." -ForegroundColor Yellow

cd "c:\Gouri\document\carddemo-react"

Write-Host "  Running: npm install"
Write-Host "  (This takes 2-3 minutes, please wait...)"
Write-Host ""

npm install

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "  ✓ Phase 2 dependencies installed!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  ✗ Phase 2 dependency installation failed!" -ForegroundColor Red
    exit 1
}

# ============= SUCCESS =============
Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "Build Complete!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""

Write-Host "Next: Run both services" -ForegroundColor Yellow
Write-Host ""
Write-Host "Terminal 1 (Backend):" -ForegroundColor Cyan
Write-Host "  cd c:\Gouri\document\carddemo-modernization" -ForegroundColor White
Write-Host "  java -jar target/carddemo-1.0.0.jar --spring.profiles.active=dev" -ForegroundColor White
Write-Host ""
Write-Host "Terminal 2 (Frontend):" -ForegroundColor Cyan
Write-Host "  cd c:\Gouri\document\carddemo-react" -ForegroundColor White
Write-Host "  npm run dev" -ForegroundColor White
Write-Host ""
Write-Host "Then access:" -ForegroundColor Cyan
Write-Host "  Frontend: http://localhost:5173" -ForegroundColor White
Write-Host "  Backend API: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
