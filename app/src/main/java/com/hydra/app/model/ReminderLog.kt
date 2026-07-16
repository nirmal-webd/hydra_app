package com.hydra.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_log")
data class ReminderLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "action")
    val action: String = ReminderAction.SHOWN,

    @ColumnInfo(name = "app_package")
    val appPackage: String? = null
)

object ReminderType {
    const val UNLOCK = "UNLOCK"
    const val APP_USAGE = "APP_USAGE"
}

object ReminderAction {
    const val SHOWN = "SHOWN"
    const val ACCEPTED = "ACCEPTED"
    const val SNOOZED = "SNOOZED"
    const val DISMISSED = "DISMISSED"
}
