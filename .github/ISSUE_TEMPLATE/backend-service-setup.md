---
name: Backend Service Setup
about: Bootstrap a Spring Boot backend service
title: "[SETUP] <service-name> bootstrap"
labels: ["type:setup", "domain:platform"]
---

## Description

Bootstrap the `<service-name>` Spring Boot service with the standard platform stack.

## Scope

-    Spring Boot 3
-    Java 21
-    Postgres + Flyway
-    Security baseline
-    Actuator + Validation
-    Temporal worker support (if applicable)

## Acceptance Criteria

-    [ ] Service starts locally
-    [ ] Flyway migrations run
-    [ ] Health endpoint available
-    [ ] Docker/local profile works

## Notes

Add any service-specific constraints or decisions here.
