#!/bin/zsh
# =============================================================================
# Tempest WMS - Demo Helper Script
# =============================================================================
# Interactive script for controlling the demo environment.
# Designed for demonstrating Temporal's resilience by killing/restarting services.
#
# Usage:
#   ./demo.sh up              - Start all services
#   ./demo.sh down            - Stop all services
#   ./demo.sh kill <service>  - Kill a specific service (ims, oms, wms, sms)
#   ./demo.sh start <service> - Start a killed service
#   ./demo.sh restart <svc>   - Restart a service
#   ./demo.sh logs <service>  - Tail logs for a service
#   ./demo.sh status          - Show status of all containers
#   ./demo.sh clean           - Remove all containers and volumes (fresh start)
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Service name mapping (short name -> container name)
typeset -A SERVICES
SERVICES=(
    ims "tempest-ims"
    oms "tempest-oms"
    wms "tempest-wms"
    sms "tempest-sms"
    ui "tempest-ui"
    temporal "tempest-temporal"
    temporal-ui "tempest-temporal-ui"
    postgres "tempest-postgres"
)

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Print header
print_header() {
    echo -e "${BLUE}=============================================${NC}"
    echo -e "${BLUE}  Tempest WMS - Demo Control${NC}"
    echo -e "${BLUE}=============================================${NC}"
    echo ""
}

# Print usage
print_usage() {
    echo -e "${BOLD}Usage:${NC} ./demo.sh <command> [service]"
    echo ""
    echo -e "${BOLD}Commands:${NC}"
    echo -e "  ${GREEN}up${NC}              Start all services"
    echo -e "  ${GREEN}down${NC}            Stop all services"
    echo -e "  ${RED}kill${NC} <service>  Kill a specific service"
    echo -e "  ${GREEN}start${NC} <service> Start a killed service"
    echo -e "  ${YELLOW}restart${NC} <svc>   Restart a service"
    echo -e "  ${CYAN}logs${NC} <service>  Tail logs for a service"
    echo -e "  ${CYAN}status${NC}          Show status of all containers"
    echo -e "  ${RED}clean${NC}           Remove all containers and volumes"
    echo ""
    echo -e "${BOLD}Services:${NC}"
    echo -e "  ims, oms, wms, sms, ui, temporal, temporal-ui, postgres"
    echo ""
    echo -e "${BOLD}Demo Examples:${NC}"
    echo -e "  ${YELLOW}# Start a wave workflow, then kill IMS to see Temporal retry${NC}"
    echo -e "  ./demo.sh kill ims"
    echo -e "  ${YELLOW}# Watch Temporal UI at http://localhost:8080${NC}"
    echo -e "  ${YELLOW}# Restart IMS to see workflow resume${NC}"
    echo -e "  ./demo.sh start ims"
    echo ""
}

# Get container name from service short name
get_container_name() {
    local service=$1
    if [[ -n "${SERVICES[$service]}" ]]; then
        echo "${SERVICES[$service]}"
    else
        echo ""
    fi
}

# Start all services
cmd_up() {
    print_header
    echo -e "${GREEN}Starting all Tempest services...${NC}"
    echo ""
    docker compose up -d --build
    echo ""
    echo -e "${GREEN}=============================================${NC}"
    echo -e "${GREEN}  All services started!${NC}"
    echo -e "${GREEN}=============================================${NC}"
    echo ""
    echo -e "Access points:"
    echo -e "  ${BLUE}UI:${NC}          http://localhost:3001"
    echo -e "  ${BLUE}Temporal UI:${NC} http://localhost:8080"
    echo -e "  ${BLUE}IMS API:${NC}     http://localhost:8081"
    echo -e "  ${BLUE}OMS API:${NC}     http://localhost:8082"
    echo -e "  ${BLUE}WMS API:${NC}     http://localhost:8083"
    echo -e "  ${BLUE}SMS API:${NC}     http://localhost:8084"
    echo ""
    echo -e "Run ${CYAN}./demo.sh status${NC} to check service health"
    echo ""
}

# Stop all services
cmd_down() {
    print_header
    echo -e "${YELLOW}Stopping all Tempest services...${NC}"
    docker compose down
    echo -e "${GREEN}All services stopped.${NC}"
}

# Kill a specific service
cmd_kill() {
    local service=$1
    if [[ -z "$service" ]]; then
        echo -e "${RED}Error: Please specify a service to kill${NC}"
        echo -e "Usage: ./demo.sh kill <service>"
        echo -e "Services: ims, oms, wms, sms, ui"
        exit 1
    fi

    local container=$(get_container_name "$service")
    if [[ -z "$container" ]]; then
        echo -e "${RED}Error: Unknown service '$service'${NC}"
        echo -e "Valid services: ims, oms, wms, sms, ui, temporal, temporal-ui, postgres"
        exit 1
    fi

    print_header
    echo -e "${RED}Killing service: ${BOLD}$service${NC} (container: $container)"
    echo ""
    docker stop "$container"
    echo ""
    echo -e "${RED}✗ $service is now DOWN${NC}"
    echo ""
    echo -e "The Temporal workflow will pause and retry the Activity."
    echo -e "Watch the Temporal UI at ${BLUE}http://localhost:8080${NC}"
    echo ""
    echo -e "To restart: ${GREEN}./demo.sh start $service${NC}"
    echo ""
}

# Start a specific service
cmd_start() {
    local service=$1
    if [[ -z "$service" ]]; then
        echo -e "${RED}Error: Please specify a service to start${NC}"
        echo -e "Usage: ./demo.sh start <service>"
        exit 1
    fi

    local container=$(get_container_name "$service")
    if [[ -z "$container" ]]; then
        echo -e "${RED}Error: Unknown service '$service'${NC}"
        echo -e "Valid services: ims, oms, wms, sms, ui, temporal, temporal-ui, postgres"
        exit 1
    fi

    print_header
    echo -e "${GREEN}Starting service: ${BOLD}$service${NC} (container: $container)"
    echo ""
    docker start "$container"
    echo ""
    echo -e "${GREEN}✓ $service is starting...${NC}"
    echo ""
    echo -e "The Temporal worker will reconnect and resume the workflow."
    echo -e "Watch the Temporal UI at ${BLUE}http://localhost:8080${NC}"
    echo ""
}

# Restart a specific service
cmd_restart() {
    local service=$1
    if [[ -z "$service" ]]; then
        echo -e "${RED}Error: Please specify a service to restart${NC}"
        echo -e "Usage: ./demo.sh restart <service>"
        exit 1
    fi

    local container=$(get_container_name "$service")
    if [[ -z "$container" ]]; then
        echo -e "${RED}Error: Unknown service '$service'${NC}"
        exit 1
    fi

    print_header
    echo -e "${YELLOW}Restarting service: ${BOLD}$service${NC}"
    docker restart "$container"
    echo -e "${GREEN}✓ $service restarted${NC}"
}

# Tail logs for a service
cmd_logs() {
    local service=$1
    if [[ -z "$service" ]]; then
        echo -e "${RED}Error: Please specify a service${NC}"
        echo -e "Usage: ./demo.sh logs <service>"
        exit 1
    fi

    local container=$(get_container_name "$service")
    if [[ -z "$container" ]]; then
        echo -e "${RED}Error: Unknown service '$service'${NC}"
        exit 1
    fi

    echo -e "${CYAN}Tailing logs for $service (Ctrl+C to stop)...${NC}"
    echo ""
    docker logs -f "$container"
}

# Show status of all containers
cmd_status() {
    print_header
    echo -e "${CYAN}Service Status:${NC}"
    echo ""
    
    # Table header
    printf "%-15s %-20s %-10s\n" "SERVICE" "CONTAINER" "STATUS"
    printf "%-15s %-20s %-10s\n" "-------" "---------" "------"
    
    for service in ims oms wms sms ui temporal temporal-ui postgres; do
        container="${SERVICES[$service]}"
        status=$(docker inspect -f '{{.State.Status}}' "$container" 2>/dev/null || echo "not found")
        
        # Color based on status
        case $status in
            "running")
                status_color="${GREEN}● running${NC}"
                ;;
            "exited")
                status_color="${RED}○ stopped${NC}"
                ;;
            *)
                status_color="${YELLOW}? $status${NC}"
                ;;
        esac
        
        printf "%-15s %-20s " "$service" "$container"
        echo -e "$status_color"
    done
    
    echo ""
    echo -e "Access points:"
    echo -e "  ${BLUE}UI:${NC}          http://localhost:3001"
    echo -e "  ${BLUE}Temporal UI:${NC} http://localhost:8080"
    echo ""
}

# Clean up everything
cmd_clean() {
    print_header
    echo -e "${RED}${BOLD}WARNING: This will remove all containers and volumes!${NC}"
    echo -e "All data will be lost (databases, workflow history, etc.)"
    echo ""
    echo -n "Are you sure? (y/N) "
    read -r REPLY
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Removing all containers and volumes...${NC}"
        docker compose down -v --remove-orphans
        echo -e "${GREEN}Clean complete. Run './demo.sh up' for a fresh start.${NC}"
    else
        echo -e "${BLUE}Cancelled.${NC}"
    fi
}

# Main command dispatcher
case "${1:-}" in
    up)
        cmd_up
        ;;
    down)
        cmd_down
        ;;
    kill)
        cmd_kill "$2"
        ;;
    start)
        cmd_start "$2"
        ;;
    restart)
        cmd_restart "$2"
        ;;
    logs)
        cmd_logs "$2"
        ;;
    status)
        cmd_status
        ;;
    clean)
        cmd_clean
        ;;
    *)
        print_header
        print_usage
        ;;
esac

