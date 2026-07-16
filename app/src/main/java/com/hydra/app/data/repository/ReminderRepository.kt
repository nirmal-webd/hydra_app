package com.hydra.app.data.repository

import com.hydra.app.data.room.ReminderLogDao
import com.hydra.app.model.ReminderLog
import com.hydra.app.model.ReminderWaterLog
import com.hydra.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderLogDao: ReminderLogDao) {

    fun getTodayRemindersShown(): Flow<Int> {
        val (start, end) = DateUtils.todayRange()
        return reminderLogDao.getRemindersShownForDay(start, end)
    }

    fun getTodayRemindersAccepted(): Flow<Int> {
        val (start, end) = DateUtils.todayRange()
        return reminderLogDao.getRemindersAcceptedForDay(start, end)
    }

    suspend fun logReminder(reminderLog: ReminderLog): Long {
        return reminderLogDao.insert(reminderLog)
    }

    suspend fun updateReminder(reminderLog: ReminderLog) {
        reminderLogDao.update(reminderLog)
    }

    suspend fun linkReminderToWaterLog(reminderId: Long, waterLogId: Long) {
        reminderLogDao.insertReminderWaterLog(
            ReminderWaterLog(reminderId = reminderId, waterLogId = waterLogId)
        )
    }
}
