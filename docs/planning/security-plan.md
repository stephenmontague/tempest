# Tempest Security Plan — MVP Baseline

This document defines the **canonical security model** for the Tempest system.
It is written as an **implementation guide**, not a theory document.

All services MUST follow this plan unless explicitly revised.

---

## 1. Core Security Principles

1. **Never build your own authentication**

     - Authentication is delegated to a real Identity Provider (IdP)
     - Services only validate tokens

2. **Trust is explicit and minimal**

     - Every request is authenticated
     - Every action is authorized
     - No implicit trust between services

3. **Identity is federated**

     - User identity comes from JWT claims
     - Company identity is represented as `tenantId`
     - Services do not store passwords or sessions

4. **Temporal runs as the system**
     - Workflows and Activities do NOT run as users
     - User identity is only metadata for audit purposes

---

## 2. Identity Provider (IdP)

### Required Capabilities

The IdP must:

-    Support OIDC / OAuth2
-    Issue JWTs signed with asymmetric keys (`RS256` or `ES256`)
-    Publish JWKS (`/.well-known/openid-configuration`)

Examples:

-    Keycloak (local-first)
-    Auth0 / Okta / Cognito (hosted)

---

### Required JWT Claims

Every access token MUST include:

| Claim              | Purpose                      |
| ------------------ | ---------------------------- |
| `iss`              | Issuer (IdP identity)        |
| `aud`              | Audience (`tempest-api`)     |
| `exp`              | Expiration                   |
| `sub`              | User ID                      |
| `tenant_id`        | Company / account identifier |
| `roles` or `scope` | Authorization                |

Example:

```json
{
     "iss": "https://idp.example.com",
     "aud": "tempest-api",
     "sub": "user-123",
     "tenant_id": "acme",
     "roles": ["WAREHOUSE_ASSOCIATE"]
}
```

---

## 3. UI (Next.js) Security Model

### Responsibilities

-    Authenticate users via IdP (OIDC)
-    Maintain user session
-    Act as Backend-for-Frontend (BFF)

### Rules

-    Browser NEVER calls backend services directly
-    Browser NEVER calls Temporal
-    Tokens NEVER stored in localStorage
-    Sensitive credentials only exist server-side

### Session Handling

-    Use HttpOnly, Secure cookies
-    Session managed by Next.js server
-    Token refresh handled server-side

---

## 4. Backend Services (OMS / IMS / WMS / SMS)

Each backend service is an **OAuth2 Resource Server**.

### Responsibilities

-    Validate JWT signature
-    Validate issuer (`iss`)
-    Validate audience (`aud`)
-    Enforce authorization (roles/scopes)
-    Enforce tenant isolation

### What Services MUST NOT Do

-    Authenticate users
-    Issue tokens
-    Store sessions
-    Trust client-supplied tenant IDs

---

## 5. JWT Validation (Spring Boot)

### Required Configuration

Each service MUST configure:

-    Issuer URI
-    JWKS validation
-    Expiration validation
-    Audience validation

### Mandatory Checks

-    Reject tokens with:

     -    invalid signature
     -    wrong issuer
     -    wrong audience
     -    expired `exp`
     -    missing `tenant_id`

---

## 6. Tenant Isolation (Multi-Tenancy)

### Tenant Model

-    `tenantId` represents a company/account
-    Derived ONLY from JWT claim
-    Stored on ALL domain records

### Rules

-    NEVER accept `tenantId` from request bodies as authoritative
-    ALL queries and writes MUST be scoped by `tenantId`
-    Uniqueness constraints are tenant-scoped

Example:

-    `UNIQUE (tenant_id, external_order_id)`

---

## 7. User Attribution & Auditing

### Actor Model

User identity is used for:

-    Audit trails
-    Event metadata
-    UI display

User identity is NOT used for:

-    Workflow ownership
-    Authorization inside workflows

### Required Fields

-    `createdByUserId`
-    `updatedByUserId`
-    Event records include actor metadata

---

## 8. Temporal Security Model

### Workflow Identity

-    Workflows run as **system identity**
-    Workflow IDs MUST be tenant-scoped

Examples:

-    `order-intake-{tenantId}-{requestId}`
-    `order-fulfillment-{tenantId}-{orderId}`

---

### Activities

-    Authenticate to services using:

     -    service credentials
     -    or internal network trust (local dev)

-    NEVER forward user JWTs into Activities

---

### Signals

-    Signals originate from:

     -    UI (via BFF)
     -    Admin APIs

-    Signal handlers MUST be idempotent
-    Signal payloads MUST be validated

---

## 9. Service-to-Service Authentication

### Allowed Patterns

-    OAuth2 Client Credentials
-    Internal service tokens
-    mTLS (advanced / optional)

### Disallowed Patterns

-    Shared static secrets
-    Passing user JWTs between services
-    Anonymous internal calls

---

## 10. Role-Based Authorization (RBAC)

### Example Roles

| Role                | Capabilities                 |
| ------------------- | ---------------------------- |
| ADMIN               | Full access                  |
| MANAGER             | Order & inventory management |
| WAREHOUSE_ASSOCIATE | Pick & pack                  |
| INTEGRATION         | API-only access              |

### Enforcement

-    Enforced at controller/service boundary
-    Never only in UI

---

## 11. API Security Rules

-    All write endpoints require authentication
-    All endpoints validate tenant scope
-    Idempotency keys required for:

     -    order creation
     -    inventory reservation
     -    shipment creation

---

## 12. Secrets Management

### Rules

-    No secrets in Git
-    No secrets in application.properties
-    Secrets via env vars or secret manager

### Ignored by Git

-    `.env*`
-    `*.pem`, `*.key`, `*.crt`
-    Local-only config files

---

## 13. Transport Security

-    HTTPS everywhere
-    No HTTP in production
-    Secure cookies only

---

## 14. Logging & Observability

### Logging Rules

-    Log authentication failures
-    Log authorization failures
-    Include correlation IDs:

     -    `tenantId`
     -    `orderId`
     -    `workflowId`

### Never Log

-    Tokens
-    Secrets
-    Passwords

---

## 15. MVP Scope (Intentional)

Included:

-    JWT-based auth
-    Tenant isolation
-    Role-based access

Excluded (for now):

-    Attribute-based access control (ABAC)
-    Row-level DB security
-    Fine-grained permissions per SKU
-    Zero-trust networking

---

## 16. Implementation Checklist (Per Service)

-    [ ] OAuth2 Resource Server configured
-    [ ] Issuer validated
-    [ ] Audience validated
-    [ ] Tenant claim enforced
-    [ ] RBAC enforced
-    [ ] Idempotency enforced
-    [ ] No secrets committed

---

## Final Rule

If a security decision is unclear:

1. Default to **deny**
2. Update this document
3. Then implement

This document is the **source of truth**.
