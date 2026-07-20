package com.hydra.app.data.repository

import com.hydra.app.data.room.WaterLogDao
import com.hydra.app.model.WaterLog
import com.hydra.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class WaterRepository(private val waterLogDao: WaterLogDao) {

    private val currentDayRange = flow {
        while (true) {
            emit(DateUtils.todayRange())
            delay(60_000) // Check every minute
        }
    }.distinctUntilChanged()

    fun getTodayLogs(): Flow<List<WaterLog>> {
        return currentDayRange.flatMapLatest { (start, end) ->
            waterLogDao.getLogsForDay(start, end)
        }
    }

    fun getTodayTotal(): Flow<Int> {
        return currentDayRange.flatMapLatest { (start, end) ->
            waterLogDao.getTotalForDay(start, end)
        }
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
