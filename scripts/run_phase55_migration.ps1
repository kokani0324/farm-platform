# Phase 5.5 migration helper
# 跑法：
#   powershell -ExecutionPolicy Bypass -File scripts\run_phase55_migration.ps1

$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$sqlFile = Join-Path $PSScriptRoot "phase55_groupbuy_order_refactor.sql"

if (-not (Test-Path $mysql)) {
    Write-Host "找不到 mysql.exe：$mysql" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $sqlFile)) {
    Write-Host "找不到 SQL：$sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "[migration] 套用 phase55_groupbuy_order_refactor.sql ..." -ForegroundColor Cyan
& $mysql --default-character-set=utf8mb4 -uroot -p123456 farm_platform -e "source $sqlFile"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[migration] FAILED. 請看上方訊息." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "[migration] 完成。請重新啟動 backend (mvn spring-boot:run)。" -ForegroundColor Green
