package com.hydra.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "reminder_water_log",
    primaryKeys = ["reminder_id", "water_log_id"],
    foreignKeys = [
        ForeignKey(
            entity = ReminderLog::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WaterLog::class,
            parentColumns = ["id"],
            childColumns = ["water_log_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderWaterLog(
    @ColumnInfo(name = "reminder_id")
    val reminderId: Long,

    @ColumnInfo(name = "water_log_id", index = true)
    val waterLogId: Long
)
