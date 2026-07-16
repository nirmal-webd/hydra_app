package com.hydra.app.data.repository

import com.hydra.app.data.room.WaterLogDao
import com.hydra.app.model.WaterLog
import com.hydra.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class WaterRepository(private val waterLogDao: WaterLogDao) {

    fun getTodayLogs(): Flow<List<WaterLog>> {
        val (start, end) = DateUtils.todayRange()
        return waterLogDao.getLogsForDay(start, end)
    }

    fun getTodayTotal(): Flow<Int> {
        val (start, end) = DateUtils.todayRange()
        return waterLogDao.getTotalForDay(start, end)
    }

    fun getAllLogs(): Flow<List<WaterLog>> {
        return waterLogDao.getAllLogs()
    }

    suspend fun getAllLogsSnapshot(): List<WaterLog> {
        return waterLogDao.getAllLogsSnapshot()
    }

    suspend fun logWater(amountMl: Int, source: String = "MANUAL"): Long {
        return waterLogDao.insert(
            WaterLog(
                amountMl = amountMl,
                source = source
            )
        )
    }

    suspend fun deleteLog(waterLog: WaterLog) {
        waterLogDao.delete(waterLog)
    }
}
