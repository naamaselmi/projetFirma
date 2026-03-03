# =====================================================
# FIRMA Security Setup Script (Windows PowerShell)
# =====================================================
# This script helps set up secure configuration for FIRMA
# Run this ONCE during project setup
#
# Usage: .\setup-security.ps1
# =====================================================

# Exit on error
$ErrorActionPreference = "Stop"

Write-Host "🔐 FIRMA Security Setup" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan
Write-Host ""

# Check if running from project root
if (-not (Test-Path "pom.xml")) {
    Write-Host "❌ Error: pom.xml not found. Please run from project root." -ForegroundColor Red
    exit 1
}

Write-Host "Step 1: Checking .gitignore configuration" -ForegroundColor Yellow
$gitignoreContent = Get-Content ".gitignore" -Raw
if ($gitignoreContent -match "src/main/resources/config\.properties") {
    Write-Host "✅ config.properties is in .gitignore" -ForegroundColor Green
} else {
    Write-Host "❌ config.properties NOT in .gitignore" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 2: Creating config.properties from template" -ForegroundColor Yellow
$configPath = "src\main\resources\config.properties"
$templatePath = "src\main\resources\config.properties.template"

if (-not (Test-Path $configPath)) {
    if (Test-Path $templatePath) {
        Copy-Item -Path $templatePath -Destination $configPath
        Write-Host "✅ Created config.properties" -ForegroundColor Green
    } else {
        Write-Host "❌ Template file not found" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "⚠️  config.properties already exists, skipping copy" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 3: Checking if config.properties was previously committed" -ForegroundColor Yellow
$gitCheck = git ls-files --error-unmatch $configPath 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "⚠️  WARNING: config.properties is already in Git history!" -ForegroundColor Red
    Write-Host ""
    Write-Host "This file contains sensitive information and must be removed:" -ForegroundColor Red
    Write-Host ""
    Write-Host "    git rm --cached src\main\resources\config.properties" -ForegroundColor White
    Write-Host "    git commit -m 'security: Remove config.properties with secrets'" -ForegroundColor White
    Write-Host "    git push origin main" -ForegroundColor White
    Write-Host ""
    Write-Host "Then regenerate all your API keys and passwords!" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host "✅ config.properties is not in Git history (good!)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 4: Configuration Instructions" -ForegroundColor Yellow
Write-Host ""
Write-Host "Now edit your local config.properties:" -ForegroundColor White
Write-Host "  notepad src\main\resources\config.properties" -ForegroundColor Gray
Write-Host ""
Write-Host "Replace all REPLACE_WITH_YOUR_* values with your actual credentials:" -ForegroundColor White
Write-Host ""
Write-Host "Required Configuration Keys:" -ForegroundColor White
Write-Host "  📍 google.maps.api.key"
Write-Host "  🤖 huggingface.api.token"
Write-Host "  💳 stripe.secret.key"
Write-Host "  💳 stripe.public.key"
Write-Host "  🗄️  db.password"
Write-Host "  📧 email.smtp.password"
Write-Host ""

Write-Host ""
Write-Host "Step 5: How to get your API keys" -ForegroundColor Yellow
Write-Host ""
Write-Host "📍 Google Maps API Key:" -ForegroundColor White
Write-Host "   https://console.cloud.google.com/apis/credentials" -ForegroundColor Gray
Write-Host ""
Write-Host "🤖 Hugging Face Token:" -ForegroundColor White
Write-Host "   https://huggingface.co/settings/tokens" -ForegroundColor Gray
Write-Host ""
Write-Host "💳 Stripe Keys:" -ForegroundColor White
Write-Host "   https://dashboard.stripe.com/apikeys" -ForegroundColor Gray
Write-Host ""
Write-Host "📧 Gmail App Password:" -ForegroundColor White
Write-Host "   https://myaccount.google.com/apppasswords" -ForegroundColor Gray
Write-Host ""

Write-Host ""
Write-Host "Step 6: Verification" -ForegroundColor Yellow

# Check if config.properties has placeholder values
$configContent = Get-Content $configPath -Raw
if ($configContent -match "REPLACE_WITH_YOUR") {
    Write-Host "⚠️  config.properties still has placeholder values" -ForegroundColor Yellow
    Write-Host "   Edit the file and replace all REPLACE_WITH_YOUR_* values" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host "✅ config.properties appears to be configured" -ForegroundColor Green
}

# Check ConfigLoader exists
$configLoaderPath = "src\main\java\Firma\tools\ConfigLoader.java"
if (Test-Path $configLoaderPath) {
    Write-Host "✅ ConfigLoader.java exists" -ForegroundColor Green
} else {
    Write-Host "❌ ConfigLoader.java not found" -ForegroundColor Red
    exit 1
}

# Check SECURITY.md exists
if (Test-Path "SECURITY.md") {
    Write-Host "✅ SECURITY.md documentation exists" -ForegroundColor Green
} else {
    Write-Host "❌ SECURITY.md not found" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=======================" -ForegroundColor Cyan
Write-Host "🎉 Security Setup Complete!" -ForegroundColor Green
Write-Host "=======================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor White
Write-Host "1. Edit config.properties with your actual API keys:" -ForegroundColor White
Write-Host "   notepad src\main\resources\config.properties" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Verify Git is ignoring your secrets:" -ForegroundColor White
Write-Host "   git status | findstr config.properties" -ForegroundColor Gray
Write-Host "   (Should show nothing)" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Build and run:" -ForegroundColor White
Write-Host "   mvn clean install" -ForegroundColor Gray
Write-Host "   mvn javafx:run" -ForegroundColor Gray
Write-Host ""
Write-Host "For team members:" -ForegroundColor White
Write-Host "- Share this setup script and SECURITY.md" -ForegroundColor White
Write-Host "- DO NOT share actual config.properties files" -ForegroundColor White
Write-Host "- DO NOT commit API keys or passwords" -ForegroundColor White
Write-Host ""

