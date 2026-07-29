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
     Orchestrates the long-running fulfillment workflow (wave execution) plus a Random DAG demo.

-    **UI (Next.js)**
     Provides a web interface and acts as a Backend-for-Frontend (BFF). It is also the
     single **Temporal client**: server actions / route handlers use the Temporal
     TypeScript SDK (`@temporalio/client`) to start workflows, send signals, and run
     queries. All Temporal credentials stay server-side.

Domains communicate via **Temporal Activities**, not direct cross-domain database access.

### Client, API, workers

- The **UI is the only Temporal client** — no backend app starts workflows. It starts
  them (and sends signals/queries/updates) via the Temporal TypeScript SDK, cross-language
  against the Java workers by workflow-type name.
- **`tempest-api`** is a single Spring Boot app that serves **all** CRUD (orders, items,
  waves, shipments) against one shared `tempest` database. It runs no Temporal.
- The **four workers** (`ims/oms/wms/sms-worker`) are standalone apps, each polling its own
  task queue (`ims-tasks`, `oms-tasks`, `wms-tasks`, `sms-tasks`). Preserving per-service
  queues means killing one worker stalls only that domain's activities (the resilience
  demo) while the API stays up.
- Shared code is split into libraries: `tempest-common` (DTOs, security, task queues,
  activity interfaces), `tempest-domain` (entities, repositories, CRUD services), and
  `tempest-temporal` (the Temporal client config — depended on by workers only).

### Prerequisite: bring your own Temporal

Temporal is **not** run by this compose stack. Run your own Temporal (tested with 1.31.1)
on `localhost:7233` with a `tempest` namespace registered, e.g.:

```bash
temporal server start-dev --namespace tempest --ip 0.0.0.0
```

The containers reach it at `host.docker.internal:7233`; set the UI's `TEMPORAL_UI_BASE_URL`
to your Temporal Web UI.

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

### Order Intake (CRUD — not a workflow)

Order intake is a single-service operation (validate → persist → mark `AWAITING_WAVE`),
so it is a plain transactional `POST /orders` endpoint on OMS, **not** a Temporal
workflow. Fulfillment orchestration is triggered later when a wave is released.

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
-    PostgreSQL database (single `tempest` DB)
-    `tempest-api` (unified REST/CRUD API)
-    Four Temporal workers (ims/oms/wms/sms)
-    Next.js UI

(Temporal itself is your own external cluster — see the prerequisite above.)

**Access points:**

| Service      | URL                      |
|--------------|--------------------------|
| UI           | http://localhost:3001    |
| API (CRUD)   | http://localhost:8081    |
| Temporal UI  | http://localhost:8080    (your external cluster) |

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