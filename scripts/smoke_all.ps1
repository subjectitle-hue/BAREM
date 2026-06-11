$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "=== 1/3 validate_all.py ===" -ForegroundColor Cyan
python scripts/validate_all.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "`n=== 2/3 testDebugUnitTest ===" -ForegroundColor Cyan
.\gradlew.bat testDebugUnitTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$devices = & $adb devices 2>&1 | Out-String
if ($devices -match "emulator-\d+\s+device|^\S+\s+device" -and $devices -notmatch "List of devices attached\s*$") {
    Write-Host "`n=== 3/3 connectedDebugAndroidTest ===" -ForegroundColor Cyan
    .\gradlew.bat connectedDebugAndroidTest `
        "-Pandroid.testInstrumentationRunnerArguments.class=tr.erdaldemir.barem.MemurWizardEngineSmokeTest,tr.erdaldemir.barem.MemurWizardUiSmokeTest"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} else {
    Write-Host "`n=== 3/3 SKIP (no adb device) ===" -ForegroundColor Yellow
    Write-Host "Connect emulator or phone, then re-run for instrumented tests."
}

Write-Host "`nSMOKE ALL PASS" -ForegroundColor Green
