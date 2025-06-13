param(
    [string]$CttFile = ""
)

# AS400 Simulator Server Startup Script
Write-Host "Starting AS400 Simulator Server..." -ForegroundColor Green

# Stop any existing simulator processes
Write-Host "Checking for existing simulator instances..." -ForegroundColor Yellow
$existingProcesses = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*AS400SimulatorServer*" }

if ($existingProcesses) {
    Write-Host "Found existing simulator instance(s). Stopping them..." -ForegroundColor Yellow
    $existingProcesses | ForEach-Object { 
        Write-Host "  Stopping process with PID: $($_.Id)" -ForegroundColor Yellow
        Stop-Process -Id $_.Id -Force 
    }
    # Wait for processes to fully terminate
    Start-Sleep -Seconds 2
}

# Also check if port 23 is in use
$portInUse = Get-NetTCPConnection -LocalPort 23 -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "Port 23 is already in use. Attempting to release it..." -ForegroundColor Red
    foreach ($connection in $portInUse) {
        $process = Get-Process -Id $connection.OwningProcess -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "  Stopping process $($process.ProcessName) (PID: $($process.Id)) that's using port 23" -ForegroundColor Yellow
            Stop-Process -Id $process.Id -Force
        }
    }
    Start-Sleep -Seconds 2
}

Set-Location "c:\AS400-automation\automation"

# Compile the server if needed
Write-Host "Compiling AS400 Simulator Server..." -ForegroundColor Yellow
mvn compile test-compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Get Maven dependencies classpath
Write-Host "Getting Maven classpath..." -ForegroundColor Yellow
mvn dependency:build-classpath "-Dmdep.outputFile=classpath.txt" -q
$classpath = Get-Content "classpath.txt" -ErrorAction SilentlyContinue

if (-not $classpath) {
    # Fallback to basic SLF4J classpath
    $m2Repo = "$env:USERPROFILE\.m2\repository"
    $classpath = "$m2Repo\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;$m2Repo\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar;$m2Repo\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar"
}

$fullClasspath = "target/test-classes;target/classes;$classpath"

# Start the server
Write-Host "Starting AS400 Simulator Server on port 23 (Telnet)..." -ForegroundColor Green
Write-Host "Available screens: SIGNON, MAIN, BUSINESS, NEWCONTRACT" -ForegroundColor Cyan
Write-Host "Test login: GIUROAL / Bucuresti2" -ForegroundColor Cyan
Write-Host "" 
Write-Host "You can now connect from another terminal using:" -ForegroundColor White
Write-Host "  telnet localhost 23" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Start the AS400 simulator in the current window (foreground) so Ctrl+C works
if ($CttFile -and (Test-Path $CttFile)) {
    Write-Host "Using CTT configuration file: $CttFile" -ForegroundColor Cyan
    Write-Host "Parsing CTT file for connection settings..." -ForegroundColor Yellow
    java -cp "$fullClasspath" -Dctt.file="$CttFile" ro.nn.qa.automation.server.AS400SimulatorServer
} else {
    Write-Host "Using default simulator configuration..." -ForegroundColor Yellow
    java -cp "$fullClasspath" ro.nn.qa.automation.server.AS400SimulatorServer
}

# Cleanup
Remove-Item "classpath.txt" -ErrorAction SilentlyContinue
