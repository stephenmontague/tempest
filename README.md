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

## 🚀 Getting Started (Local Development)

### Prerequisites

-    Java 21
-    Node.js 18+
-    Docker & Docker Compose
-    Maven
-    Temporal (local or Temporal Cloud account)

---

### 1. Start Infrastructure

Start PostgreSQL and Temporal (example with Docker):

```bash
docker compose up -d
```

(Exact `docker-compose.yml` may evolve as the project matures.)

---

### 2. Run Backend Services

Each service can be run independently:

```bash
cd ims
./mvnw spring-boot:run
```

Repeat for:

-    `oms`
-    `wms`
-    `sms`

---

### 3. Run the UI

```bash
cd ui
npm install
npm run dev
```

The UI will be available at:

```
http://localhost:3000
```

---

## ⚙️ Configuration

All services use standard Spring Boot configuration:

-    `application.properties` contains safe defaults
-    Sensitive values (DB passwords, Temporal credentials, etc.) are supplied via environment variables
-    Local-only overrides can be placed in `application-local.properties` (ignored by Git)

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