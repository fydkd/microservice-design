$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$vectorExe = Join-Path $repoRoot "installers\vector\bin\vector.exe"
$config = Join-Path $repoRoot "log-service\src\main\resources\collector\vector.toml"

if (-not (Test-Path $vectorExe)) {
    throw "Vector executable not found: $vectorExe"
}

New-Item -ItemType Directory -Force -Path (Join-Path $repoRoot "run-logs\access") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $repoRoot "run-logs\vector-data") | Out-Null

& $vectorExe --config $config
