#!/bin/bash
# =====================================================
# FIRMA Security Setup Script
# =====================================================
# This script helps set up secure configuration for FIRMA
# Run this ONCE during project setup
#
# Usage: bash setup-security.sh
# =====================================================

set -e  # Exit on error

echo "🔐 FIRMA Security Setup"
echo "======================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running from project root
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ Error: pom.xml not found. Please run from project root.${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Checking .gitignore configuration${NC}"
if grep -q "src/main/resources/config.properties" .gitignore; then
    echo -e "${GREEN}✅ config.properties is in .gitignore${NC}"
else
    echo -e "${RED}❌ config.properties NOT in .gitignore${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Step 2: Creating config.properties from template${NC}"
if [ ! -f "src/main/resources/config.properties" ]; then
    if [ -f "src/main/resources/config.properties.template" ]; then
        cp src/main/resources/config.properties.template src/main/resources/config.properties
        echo -e "${GREEN}✅ Created config.properties${NC}"
    else
        echo -e "${RED}❌ Template file not found${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️  config.properties already exists, skipping copy${NC}"
fi

echo ""
echo -e "${YELLOW}Step 3: Checking if config.properties was previously committed${NC}"
if git ls-files --error-unmatch src/main/resources/config.properties 2>/dev/null; then
    echo -e "${RED}⚠️  WARNING: config.properties is already in Git history!${NC}"
    echo ""
    echo "This file contains sensitive information and must be removed:"
    echo ""
    echo "    git rm --cached src/main/resources/config.properties"
    echo "    git commit -m 'security: Remove config.properties with secrets'"
    echo "    git push origin main"
    echo ""
    echo -e "${YELLOW}Then regenerate all your API keys and passwords!${NC}"
    echo ""
else
    echo -e "${GREEN}✅ config.properties is not in Git history (good!)${NC}"
fi

echo ""
echo -e "${YELLOW}Step 4: Configuration Instructions${NC}"
echo ""
echo "Now edit your local config.properties:"
echo "  nano src/main/resources/config.properties"
echo ""
echo "Replace all REPLACE_WITH_YOUR_* values with your actual credentials:"
echo ""
echo "Required Configuration Keys:"
echo "  📍 google.maps.api.key"
echo "  🤖 huggingface.api.token"
echo "  💳 stripe.secret.key"
echo "  💳 stripe.public.key"
echo "  🗄️  db.password"
echo "  📧 email.smtp.password"
echo ""

echo -e "${YELLOW}Step 5: How to get your API keys${NC}"
echo ""
echo "📍 Google Maps API Key:"
echo "   https://console.cloud.google.com/apis/credentials"
echo ""
echo "🤖 Hugging Face Token:"
echo "   https://huggingface.co/settings/tokens"
echo ""
echo "💳 Stripe Keys:"
echo "   https://dashboard.stripe.com/apikeys"
echo ""
echo "📧 Gmail App Password:"
echo "   https://myaccount.google.com/apppasswords"
echo ""

echo -e "${YELLOW}Step 6: Verification${NC}"
# Check if config.properties has placeholder values
if grep -q "REPLACE_WITH_YOUR" src/main/resources/config.properties; then
    echo -e "${YELLOW}⚠️  config.properties still has placeholder values${NC}"
    echo "   Edit the file and replace all REPLACE_WITH_YOUR_* values"
    echo ""
else
    echo -e "${GREEN}✅ config.properties appears to be configured${NC}"
fi

# Check ConfigLoader exists
if [ -f "src/main/java/Firma/tools/ConfigLoader.java" ]; then
    echo -e "${GREEN}✅ ConfigLoader.java exists${NC}"
else
    echo -e "${RED}❌ ConfigLoader.java not found${NC}"
    exit 1
fi

# Check SECURITY.md exists
if [ -f "SECURITY.md" ]; then
    echo -e "${GREEN}✅ SECURITY.md documentation exists${NC}"
else
    echo -e "${RED}❌ SECURITY.md not found${NC}"
    exit 1
fi

echo ""
echo "======================="
echo -e "${GREEN}🎉 Security Setup Complete!${NC}"
echo "======================="
echo ""
echo "Next Steps:"
echo "1. Edit config.properties with your actual API keys"
echo "2. Run: mvn clean install"
echo "3. Run: mvn javafx:run"
echo ""
echo "For team members:"
echo "- Share this setup script and SECURITY.md"
echo "- DO NOT share actual config.properties files"
echo "- DO NOT commit API keys or passwords"
echo ""

