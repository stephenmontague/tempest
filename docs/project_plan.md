Below is a **single Markdown file** you can drop into your repo (or paste into GitHub) and then copy/paste each section into **GitHub Issues**. It’s structured **by phase**, ordered the way a principal engineer would actually build this system, with **Temporal-first thinking** baked in.

You’ll notice:

-    Backend-first (as you requested)
-    Each issue has **clear acceptance criteria**
-    Temporal work is explicit and incremental
-    Scope is MVP-tight but extensible

---

# Warehouse Management System (Temporal Learning Project)

This document defines the **GitHub Issues roadmap** for building a scalable MVP Warehouse Management System with **Spring Boot + Temporal + PostgreSQL**, followed by a **Next.js BFF/UI**.

---

## Phase 0 — Foundation & Platform Setup

### Issue: Create monorepo and baseline project structure

**Description**

-    Create a mono-repo (or coordinated multi-repo) structure for:

     -    `order-management-service`
     -    `inventory-management-service`
     -    `warehouse-management-service`
     -    `shipping-management-service`
     -    `ui` (future)

-    Standardize naming, Java package structure, and conventions.

**Acceptance Criteria**

-    All services build and run independently
-    Common README describing architecture and dev workflow

---

### Issue: Configure local development infrastructure

**Description**

-    Add Docker Compose for:

     -    PostgreSQL (multiple schemas or DBs)
     -    Temporal (local dev or Temporal Cloud config stub)

-    Environment-based configuration (`application.yml` + profiles)

**Acceptance Criteria**

-    All services can start locally
-    Temporal UI accessible (local or cloud)
-    Services connect to Postgres via Flyway

---

## Phase 1 — Inventory Management Service (IMS)

### Issue: Bootstrap Inventory Management Spring Boot service

**Description**

-    Spring Boot 3, Java 21
-    JPA + Flyway + Postgres
-    Actuator + Validation + Security baseline

**Acceptance Criteria**

-    Service starts cleanly
-    Flyway runs successfully
-    Health endpoint available

---

### Issue: Implement core inventory domain model

**Description**
Create entities:

-    `Item`
-    `ItemComponent` (BOM)
-    `InventoryBalance`
-    `InventoryReservation`

**Acceptance Criteria**

-    JPA entities mapped
-    Flyway migrations created
-    Referential integrity enforced

---

### Issue: Inventory CRUD and reservation APIs

**Description**
Implement APIs:

-    Create/update items
-    Receive inventory into a location
-    Reserve inventory (idempotent)
-    Release reservation

**Acceptance Criteria**

-    Reservation idempotency enforced
-    Unique constraints prevent double allocation
-    Unit + integration tests pass

---

### Issue: Inventory allocation Temporal Activities

**Description**

-    Implement Temporal Activities:

     -    `allocateInventory`
     -    `releaseInventory`

-    Activities must be idempotent and retry-safe

**Acceptance Criteria**

-    Activities registered on task queue
-    Failures retry correctly
-    Allocation state consistent after retries

---

## Phase 2 — Order Management Service (OMS)

### Issue: Bootstrap Order Management Spring Boot service

**Description**

-    Same platform stack as IMS
-    Dedicated database schema

**Acceptance Criteria**

-    Service builds and runs
-    Security + Flyway configured

---

### Issue: Implement order domain model

**Description**
Create:

-    `Order`
-    `OrderLine`
-    `OrderEvent` (append-only lifecycle log)

**Acceptance Criteria**

-    Order lifecycle states defined
-    Events recorded on state transitions

---

### Issue: Order intake and validation APIs

**Description**

-    Create order intake endpoint
-    Validate payloads
-    Generate stable order/request IDs

**Acceptance Criteria**

-    Orders persist successfully
-    Duplicate submissions handled idempotently

---

### Issue: OMS Temporal Activities

**Description**
Implement Activities:

-    `validateOrder`
-    `markOrderReserved`
-    `markOrderShipped`
-    `recordOrderFailure`

**Acceptance Criteria**

-    Activities are deterministic
-    Activity retries do not corrupt order state

---

## Phase 3 — Warehouse Management Service (WMS)

### Issue: Bootstrap Warehouse Management Spring Boot service

**Description**

-    Same stack as OMS/IMS
-    Focused on execution, not inventory ownership

**Acceptance Criteria**

-    Service builds and runs
-    Database initialized

---

### Issue: Warehouse execution domain model

**Description**
Implement:

-    `Facility`
-    `Location`
-    `Wave`
-    `PickTask`
-    `PackCarton`
-    `StagingUnit`

**Acceptance Criteria**

-    Location types supported (warehouse/store)
-    Execution state modeled explicitly

---

### Issue: Picking and packing APIs

**Description**

-    Create wave from orders
-    Generate pick tasks
-    Confirm pick and pack

**Acceptance Criteria**

-    Picking is idempotent
-    Partial picks handled correctly

---

### Issue: WMS Temporal Activities and Signals

**Description**

-    Activities:

     -    `createPickWave`

-    Signals:

     -    `pickCompleted`
     -    `packCompleted`

**Acceptance Criteria**

-    Workflow waits correctly on signals
-    Execution state survives restarts

---

## Phase 4 — Shipping Management Service (SMS)

### Issue: Bootstrap Shipping Management Spring Boot service

**Description**

-    Same stack as other services
-    Carrier abstraction (stubbed)

**Acceptance Criteria**

-    Service builds and runs
-    Schema initialized

---

### Issue: Shipment and label domain model

**Description**
Implement:

-    `Shipment`
-    `Parcel`
-    `ShippingLabel`

**Acceptance Criteria**

-    Shipment lifecycle modeled
-    Tracking number uniqueness enforced

---

### Issue: Shipping APIs and carrier stubs

**Description**

-    Create shipment
-    Generate label (stub)
-    Mark shipped

**Acceptance Criteria**

-    APIs are idempotent
-    Failures recoverable via retries

---

### Issue: Shipping Temporal Activities

**Description**

-    `createShipment`
-    `generateLabel`
-    `confirmShipment`

**Acceptance Criteria**

-    Activities retry safely
-    No duplicate shipments created

---

## Phase 5 — Temporal Orchestration (Core Learning Phase)

### Issue: Implement OrderIntakeWorkflow

**Description**
Workflow responsibilities:

1. Validate order
2. Persist intake intent
3. Create order in OMS
4. Trigger fulfillment workflow

**Acceptance Criteria**

-    Workflow survives OMS downtime
-    Idempotent workflow start via requestId

---

### Issue: Implement OrderFulfillmentWorkflow

**Description**
Steps:

1. Allocate inventory
2. Create pick wave
3. Wait for pick completion
4. Wait for pack completion
5. Create shipment
6. Mark order shipped

**Acceptance Criteria**

-    Retries work across service failures
-    Signals unblock workflow correctly
-    Workflow query exposes progress

---

### Issue: Add workflow queries and admin tooling

**Description**

-    Workflow queries for status
-    Admin endpoint to inspect workflow state

**Acceptance Criteria**

-    UI-independent visibility into workflow progress
-    Useful for debugging and demos

---

## Phase 6 — Integration & Reliability Hardening

### Issue: Implement idempotency and deduplication strategy

**Description**

-    Enforce idempotency keys across all inbound APIs
-    Unique constraints + defensive checks

**Acceptance Criteria**

-    No duplicate orders/shipments under retries

---

### Issue: Add metrics, logging, and tracing

**Description**

-    Actuator metrics
-    Temporal workflow metrics
-    Correlated logging (orderId, workflowId)

**Acceptance Criteria**

-    Easy to trace an order end-to-end

---

## Phase 7 — UI & BFF (Future)

> UI work intentionally deferred until backend + workflows are solid.

### Issue: Create Next.js BFF with Temporal access

### Issue: Order intake UI

### Issue: Workflow-driven order status timeline

### Issue: Warehouse picking UI

### Issue: Shipping confirmation UI

---

## Final Notes

-    **Backend correctness > UI polish**
-    Temporal is the _source of orchestration truth_
-    Every Activity must be retry-safe
-    Every workflow must assume services can disappear

---

If you want, next I can:

-    Convert this into **GitHub issue templates**
-    Add **labels/milestones**
-    Or break this into **week-by-week execution plan** aligned to learning goals
