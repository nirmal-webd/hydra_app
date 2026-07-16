package com.hydra.app

import android.app.Application
import com.hydra.app.data.datastore.PreferencesManager
import com.hydra.app.data.datastore.ReminderStateStore
import com.hydra.app.data.repository.ReminderRepository
import com.hydra.app.data.repository.WaterRepository
import com.hydra.app.data.room.HydraDatabase
import com.hydra.app.model.ReminderEvent
import com.hydra.app.model.WaterLogSource
import com.hydra.app.service.ReminderStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HydraApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: HydraDatabase by lazy { HydraDatabase.getInstance(this) }
    val waterRepository: WaterRepository by lazy { WaterRepository(database.waterLogDao()) }
    val reminderRepository: ReminderRepository by lazy { ReminderRepository(database.reminderLogDao()) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }
    val reminderStateStore: ReminderStateStore by lazy { ReminderStateStore(this) }

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

    /**
     * Called once from MainActivity.onCreate.
     * Arms the FSM cooldown on first ever launch (when cooldownEndsAt == 0L).
     */
    fun bootstrapEngineIfNeeded() {
        appScope.launch {
            val meta = reminderStateStore.getCurrentMetadata()
            if (meta.cooldownEndsAt == 0L) {
                // First launch — fire a silent WaterLogged(0) to start the first cooldown.
                // Amount 0 means no water is logged to Room, just the engine is armed.
                reminderStateManager.dispatch(
                    ReminderEvent.WaterLogged(amountMl = 0, source = WaterLogSource.MANUAL)
                )
            }
        }
    }
}
