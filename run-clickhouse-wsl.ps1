param(
    [string]$Distro = "Ubuntu-24.04"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$archive = Join-Path $repoRoot "installers\clickhouse-common-static-25.5.2.47-amd64.tgz"
$schema = Join-Path $repoRoot "log-service\src\main\resources\collector\access_log_all.sql"

if (-not (Test-Path $archive)) {
    throw "ClickHouse archive not found: $archive"
}

if (-not (Test-Path $schema)) {
    throw "ClickHouse schema not found: $schema"
}

function Convert-ToWslPath([string]$WindowsPath) {
    $fullPath = [System.IO.Path]::GetFullPath($WindowsPath)
    $drive = $fullPath.Substring(0, 1).ToLowerInvariant()
    $path = $fullPath.Substring(2).Replace("\", "/")
    return "/mnt/$drive$path"
}

$archiveWsl = Convert-ToWslPath $archive
$schemaWsl = Convert-ToWslPath $schema

Write-Host "Using WSL distro: $Distro"

wsl -d $Distro -- bash -lc "set -e; mkdir -p ~/clickhouse; if [ ! -x ~/clickhouse/clickhouse-common-static-25.5.2.47/usr/bin/clickhouse ]; then tar -xzf '$archiveWsl' -C ~/clickhouse; fi"

wsl -d $Distro -- bash -lc @"
set -e
cd ~/clickhouse
mkdir -p data tmp logs
if ! pgrep -x clickhouse >/dev/null 2>&1; then
  nohup ./clickhouse-common-static-25.5.2.47/usr/bin/clickhouse server -- --path=`$HOME/clickhouse/data --tmp_path=`$HOME/clickhouse/tmp --logger.log=`$HOME/clickhouse/logs/clickhouse.log --logger.errorlog=`$HOME/clickhouse/logs/clickhouse.err.log > `$HOME/clickhouse/logs/nohup.out 2>&1 &
fi
for i in {1..20}; do
  if ./clickhouse-common-static-25.5.2.47/usr/bin/clickhouse client --query 'SELECT 1' >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
pgrep -a clickhouse
"@

wsl -d $Distro -- bash -lc "~/clickhouse/clickhouse-common-static-25.5.2.47/usr/bin/clickhouse client --multiquery < '$schemaWsl'"

$ping = Invoke-RestMethod -Uri "http://127.0.0.1:8123/ping" -TimeoutSec 10
Write-Host "ClickHouse HTTP ping: $ping"
Write-Host "Schema ready: logs.access_log_all"
