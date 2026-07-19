# Hydra — Complete Design Specification

> Context-aware hydration reminder app for Android.
> **Hypothesis**: People drink more water when reminders appear during natural phone interactions rather than at arbitrary times.

---

## Identity

| Field | Value |
|---|---|
| App Name | Hydra |
| Package Name | `com.hydra.app` |
| Min SDK | 26 |
| Target SDK | Latest stable |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |

---

## Navigation

**Bottom Navigation Bar** with 3 tabs:

| Tab | Screen | Purpose |
|---|---|---|
| 💧 Dashboard | `DashboardScreen` | Intake, progress ring, quick-add, stats |
| 📋 History | `HistoryScreen` | Past water logs grouped by day |
| ⚙️ Settings | `SettingsScreen` | Goal, reminders, quiet hours, app picker |

Sub-screen: **App Picker** (navigated from Settings)

---

## Theme & Colors

- **Dynamic Color (Material You)** on Android 12+ (API 31+)
- **Fallback**: Curated water-blue/cyan palette on SDK 26–30
- **Dark mode**: Follows system default (`isSystemInDarkTheme()`)
- No in-app theme toggle for MVP

---

## Dashboard Screen

### Hero Element
- **Large circular progress ring** (centered, animated)
- Center text: `1250 / 2000 ml`
- Below ring: percentage, e.g. `62%`
- Animates smoothly on intake changes
- Goal-reached state: ✅ checkmark, celebration color

### Quick-Add Buttons
- Row of large buttons below the ring:
  - `+250 ml`
  - `+500 ml`
  - `+750 ml`
  - `Custom` (opens input dialog)
- One-tap logging — no confirmation needed

### Reminder Status
- Indicator showing: Reminders active / Paused (quiet hours) / Goal reached

### Statistics Section (inline, below buttons)
- Water consumed today
- Goal completion %
- Reminders shown today
- Reminders accepted today
- Current hydration streak (days)

---

## History Screen

- **Grouped by day**, newest first
- Today: expanded by default showing individual logs
- Previous days: collapsed, showing date + daily total
- Tap to expand/collapse
- Each log entry: time (e.g. `2:34 PM`) + amount (e.g. `250 ml`)
- **Swipe-to-delete** on individual entries

---

## Settings Screen

Single scrollable screen with sections:

### Hydration Goal
- Slider: 1.0L – 5.0L, step 0.25L
- Default: **2.0L**

### Reminders
- Toggle: Unlock reminders (default: ON)
- Toggle: App usage reminders (default: ON)
- Cooldown slider: 15 – 90 minutes, step 5 min
- Default cooldown: **30 minutes**

### Quiet Hours
- Start time picker (default: **10:00 PM**)
- End time picker (default: **7:00 AM**)
- During quiet hours: all reminders suppressed

### Monitored Apps
- Tap → navigates to App Picker sub-screen

### App Reminder Duration
- Slider: 5 – 60 minutes, step 5 min
- Default: **15 minutes**

---

## App Picker Screen

- List of all installed **launchable** apps (from `PackageManager`)
- App icon + name + checkbox
- **Searchable** (search bar at top)
- Pre-select popular apps if installed: Instagram, YouTube, Reddit, Chrome, X/Twitter, Facebook, TikTok
- User toggles apps on/off
- Selections persisted in DataStore (stored as Set<String> of package names)

---

## Onboarding (First Launch)

4-step flow, then straight to Dashboard:

1. **Set Daily Goal** — slider, default 2.0L
2. **Select Monitored Apps** — app picker with pre-selections
3. **Set Quiet Hours** — time pickers, default 10 PM – 7 AM
4. **Grant Permissions** — POST_NOTIFICATIONS + PACKAGE_USAGE_STATS (with explanatory text directing user to Usage Access settings)

After completing: start foreground service, navigate to Dashboard.

---

## Notifications

### Notification Channels

| Channel | ID | Importance | Purpose |
|---|---|---|---|
| Hydration Service | `hydra_service` | LOW | Persistent progress notification |
| Hydration Reminders | `hydra_reminders` | HIGH | Unlock + app usage reminders |

### Persistent Notification (Foreground Service)
- Always visible while service runs
- Content: `💧 750 / 2000 ml` (updates on every log)
- Goal reached: `🎉 Goal complete! 2000 / 2000 ml`
- Tap opens Dashboard

### Reminder Notifications

**Unlock Reminder:**
> 💧 Time for a sip?

**App Usage Reminder:**
> 💧 You've been using Instagram for 15 min. Hydration break?

**Action Buttons (3):**

| Button | Behavior |
|---|---|
| 💧 250 ml | Instantly logs 250ml, dismisses notification, starts cooldown |
| ☕ Custom | Opens app to quick-add screen |
| ⏰ Later | Snoozes for 10 minutes, then re-shows (max 1 snooze per reminder) |

---

## Reminder Logic

### Unlock Reminder
1. `ACTION_USER_PRESENT` fires (user unlocks phone)
2. Wait **random 5–10 seconds**
3. Check:
   - Is cooldown active? → skip
   - Is it quiet hours? → skip
   - Is daily goal already met? → skip
4. Show reminder notification

### App Usage Reminder
1. Poll `UsageStatsManager` every **60 seconds**
2. If a monitored app has been in foreground ≥ configured threshold (default 15 min):
   - Check: cooldown, quiet hours, goal met → skip if any
   - Show reminder notification with app name

### Smart Cooldown
- After **any** reminder (shown, accepted, snoozed, or dismissed): start cooldown timer
- Default: **30 minutes** (configurable 15–90 min)
- Snooze bypasses cooldown (it's an explicit user request)

### Goal Reached
- Once daily intake ≥ goal: **stop all reminders** for the rest of the day
- Update persistent notification to celebration state
- Reminders resume next day at end of quiet hours

---

## Database Schema (Room)

### Table: `water_log`

| Column | Type | Notes |
|---|---|---|
| `id` | Long (PK, auto) | |
| `amount_ml` | Int | e.g. 250, 500 |
| `timestamp` | Long | Epoch milliseconds |
| `source` | String | `MANUAL`, `NOTIFICATION_QUICK`, `NOTIFICATION_CUSTOM` |

### Table: `reminder_log`

| Column | Type | Notes |
|---|---|---|
| `id` | Long (PK, auto) | |
| `type` | String | `UNLOCK`, `APP_USAGE` |
| `timestamp` | Long | Epoch milliseconds |
| `action` | String | `SHOWN`, `ACCEPTED`, `SNOOZED`, `DISMISSED` |
| `app_package` | String? | Nullable, populated for APP_USAGE type |

### Table: `reminder_water_log` (Junction)

| Column | Type | Notes |
|---|---|---|
| `reminder_id` | Long (FK → reminder_log.id) | |
| `water_log_id` | Long (FK → water_log.id) | |

> This junction table links which reminder led to which water log, enabling hypothesis validation queries like: "What % of unlock reminders resulted in water intake?"

---

## DataStore Preferences

| Key | Type | Default |
|---|---|---|
| `daily_goal_ml` | Int | 2000 |
| `cooldown_minutes` | Int | 30 |
| `app_duration_minutes` | Int | 15 |
| `quiet_hours_start` | String | `"22:00"` |
| `quiet_hours_end` | String | `"07:00"` |
| `unlock_reminders_enabled` | Boolean | true |
| `app_reminders_enabled` | Boolean | true |
| `monitored_apps` | Set\<String\> | Pre-selected package names |
| `onboarding_completed` | Boolean | false |

---

## Service Architecture

### `HydraService` (Foreground Service)
- Starts on first launch (after onboarding)
- Restarts on device reboot via `BOOT_COMPLETED` BroadcastReceiver
- Runs with persistent progress notification
- Hosts:
  - `ACTION_USER_PRESENT` BroadcastReceiver (runtime registered)
  - 60-second `UsageStatsManager` polling loop
  - Cooldown timer management

### Foreground Service Type
- `SPECIAL_USE` (SDK 34+) or `DATA_SYNC` depending on target SDK requirements

---

## Permissions

| Permission | Type | Purpose |
|---|---|---|
| `POST_NOTIFICATIONS` | Runtime (API 33+) | Show reminder notifications |
| `PACKAGE_USAGE_STATS` | Special (Settings) | Read foreground app via UsageStatsManager |
| `RECEIVE_BOOT_COMPLETED` | Normal (manifest) | Restart service after reboot |
| `FOREGROUND_SERVICE` | Normal (manifest) | Run foreground service |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Normal (manifest) | Foreground service type declaration |

---

## Streak Calculation

- **Definition**: Consecutive days where total intake ≥ daily goal
- Computed on-the-fly from `water_log` grouped by date
- If today's goal is not yet met, streak shows the count of consecutive *completed* days before today
- Resets when a day is missed (total < goal)

---

## Project Structure

```
app/src/main/java/com/hydra/app/
├── ui/
│   ├── screens/
│   │   ├── DashboardScreen.kt
│   │   ├── HistoryScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── AppPickerScreen.kt
│   │   └── OnboardingScreen.kt
│   ├── components/
│   │   ├── CircularProgressRing.kt
│   │   ├── QuickAddButtons.kt
│   │   ├── StatsCard.kt
│   │   ├── WaterLogItem.kt
│   │   └── CustomAmountDialog.kt
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── viewmodel/
│   ├── DashboardViewModel.kt
│   ├── HistoryViewModel.kt
│   ├── SettingsViewModel.kt
│   └── OnboardingViewModel.kt
├── data/
│   ├── room/
│   │   ├── HydraDatabase.kt
│   │   ├── WaterLogDao.kt
│   │   ├── ReminderLogDao.kt
│   │   └── Converters.kt
│   ├── datastore/
│   │   └── PreferencesManager.kt
│   └── repository/
│       ├── WaterRepository.kt
│       └── ReminderRepository.kt
├── service/
│   ├── HydraService.kt
│   ├── ReminderManager.kt
│   ├── UsageMonitor.kt
│   ├── NotificationHelper.kt
│   └── BootReceiver.kt
├── model/
│   ├── WaterLog.kt
│   ├── ReminderLog.kt
│   └── ReminderWaterLog.kt
├── navigation/
│   └── HydraNavGraph.kt
├── utils/
│   └── DateUtils.kt
├── HydraApp.kt
└── MainActivity.kt
```

---

## Internal Constants (Not Exposed to User)

| Constant | Value |
|---|---|
| Unlock delay range | 5–10 seconds (randomized) |
| UsageStats poll interval | 60 seconds |
| Max snoozes per reminder | 1 |
| Snooze duration | 10 minutes |
| Quick-log amount (notification) | 250 ml |

---

## Development Steps

| Step | Task |
|---|---|
| 1 | Create project structure, Gradle setup, dependencies |
| 2 | Implement navigation (bottom nav + nav graph) |
| 3 | Implement Room (entities, DAOs, database) |
| 4 | Implement DataStore (PreferencesManager) |
| 5 | Build Dashboard UI (progress ring, quick-add, stats) |
| 6 | Implement water logging (repository, ViewModel, UI wiring) |
| 7 | Implement notifications (channels, helper, actions) |
| 8 | Implement UsageStatsManager monitoring |
| 9 | Implement reminder scheduling (foreground service, unlock detection, cooldown, quiet hours) |

---

## Future Features (Design For, Don't Build)

- AI reminder timing optimization
- Accessibility-based scroll detection
- Multiple habit tracking
- Home screen widgets
- Health Connect integration
- Wear OS companion
- Cloud sync
- Analytics/telemetry
