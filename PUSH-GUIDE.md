# 🚀 Quick Git Commands for FIRMA Push

## ⚡ One-Liner: Verify & Push Safely

```bash
# BEFORE PUSHING - Run this:
git diff --cached | grep -i "password\|secret\|key\|token" || echo "✅ No secrets found"

# Then push safely:
git add .
git commit -m "security: Implement comprehensive secret management"
git push origin main
```

---

## 📋 Step-by-Step: Safe Push Procedure

### Step 1: Prepare Changes
```bash
# Stage new security files
git add \
  .gitignore \
  SECURITY.md \
  SECURITY-CHECKLIST.md \
  IMPLEMENTATION_SUMMARY.md \
  src/main/java/Firma/tools/ConfigLoader.java \
  src/main/resources/config.properties.template \
  .env.example \
  setup-security.sh \
  setup-security.ps1 \
  README.md
```

### Step 2: Verify No Secrets Staged
```bash
# Check what's about to be committed
git diff --cached | grep -i "password\|secret\|key\|token"
# Should output: NOTHING

# If it shows anything, unstage it:
git reset HEAD <filename>
```

### Step 3: Verify config.properties NOT Staged
```bash
git status | grep "config.properties"
# Should show: nothing

# If it shows modified/staged, unstage it:
git reset HEAD src/main/resources/config.properties
```

### Step 4: Commit
```bash
git commit -m "security: Implement comprehensive secret management

- Add ConfigLoader utility for secure configuration
- Create config.properties.template (no real secrets)
- Update .gitignore with comprehensive security rules
- Add setup scripts for team (Windows & Linux/Mac)
- Add SECURITY.md with complete documentation
- Add SECURITY-CHECKLIST.md for pre-push verification
- Update README.md with security section

This ensures API keys and credentials are never committed.
Team members can now safely push without exposing secrets."
```

### Step 5: Verify Commit
```bash
git log --oneline -5
# Should show your commit at the top
```

### Step 6: Push
```bash
# Dry run first (safe!)
git push --dry-run origin main
# Review the output to verify

# Then actually push
git push origin main
```

---

## 🔍 Verification Commands

### Check Git History Has No Secrets
```bash
# Search entire history for exposed keys
git log --all -S "hf_" --oneline
git log --all -S "sk_" --oneline
git log --all -S "AIzaSy" --oneline
# All should output: nothing

# If they show commits, you have a problem!
# See: SECURITY-CHECKLIST.md "If You Accidentally Pushed Secrets"
```

### Check config.properties Is Ignored
```bash
# Method 1: Status
git status | grep config.properties
# Should show: nothing

# Method 2: ls-files
git ls-files | grep "config\.properties$"
# Should show: nothing (only config.properties.template)

# Method 3: check-ignore
git check-ignore src/main/resources/config.properties
# Should output: src/main/resources/config.properties
```

### Check .gitignore Is Correct
```bash
# Verify the rule exists
grep "src/main/resources/config.properties" .gitignore
# Should output: src/main/resources/config.properties

# Verify .env is ignored
grep "^\.env" .gitignore
# Should output: .env and variants
```

---

## 🛑 Emergency: If You Pushed Secrets

### IMMEDIATE ACTION:
```bash
# 1. Remove from Git (local)
git rm --cached src/main/resources/config.properties
git commit -m "security: Remove config.properties with exposed secrets"

# 2. Force push (careful!)
git push origin main --force-with-lease

# 3. Check it's gone
git ls-files | grep config.properties
# Should show: nothing

# 4. ROTATE ALL SECRETS NOW!
#    - Google Maps API key (get new one)
#    - Hugging Face token (create new one)
#    - Stripe keys (regenerate)
#    - Database password (change it)
#    - Gmail app password (revoke and create new)
```

---

## 📊 Status Checks

### Comprehensive Safety Check
```bash
#!/bin/bash
# Run this before every push

echo "🔍 FIRMA Security Pre-Push Check"
echo "================================"
echo ""

# Check 1: Staged secrets
echo "✓ Checking for secrets in staged changes..."
SECRETS=$(git diff --cached | grep -i "password\|secret\|key\|token" | wc -l)
if [ $SECRETS -eq 0 ]; then
    echo "  ✅ No secrets found in staged changes"
else
    echo "  ❌ WARNING: Found $SECRETS lines with potential secrets!"
    exit 1
fi

# Check 2: config.properties not staged
echo "✓ Checking if config.properties is staged..."
if git diff --cached --name-only | grep -q "config\.properties$"; then
    echo "  ❌ ERROR: config.properties is staged!"
    echo "  Run: git reset HEAD src/main/resources/config.properties"
    exit 1
else
    echo "  ✅ config.properties is NOT staged"
fi

# Check 3: config.properties is ignored
echo "✓ Checking if config.properties is ignored..."
if git check-ignore -q src/main/resources/config.properties; then
    echo "  ✅ config.properties is properly ignored"
else
    echo "  ❌ ERROR: config.properties is NOT ignored!"
    exit 1
fi

# Check 4: .env is ignored
echo "✓ Checking if .env files are ignored..."
if git check-ignore -q .env; then
    echo "  ✅ .env files are properly ignored"
else
    echo "  ❌ WARNING: .env might not be ignored"
fi

echo ""
echo "================================"
echo "✅ All checks passed! Safe to push"
echo "================================"
```

Save this as `pre-push-check.sh` and run: `bash pre-push-check.sh`

---

## 📝 Commit Message Template

Use this when committing security changes:

```
security: Implement comprehensive secret management

CHANGES:
- Add ConfigLoader utility for secure configuration management
- Create config.properties.template for reference (no real secrets)
- Update .gitignore with comprehensive security rules
- Add automated setup scripts (Windows & Linux/Mac)
- Add SECURITY.md documentation
- Add SECURITY-CHECKLIST.md for verification

SECURITY:
- API keys no longer hardcoded
- Environment variables have priority
- Team can safely commit without exposing secrets
- Setup is automated and documented

TEAM IMPACT:
- One-time setup required (3-5 minutes)
- Run: setup-security.sh or setup-security.ps1
- Edit: config.properties with your API keys
- No ongoing maintenance needed

REFERENCES:
- See SECURITY.md for complete documentation
- See SECURITY-CHECKLIST.md for pre-push verification
- See README.md for quick start
```

---

## 🎯 Final Checklist Before Push

- [ ] Ran `git diff --cached | grep -i "password\|secret\|key\|token"` → Found NOTHING
- [ ] Ran `git status | grep config.properties` → Found NOTHING
- [ ] Ran `git check-ignore src/main/resources/config.properties` → Shows the file
- [ ] All new security files are staged (ConfigLoader, SECURITY.md, etc.)
- [ ] `.gitignore` is updated and staged
- [ ] README.md has security section
- [ ] No hardcoded secrets in any source code changes
- [ ] Commit message is clear and references security

**Ready to push!** ✅

---

## 🆘 Troubleshooting Commands

### Config.properties showing as modified but shouldn't be
```bash
# Check if it's tracked
git ls-files | grep config.properties
# If it shows anything:
git rm --cached src/main/resources/config.properties
git add .gitignore
git commit -m "security: Remove config.properties from Git tracking"
```

### .gitignore not working
```bash
# Verify the rule exists
grep "src/main/resources/config.properties" .gitignore

# Force Git to respect .gitignore
git rm --cached -r .
git add .

# Check it's ignored now
git status | grep config.properties
# Should show: nothing
```

### Can't remember if secrets are in my staging area
```bash
# This is safe - just shows what would be pushed
git diff --cached | less

# Search for sensitive keywords
git diff --cached | grep -i "password\|secret\|key\|token"
# Should show: nothing
```

---

## 📞 Getting Help

If you're stuck:
1. **Read:** SECURITY.md (complete guide)
2. **Check:** SECURITY-CHECKLIST.md (pre-push guide)
3. **Run:** setup-security.sh or setup-security.ps1
4. **Verify:** git status and git diff commands above
5. **Contact:** DevOps or project lead

---

**Last Updated:** 2026-03-03  
**Status:** ✅ Ready for production  
**Safe to push:** YES 🚀

