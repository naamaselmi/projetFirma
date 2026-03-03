# 🔐 FIRMA Security Checklist

Use this checklist BEFORE pushing code to GitHub to ensure no secrets are leaked.

## ✅ Pre-Push Security Checklist

### 1. Configuration Files
- [ ] `config.properties` is in `.gitignore`
- [ ] `config.properties` is NOT staged for commit
  ```bash
  git status | grep config.properties
  # Should show: nothing
  ```
- [ ] `.env*` files are in `.gitignore`
- [ ] `secrets.json` is in `.gitignore`

### 2. No Hardcoded Secrets in Code
- [ ] No API keys in Java source files
  ```bash
  git diff --cached | grep -i "AIzaSy\|hf_\|sk_\|pk_"
  # Should show: nothing
  ```
- [ ] No database passwords in code
  ```bash
  git diff --cached | grep -i "password\|passwd"
  # Should show: nothing (except comments about config)
  ```
- [ ] No email credentials in code
  ```bash
  git diff --cached | grep -i "smtp_password\|gmail"
  # Should show: nothing
  ```
- [ ] No Stripe keys in code
  ```bash
  git diff --cached | grep -i "stripe"
  # Should show: nothing (except variable names)
  ```

### 3. Files Ready for Commit
- [ ] `config.properties.template` IS committed (for reference)
- [ ] `SECURITY.md` IS committed (documentation)
- [ ] `.gitignore` IS committed (with security rules)
- [ ] `ConfigLoader.java` IS committed (utility class)
- [ ] `setup-security.sh` IS committed (setup script)
- [ ] `setup-security.ps1` IS committed (setup script)
- [ ] This file `SECURITY-CHECKLIST.md` IS committed

### 4. Template and Configuration
- [ ] `config.properties.template` contains NO real values
  ```bash
  grep "REPLACE_WITH_YOUR" src/main/resources/config.properties.template
  # Should show: all keys have REPLACE_WITH_YOUR_*
  ```
- [ ] `SECURITY.md` has setup instructions
- [ ] `README.md` references security setup

### 5. Git History Check
- [ ] No secrets in previous commits
  ```bash
  git log --all -S "hf_\|sk_\|AIzaSy" --oneline
  # Should show: nothing
  ```
- [ ] No accidental commits of config.properties
  ```bash
  git ls-files | grep "config.properties$"
  # Should show: nothing
  # (Only config.properties.template)
  ```

### 6. Team Coordination
- [ ] All team members have run `setup-security.sh` or `setup-security.ps1`
- [ ] All team members have their own `config.properties` (NOT shared)
- [ ] `.gitignore` rules are understood by all team members
- [ ] Team has access to DevOps for production credentials

### 7. CI/CD and Deployment
- [ ] CI/CD pipeline uses environment variables for secrets
- [ ] No hardcoded credentials in GitHub Actions workflows
- [ ] Docker builds use secrets injection (not COPY of config.properties)
- [ ] Kubernetes/deployment uses Secret objects (not ConfigMaps)

---

## 🚨 Common Mistakes to Avoid

### ❌ Mistake 1: Staging config.properties
```bash
# WRONG - Don't do this!
git add src/main/resources/config.properties
git commit -m "Add configuration"

# RIGHT - Only add the template
git add src/main/resources/config.properties.template
```

### ❌ Mistake 2: Forgetting .gitignore
```bash
# Check that rule exists
grep "src/main/resources/config.properties" .gitignore
# Must show: src/main/resources/config.properties
```

### ❌ Mistake 3: Hardcoding secrets in source code
```java
// WRONG - Never do this!
String apiKey = "AIzaSyBtbNNKGMw6lhMYAoqnN7QeVWRcyZTlJm8";

// RIGHT - Load from ConfigLoader
String apiKey = ConfigLoader.get("google.maps.api.key");
```

### ❌ Mistake 4: Committing secrets in comments
```java
// WRONG - Secrets in comments!
// Google Maps API Key: AIzaSyBtbNNKGMw6lhMYAoqnN7QeVWRcyZTlJm8
String key = getApiKey();

// RIGHT - No secrets anywhere
// Load Google Maps API Key from configuration
String key = ConfigLoader.get("google.maps.api.key");
```

---

## 🔍 How to Check Before Pushing

### 1. Check what you're about to push
```bash
git diff origin/main HEAD | grep -i "password\|secret\|key\|token"
# Should show: nothing
```

### 2. Check staged changes
```bash
git diff --cached | head -100  # View first 100 lines
# Verify no secrets are staged
```

### 3. Check .gitignore is working
```bash
# List all files Git would track (if .gitignore didn't exist)
git status --ignored | grep config.properties
# Should show: Ignored files: src/main/resources/config.properties
```

### 4. Final safety check before push
```bash
# This command shows what will be pushed
git push --dry-run origin main

# Carefully review the file list
# config.properties should NOT appear in the list
```

---

## 🛠️ If You Accidentally Pushed Secrets

### IMMEDIATE ACTION REQUIRED:

```bash
# 1. Remove from Git history (local)
git rm --cached src/main/resources/config.properties
git commit -m "security: Remove config.properties with exposed secrets"

# 2. Update .gitignore if needed
git add .gitignore
git commit -m "security: Update .gitignore rules"

# 3. Force push (only if allowed)
git push origin main --force-with-lease

# 4. Rotate ALL secrets immediately
#    - Google Maps API key
#    - Hugging Face token
#    - Stripe keys
#    - Database password
#    - Gmail app password
#    - Any other exposed credentials

# 5. Alert your team and manager
```

---

## 📋 For Code Review

When reviewing a pull request, verify:

- [ ] No `config.properties` file (only `.template`)
- [ ] No `.env` files
- [ ] No hardcoded API keys or passwords
- [ ] `.gitignore` includes all sensitive files
- [ ] ConfigLoader is used for configuration
- [ ] Comments don't contain credentials
- [ ] No suspicious base64-encoded strings (might be encoded credentials)

---

## 🔗 Related Documents

- [SECURITY.md](SECURITY.md) - Full security documentation
- [README.md](README.md) - Setup instructions
- [.gitignore](.gitignore) - Ignore rules
- [setup-security.sh](setup-security.sh) - Linux/Mac setup
- [setup-security.ps1](setup-security.ps1) - Windows setup

---

## 📞 Questions or Issues?

- If you accidentally committed secrets: Contact DevOps immediately
- If you need help with setup: See [SECURITY.md](SECURITY.md)
- If you can't access an API: Ask team lead for credentials
- If .gitignore is not working: Run `git rm --cached filename`

---

**Last Updated:** 2026-03-03  
**Version:** 1.0  
**Status:** ✅ Active

