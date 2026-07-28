#!/bin/bash
# =============================================================================
# Tempest - Build All Script
# =============================================================================
# Builds all Maven modules in dependency order:
#   libs:    tempest-common -> tempest-domain -> tempest-temporal
#   apps:    tempest-api (REST/CRUD) + ims/oms/wms/sms-worker (Temporal workers)
# Run this before starting Docker Compose.
#
# Usage: ./build-all.sh
# =============================================================================

set -e  # Exit on error

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=============================================${NC}"
echo -e "${BLUE}  Tempest - Building All Modules${NC}"
echo -e "${BLUE}=============================================${NC}"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Shared libraries first (installed to local .m2), then the apps.
LIBS=(tempest-common tempest-domain tempest-temporal)
APPS=(tempest-api ims-worker oms-worker wms-worker sms-worker)

step=1
total=$(( ${#LIBS[@]} + ${#APPS[@]} ))

for m in "${LIBS[@]}"; do
	echo -e "${YELLOW}[$step/$total] Building $m (install)...${NC}"
	(cd "$m" && ./mvnw clean install -DskipTests -q)
	echo -e "${GREEN}✓ $m${NC}"
	step=$((step+1))
done

for m in "${APPS[@]}"; do
	echo -e "${YELLOW}[$step/$total] Building $m (package)...${NC}"
	(cd "$m" && ./mvnw clean package -DskipTests -q)
	echo -e "${GREEN}✓ $m${NC}"
	step=$((step+1))
done

echo ""
echo -e "${GREEN}=============================================${NC}"
echo -e "${GREEN}  All modules built successfully!${NC}"
echo -e "${GREEN}=============================================${NC}"
echo ""
echo -e "Next steps:"
echo -e "  1. Ensure your Temporal 1.31.1 is running on localhost:7233 with a 'tempest' namespace"
echo -e "  2. Run ${BLUE}./demo.sh up${NC} to start postgres, tempest-api, the 4 workers, and the UI"
echo -e "  3. Access the UI at ${BLUE}http://localhost:3001${NC}"
echo ""
