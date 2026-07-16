package com.hydra.app

import android.app.Application
import com.hydra.app.data.datastore.PreferencesManager
import com.hydra.app.data.datastore.ReminderStateStore
import com.hydra.app.data.repository.ReminderRepository
import com.hydra.app.data.repository.WaterRepository
import com.hydra.app.data.room.HydraDatabase
import com.hydra.app.service.ReminderStateManager

class HydraApp : Application() {

    val database: HydraDatabase by lazy { HydraDatabase.getInstance(this) }
    val waterRepository: WaterRepository by lazy { WaterRepository(database.waterLogDao()) }
    val reminderRepository: ReminderRepository by lazy { ReminderRepository(database.reminderLogDao()) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }
    val reminderStateStore: ReminderStateStore by lazy { ReminderStateStore(this) }

    // Single source of truth for the reminder engine
    val reminderStateManager: ReminderStateManager by lazy {
        ReminderStateManager(
            context = this,
            stateStore = reminderStateStore,
            waterRepository = waterRepository,
            reminderRepository = reminderRepository
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
