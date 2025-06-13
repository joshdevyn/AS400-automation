# AS400 Automation Test Execution Script
# This script provides various test execution options

param(
    [string]$TestSuite = "smoke",
    [string]$Tags = "",
    [switch]$MockMode = $false,
    [switch]$GenerateReports = $true,
    [string]$OutputDir = "target"
)

Write-Host "AS400 Automation Test Execution" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Set working directory
Set-Location "c:\AS400-automation"

# Clean previous test results
Write-Host "Cleaning previous test results..." -ForegroundColor Yellow
if (Test-Path $OutputDir) {
    Remove-Item "$OutputDir\*" -Recurse -Force -ErrorAction SilentlyContinue
}

# Determine test runner based on suite
$TestRunner = switch ($TestSuite.ToLower()) {
    "smoke" { "ro.nn.qa.automation.tests.SmokeTestRunner" }
    "regression" { "ro.nn.qa.automation.tests.RegressionTestRunner" }
    "all" { "ro.nn.qa.automation.tests.AS400TestRunner" }
    default { "ro.nn.qa.automation.tests.SmokeTestRunner" }
}

# Build Maven command
$MavenCmd = "mvn clean test"

# Add test runner
$MavenCmd += " -Dtest=$TestRunner"

# Add custom tags if specified
if ($Tags) {
    $MavenCmd += " -Dcucumber.options=`"--tags $Tags`""
}

# Enable mock mode if specified
if ($MockMode) {
    $MavenCmd += " -Dmock.terminal.enabled=true"
    Write-Host "Running in Mock Mode (no AS400 connection required)" -ForegroundColor Cyan
}

# Execute tests
Write-Host "Executing test suite: $TestSuite" -ForegroundColor Yellow
Write-Host "Command: $MavenCmd" -ForegroundColor Gray

try {
    Invoke-Expression $MavenCmd
    $TestResult = $LASTEXITCODE
    
    if ($TestResult -eq 0) {
        Write-Host "Tests completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Tests completed with failures!" -ForegroundColor Red
    }
    
    # Generate reports if requested
    if ($GenerateReports -and (Test-Path "$OutputDir\cucumber-reports")) {
        Write-Host "Generating test reports..." -ForegroundColor Yellow
        
        # List generated reports
        $Reports = Get-ChildItem "$OutputDir\*reports" -Directory
        foreach ($Report in $Reports) {
            Write-Host "Report available at: $($Report.FullName)" -ForegroundColor Cyan
            
            # Open HTML reports if available
            $HtmlReport = Get-ChildItem "$($Report.FullName)\*.html" | Select-Object -First 1
            if ($HtmlReport) {
                Write-Host "HTML Report: $($HtmlReport.FullName)" -ForegroundColor Green
            }
        }
    }
    
} catch {
    Write-Host "Error executing tests: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`nTest execution completed!" -ForegroundColor Green
exit $TestResult
