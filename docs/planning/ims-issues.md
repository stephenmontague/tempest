# Inventory Management System (IMS) — Issue Breakdown

This document is the canonical planning reference for the **Inventory Management System**.
IMS is the **system of record for items, inventory balances, and reservations** and must
be extremely correct under retries and partial failures.

Milestone: Phase 1 – Inventory Management Service

---

## EPIC 1 — IMS Service Foundation

### Goal

Bootstrap a Spring Boot service that owns inventory truth and integrates safely with Temporal.

---

### STORY 1.1 — Bootstrap IMS Spring Boot service

**Type:** Setup  
**Labels:** domain:inventory, type:setup, priority:core

**Description**  
Create the Inventory Management System (IMS) Spring Boot service using the standard platform stack.

**Scope**

-    Spring Boot 3
-    Java 21
-    JPA + Flyway + PostgreSQL
-    Security baseline
-    Actuator + Validation
-    Temporal worker runtime

**Acceptance Criteria**

-    Service starts locally
-    Flyway migrations run successfully
-    `/actuator/health` returns UP
-    Temporal worker connects successfully

---

### STORY 1.2 — IMS configuration & environment profiles

**Type:** Setup  
**Labels:** domain:inventory, type:setup

**Description**  
Add environment-based configuration:

-    `local`
-    `dev`
-    `prod` (stub)

**Acceptance Criteria**

-    Profile selection via `SPRING_PROFILES_ACTIVE`
-    No secrets committed
-    Local profile usable with Docker Compose

---

## EPIC 2 — Item & BOM Domain Model

### Goal

Define items and item composition (kits/BOMs) cleanly and extensibly.

---

### STORY 2.1 — Item entity

**Type:** Domain  
**Labels:** domain:inventory, database, priority:core

**Fields**

-    id
-    sku
-    type (SIMPLE | KIT)
-    uom
-    active
-    attributes (JSON)

**Acceptance Criteria**

-    SKU uniqueness enforced
-    Item type enum validated
-    Flyway migration created

---

### STORY 2.2 — ItemComponent (BOM) entity

**Type:** Domain  
**Labels:** domain:inventory, database

**Description**
Defines component relationships for KIT items.

**Fields**

-    id
-    parentSku
-    componentSku
-    quantity

**Acceptance Criteria**

-    Parent item must be KIT
-    No circular BOMs
-    Quantity > 0 enforced

---

## EPIC 3 — Inventory Balance Model

### Goal

Represent **on-hand vs reserved inventory** at a granular location level.

---

### STORY 3.1 — InventoryBalance entity

**Type:** Domain  
**Labels:** domain:inventory, database, priority:core

**Fields**

-    id
-    facilityId
-    locationId
-    sku
-    onHandQuantity
-    reservedQuantity

**Acceptance Criteria**

-    Unique `(facilityId, locationId, sku)`
-    reserved ≤ onHand
-    Quantities never negative

---

### STORY 3.2 — Inventory adjustment logic

**Type:** Domain  
**Labels:** domain:inventory, priority:learning

**Description**
Support receiving and adjustment of inventory.

**Acceptance Criteria**

-    Receiving increases onHand
-    Adjustments logged
-    Negative inventory prevented

---

## EPIC 4 — Inventory Reservation Model

### Goal

Allow inventory to be **safely reserved** for orders with idempotency guarantees.

---

### STORY 4.1 — InventoryReservation entity

**Type:** Domain  
**Labels:** domain:inventory, database, priority:core

**Fields**

-    id
-    orderId
-    sku
-    quantity
-    facilityId
-    status (ACTIVE | RELEASED | CONSUMED)

**Acceptance Criteria**

-    Unique `(orderId, sku)`
-    Status transitions enforced
-    Quantity validated

---

### STORY 4.2 — Reservation allocation logic

**Type:** Domain  
**Labels:** domain:inventory, priority:core

**Description**
Allocate inventory by:

-    Checking availability
-    Incrementing reserved
-    Creating reservation record

**Acceptance Criteria**

-    Allocation atomic
-    Fails cleanly if insufficient stock
-    Safe under concurrent requests

---

## EPIC 5 — Inventory APIs

### Goal

Expose inventory operations to other services and Temporal Activities.

---

### STORY 5.1 — Item management APIs

**Type:** API  
**Labels:** domain:inventory, api

**Endpoints**

-    `POST /items`
-    `GET /items/{sku}`

**Acceptance Criteria**

-    Validated input
-    SKU uniqueness enforced

---

### STORY 5.2 — Inventory receiving API

**Type:** API  
**Labels:** domain:inventory, api

**Endpoints**

-    `POST /inventory/receive`

**Acceptance Criteria**

-    Increases onHand quantity
-    Adjustment recorded
-    Idempotency supported

---

### STORY 5.3 — Inventory reservation API

**Type:** API  
**Labels:** domain:inventory, api, priority:core

**Endpoints**

-    `POST /inventory/reserve`
-    `POST /inventory/release`

**Acceptance Criteria**

-    Idempotent behavior
-    Cannot over-reserve
-    Correct error responses

---

## EPIC 6 — Temporal Activities (IMS)

### Goal

Expose inventory operations to Temporal workflows safely.

---

### STORY 6.1 — AllocateInventory Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:inventory, priority:core

**Responsibilities**

-    Resolve BOM if KIT
-    Allocate inventory
-    Create reservations

**Acceptance Criteria**

-    Idempotent
-    Safe under retries
-    Partial allocation handled explicitly

---

### STORY 6.2 — ReleaseInventory Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:inventory

**Responsibilities**

-    Release reservations
-    Decrement reserved quantity

**Acceptance Criteria**

-    Safe to retry
-    No negative quantities

---

### STORY 6.3 — ConsumeInventory Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:inventory, priority:learning

**Responsibilities**

-    Convert reservations to consumed inventory after pick

**Acceptance Criteria**

-    Idempotent
-    Cannot consume more than reserved

---

## EPIC 7 — Observability & Correctness

### Goal

Make inventory behavior transparent and provably correct.

---

### STORY 7.1 — Inventory audit logging

**Type:** Observability  
**Labels:** observability, domain:inventory

**Acceptance Criteria**

-    All adjustments logged
-    OrderId correlated where applicable

---

### STORY 7.2 — Concurrency & retry tests

**Type:** Test  
**Labels:** temporal, domain:inventory, type:test

**Acceptance Criteria**

-    Concurrent reservations handled safely
-    Temporal retries do not corrupt balances

---

## Recommended Execution Order

1. EPIC 1 — Service Foundation
2. EPIC 2 — Item & BOM Model
3. EPIC 3 — Inventory Balances
4. EPIC 4 — Reservations
5. EPIC 5 — APIs
6. EPIC 6 — Temporal Activities
7. EPIC 7 — Hardening

---

## Cursor Usage Notes

-    Inventory bugs are **expensive** — go slow
-    One STORY at a time
-    Favor correctness over cleverness
-    Add constraints first, code second
