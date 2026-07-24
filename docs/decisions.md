# Architecture Decision Records (ADRs)

> **CRITICAL DIRECTIVE:** Record why important decisions were made here. Do not revert or change these decisions without explicit user approval. Any AI assistant working on this codebase must read this file to understand the context behind non-standard architectural choices.

---

# ADR-001
**Decision:** 
Use a Pure Functional Finite State Machine (FSM) for the Reminder Engine.

**Reason:** 
Hydration logic (snoozing, quiet hours, cooldowns, goal tracking) is highly complex. Using scattered boolean flags inside a ViewModel or Service leads to unpredictable race conditions and impossible-to-debug edge cases. A pure reducer (`ReminderReducer`) allows for predictable, atomic state transitions and exhaustive testing without needing to mock Android-specific system classes.

**Rejected Alternatives:** 
Standard timer logic in a Service, WorkManager periodic work, complex `if/else` logic trees.

---

# ADR-002
**Decision:** 
Use `AlarmManager` combined with a `Foreground Service`, explicitly avoiding `WorkManager`.

**Reason:** 
`WorkManager` has a hard-coded minimum interval of 15 minutes for periodic work and does not guarantee exact execution times, as it is heavily influenced by Android's Doze mode and battery optimizations. Because Hydra relies on exact timing for things like 10-minute Auto-Snoozes and precise Cooldowns, `AlarmManager` with `setExactAndAllowWhileIdle` is required. The `Foreground Service` ensures the app process stays alive to manage the orchestrator state.

**Rejected Alternatives:** 
`WorkManager`, standard background services (which get killed).

---

# ADR-003
**Decision:** 
Use Context-Aware Triggers (Wait for Unlock) over fixed rigid clocks.

**Reason:** 
Standard hydration apps act as "dumb alarms" that buzz when a user is busy (e.g., driving, in a meeting) and get instantly swiped away. By using the `ACTION_USER_PRESENT` broadcast receiver, Hydra intentionally waits for the exact moment the user unlocks their phone—catching them during a task transition. This guarantees higher engagement and prevents notification blindness.

**Rejected Alternatives:** 
Firing notifications exactly every 2 hours regardless of user context.

---

# ADR-004
**Decision:** 
Implement a Ticker Coroutine as a Failsafe mechanism in `ReminderStateManager`.

**Reason:** 
Even with `AlarmManager`, the Android OS (especially on heavily customized OEM ROMs like MIUI or OneUI) can aggressively drop alarms or delay broadcasts to save battery. The `ReminderStateManager` runs a 60-second `while(true)` coroutine loop while the Foreground Service is active. This acts as a self-healing mechanism that forces state evaluations and processes missed events if the system drops an alarm broadcast.

**Rejected Alternatives:** 
Relying entirely on AlarmManager broadcasts.
