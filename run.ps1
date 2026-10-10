# Launch Smart Tourism Management System
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectRoot
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Launching Smart Tourism Management System (STMS)" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
java -cp "bin;lib\*" smarttourism.Main
