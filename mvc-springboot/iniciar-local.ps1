$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

$jar = 'target/bancosangre-mvc-0.1.0.jar'
$configLocal = 'src/main/resources/application-local.properties'

function Confirm-Command($description) {
    if ($LASTEXITCODE -ne 0) { throw "$description falló (código $LASTEXITCODE)." }
}

if (-not (Test-Path -LiteralPath $configLocal)) {
    throw "Falta $configLocal. Crea ese archivo con la conexión a localhost:5432 antes de iniciar."
}

mvn package
Confirm-Command 'Compilar Spring Boot'
Write-Host 'Abre http://localhost:18081/login cuando Spring Boot termine de iniciar.'
java -jar $jar --server.address=localhost --server.port=18081
Confirm-Command 'Ejecutar Spring Boot'
