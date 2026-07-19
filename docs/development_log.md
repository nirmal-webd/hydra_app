# Hydra Development & Architecture Log

This document serves as the historical track record and decision log for Hydra's development. Going forward, all major architectural changes, refactors, and feature pivots (including undo operations) will be logged here with the logic and reasoning behind them.

---

## 1. How We Started
Hydra began as a smart hydration tracker built on a modern Android tech stack:
- **UI:** Jetpack Compose & Material 3
- **Architecture:** MVVM, Coroutines, Flow
- **Local Storage:** Room Database (for water logs) & DataStore (for settings)
- **Background Work:** Foreground Services and BroadcastReceivers for screen-unlock detection.

The initial goal was simple: remind the user to drink water only when they actually unlock their phone, avoiding annoying spam when the device is in their pocket.

## 2. The Core Logic Problem
As we built the reminder engine, the logic quickly became tangled. Tracking whether the screen was locked, if a timer had expired, if the user had snoozed the app, or if they just drank water resulted in a chaotic web of overlapping booleans and race conditions. The app was unpredictable.

## 3. The Pivot to a Finite State Machine (FSM)
**Why we did it:** To resolve the chaos, we completely re-architected the reminder engine into an **Event-Driven Finite State Machine**. 
By explicitly defining the states the app could be in (`IDLE`, `COOLDOWN`, `PENDING_UNLOCK`, `SNOOZED`) and the events that could trigger transitions (`ScreenUnlocked`, `WaterLogged`, `TimerExpired`, `ReminderSnoozed`), the system became 100% deterministic and predictable.

All side-effects (like launching a notification or setting an alarm) were isolated as outputs of these transitions.

## 4. Key Refinements and "Undos" (The Pivot History)

### 4.1. Undoing `GoalReached` as an FSM State
- **What we did:** Initially, we had a `GOAL_REACHED` state in the FSM to stop reminders once the daily goal was met.
- **Why we undid it:** Achieving a goal is *business logic*, not a structural engine state. By making it a rigid state, we accidentally locked the engine and prevented users from logging extra water or resetting properly. We removed the state and instead let the engine check the `Metadata.goalReached` flag to decide if it should suppress the notification during standard transitions.

### 4.2. Undoing the `ForcePending` Bootstrap Logic
- **What we did:** When the app was opened or the service restarted, we forced a `ForcePending` event to immediately trigger a reminder on the next unlock.
- **Why we undid it:** This bypassed the user's explicitly chosen cooldown timer in the settings. If a user wanted a reminder every 2 hours, restarting the app would instantly nag them. We removed this and reverted to starting a standard `COOLDOWN` timer on bootstrap.

### 4.3. The Overlay vs. Sticky Notification Pivot
- **What we did:** We built a highly advanced, full-screen `WindowManager` overlay (`TYPE_APPLICATION_OVERLAY`) to aggressively but elegantly prompt the user to drink water.
- **Why we undid it:** The overlay required the "Display over other apps" permission. This permission is notoriously scary for average users during onboarding and causes high drop-off rates. 
- **The Solution:** We deleted the overlay code entirely and pivoted to a **Sticky Notification**. By using `.setOngoing(true)`, the notification pins itself to the drawer and cannot be swiped away, forcing interaction via action buttons. This achieved the aggressive reminder goal without the intrusive permission.

### 4.4. Notification Button Text Shortening
- **What we did:** Initially, the sticky notification action buttons read `"💧 Drank!"`, `"⏰ 10 min"`, and `"🚫 Not Now"`.
- **Why we changed it:** Android OS does not allow developers to change the font size of notification action buttons. On smaller screens, the text was truncating and looked unpolished. We shortened them to `"💧 250ml"`, `"⏰ 10 min"`, and `"🚫 Skip"` to ensure perfect visibility.

---

## 5. Going Forward
Every major decision, feature addition, or logic reversal must be appended to this document. The rationale is just as important as the code. 
