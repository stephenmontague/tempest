# Warehouse Management System (WMS) — Issue Breakdown

This document is the canonical planning reference for the **Warehouse Management System**.
It is designed to be:

-    Cursor-friendly
-    GitHub-issue-compatible
-    Incremental and learning-focused

Milestone: Phase 3 – Warehouse Management Service

---

## EPIC 1 — WMS Service Foundation

### Goal

Bootstrap a Spring Boot service capable of hosting Temporal workers and warehouse execution APIs.

---

### STORY 1.1 — Bootstrap WMS Spring Boot service

**Type:** Setup  
**Labels:** domain:warehouse, type:setup, priority:core

**Description**  
Create the Warehouse Management System (WMS) Spring Boot service using the standard platform stack.

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
-    Temporal worker connects to Temporal (local or cloud)

---

### STORY 1.2 — Configuration & environment profiles

**Type:** Setup  
**Labels:** domain:warehouse, type:setup

**Description**  
Add environment-based configuration:

-    `local`
-    `dev`
-    `prod` (stub)

**Acceptance Criteria**

-    Profile selected via `SPRING_PROFILES_ACTIVE`
-    No secrets committed
-    Local profile works out of the box

---

## EPIC 2 — Warehouse & Location Domain Model

### Goal

Model physical warehouse/store structures to support execution workflows.

---

### STORY 2.1 — Facility entity

**Type:** Domain  
**Labels:** domain:warehouse, database

**Fields**

-    id
-    name
-    type (WAREHOUSE | STORE)
-    timezone
-    active

**Acceptance Criteria**

-    JPA entity implemented
-    Flyway migration created
-    Repository CRUD works

---

### STORY 2.2 — Location entity

**Type:** Domain  
**Labels:** domain:warehouse, database

**Fields**

-    id
-    facilityId
-    code
-    type (PICK_FACE | BULK | STAGING | BACKROOM)
-    active

**Acceptance Criteria**

-    Locations belong to a Facility
-    Unique `(facilityId, code)`
-    Type enforced

---

## EPIC 3 — Picking & Wave Execution Model

### Goal

Represent warehouse execution work units (waves and picks).

---

### STORY 3.1 — Wave entity

**Type:** Domain  
**Labels:** domain:warehouse, database

**Fields**

-    id
-    facilityId
-    strategy (SINGLE_ORDER | BATCH)
-    status (CREATED | IN_PROGRESS | COMPLETE)

**Acceptance Criteria**

-    Status transitions enforced
-    Persisted lifecycle

---

### STORY 3.2 — PickTask entity

**Type:** Domain  
**Labels:** domain:warehouse, database

**Fields**

-    id
-    waveId
-    orderId
-    locationId
-    sku
-    quantity
-    status (OPEN | PICKED | CANCELLED)

**Acceptance Criteria**

-    Linked to Wave
-    Idempotent completion
-    Partial picks supported

---

## EPIC 4 — Temporal Activities (WMS)

### Goal

Expose warehouse execution logic as Temporal Activities.

---

### STORY 4.1 — CreatePickWave Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:warehouse, priority:core

**Responsibilities**

-    Validate facility
-    Create wave
-    Create pick tasks

**Acceptance Criteria**

-    Activity idempotent
-    No duplicate waves on retry
-    Returns waveId

---

### STORY 4.2 — CompletePickTask Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:warehouse

**Responsibilities**

-    Mark PickTask as PICKED

**Acceptance Criteria**

-    Safe to retry
-    Invalid transitions rejected
-    Execution event recorded

---

## EPIC 5 — Packing & Staging

### Goal

Model post-picking execution steps.

---

### STORY 5.1 — PackCarton entity

**Type:** Domain  
**Labels:** domain:warehouse, database

**Acceptance Criteria**

-    Linked to orderId
-    Supports multiple cartons per order

---

### STORY 5.2 — StagingUnit entity

**Type:** Domain  
**Labels:** domain:warehouse, database

**Acceptance Criteria**

-    Represents staged shipment
-    Status reflects readiness to ship

---

## EPIC 6 — Temporal Signals & Workflow Integration

### Goal

Allow workflows to wait on warehouse execution events.

---

### STORY 6.1 — Pick completion signal

**Type:** Temporal Signal  
**Labels:** temporal, domain:warehouse, priority:core

**Acceptance Criteria**

-    Signal unblocks waiting workflow
-    Duplicate signals ignored safely

---

### STORY 6.2 — Pack completion signal

**Type:** Temporal Signal  
**Labels:** temporal, domain:warehouse

**Acceptance Criteria**

-    Validates packing state
-    Unblocks workflow

---

## EPIC 7 — Observability & Correctness

### Goal

Make execution debuggable and safe under retries.

---

### STORY 7.1 — Correlated execution logging

**Type:** Observability  
**Labels:** observability, domain:warehouse

**Acceptance Criteria**

-    Logs include orderId, waveId, workflowId
-    Easy end-to-end traceability

---

### STORY 7.2 — Activity retry behavior tests

**Type:** Test  
**Labels:** temporal, domain:warehouse, type:test

**Acceptance Criteria**

-    Activities retry safely
-    No duplicate side effects
-    System remains consistent

---

## Recommended Execution Order

1. EPIC 1 — Service Foundation
2. EPIC 2 — Physical Model
3. EPIC 3 — Execution Model
4. EPIC 4 — Temporal Activities
5. EPIC 6 — Signals
6. EPIC 5 — Packing
7. EPIC 7 — Hardening

---

## Cursor Usage Tips

-    Open **one STORY at a time** in Cursor
-    Paste only that story into the prompt
-    Do not include other epics or future work
-    Commit after every completed story
