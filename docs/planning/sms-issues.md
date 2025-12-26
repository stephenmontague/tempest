# Shipping Management System (SMS) — Issue Breakdown

This document is the canonical planning reference for the **Shipping Management System**.
SMS owns the **shipment lifecycle**, **carrier abstraction**, **label generation**, and
**tracking updates**, and integrates with Temporal workflows safely and idempotently.

Milestone: Phase 4 – Shipping Management Service

---

## EPIC 1 — SMS Service Foundation

### Goal

Bootstrap a Spring Boot service responsible for shipment execution and carrier integration.

---

### STORY 1.1 — Bootstrap SMS Spring Boot service

**Type:** Setup  
**Labels:** domain:shipping, type:setup, priority:core

**Description**  
Create the Shipping Management System (SMS) Spring Boot service using the standard platform stack.

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

### STORY 1.2 — SMS configuration & environment profiles

**Type:** Setup  
**Labels:** domain:shipping, type:setup

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

## EPIC 2 — Shipment Domain Model

### Goal

Define a **durable, auditable shipment lifecycle** independent of any specific carrier.

---

### STORY 2.1 — Shipment entity

**Type:** Domain  
**Labels:** domain:shipping, database, priority:core

**Fields**

-    id
-    orderId
-    facilityId
-    carrier
-    serviceLevel
-    status (CREATED | LABELED | SHIPPED | CANCELLED | FAILED)
-    createdAt
-    updatedAt

**Acceptance Criteria**

-    JPA entity implemented
-    Flyway migration created
-    Status transitions enforced

---

### STORY 2.2 — Parcel entity

**Type:** Domain  
**Labels:** domain:shipping, database

**Description**
Represents a physical carton/package in a shipment.

**Fields**

-    id
-    shipmentId
-    weight
-    length
-    width
-    height

**Acceptance Criteria**

-    Supports multi-parcel shipments
-    Dimensions validated
-    Linked to Shipment

---

### STORY 2.3 — ShippingLabel entity

**Type:** Domain  
**Labels:** domain:shipping, database, priority:learning

**Fields**

-    id
-    shipmentId
-    carrier
-    trackingNumber
-    labelUrl (or blob reference)
-    createdAt

**Acceptance Criteria**

-    Tracking number uniqueness enforced
-    Label immutable once created

---

## EPIC 3 — Carrier Abstraction Layer

### Goal

Isolate carrier-specific logic behind a clean abstraction.

---

### STORY 3.1 — Carrier interface

**Type:** Domain  
**Labels:** domain:shipping, priority:core

**Description**
Define a common interface for carriers.

**Methods**

-    `rateShipment(...)`
-    `createLabel(...)`
-    `voidShipment(...)`

**Acceptance Criteria**

-    Interface defined
-    No carrier-specific logic leaks outside adapters

---

### STORY 3.2 — Stub carrier implementation

**Type:** Feature  
**Labels:** domain:shipping, priority:learning

**Description**
Implement a fake carrier for local development.

**Acceptance Criteria**

-    Deterministic tracking numbers
-    Fake label generation
-    Configurable latency/failure simulation

---

## EPIC 4 — Shipping APIs

### Goal

Expose shipment operations safely to other services and Temporal Activities.

---

### STORY 4.1 — Create shipment API

**Type:** API  
**Labels:** domain:shipping, api, priority:core

**Endpoints**

-    `POST /shipments`

**Acceptance Criteria**

-    Idempotent creation
-    Duplicate requests return existing shipment
-    Initial status = CREATED

---

### STORY 4.2 — Generate label API

**Type:** API  
**Labels:** domain:shipping, api

**Endpoints**

-    `POST /shipments/{id}/label`

**Acceptance Criteria**

-    Cannot generate label twice
-    Shipment transitions to LABELED

---

### STORY 4.3 — Mark shipped API

**Type:** API  
**Labels:** domain:shipping, api

**Endpoints**

-    `POST /shipments/{id}/ship`

**Acceptance Criteria**

-    Only LABELED shipments can ship
-    Status transitions enforced

---

## EPIC 5 — Temporal Activities (SMS)

### Goal

Expose shipping execution as Temporal Activities.

---

### STORY 5.1 — CreateShipment Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:shipping, priority:core

**Responsibilities**

-    Create shipment record
-    Validate order state

**Acceptance Criteria**

-    Idempotent
-    Safe under retries
-    Returns shipmentId

---

### STORY 5.2 — GenerateShippingLabel Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:shipping

**Responsibilities**

-    Call carrier adapter
-    Persist label and tracking number

**Acceptance Criteria**

-    No duplicate labels on retry
-    Handles carrier failures cleanly

---

### STORY 5.3 — ConfirmShipment Activity

**Type:** Temporal Activity  
**Labels:** temporal, domain:shipping

**Responsibilities**

-    Mark shipment as SHIPPED
-    Record ship timestamp

**Acceptance Criteria**

-    Idempotent
-    Final state enforced

---

## EPIC 6 — Tracking & External Updates

### Goal

Track shipment progress and expose status externally.

---

### STORY 6.1 — Tracking update model

**Type:** Domain  
**Labels:** domain:shipping, priority:learning

**Description**
Persist carrier tracking updates.

**Acceptance Criteria**

-    Multiple updates per shipment supported
-    Ordered by timestamp

---

### STORY 6.2 — Tracking webhook endpoint (stub)

**Type:** Integration  
**Labels:** domain:shipping, integration

**Acceptance Criteria**

-    Accepts carrier callbacks
-    Updates tracking status safely

---

## EPIC 7 — Observability & Correctness

### Goal

Make shipping execution debuggable and resilient.

---

### STORY 7.1 — Correlated shipping logs

**Type:** Observability  
**Labels:** observability, domain:shipping

**Acceptance Criteria**

-    Logs include orderId, shipmentId, workflowId
-    End-to-end traceability

---

### STORY 7.2 — Activity retry & failure tests

**Type:** Test  
**Labels:** temporal, domain:shipping, type:test

**Acceptance Criteria**

-    Carrier failures retried correctly
-    No duplicate labels or shipments
-    System remains consistent

---

## Recommended Execution Order

1. EPIC 1 — Service Foundation
2. EPIC 2 — Shipment Domain
3. EPIC 3 — Carrier Abstraction
4. EPIC 4 — APIs
5. EPIC 5 — Temporal Activities
6. EPIC 6 — Tracking
7. EPIC 7 — Hardening

---

## Cursor Usage Notes

-    Shipping failures are normal — design for retries
-    Never assume carriers are reliable
-    Make idempotency explicit
-    Prefer state machines over conditionals
