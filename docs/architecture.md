# Hydra Architecture

> **CRITICAL DIRECTIVE:** Read this document completely before proposing any architectural or structural changes. Do not attempt to standardize or refactor components without understanding the context-aware execution flow documented here.

## 📁 Folder Structure

```text
com/hydra/app/
├── HydraApp.kt                              ← DI container + Engine bootstrap
├── MainActivity.kt                           ← Permission handling, service start
├── navigation/
│   └── HydraNavGraph.kt                      ← Compose Navigation
├── model/
│   ├── WaterLog.kt, ReminderLog.kt           ← Database Entities
│   └── ReminderState.kt, ReminderEvent.kt    ← FSM definitions (State, Events, Effects)
├── data/
│   ├── room/                                 ← Room Database + DAOs
│   ├── datastore/                            ← Preferences + FSM State persistence
│   └── repository/                           ← Repository pattern for DB/DataStore
├── service/
│   ├── ReminderReducer.kt                   ← Pure FSM reducer (Core Business Logic)
│   ├── ReminderStateManager.kt              ← Stateful Orchestrator (Alarms, Notifications)
│   ├── ReminderForegroundService.kt         ← Long-running host for the Orchestrator
│   └── *Receiver.kt                         ← BroadcastReceivers (Alarm, Unlock, Notification)
├── viewmodel/
│   └── Dashboard, History, Settings ViewModels
├── ui/
│   ├── screens/                             ← Compose Screens
│   └── theme/                               ← Material 3 Theme
└── utils/
    └── NotificationHelper.kt                ← Notification channels and builders
```

## 🏗 Major Components & Data Flow

### The FSM (Finite State Machine) Engine
The core of the app is not a standard timer, but a **Pure Functional FSM**. 
- **`ReminderReducer`**: A pure function `(State, Event) -> Pair<State, Set<Effect>>`. It takes the current state and an event (e.g., `TimerExpired`, `PhoneUnlocked`, `WaterLogged`), and returns the *new* state plus a set of side effects to perform (e.g., `ScheduleAlarm`, `ShowNotification`).
- **`ReminderStateManager`**: The stateful container that holds the current FSM state in memory (and persists it to DataStore). It receives events, passes them to the reducer, updates its internal state, and actually executes the resulting effects (interacting with Android system APIs).

### Context-Aware Triggers
- **`UnlockReceiver`**: Listens for `Intent.ACTION_USER_PRESENT` (Device Unlocked). This sends a `PhoneUnlocked` event to the FSM. If the state is `PENDING` (waiting for the user to be active), the FSM transitions to `SHOWING` and issues a `ShowNotification` effect.

### Data Flow Diagram
```text
Sensor (Unlock/Alarm) → Context Analyzer (StateManager) → FSM (Reducer) → Scheduler (AlarmManager) → Notification
```

### Background Execution
- **Foreground Service**: `ReminderForegroundService` keeps the app alive and immune to most Doze mode restrictions, ensuring the `ReminderStateManager` coroutine scope stays active.
- **AlarmManager**: Used for exact timing (e.g., Cooldowns, Auto-Snoozes) via `ReminderAlarmReceiver`.
- **Ticker Failsafe**: A `while(true)` coroutine loop in `ReminderStateManager` checks every 60 seconds to see if any timers have expired, acting as a backup in case the Android system drops an AlarmManager broadcast.

## 🗄 Database Schema (Room)

1. **`water_log`**: Records actual hydration events.
   - `id` (Long, PK)
   - `amount_ml` (Int)
   - `timestamp` (Long)
   - `source` (String: MANUAL, NOTIFICATION_QUICK, NOTIFICATION_CUSTOM)

2. **`reminder_log`**: Analytics/telemetry for the FSM to track user behavior.
   - `id` (Long, PK)
   - `timestamp` (Long)
   - `type` (String)
   - `action` (String: SNOOZE, NOT_NOW, WATER_LOGGED)
   - `reason` (String: UNLOCK, ALARM, TICKER)

## 📦 Third-Party Libraries
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: AndroidX ViewModels, Kotlin Coroutines, StateFlow
- **Storage**: Room (SQLite), DataStore (Preferences)

## 🧠 Why Certain Patterns Are Used
- **Pure Reducer**: Hydration logic became too complex for scattered boolean flags (e.g., "is it quiet hours?", "did they snooze?", "is the screen off?"). The Redux-style pattern allows exhaustive testing of all transitions without mocking Android classes.
- **MVI (Model-View-Intent)**: The UI observes `StateFlows` from ViewModels and sends user actions as explicit functions, matching the declarative nature of Compose.
