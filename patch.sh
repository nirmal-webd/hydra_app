sed -i 's/object ReminderDismissed : ReminderEvent()/object ReminderDismissed : ReminderEvent()\n    object ReminderSwiped : ReminderEvent()/g' app/src/main/java/com/hydra/app/model/ReminderEvent.kt
