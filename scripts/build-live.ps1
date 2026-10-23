param(
    [string]$ProjectRef = 'yprwaxdreyjemazkgjft'
)
$ErrorActionPreference = 'Stop'
if ($ProjectRef -notmatch '^[a-z0-9]+$') { throw 'Project Ref inválido.' }
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    & .\gradlew.bat :app:assembleDebug -PuseRemoteDeals=true "-PedgeRuntimeBaseUrl=https://$ProjectRef.supabase.co/functions/v1"
    if ($LASTEXITCODE -ne 0) { throw 'La compilación falló.' }
    Write-Host 'APK: app/build/outputs/apk/debug/app-debug.apk'
} finally { Pop-Location }
