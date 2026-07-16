param(
    [string]$DatabaseName = "govshield",
    [string]$MySqlUser = $env:DB_USERNAME,
    [string]$MySqlPassword = $env:DB_PASSWORD
)

$ErrorActionPreference = "Stop"

function Load-DotEnv {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return
    }

    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#") -or -not $line.Contains("=")) {
            return
        }

        $parts = $line.Split("=", 2)
        $name = $parts[0].Trim()
        $value = $parts[1].Trim().Trim('"')

        if ($name -eq "DB_USERNAME" -and -not $script:MySqlUser) {
            $script:MySqlUser = $value
        }
        elseif ($name -eq "DB_PASSWORD" -and -not $script:MySqlPassword) {
            $script:MySqlPassword = $value
        }
    }
}

function Invoke-MySql {
    param(
        [string[]]$Arguments,
        [string]$InputPath
    )

    if ($InputPath) {
        Get-Content $InputPath -Raw | & mysql @Arguments
    }
    else {
        & mysql @Arguments
    }

    if ($LASTEXITCODE -ne 0) {
        throw "MySQL command failed with exit code $LASTEXITCODE"
    }
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$schemaPath = Join-Path $repoRoot "database\schema.sql"
$dataPath = Join-Path $repoRoot "database\data.sql"
$dotenvPath = Join-Path $repoRoot ".env"

Load-DotEnv -Path $dotenvPath

if (-not $MySqlUser) {
    $MySqlUser = "root"
}

if (-not $MySqlPassword) {
    $MySqlPassword = "root"
}

Write-Host "Resetting database '$DatabaseName'..."
Invoke-MySql -Arguments @("--user=$MySqlUser", "--password=$MySqlPassword", "-e", "DROP DATABASE IF EXISTS $DatabaseName; CREATE DATABASE $DatabaseName;")

Write-Host "Importing schema..."
Invoke-MySql -Arguments @("--user=$MySqlUser", "--password=$MySqlPassword", $DatabaseName) -InputPath $schemaPath

Write-Host "Importing data..."
Invoke-MySql -Arguments @("--user=$MySqlUser", "--password=$MySqlPassword", $DatabaseName) -InputPath $dataPath

Write-Host "Database setup complete."
