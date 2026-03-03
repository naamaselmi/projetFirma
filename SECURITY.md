# 🔐 FIRMA Security & Configuration Guide

## ⚠️ CRITICAL: Never Commit Secrets to Git!

This document explains how to manage sensitive configuration and API credentials securely in FIRMA.

---

## 📋 Table of Contents

1. [Sensitive Files](#sensitive-files)
2. [Setup Instructions](#setup-instructions)
3. [Configuration Methods](#configuration-methods)
4. [Environment Variables](#environment-variables)
5. [Using ConfigLoader](#using-configloader)
6. [Before Pushing to GitHub](#before-pushing-to-github)

---

## Sensitive Files

### 🔴 Files That MUST NOT Be Committed

| File | Contains | Status |
|------|----------|--------|
| `src/main/resources/config.properties` | API Keys, DB Password, Stripe Keys | 🔴 IGNORED |
| `.env` | Environment variables | 🔴 IGNORED |
| `.env.local` | Local dev secrets | 🔴 IGNORED |
| Wallet/keystore files | Signing certificates | 🔴 IGNORED |

### ✅ Files That SHOULD Be Committed

| File | Purpose |
|------|---------|
| `src/main/resources/config.properties.template` | Configuration template example |
| `src/main/java/Firma/tools/ConfigLoader.java` | Secrets loading utility |
| `.gitignore` | Git ignore rules |
| `SECURITY.md` | This documentation |

---

## Setup Instructions

### 1️⃣ Copy Configuration Template

```bash
cd src/main/resources/
cp config.properties.template config.properties
```

### 2️⃣ Fill in Your Actual Values

Open `config.properties` and replace all `REPLACE_WITH_YOUR_*` values:

```properties
# Example - DO THIS FOR ALL KEYS:
google.maps.api.key=AIzaSy...YourActualKeyHere...
huggingface.api.token=hf_...YourActualTokenHere...
stripe.secret.key=sk_...YourActualKeyHere...
db.password=your_database_password
email.smtp.password=your_gmail_app_password
```

### 3️⃣ Verify `.gitignore` Has the Rule

```bash
# Check that config.properties is in .gitignore
grep "src/main/resources/config.properties" .gitignore
```

Should output:
```
src/main/resources/config.properties
```

### 4️⃣ Verify the File Is Ignored by Git

```bash
# Check if Git is tracking config.properties
git status src/main/resources/config.properties
```

Should output:
```
fatal: pathspec 'src/main/resources/config.properties' did not match any files
```

If it says `modified: src/main/resources/config.properties`, it means it was already committed. See "Remove Secrets from Git History" section.

---

## Configuration Methods

### Method 1: Using config.properties (Local Development)

**When:** Local development machine

**How:**
1. Copy `config.properties.template` → `config.properties`
2. Fill in your local values
3. Java code loads from file via `ConfigLoader.get()`

**Pros:**
- ✅ Simple for local development
- ✅ No environment setup needed

**Cons:**
- ❌ Easy to accidentally commit
- ❌ Not suitable for production

**Example:**
```java
String googleMapsKey = ConfigLoader.get("google.maps.api.key");
```

### Method 2: Using Environment Variables (Recommended for Production)

**When:** Production, CI/CD, Docker, Kubernetes

**How:**
1. Set environment variables on your system/container
2. Java code reads from environment via `ConfigLoader.get()` (checks env first)
3. Don't need a `config.properties` file

**Pros:**
- ✅ Secure - no files to commit
- ✅ Industry standard
- ✅ Works with Docker, Kubernetes, CI/CD
- ✅ Easy to rotate secrets

**Cons:**
- ❌ Requires environment setup

**Example - Linux/Mac:**
```bash
export GOOGLE_MAPS_API_KEY="AIzaSy..."
export HUGGINGFACE_API_TOKEN="hf_..."
export STRIPE_SECRET_KEY="sk_..."
export DB_PASSWORD="your_db_pass"
export EMAIL_SMTP_PASSWORD="your_gmail_app_password"

java -jar firma.jar
```

**Example - Windows PowerShell:**
```powershell
$env:GOOGLE_MAPS_API_KEY = "AIzaSy..."
$env:HUGGINGFACE_API_TOKEN = "hf_..."
$env:STRIPE_SECRET_KEY = "sk_..."
$env:DB_PASSWORD = "your_db_pass"
$env:EMAIL_SMTP_PASSWORD = "your_gmail_app_password"

java -jar firma.jar
```

**Example - Docker:**
```dockerfile
FROM openjdk:17-slim

COPY target/firma.jar /app/firma.jar

# Secrets injected at runtime via -e flag
CMD ["java", "-jar", "/app/firma.jar"]
```

```bash
docker run \
  -e GOOGLE_MAPS_API_KEY="AIzaSy..." \
  -e HUGGINGFACE_API_TOKEN="hf_..." \
  -e STRIPE_SECRET_KEY="sk_..." \
  -e DB_PASSWORD="your_db_pass" \
  firma:latest
```

---

## Environment Variables

### Standard Variable Names (Auto-Converted by ConfigLoader)

The `ConfigLoader` automatically converts configuration keys to environment variables:

| config.properties Key | Environment Variable |
|----------------------|----------------------|
| `google.maps.api.key` | `GOOGLE_MAPS_API_KEY` |
| `huggingface.api.token` | `HUGGINGFACE_API_TOKEN` |
| `stripe.secret.key` | `STRIPE_SECRET_KEY` |
| `stripe.public.key` | `STRIPE_PUBLIC_KEY` |
| `db.password` | `DB_PASSWORD` |
| `db.username` | `DB_USERNAME` |
| `db.url` | `DB_URL` |
| `email.smtp.password` | `EMAIL_SMTP_PASSWORD` |
| `email.smtp.user` | `EMAIL_SMTP_USER` |

**Example:**
```java
// These all check GOOGLE_MAPS_API_KEY environment variable first:
ConfigLoader.get("google.maps.api.key");
ConfigLoader.getEnv("GOOGLE_MAPS_API_KEY");
```

---

## Using ConfigLoader

The `ConfigLoader` utility class makes it easy to load configuration securely.

### Basic Usage

```java
import Firma.tools.ConfigLoader;

public class MyService {
    
    // Load API key (checks env var first, then config.properties)
    String googleMapsKey = ConfigLoader.get("google.maps.api.key");
    
    // With default value if missing
    int timeout = ConfigLoader.getInt("app.timeout", 5000);
    
    // Boolean values
    boolean aiEnabled = ConfigLoader.getBoolean("feature.ai.enabled", true);
}
```

### Advanced Usage

```java
// Get from environment variables only (production)
String secret = ConfigLoader.getEnv("MY_SECRET");

// Validate required keys exist before starting app
ConfigLoader.validateRequired(
    "google.maps.api.key",
    "stripe.secret.key",
    "db.password"
);

// Print non-sensitive configuration (redacts passwords/keys)
ConfigLoader.printConfig();
```

### Updating Existing Code

**Before (Unsafe - Hardcoded):**
```java
String apiKey = "AIzaSyBtbNNKGMw6lhMYAoqnN7QeVWRcyZTlJm8"; // ❌ NEVER DO THIS
```

**After (Safe - ConfigLoader):**
```java
String apiKey = ConfigLoader.get("google.maps.api.key"); // ✅ CORRECT
```

---

## Before Pushing to GitHub

### ✅ Checklist

- [ ] `config.properties` is in `.gitignore`
- [ ] `config.properties` is NOT staged for commit
  ```bash
  git status | grep config.properties  # Should show nothing
  ```
- [ ] `.env` and `.env.*` are in `.gitignore`
- [ ] No hardcoded API keys in Java source code
  ```bash
  git diff --cached | grep -i "api.key\|secret\|password\|token"  # Should find nothing
  ```
- [ ] `config.properties.template` IS committed (for reference)
- [ ] Team members have `SECURITY.md` documentation

### 🔴 If You Accidentally Committed Secrets

**Remove from Git history:**

```bash
# 1. Remove the file from Git (but keep local copy)
git rm --cached src/main/resources/config.properties

# 2. Add to .gitignore (already done)
git add .gitignore

# 3. Commit the removal
git commit -m "security: Remove config.properties with secrets from Git"

# 4. Push to remote
git push origin main
```

**⚠️ THEN IMMEDIATELY ROTATE ALL SECRETS!**
- Generate new API keys
- Change database password
- Regenerate Stripe keys
- Update Gmail app password
- Etc.

---

## Required API Keys & How to Get Them

### 🗺️ Google Maps API Key

1. Go to: https://console.cloud.google.com/
2. Create a new project
3. Enable APIs:
   - Maps JavaScript API
   - Geocoding API
   - Places API
4. Create an API key
5. Restrict to your domain (production)

**Example:**
```bash
export GOOGLE_MAPS_API_KEY="AIzaSy..."
```

### 🤖 Hugging Face API Token

1. Go to: https://huggingface.co/settings/tokens
2. Create a new token with "read" permissions
3. Copy the token (starts with `hf_`)

**Example:**
```bash
export HUGGINGFACE_API_TOKEN="hf_..."
```

### 💳 Stripe API Keys

1. Go to: https://dashboard.stripe.com/apikeys
2. Copy both:
   - **Publishable key** (starts with `pk_`)
   - **Secret key** (starts with `sk_`)

**Example:**
```bash
export STRIPE_PUBLIC_KEY="pk_..."
export STRIPE_SECRET_KEY="sk_..."
```

### 📧 Gmail App Password (for email service)

1. Go to: https://myaccount.google.com/apppasswords
2. Select "Mail" and "Windows Computer"
3. Google will generate a 16-character password
4. Copy it (not your real Gmail password!)

**Example:**
```bash
export EMAIL_SMTP_USER="your-email@gmail.com"
export EMAIL_SMTP_PASSWORD="xxxx xxxx xxxx xxxx"  # 16 chars with spaces
```

---

## Development Workflow

### First Time Setup

```bash
# 1. Clone the repo
git clone https://github.com/your-repo/firma.git
cd firma

# 2. Copy configuration template
cp src/main/resources/config.properties.template src/main/resources/config.properties

# 3. Fill in YOUR actual API keys
nano src/main/resources/config.properties

# 4. Verify it's ignored by Git
git status | grep config.properties  # Should show nothing

# 5. Build and run
mvn clean install
mvn javafx:run
```

### Daily Development

```bash
# No special setup needed - ConfigLoader finds your config.properties

# Just code and test!
mvn test
mvn javafx:run
```

### Before Committing

```bash
# 1. Check you're not committing secrets
git diff --cached | grep -i "password\|secret\|key\|token"  # Should find nothing

# 2. Check config.properties is NOT staged
git status | grep config.properties  # Should find nothing

# 3. Commit safely
git commit -m "feature: Add new feature"
git push origin feature-branch
```

---

## Troubleshooting

### "ConfigLoader: config.properties not found"

**Problem:** Application can't load config.properties

**Solutions:**
1. Check file exists: `ls -la src/main/resources/config.properties`
2. Check Maven copy: `mvn clean compile`
3. Check IDE: Rebuild project
4. Use environment variables instead (recommended for production)

### "Invalid configuration value for key X"

**Problem:** Configuration value is wrong format

**Solution:**
```java
// Add validation when loading
String value = ConfigLoader.get("my.key");
if (value != null && !value.isEmpty()) {
    // Use value
}
```

### "Missing required configuration keys"

**Problem:** `ConfigLoader.validateRequired()` throws exception

**Solution:**
1. Check environment variables are set
2. Check config.properties has all required keys
3. Restart your application

---

## Team Guidelines

✅ **DO:**
- Use `ConfigLoader.get()` for all configuration
- Store secrets in environment variables (production)
- Use `config.properties` for local development only
- Document required API keys in `SECURITY.md`
- Rotate secrets regularly
- Review code for hardcoded credentials before commits

❌ **DON'T:**
- Hardcode API keys in source code
- Commit `config.properties` to Git
- Share credentials via email or chat
- Use the same key for dev and production
- Forget to update `.gitignore`
- Leave commented-out credentials in code

---

## Questions?

See the team wiki or contact DevOps for:
- Production credentials management
- CI/CD pipeline secrets
- Docker/Kubernetes secret injection
- Credential rotation procedures

---

**Last Updated:** 2026-03-03  
**Maintainer:** DevOps Team  
**Status:** ✅ Active

