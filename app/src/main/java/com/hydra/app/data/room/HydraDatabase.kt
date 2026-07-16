package com.hydra.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hydra.app.model.ReminderLog
import com.hydra.app.model.ReminderWaterLog
import com.hydra.app.model.WaterLog

@Database(
    entities = [WaterLog::class, ReminderLog::class, ReminderWaterLog::class],
    version = 1,
    exportSchema = false
)
abstract class HydraDatabase : RoomDatabase() {

    abstract fun waterLogDao(): WaterLogDao
    abstract fun reminderLogDao(): ReminderLogDao

    companion object {
        @Volatile
        private var INSTANCE: HydraDatabase? = null

        fun getInstance(context: Context): HydraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydraDatabase::class.java,
                    "hydra_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
