<p align="center">
  <img src="docs/assets/logo_bovedakmp.png" width="100" alt="Bóveda Logo">
</p>

<h1 align="center">Bóveda KMP</h1>

<p align="center">
  <strong>Offline-First Synchronization Engine (Proof of Concept) built with Kotlin Multiplatform.</strong>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.x-blue.svg?style=flat-square&logo=kotlin" alt="Kotlin"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose-Multiplatform-purple.svg?style=flat-square&logo=android" alt="Compose Multiplatform"></a>
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-orange.svg?style=flat-square" alt="Clean Architecture">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS-black.svg?style=flat-square" alt="Platforms">
</p>

<p align="center">
  <a href="https://github.com/JastinBolanos/Boveda-KMP/releases/download/v1.2.0/BovedaKMP.apk">
    <img src="https://img.shields.io/badge/Descargar-APK%20Android-success?style=for-the-badge&logo=android&logoColor=white" alt="Descargar APK">
  </a>
</p>

---

## Overview

**Transparency note:** While Bóveda KMP features a fintech interface, it is **not a financial application**. It is an architectural playground designed to showcase a custom **offline-first synchronization engine**. 

The UI serves purely as a shell to demonstrate how the underlying engine handles critical operations (like mock transactions) under intermittent or missing network connectivity, guaranteeing idempotency and zero data loss.

---

## App Preview (The UI Shell)

| Dashboard & Menu | Transfers & Activity | Offline Sync Flow |
| :---: | :---: | :---: |
| <img src="docs/assets/home_dark.png" width="220" alt="Dashboard"> <br> <img src="docs/assets/menu_dark.png" width="220" alt="Menu"> | <img src="docs/assets/transfer_dark.png" width="220" alt="Transfer"> <br> <img src="docs/assets/activity_dark.png" width="220" alt="Activity"> | <img src="docs/assets/receipt_pending.png" width="220" alt="Pending"> <br> <img src="docs/assets/receipt_success.png" width="220" alt="Success"> |

---

## The Synchronization Engine

* **Offline-First & Single Source of Truth:** The app uses local SQLite via `SQLDelight`. The engine ensures the UI observes database mutations directly using `StateFlow`, never binding to volatile network calls.
* **Guaranteed Idempotency:** Each operation generates a unique local UUID used as an `Idempotency-Key`. This prevents the engine from executing the same operation twice during network retries.
* **Immutable State Machine:** Records are append-only (`PENDING` → `COMPLETED` / `FAILED`); the engine never deletes or rewrites pending intents.
* **Clean Architecture + MVI:** Total separation of concerns. UI emits intents, domain use cases remain pure, and platform-specific implementations are isolated via `expect/actual`.

---

## How the Engine Handles Failures

```mermaid
sequenceDiagram
    autonumber
    participant UI as UI (Compose / MVI)
    participant Domain as Domain Use Cases
    participant DB as SQLite (SSOT)
    participant Worker as Background Sync Engine
    participant API as Remote Server

    UI->>Domain: Trigger Intent (e.g., Transfer)
    Domain->>DB: Persist Intent (UUID, Status: PENDING)
    DB-->>UI: StateFlow emission (UI shows: Pending)
    Note over UI, DB: Operation safely queued locally. No internet needed.
    Worker->>DB: Network restored -> Fetch PENDING queue
    Worker->>API: POST /sync (UUID as Idempotency-Key)
    API-->>Worker: 200 OK
    Worker->>DB: Update status to COMPLETED
    DB-->>UI: StateFlow emission (UI updates: Success)
    
```
---

## Tech Stack

* **Core & UI:** Kotlin Multiplatform (KMP)
* **Architecture:** Clean Architecture + MVVM
* **Persistence:** SQLDelight 
* **Asynchrony & Reactivity:** Kotlin Coroutines
* **Dependency Injection:** Koin
* **Version Management:** Gradle Version Catalog (`libs.versions.toml`)

---

## Installation and Execution
The project includes a Gradle Wrapper, eliminating the need for complex configurations. **Clone, sync, and run.**

```bash
git clone [https://github.com/JastinBolanos/Boveda-KMP.git](https://github.com/JastinBolanos/Boveda-KMP.git)
cd BovedaKMP

```

**For Android:**
- Open the project in Android Studio.
- Select the `androidApp` configuration.
- Press *Run*.
