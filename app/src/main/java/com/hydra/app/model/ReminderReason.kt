package com.hydra.app.model

/**
 * Why a reminder exists. Stored in ReminderMetadata — not encoded as FSM states.
 * Adding new reminder types (AI timing, social apps) only requires a new enum value.
 */
enum class ReminderReason {
    COOLDOWN_COMPLETE,    // Normal cooldown finished
    SNOOZE_EXPIRED,       // Snooze timer ran out
    SNOOZE_LIMIT,         // 3rd consecutive snooze — reset to PENDING
    USER_DISMISSED,       // User tapped "Not Now"
    PHONE_UNLOCK,         // Reserved for unlock-triggered reminders (future)
    SOCIAL_APP_USAGE      // Future: detected extended social app session
}
