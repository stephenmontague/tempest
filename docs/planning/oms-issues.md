# Order Management System (OMS) — Issue Breakdown

This document is the canonical planning reference for the **Order Management System**.
It defines epics and stories to build OMS incrementally with strong correctness,
idempotency, and Temporal integration.

Milestone: Phase 2 – Order Management Service

---

## EPIC 1 — OMS Service Foundation

### Goal

Bootstrap a Spring Boot service that acts as the **system of record for orders**
and integrates cleanly with Temporal workflows.

---

### STORY 1.1 — Bootstrap OMS Spring Boot service

**Type:** Setup  
**Labels:** domain:orders, type:setup, priority:core

**Description**  
Create the Order Management System (OMS) Spring Boot service using the standard platform stack.

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

### STORY 1.2 — OMS configuration & environment profiles

**Type:** Setup  
**Labels:** domain:orders, type:setup

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

## EPIC 2 — Order Domain Model

### Goal

Define a **durable, auditable order model** with explicit lifecycle state.

---

### STORY 2.1 — Order entity

**Type:** Domain  
**Labels:** domain:orders, database, priority:core

**Fields**

-    id
-    externalOrderId
-    channel (MANUAL | SHOPIFY | OTHER)
-    status
-    priority
-    shipTo (embedded or reference)
-    createdAt
-    updatedAt

**Acceptance Criteria**

-    JPA entity implemented
-    Flyway migration created
-    Status enum defined and enforced

---

### STORY 2.2 — OrderLine entity

**Type:** Domain  
**Labels:** domain:orders, database

**Fields**

-    id
-    orderId
-    sku
-    quantity
-    unitPrice
-    status

**Acceptance Criteria**

-    Lines belong to an Order
-    Quantity validation enforced
-    Lines persisted with order

---

### STORY 2.3 — OrderEvent entity (append-only)

**Type:** Domain  
**Labels:** domain:orders, database, priority:learning

**Description**  
Record all meaningful order lifecycle transitions as immutable events.

**Fields**

-    id
-    orderId
-    type
-    payload (JSON)
-    createdAt

**Acceptance Criteria**

-    Events are append-only
-    Order state transitions emit events
-    Events queryable by orderId

---

## EPIC 3 — Order Intake & Idempotency

### Goal

Accept orders safely and **never duplicate them**, even under retries or failures.

---

### STORY 3.1 — Order intake request model

**Type:** API  
**Labels:** domain:orders, api

**Description**
Define DTOs for inbound order creation.

**Acceptance Criteria**

-    Validation annotations applied
-    Clear separation from JPA entities

---

### STORY 3.2 — Create order API (idempotent)

**Type:** API  
**Labels:** domain:orders, api, priority:core

**Description**
Implement `POST /orders` to create a new order.

**Requirements**

-    Idempotency via externalOrderId or requestId
-    Duplicate submissions return existing order

**Acceptance Criteria**

-    Order created exactly once
-    Duplicate submissions do not create duplicates
-    OrderEvent emitted

---

## EPIC 4 — Order Lifecycle Management

### Goal

Explicitly manage order state transitions.

---

### STORY 4.1 — Order status state machine

**Type:** Domain  
**Labels:** domain:orders, priority:core

**States**

-    CREATED
-    VALIDATED
-    RESERVED
-    FULFILLING
-    SHIPPED
-    CANCELLED
-    FAILED

**Acceptance Criteria**

-    Illegal transitions rejected
-    Transitions emit OrderEvents

---

### STORY 4.2 — Order query APIs

**Type:** API  
**Labels:** domain:orders, api

**Endpoints**

-    `GET /orders/{id}`
-    `GET /orders?status=...`

**Acceptance Criteria**

-    Returns current order state
-    Includes order lines
-    Fast enough for UI usage

---

## EPIC 5 — Temporal Activities (OMS)

### Goal

Expose order operations to Temporal workflows safely.

---

### STORY 5.1 — ValidateOrder Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:orders, priority:core

**Responsibilities**

-    Validate order integrity
-    Confirm order is in valid state for fulfillment

**Acceptance Criteria**

-    Idempotent
-    Safe to retry
-    Throws non-retryable error for invalid orders

---

### STORY 5.2 — MarkOrderReserved Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:orders

**Responsibilities**

-    Transition order to RESERVED

**Acceptance Criteria**

-    Safe to retry
-    No duplicate events emitted

---

### STORY 5.3 — MarkOrderShipped Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:orders

**Responsibilities**

-    Transition order to SHIPPED
-    Emit shipment event

**Acceptance Criteria**

-    Idempotent
-    Final state enforced

---

### STORY 5.4 — RecordOrderFailure Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:orders, priority:learning

**Responsibilities**

-    Record terminal failure
-    Persist failure reason

**Acceptance Criteria**

-    Safe under retries
-    Does not overwrite SHIPPED orders

---

## EPIC 6 — Temporal Workflow Integration

### Goal

Allow OMS to **initiate and participate in workflows**.

---

### STORY 6.1 — OrderIntakeWorkflow start integration

**Type:** Temporal  
**Labels:** temporal, domain:orders, priority:core

**Description**
Start `OrderIntakeWorkflow` after successful order intake.

**Acceptance Criteria**

-    Workflow started exactly once
-    WorkflowId deterministic
-    Safe under retries

---

### STORY 6.2 — Workflow query support

**Type:** Temporal  
**Labels:** temporal, domain:orders

**Acceptance Criteria**

-    OMS exposes workflow status via API
-    Query uses Temporal workflow queries

---

## EPIC 7 — Observability & Correctness

### Goal

Make order processing debuggable and safe.

---

### STORY 7.1 — Correlated logging

**Type:** Observability  
**Labels:** observability, domain:orders

**Acceptance Criteria**

-    Logs include orderId and workflowId
-    Easy to trace lifecycle

---

### STORY 7.2 — Retry & failure testing

**Type:** Test  
**Labels:** temporal, domain:orders, type:test

**Acceptance Criteria**

-    Activities retry without corruption
-    Failure paths verified

---

## Recommended Execution Order

1. EPIC 1 — Service Foundation
2. EPIC 2 — Domain Model
3. EPIC 3 — Order Intake
4. EPIC 4 — Lifecycle
5. EPIC 5 — Temporal Activities
6. EPIC 6 — Workflow Integration
7. EPIC 7 — Hardening

---

## Cursor Usage Notes

-    Work **one STORY at a time**
-    Paste only the STORY into Cursor
-    Avoid combining domain + Temporal work in one story
-    Commit frequently
