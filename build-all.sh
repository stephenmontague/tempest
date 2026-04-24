#!/bin/bash
# =============================================================================
# Tempest WMS - Build All Script
# =============================================================================
# Builds all Maven projects in the correct order (tempest-common first,
# then workflow-worker and all services). Run this before starting Docker Compose.
#
# Usage: ./build-all.sh
# =============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=============================================${NC}"
echo -e "${BLUE}  Tempest WMS - Building All Services${NC}"
echo -e "${BLUE}=============================================${NC}"
echo ""

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Step 1: Build tempest-common (shared library)
# Note: tempest-common doesn't have its own mvnw, so we use the one from ims
echo -e "${YELLOW}[1/6] Building tempest-common...${NC}"
cd tempest-common
../ims/mvnw clean install -DskipTests -q
echo -e "${GREEN}✓ tempest-common built successfully${NC}"
cd "$SCRIPT_DIR"

# Step 2: Build workflow-worker (standalone workflow worker)
echo -e "${YELLOW}[2/6] Building workflow-worker...${NC}"
cd workflow-worker
../ims/mvnw clean package -DskipTests -q
echo -e "${GREEN}✓ workflow-worker built successfully${NC}"
cd "$SCRIPT_DIR"

# Step 3: Build IMS
echo -e "${YELLOW}[3/6] Building IMS...${NC}"
cd ims
./mvnw clean package -DskipTests -q
echo -e "${GREEN}✓ IMS built successfully${NC}"
cd "$SCRIPT_DIR"

# Step 4: Build OMS
echo -e "${YELLOW}[4/6] Building OMS...${NC}"
cd oms
./mvnw clean package -DskipTests -q
echo -e "${GREEN}✓ OMS built successfully${NC}"
cd "$SCRIPT_DIR"

# Step 5: Build WMS
echo -e "${YELLOW}[5/6] Building WMS...${NC}"
cd wms
./mvnw clean package -DskipTests -q
echo -e "${GREEN}✓ WMS built successfully${NC}"
cd "$SCRIPT_DIR"

# Step 6: Build SMS
echo -e "${YELLOW}[6/6] Building SMS...${NC}"
cd sms
./mvnw clean package -DskipTests -q
echo -e "${GREEN}✓ SMS built successfully${NC}"
cd "$SCRIPT_DIR"

echo ""
echo -e "${GREEN}=============================================${NC}"
echo -e "${GREEN}  All services built successfully!${NC}"
echo -e "${GREEN}=============================================${NC}"
echo ""
echo -e "Next steps:"
echo -e "  1. Run ${BLUE}./demo.sh up${NC} to start all services"
echo -e "  2. Access the UI at ${BLUE}http://localhost:3001${NC}"
echo -e "  3. Access Temporal UI at ${BLUE}http://localhost:8080${NC}"
echo ""
