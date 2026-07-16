package com.hydra.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_log")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount_ml")
    val amountMl: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "source")
    val source: String = WaterLogSource.MANUAL
)

object WaterLogSource {
    const val MANUAL = "MANUAL"
    const val NOTIFICATION_QUICK = "NOTIFICATION_QUICK"
    const val NOTIFICATION_CUSTOM = "NOTIFICATION_CUSTOM"
}
