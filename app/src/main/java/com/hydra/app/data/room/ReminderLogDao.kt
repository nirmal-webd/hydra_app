package com.hydra.app.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hydra.app.model.ReminderLog
import com.hydra.app.model.ReminderWaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {

    @Insert
    suspend fun insert(reminderLog: ReminderLog): Long

    @Update
    suspend fun update(reminderLog: ReminderLog)

    @Insert
    suspend fun insertReminderWaterLog(reminderWaterLog: ReminderWaterLog)

    @Query("SELECT COUNT(*) FROM reminder_log WHERE action = 'SHOWN' AND timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getRemindersShownForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminder_log WHERE action = 'ACCEPTED' AND timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getRemindersAcceptedForDay(startOfDay: Long, endOfDay: Long): Flow<Int>
}
