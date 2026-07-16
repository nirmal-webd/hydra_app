# Hydra Reminder Engine Architecture

This document outlines the background services, receivers, and the core event-driven finite state machine (FSM) that powers Hydra's hydration reminders.

## Overview

The reminder engine ensures that users get notified at the exact right moment, without being spammed. It operates entirely in the background, utilizing Android's `ForegroundService`, `AlarmManager`, and `UsageStatsManager`.

## Core Components

### 1. `ReminderForegroundService`
- **Responsibility:** A sticky foreground service that keeps the tracking engine alive.
- **Key Functions:**
  - Instantiates and starts the `UsageMonitor`.
  - Registers the `UnlockReceiver` dynamically (so it only fires when the service is active).
  - Tells `ReminderStateManager` to restore any timers if the service was restarted by the system after process death.

### 2. `ReminderStateManager`
- **Responsibility:** The orchestrator and single source of truth for the entire engine.
- **Key Functions:**
  - All external events flow into its `dispatch()` function.
  - Utilizes a `Mutex` to serialize incoming events to ensure thread safety.
  - Queries `PreferencesManager` and `DateUtils` to compute the `inQuietHours` context.
  - Passes the current state, metadata, event, unlocked status, and quiet hours status into the `ReminderReducer` (the pure FSM).
  - Executes the side effects produced by the Reducer (e.g. `ShowNotification`, `LogWater`, `StartCooldownTimer`).
  - Persists the new state and metadata to DataStore.

### 3. `ReminderReducer`
- **Responsibility:** A pure function FSM (Finite State Machine) that computes transitions.
- **Key Functions:**
  - **Inputs:** `state`, `metadata`, `event`, `isDeviceUnlocked`, `inQuietHours`, `cooldownDurationMs`.
  - **Outputs:** `TransitionResult` (which contains `newState`, `newMetadata`, and `effects`).
  - **States:** `COOLDOWN`, `PENDING`, `SHOWING`, `SNOOZED`.
  - **Quiet Hours Logic:** Prevents transitions to `SHOWING` if `inQuietHours` is true, opting to transition to `PENDING` instead, which silently waits for the next valid phone unlock after quiet hours end.

### 4. `UsageMonitor`
- **Responsibility:** Tracks foreground app usage to trigger App Usage Reminders.
- **Key Functions:**
  - Runs a coroutine loop that checks `UsageStatsManager` every 60 seconds.
  - Reads `appRemindersEnabled`, `monitoredApps`, and `appDurationMinutes` from preferences.
  - Tracks consecutive minutes a monitored app (e.g. Instagram) is in the foreground.
  - When the threshold is met, it dispatches a `ReminderEvent.AppUsageDetected(packageName)` to the FSM and resets its counter.

### 5. `ReminderAlarmReceiver`
- **Responsibility:** Wakes up the app precisely when timer alarms go off.
- **Key Functions:**
  - Registered in `AndroidManifest.xml`.
  - Listens for `ACTION_COOLDOWN_EXPIRED`, `ACTION_SNOOZE_EXPIRED`, and `ACTION_DAY_RESET`.
  - Dispatches the corresponding FSM event.

### 6. `NotificationActionReceiver`
- **Responsibility:** Bridges the gap between the Android Notification Tray buttons and the FSM.
- **Key Functions:**
  - Registered in `AndroidManifest.xml`.
  - Maps notification actions (like "Drank Water", "10 min snooze", "Not Now") to `ReminderAccepted`, `ReminderSnoozed`, and `ReminderDismissed` FSM events.

### 7. `UnlockReceiver`
- **Responsibility:** Listens for `Intent.ACTION_USER_PRESENT` broadcasts.
- **Key Functions:**
  - Dynamically registered by the foreground service.
  - Dispatches `ReminderEvent.PhoneUnlocked` whenever the user successfully unlocks their device. This is crucial for resolving `PENDING` reminders.

## Recent Changes & Bug Fixes

1. **UsageStats Implementation (Step 8):** Added the `UsageMonitor` class and wired it to `ReminderForegroundService` to satisfy the MVP requirement for App Usage Reminders.
2. **Missing FSM Effect:** Fixed a bug where logging water manually failed to save to the Room database. The `WaterLogged` global transition in the `ReminderReducer` was missing the `LogWater` side effect.
3. **Quiet Hours Suppression:** Completed the quiet hours logic. Added `inQuietHours` computation to `ReminderStateManager.dispatch()` and updated `ReminderReducer` to properly suppress alarms and phone unlock events if they occur during user-defined quiet hours. Suppressed reminders are transitioned to `PENDING` so they appear on the very next unlock after quiet hours expire.
4. **Resilience & Process Death (BootReceiver):** Implemented `BootReceiver` inside `AndroidManifest.xml` to respond to `ACTION_BOOT_COMPLETED` and `ACTION_MY_PACKAGE_REPLACED`. This ensures `ReminderForegroundService` automatically revives the engine after the phone restarts.
5. **Eternal Goal Suppression Fix (Midnight Reset):** The FSM engine requires `ReminderEvent.DayReset` to reset the `dailyWaterConsumed` to `0` at midnight. This was silently failing because `ACTION_DAY_RESET` was never scheduled. Added `scheduleMidnightReset()` in `ReminderStateManager` to create a self-perpetuating daily midnight alarm, guaranteeing users receive reminders the day after they hit their goal.
6. **Dynamic Reminder Context (App Package Persistence):** When transitioning to `SHOWING` due to an App Usage trigger, the specific app package (e.g. Instagram) is stored in `ReminderMetadata`. Fixed a silent failure in `ReminderStateStore` where `APP_PACKAGE` was omitted from DataStore preferences. If the app died while `PENDING`, the contextual string would be lost upon restoration, falling back to a generic reminder. Context is now fully serialized across reboots.
