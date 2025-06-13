# File: run-expect-test.ps1
# Purpose: Run AS400 automation tests with support for .ctt files

param(
    [Parameter(Mandatory=$false)]
    [string]$CttFile = "c:\AS400-automation\test-sample.ctt",
    
    [Parameter(Mandatory=$false)]
    [int]$SimulatorStartupTimeout = 30,
    
    [Parameter(Mandatory=$false)]
    [switch]$DebugMode
)

# Error handling function
function Write-ErrorAndExit {
    param($Message)
    Write-Host "ERROR: $Message" -ForegroundColor Red
    exit 1
}

# Validate inputs
if (-not (Test-Path $CttFile)) {
    Write-ErrorAndExit "CTT file not found: $CttFile"
}

# Initialize test environment
$ErrorActionPreference = "Stop"
$simulatorProcess = $null
$testStartTime = Get-Date

Write-Host "Starting AS400 automation test suite..." -ForegroundColor Green
Write-Host "Using CTT file: $CttFile" -ForegroundColor Cyan

try {
    # Start the AS400 simulator with the CTT file
    Write-Host "Starting AS400 simulator..." -ForegroundColor Yellow
    $simulatorProcess = Start-Process "c:\AS400-automation\start-as400-simulator.ps1" -ArgumentList "-CttFile `"$CttFile`"" -PassThru
    
    # Wait for simulator to be ready (port 23 to be available)
    Write-Host "Waiting for simulator to be ready (timeout: ${SimulatorStartupTimeout}s)..." -ForegroundColor Yellow
    $ready = $false
    $timer = [System.Diagnostics.Stopwatch]::StartNew()
    
    while (-not $ready -and $timer.Elapsed.TotalSeconds -lt $SimulatorStartupTimeout) {
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $result = $tcpClient.BeginConnect("localhost", 23, $null, $null)
            $success = $result.AsyncWaitHandle.WaitOne(1000)
            if ($success) {
                $ready = $true
                $tcpClient.EndConnect($result)
            }
            $tcpClient.Close()
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    
    if (-not $ready) {
        throw "Simulator failed to start within ${SimulatorStartupTimeout} seconds"
    }
    
    Write-Host "Simulator is ready!" -ForegroundColor Green
    
    # Run the expect module tests
    Write-Host "Running expect module tests..." -ForegroundColor Cyan
    Set-Location "c:\AS400-automation\expect"
    
    # Set environment variables for test configuration
    $env:AS400_CTT_FILE = $CttFile
    $env:AS400_DEBUG_MODE = if ($DebugMode) { "true" } else { "false" }
    
    mvn test
    
    # Process test results
    Write-Host "`nTest Results Summary:" -ForegroundColor Green
    Write-Host "===================" -ForegroundColor Green
    
    $testResultsFile = "c:\AS400-automation\expect\target\surefire-reports\ro.nn.qa.test4nn.ExpectTest.txt"
    if (Test-Path $testResultsFile) {
        $results = Get-Content $testResultsFile
        $failureCount = ($results | Select-String "FAILURE" | Measure-Object).Count
        $errorCount = ($results | Select-String "ERROR" | Measure-Object).Count
        
        Write-Host "Test Execution Time: $($timer.Elapsed.ToString())" -ForegroundColor Cyan
        Write-Host "Results from file:" -ForegroundColor Yellow
        $results | Select-Object -First 10
        
        if ($failureCount -gt 0 -or $errorCount -gt 0) {
            Write-Host "`nFound $failureCount failures and $errorCount errors" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-ErrorAndExit "Test results file not found: $testResultsFile"
    }
} catch {
    Write-ErrorAndExit $_.Exception.Message
} finally {
    # Cleanup
    Write-Host "`nCleaning up resources..." -ForegroundColor Yellow
    
    # Stop the simulator process if it's still running
    if ($simulatorProcess -and -not $simulatorProcess.HasExited) {
        Stop-Process -Id $simulatorProcess.Id -Force -ErrorAction SilentlyContinue
    }
    
    # Kill any remaining tn3270 processes
    Get-Process | Where-Object { $_.Name -like "*tn3270*" } | 
        ForEach-Object { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue }
    
    # Reset location
    Set-Location "c:\AS400-automation"
}

Write-Host "`nTest execution completed!" -ForegroundColor Green
