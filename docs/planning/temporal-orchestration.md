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

| Workflow                           | Purpose                               |
| ---------------------------------- | ------------------------------------- |
| `OrderIntakeWorkflow`              | Durable order submission & validation |
| `OrderFulfillmentWorkflow`         | End-to-end fulfillment orchestration  |
| `ReplenishmentWorkflow` (optional) | Inventory restock automation          |

---

## Workflow 1 — OrderIntakeWorkflow

### Purpose

Ensure an order submission is **never lost**, even if OMS is down.

This workflow represents **durable intent to create an order**.

---

### Workflow Identity

-    **Workflow ID:** `order-intake-{requestId}`
-    **Start Condition:** API/BFF receives order submission
-    **Retry Policy:** Infinite retry (bounded by workflow timeout)

---

### Inputs

-    `requestId` (idempotency key)
-    `OrderIntakeRequest` (validated DTO)

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

3. **Emit Order Created Event**

     - Activity: `EmitOrderEvent`
     - Owner: OMS

4. **Start Fulfillment Workflow**

     - Child Workflow: `OrderFulfillmentWorkflow`
     - Workflow ID: `order-fulfillment-{orderId}`

5. **Complete**
     - Order intake intent satisfied

---

### Activities Used

| Activity       | Service |
| -------------- | ------- |
| ValidateOrder  | OMS     |
| CreateOrder    | OMS     |
| EmitOrderEvent | OMS     |

---

### Failure Modes

| Scenario            | Behavior                   |
| ------------------- | -------------------------- |
| OMS down            | Workflow retries           |
| Validation fails    | Workflow fails permanently |
| Duplicate requestId | Existing workflow reused   |

---

### Queries

-    `getStatus()` → RECEIVED / VALIDATED / CREATED / FAILED

---

## Workflow 2 — OrderFulfillmentWorkflow

### Purpose

Coordinate **inventory allocation, warehouse execution, and shipping**
into a single durable business process.

---

### Workflow Identity

-    **Workflow ID:** `order-fulfillment-{orderId}`
-    **Start Condition:** Order created successfully
-    **Timeout:** Long-running (hours/days)

---

### Inputs

-    `orderId`

---

### Workflow Steps (Happy Path)

1. **Allocate Inventory**

     - Activity: `AllocateInventory`
     - Owner: IMS
     - Handles BOM expansion if needed

2. **Mark Order Reserved**

     - Activity: `MarkOrderReserved`
     - Owner: OMS

3. **Create Pick Wave**

     - Activity: `CreatePickWave`
     - Owner: WMS

4. **Wait for Pick Completion**

     - Signal: `PickCompleted`
     - Owner: WMS

5. **Consume Inventory**

     - Activity: `ConsumeInventory`
     - Owner: IMS

6. **Pack Order**

     - Signal: `PackCompleted`
     - Owner: WMS

7. **Create Shipment**

     - Activity: `CreateShipment`
     - Owner: SMS

8. **Generate Shipping Label**

     - Activity: `GenerateShippingLabel`
     - Owner: SMS

9. **Confirm Shipment**

     - Activity: `ConfirmShipment`
     - Owner: SMS

10. **Mark Order Shipped**
     - Activity: `MarkOrderShipped`
     - Owner: OMS

---

### Activities Used

| Activity              | Service |
| --------------------- | ------- |
| AllocateInventory     | IMS     |
| ConsumeInventory      | IMS     |
| MarkOrderReserved     | OMS     |
| MarkOrderShipped      | OMS     |
| CreatePickWave        | WMS     |
| CreateShipment        | SMS     |
| GenerateShippingLabel | SMS     |
| ConfirmShipment       | SMS     |

---

### Signals

| Signal        | Sent By     | Meaning             |
| ------------- | ----------- | ------------------- |
| PickCompleted | WMS         | All pick tasks done |
| PackCompleted | WMS         | Packing complete    |
| CancelOrder   | OMS / Admin | Abort fulfillment   |

---

### Cancellation & Compensation

| Scenario                   | Action                              |
| -------------------------- | ----------------------------------- |
| Inventory allocation fails | Mark order FAILED                   |
| Pick never completes       | Timeout → release inventory         |
| Shipment fails             | Retry label or fail order           |
| Order cancelled            | Release inventory + cancel shipment |

---

### Queries

-    `getFulfillmentStatus()`
-    `getCurrentStep()`
-    `getBlockingReason()`

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
| Shipment lifecycle  | SMS      |
| Orchestration       | Temporal |

---

## What This MVP Does NOT Do (Yet)

-    Partial fulfillment
-    Split shipments
-    Multi-warehouse routing
-    Carrier selection optimization
-    Event streaming (Kafka)

These are intentional omissions.

---

## Recommended Implementation Order

1. Implement **IMS Activities**
2. Implement **OMS Activities**
3. Implement **WMS Activities**
4. Implement **SMS Activities**
5. Implement `OrderIntakeWorkflow`
6. Implement `OrderFulfillmentWorkflow`
7. Add signals and queries
8. Harden retries and failure paths

---

## Cursor Usage Notes

-    Keep this file open while coding workflows
-    Never improvise orchestration logic
-    If unsure, update this document first
-    Treat it as executable architecture
