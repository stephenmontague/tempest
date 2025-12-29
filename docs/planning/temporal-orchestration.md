# Temporal Orchestration — Canonical Design

This document defines the **authoritative Temporal orchestration model**
for the Warehouse Management System MVP.

Temporal is the **durable orchestrator of business intent**.
Domain services (OMS, IMS, WMS, SMS) remain systems of record.

---

## Core Principles

1. **Temporal owns intent, not data**

     - Workflows express _what must happen_
     - Services own _what exists_

2. **Every Activity is idempotent**

     - Activities may run multiple times
     - Duplicate side effects must be impossible

3. **Workflows assume services can disappear**

     - All calls are retried
     - Timeouts and failure states are explicit

4. **Workflow IDs are deterministic**
     - Idempotent workflow starts
     - No duplicate business processes

---

## Workflow Inventory

| Workflow                           | Owner | Purpose                                    |
| ---------------------------------- | ----- | ------------------------------------------ |
| `OrderIntakeWorkflow`              | OMS   | Durable order submission & validation      |
| `WaveExecutionWorkflow`            | WMS   | Batch fulfillment of orders in a wave      |
| `OrderFulfillmentWorkflow`         | OMS   | (Legacy) Single-order fulfillment          |
| `ReplenishmentWorkflow` (optional) | IMS   | Inventory restock automation               |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ORDER FLOW                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Customer Order                                                            │
│        │                                                                    │
│        ▼                                                                    │
│   ┌─────────────────────┐                                                   │
│   │ OrderIntakeWorkflow │  (OMS)                                            │
│   │  - Validate         │                                                   │
│   │  - Create Order     │                                                   │
│   │  - Mark AWAITING_   │                                                   │
│   │    WAVE             │                                                   │
│   └─────────┬───────────┘                                                   │
│             │                                                               │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │  Orders Queue       │  Status: AWAITING_WAVE                            │
│   │  (OMS Database)     │                                                   │
│   └─────────┬───────────┘                                                   │
│             │                                                               │
│             │  Warehouse Manager creates wave                               │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │  WaveService        │  (WMS - Spring @Service)                          │
│   │  - createWave()     │  CRUD only, no workflow                           │
│   │  - releaseWave()    │  Starts WaveExecutionWorkflow                     │
│   └─────────┬───────────┘                                                   │
│             │                                                               │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │WaveExecutionWorkflow│  (WMS)                                            │
│   │  - Allocate all     │                                                   │
│   │  - Create picks     │                                                   │
│   │  - Wait for picks   │◄─── Signal: allPicksCompleted                     │
│   │  - Consume inv      │                                                   │
│   │  - Wait for packs   │◄─── Signal: allPacksCompleted                     │
│   │  - Ship all orders  │                                                   │
│   └─────────────────────┘                                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Workflow 1 — OrderIntakeWorkflow

### Purpose

Ensure an order submission is **never lost**, even if OMS is down.

This workflow represents **durable intent to create an order**.

Orders complete intake in `AWAITING_WAVE` status, ready for wave planning.

---

### Workflow Identity

-    **Workflow ID:** `order-intake-{requestId}`
-    **Task Queue:** `oms-tasks`
-    **Start Condition:** API/BFF receives order submission
-    **Retry Policy:** Infinite retry (bounded by workflow timeout)

---

### Inputs

-    `requestId` (idempotency key)
-    `OrderIntakeRequest` (validated DTO with facilityId)

---

### Workflow Steps

1. **Validate Order**

     - Activity: `ValidateOrder`
     - Owner: OMS
     - Failure: non-retryable if structurally invalid

2. **Create Order**

     - Activity: `CreateOrder`
     - Owner: OMS
     - Retry: yes (OMS may be down)

3. **Mark Order Awaiting Wave**

     - Activity: `MarkOrderAwaitingWave`
     - Owner: OMS
     - Sets order status to `AWAITING_WAVE`

4. **Complete**
     - Order intake intent satisfied
     - Order is now eligible for wave planning

---

### Activities Used

| Activity              | Service |
| --------------------- | ------- |
| ValidateOrder         | OMS     |
| CreateOrder           | OMS     |
| MarkOrderAwaitingWave | OMS     |

---

### Failure Modes

| Scenario            | Behavior                   |
| ------------------- | -------------------------- |
| OMS down            | Workflow retries           |
| Validation fails    | Workflow fails permanently |
| Duplicate requestId | Existing workflow reused   |

---

### Queries

-    `getStatus()` → RECEIVED / VALIDATING / VALIDATED / CREATING / CREATED / AWAITING_WAVE

---

## Workflow 2 — WaveExecutionWorkflow (Primary Fulfillment)

### Purpose

Coordinate **batch fulfillment of orders in a wave** through inventory allocation,
warehouse execution, and shipping.

This is the **primary fulfillment workflow** for the wave-driven architecture.

---

### Workflow Identity

-    **Workflow ID:** `wave-execution-{waveId}`
-    **Task Queue:** `wms-tasks`
-    **Start Condition:** Warehouse manager releases a wave
-    **Timeout:** Long-running (hours/days)

---

### Inputs

-    `waveId`
-    `facilityId`
-    `waveNumber`
-    `orders` (list of WaveOrderDto with order lines and ship-to addresses)

---

### Workflow Steps (Happy Path)

1. **Allocate Inventory (all orders)**

     - Activity: `AllocateInventory` (per order line)
     - Owner: IMS
     - Handles BOM expansion if needed

2. **Mark Orders Reserved**

     - Activity: `MarkOrderReserved` (per order)
     - Owner: OMS

3. **Create Pick Tasks**

     - Activity: `CreatePickWave` (per order)
     - Owner: WMS

4. **Wait for All Picks**

     - Signal: `allPicksCompleted`
     - Sent by: WMS (via API when pickers finish)

5. **Consume Inventory (all orders)**

     - Activity: `ConsumeInventory` (per order line)
     - Owner: IMS

6. **Wait for All Packs**

     - Signal: `allPacksCompleted`
     - Sent by: WMS (via API when packers finish)

7. **Create Shipments (all orders)**

     - Activity: `CreateShipment` (per order)
     - Owner: SMS

8. **Generate Shipping Labels**

     - Activity: `GenerateShippingLabel` (per order)
     - Owner: SMS

9. **Confirm Shipments**

     - Activity: `ConfirmShipment` (per order)
     - Owner: SMS

10. **Mark Orders Shipped**
     - Activity: `MarkOrderShipped` (per order)
     - Owner: OMS

---

### Activities Used

| Activity              | Service | Task Queue  |
| --------------------- | ------- | ----------- |
| AllocateInventory     | IMS     | ims-tasks   |
| ConsumeInventory      | IMS     | ims-tasks   |
| ReleaseInventory      | IMS     | ims-tasks   |
| MarkOrderReserved     | OMS     | oms-tasks   |
| MarkOrderShipped      | OMS     | oms-tasks   |
| CreatePickWave        | WMS     | wms-tasks   |
| CreateShipment        | SMS     | sms-tasks   |
| GenerateShippingLabel | SMS     | sms-tasks   |
| ConfirmShipment       | SMS     | sms-tasks   |

---

### Signals

| Signal             | Sent By     | Meaning                        |
| ------------------ | ----------- | ------------------------------ |
| allPicksCompleted  | WMS         | All pick tasks in wave done    |
| allPacksCompleted  | WMS         | All packing in wave complete   |
| orderPickCompleted | WMS         | Single order's picks done      |
| orderPackCompleted | WMS         | Single order's packing done    |
| cancelWave         | WMS / Admin | Abort wave, release inventory  |

---

### Cancellation & Compensation

| Scenario                   | Action                              |
| -------------------------- | ----------------------------------- |
| Inventory allocation fails | Mark order FAILED, continue others  |
| Pick never completes       | Timeout → release inventory         |
| Shipment fails             | Mark order FAILED, continue others  |
| Wave cancelled             | Release all inventory               |

---

### Queries

-    `getWaveStatus()` → Full status with per-order tracking
-    `getCurrentStep()`
-    `getBlockingReason()`

---

## Workflow 3 — OrderFulfillmentWorkflow (Legacy/Express)

### Purpose

Single-order fulfillment workflow. Retained for:
- Express/priority orders that bypass wave planning
- Backward compatibility during migration

**Note:** For standard fulfillment, use `WaveExecutionWorkflow` instead.

---

### Workflow Identity

-    **Workflow ID:** `order-fulfillment-{orderId}`
-    **Task Queue:** `oms-tasks`
-    **Start Condition:** Explicit start for express orders
-    **Timeout:** Long-running (hours/days)

---

## Wave Planning (Not a Workflow)

Wave creation and planning is handled by `WaveService` in WMS.
This is **not a Temporal workflow** because it's simple CRUD.

### WaveService Operations

| Method         | Type | Description                                |
| -------------- | ---- | ------------------------------------------ |
| createWave()   | CRUD | Create wave with order IDs                 |
| releaseWave()  | CRUD + Workflow | Starts WaveExecutionWorkflow    |
| cancelWave()   | CRUD + Signal | Signals workflow cancellation     |
| getWave()      | CRUD | Get wave details                           |

### Wave States

```
CREATED → RELEASED → IN_PROGRESS → COMPLETED
                 ↓
            CANCELLED
```

---

## Order Status Flow

```
CREATED → AWAITING_WAVE → RESERVED → PICKING → PICKED → PACKING → SHIPPED
                    ↓                                        ↓
               CANCELLED                                  FAILED
```

---

## Retry & Timeout Philosophy

### Activities

-    Retries: enabled
-    Backoff: exponential
-    Non-retryable errors:
     -    validation errors
     -    illegal state transitions

### Workflows

-    Prefer **waiting** over failing
-    Fail only when intent is invalid or explicitly cancelled
-    Per-order failures don't fail the wave

---

## Idempotency Strategy

| Layer          | Strategy                 |
| -------------- | ------------------------ |
| Workflow start | Deterministic workflowId |
| Activities     | DB uniqueness + guards   |
| Signals        | Idempotent handlers      |
| APIs           | Idempotency keys         |

---

## Ownership Boundaries (Critical)

| Concern             | Owner    |
| ------------------- | -------- |
| Order lifecycle     | OMS      |
| Inventory truth     | IMS      |
| Warehouse execution | WMS      |
| Wave orchestration  | WMS      |
| Shipment lifecycle  | SMS      |
| Orchestration       | Temporal |

---

## What This MVP Does NOT Do (Yet)

-    Partial fulfillment
-    Split shipments
-    Multi-warehouse routing
-    Carrier selection optimization
-    Event streaming (Kafka)
-    Advanced wave optimization algorithms

These are intentional omissions.

---

## API Endpoints (WMS)

| Endpoint                         | Method | Description                    |
| -------------------------------- | ------ | ------------------------------ |
| /api/waves                       | POST   | Create a new wave              |
| /api/waves/{id}                  | GET    | Get wave details               |
| /api/waves/{id}/release          | POST   | Release wave (start workflow)  |
| /api/waves/{id}                  | DELETE | Cancel wave                    |
| /api/waves/{id}/picks-completed  | POST   | Signal all picks done          |
| /api/waves/{id}/packs-completed  | POST   | Signal all packs done          |

---

## Recommended Implementation Order

1. Implement **IMS Activities**
2. Implement **OMS Activities** (including MarkOrderAwaitingWave)
3. Implement **WMS Activities**
4. Implement **SMS Activities**
5. Implement `OrderIntakeWorkflow` (stops at AWAITING_WAVE)
6. Implement `WaveExecutionWorkflow`
7. Implement `WaveService` and `WaveController`
8. Add signals and queries
9. Harden retries and failure paths

---

## Cursor Usage Notes

-    Keep this file open while coding workflows
-    Never improvise orchestration logic
-    If unsure, update this document first
-    Treat it as executable architecture
