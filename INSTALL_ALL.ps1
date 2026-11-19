# CardDemo Installation Script - Downloads & Installs all tools
# Run this with: powershell -ExecutionPolicy Bypass -File INSTALL_ALL.ps1

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "CardDemo Phase 1 & Phase 2 Setup" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

$tempDir = "C:\temp\carddemo-setup"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

# ============= JAVA 17 INSTALLATION =============
Write-Host "Step 1: Installing Java 17 JDK..." -ForegroundColor Yellow

$javaUrl = "https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe"
$javaInstaller = "$tempDir\jdk-17-installer.exe"

Write-Host "  Downloading Java 17... (Large file ~180MB, may take 2-3 minutes)"
try {
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
    (New-Object System.Net.WebClient).DownloadFile($javaUrl, $javaInstaller)
    Write-Host "  ✓ Java 17 downloaded" -ForegroundColor Green
    
    Write-Host "  Installing Java 17... (this may take 2-3 minutes)"
    Start-Process -FilePath $javaInstaller -ArgumentList '/s INSTALLDIR=C:\Java\jdk-17' -Wait
    Write-Host "  ✓ Java 17 installed" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Error downloading Java 17: $_" -ForegroundColor Red
    Write-Host "  Alternative: Download manually from https://www.oracle.com/java/technologies/downloads/#java17" -ForegroundColor Yellow
}

# ============= MAVEN INSTALLATION =============
Write-Host "Step 2: Installing Apache Maven 3.9.6..." -ForegroundColor Yellow

$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$mavenZip = "$tempDir\apache-maven-3.9.6-bin.zip"

Write-Host "  Downloading Maven... (~10 MB, 30 seconds)"
try {
    (New-Object System.Net.WebClient).DownloadFile($mavenUrl, $mavenZip)
    Write-Host "  ✓ Maven downloaded" -ForegroundColor Green
    
    Write-Host "  Extracting Maven..."
    Expand-Archive -Path $mavenZip -DestinationPath "C:\" -Force
    Write-Host "  ✓ Maven extracted to C:\apache-maven-3.9.6" -ForegroundColor Green
    
    # Add to PATH
    $mavenPath = "C:\apache-maven-3.9.6\bin"
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", [EnvironmentVariableTarget]::Machine)
    if ($currentPath -notlike "*$mavenPath*") {
        [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$mavenPath", [EnvironmentVariableTarget]::Machine)
        Write-Host "  ✓ Maven added to PATH" -ForegroundColor Green
    }
} catch {
    Write-Host "  ✗ Error downloading Maven: $_" -ForegroundColor Red
}

# ============= NODE.JS INSTALLATION =============
Write-Host "Step 3: Installing Node.js 20 LTS..." -ForegroundColor Yellow

$nodeUrl = "https://nodejs.org/dist/v20.10.0/node-v20.10.0-x64.msi"
$nodeMsi = "$tempDir\node-v20.10.0-x64.msi"

Write-Host "  Downloading Node.js... (~50 MB, 1-2 minutes)"
try {
    (New-Object System.Net.WebClient).DownloadFile($nodeUrl, $nodeMsi)
    Write-Host "  ✓ Node.js downloaded" -ForegroundColor Green
    
    Write-Host "  Installing Node.js... (this may take 1-2 minutes)"
    Start-Process -FilePath "msiexec.exe" -ArgumentList "/i `"$nodeMsi`" /quiet /norestart" -Wait
    Write-Host "  ✓ Node.js installed" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Error downloading Node.js: $_" -ForegroundColor Red
    Write-Host "  Alternative: Download manually from https://nodejs.org/" -ForegroundColor Yellow
}

# ============= VERIFICATION =============
Write-Host ""
Write-Host "Verifying installations..." -ForegroundColor Cyan

$javaVer = cmd /c java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Java 17: $(($javaVer[0] -split '"')[1])" -ForegroundColor Green
} else {
    Write-Host "  ✗ Java not found in PATH. Please restart PowerShell or computer." -ForegroundColor Yellow
}

$mvnVer = cmd /c mvn --version 2>&1 | Select-Object -First 1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Maven: $mvnVer" -ForegroundColor Green
} else {
    Write-Host "  ✗ Maven not found in PATH. Please restart PowerShell or computer." -ForegroundColor Yellow
}

$nodeVer = cmd /c node --version 2>&1
$npmVer = cmd /c npm --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Node.js: $nodeVer, npm: $npmVer" -ForegroundColor Green
} else {
    Write-Host "  ✗ Node.js not found in PATH. Please restart PowerShell or computer." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Installation Complete!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Restart PowerShell or your computer (important!)"
Write-Host "2. Run: RUN_CARDDEMO.ps1"
Write-Host ""
