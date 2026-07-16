package com.hydra.app.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.hydra.app.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Insert
    suspend fun insert(waterLog: WaterLog): Long

    @Delete
    suspend fun delete(waterLog: WaterLog)

    @Query("SELECT * FROM water_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterLog>>

    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getTotalForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT * FROM water_log ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLog>>

    @Query("SELECT COALESCE(SUM(amount_ml), 0) as total, timestamp / :dayMillis as day_index FROM water_log GROUP BY day_index ORDER BY day_index DESC")
    fun getDailyTotals(dayMillis: Long = 86_400_000L): Flow<List<DailyTotal>>
}

data class DailyTotal(
    val total: Int,
    @androidx.room.ColumnInfo(name = "day_index") val dayIndex: Long
)
