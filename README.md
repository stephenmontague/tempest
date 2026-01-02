# Tempest — Warehouse Management System (MVP)

Tempest is a **Temporal-powered Warehouse Management System (WMS) MVP** designed to model a realistic order-to-ship supply chain using **modern distributed system patterns**.

The system is composed of multiple Spring Boot services, a Next.js frontend, PostgreSQL for persistence, and **Temporal** as the orchestration engine for long-running business workflows.

---

## 🏗️ High-Level Architecture

Tempest is built as a set of independently deployable services:

-    **Inventory Management Service (IMS)**
     Manages items, bills of materials (kits), inventory balances, and reservations.

-    **Order Management Service (OMS)**
     Manages orders, order lines, and order lifecycle state.

-    **Warehouse Management Service (WMS)**
     Handles warehouse execution such as pick waves, pick tasks, packing, and staging.

-    **Shipping Management Service (SMS)**
     Manages shipments, carrier integration, labels, and tracking.

-    **Temporal**
     Orchestrates long-running workflows such as order intake and fulfillment.

-    **UI (Next.js)**
     Provides a web interface and acts as a Backend-for-Frontend (BFF).

Each service owns its own data and communicates with others via APIs and **Temporal Activities**, not direct database access.

---

## 📦 Repository Structure

```
.
├── ims/        # Inventory Management Service
├── oms/        # Order Management Service
├── wms/        # Warehouse Management Service
├── sms/        # Shipping Management Service
├── ui/         # Next.js UI + BFF
├── docs/       # Architecture and planning documents
└── .github/    # GitHub issue templates and config
```

---

## 🔁 Core Workflows

The system currently focuses on two primary workflows:

### Order Intake

-    Accepts an order submission
-    Validates and persists the order
-    Starts fulfillment orchestration

### Order Fulfillment

-    Allocates inventory
-    Creates warehouse pick work
-    Waits for picking and packing
-    Creates shipment and label
-    Marks order as shipped

All workflows are **durable, retryable, and resumable** using Temporal.

---

## 🚀 Getting Started

### Prerequisites

-    **Docker & Docker Compose** — required for quick start
-    **Java 21 + Maven** — only needed for manual development
-    **Node.js 18+** — only needed for manual UI development

---

### Quick Start (Docker)

The easiest way to run Tempest is with the included demo script, which starts everything in Docker containers:

```bash
./demo.sh up
```

This starts:
-    PostgreSQL database
-    Temporal server + UI
-    All backend services (IMS, OMS, WMS, SMS)
-    Next.js UI

**Access points:**

| Service      | URL                      |
|--------------|--------------------------|
| UI           | http://localhost:3000    |
| Temporal UI  | http://localhost:8080    |
| IMS API      | http://localhost:8081    |
| OMS API      | http://localhost:8082    |
| WMS API      | http://localhost:8083    |
| SMS API      | http://localhost:8084    |

**Useful commands:**

```bash
./demo.sh status          # Check service health
./demo.sh logs <service>  # Tail logs (e.g., ./demo.sh logs ims)
./demo.sh down            # Stop all services
./demo.sh clean           # Remove all containers and volumes (fresh start)
```

---

### Demonstrating Temporal Resilience

One of Tempest's key features is demonstrating how Temporal handles service failures. Try this:

1. Start a wave workflow in the UI
2. Kill a service mid-workflow:
   ```bash
   ./demo.sh kill ims
   ```
3. Watch the Temporal UI at http://localhost:8080 — the workflow will pause and retry
4. Restart the service:
   ```bash
   ./demo.sh start ims
   ```
5. Watch the workflow automatically resume and complete

---

### Manual Development

For active development, you may want to run services individually:

**1. Build the shared library:**

```bash
cd tempest-common
mvn clean install
```

This installs the shared DTOs and utilities used by all backend services.

**2. Create local configuration files:**

Each service needs an `application-local.yml` file for local development. These files are gitignored.

Create `ims/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tempest_ims
    username: your_postgres_user
    password: your_postgres_password

temporal:
  namespace: tempest
  address: localhost:7233

logging:
  level:
    app.tempest.ims: DEBUG
```

Repeat for each service (`oms`, `wms`, `sms`), adjusting:
- Database name: `tempest_oms`, `tempest_wms`, `tempest_sms`
- Logging package: `app.tempest.oms`, `app.tempest.wms`, `app.tempest.sms`

**3. Start infrastructure** (or use the demo stack for Temporal):

```bash
docker compose up -d postgres temporal temporal-ui
```

**4. Run backend services:**

```bash
cd ims
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Repeat for `oms`, `wms`, and `sms`.

**5. Run the UI:**

```bash
cd ui
npm install
npm run dev
```

---

### Using Temporal CLI (Alternative)

If you prefer running Temporal locally via the CLI instead of Docker, you can use the `temporal` CLI tool:

**1. Install the Temporal CLI:**

```bash
# macOS (Homebrew)
brew install temporal

# Or download from: https://temporal.io/download
```

**2. Start a local Temporal dev server:**

```bash
temporal server start-dev --namespace tempest
```

This starts Temporal on `localhost:7233` with the UI at `http://localhost:8233`.

**3. Start just the database:**

```bash
docker compose up -d postgres
```

**4. Run the backend services** pointing to your local Temporal:

```bash
cd ims
TEMPORAL_ADDRESS=localhost:7233 mvn spring-boot:run
```

Repeat for `oms`, `wms`, and `sms`.

**Note:** When using the Temporal CLI, the UI is available at port `8233` instead of `8080`.

---

## ⚙️ Configuration

All services use standard Spring Boot configuration:

-    `application.yml` contains safe defaults
-    Sensitive values (DB passwords, Temporal credentials, etc.) are supplied via environment variables
-    Local-only overrides can be placed in `application-local.yml` (ignored by Git, see Manual Development above)

---

## 🔐 Security

-    No secrets are committed to the repository
-    Services are designed to support JWT/OAuth2 for API security
-    Temporal credentials are kept server-side only

---

## 🧪 Status

This project is an **MVP** and under active development.

Current focus:

-    Backend services
-    Temporal workflows
-    Core warehouse fulfillment flow

Future work includes:

-    UI polish
-    External integrations (e.g., Shopify, ERP systems)
-    Advanced fulfillment scenarios

---

## 📄 License

MIT (or specify another license if desired)