$ErrorActionPreference = 'Stop'

$email = $env:SUPER_ADMIN_EMAIL
if ([string]::IsNullOrWhiteSpace($email)) {
    $email = Read-Host 'Adresse email du super-administrateur local'
}
if ([string]::IsNullOrWhiteSpace($email)) {
    throw 'L’adresse email du super-administrateur ne peut pas être vide.'
}

$dbPasswordSecure = Read-Host 'Mot de passe PostgreSQL local' -AsSecureString
$superAdminPasswordSecure = Read-Host 'Nouveau mot de passe local du super-administrateur' -AsSecureString
$superAdminPasswordConfirmation = Read-Host 'Confirmer le mot de passe du super-administrateur' -AsSecureString

if ($dbPasswordSecure.Length -eq 0) {
    throw 'Le mot de passe PostgreSQL local ne peut pas être vide.'
}
if ($superAdminPasswordSecure.Length -lt 8) {
    throw 'Le mot de passe du super-administrateur doit contenir au moins 8 caractères.'
}

function ConvertTo-PlainText([Security.SecureString] $secureValue) {
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

$env:SPRING_DATASOURCE_URL = if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_URL)) {
    'jdbc:postgresql://localhost:5432/itc_innovation'
} else {
    $env:SPRING_DATASOURCE_URL
}
$env:SPRING_DATASOURCE_USERNAME = if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_USERNAME)) {
    'postgres'
} else {
    $env:SPRING_DATASOURCE_USERNAME
}
$superAdminPassword = ConvertTo-PlainText $superAdminPasswordSecure
if ($superAdminPassword -cne (ConvertTo-PlainText $superAdminPasswordConfirmation)) {
    throw 'Les mots de passe du super-administrateur ne correspondent pas.'
}

$env:SPRING_DATASOURCE_PASSWORD = ConvertTo-PlainText $dbPasswordSecure
$env:SUPER_ADMIN_EMAIL = $email
$env:SUPER_ADMIN_PASSWORD = $superAdminPassword

if ([string]::IsNullOrWhiteSpace($env:APP_JWT_SECRET) -and [string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    $jwtBytes = New-Object byte[] 32
    $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($jwtBytes)
    }
    finally {
        $generator.Dispose()
    }
    $env:APP_JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
}

Write-Host "Compte super-administrateur local : $env:SUPER_ADMIN_EMAIL"
Write-Host 'Démarrage du backend...'
& .\mvnw.cmd spring-boot:run
