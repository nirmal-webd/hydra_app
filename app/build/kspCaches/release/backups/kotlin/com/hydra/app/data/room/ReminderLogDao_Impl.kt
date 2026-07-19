package com.hydra.app.`data`.room

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.hydra.app.model.ReminderLog
import com.hydra.app.model.ReminderWaterLog
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReminderLogDao_Impl(
  __db: RoomDatabase,
) : ReminderLogDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfReminderLog: EntityInsertAdapter<ReminderLog>

  private val __insertAdapterOfReminderWaterLog: EntityInsertAdapter<ReminderWaterLog>

  private val __updateAdapterOfReminderLog: EntityDeleteOrUpdateAdapter<ReminderLog>
  init {
    this.__db = __db
    this.__insertAdapterOfReminderLog = object : EntityInsertAdapter<ReminderLog>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `reminder_log` (`id`,`type`,`timestamp`,`action`,`app_package`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderLog) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.type)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.action)
        val _tmpAppPackage: String? = entity.appPackage
        if (_tmpAppPackage == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpAppPackage)
        }
      }
    }
    this.__insertAdapterOfReminderWaterLog = object : EntityInsertAdapter<ReminderWaterLog>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `reminder_water_log` (`reminder_id`,`water_log_id`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderWaterLog) {
        statement.bindLong(1, entity.reminderId)
        statement.bindLong(2, entity.waterLogId)
      }
    }
    this.__updateAdapterOfReminderLog = object : EntityDeleteOrUpdateAdapter<ReminderLog>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `reminder_log` SET `id` = ?,`type` = ?,`timestamp` = ?,`action` = ?,`app_package` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderLog) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.type)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.action)
        val _tmpAppPackage: String? = entity.appPackage
        if (_tmpAppPackage == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpAppPackage)
        }
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insert(reminderLog: ReminderLog): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfReminderLog.insertAndReturnId(_connection, reminderLog)
    _result
  }

  public override suspend fun insertReminderWaterLog(reminderWaterLog: ReminderWaterLog): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfReminderWaterLog.insert(_connection, reminderWaterLog)
  }

  public override suspend fun update(reminderLog: ReminderLog): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfReminderLog.handle(_connection, reminderLog)
  }

  public override fun getRemindersShownForDay(startOfDay: Long, endOfDay: Long): Flow<Int> {
    val _sql: String =
        "SELECT COUNT(*) FROM reminder_log WHERE action = 'SHOWN' AND timestamp >= ? AND timestamp < ?"
    return createFlow(__db, false, arrayOf("reminder_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startOfDay)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endOfDay)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getRemindersAcceptedForDay(startOfDay: Long, endOfDay: Long): Flow<Int> {
    val _sql: String =
        "SELECT COUNT(*) FROM reminder_log WHERE action = 'ACCEPTED' AND timestamp >= ? AND timestamp < ?"
    return createFlow(__db, false, arrayOf("reminder_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startOfDay)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endOfDay)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): ReminderLog? {
    val _sql: String = "SELECT * FROM reminder_log WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfAction: Int = getColumnIndexOrThrow(_stmt, "action")
        val _columnIndexOfAppPackage: Int = getColumnIndexOrThrow(_stmt, "app_package")
        val _result: ReminderLog?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpAction: String
          _tmpAction = _stmt.getText(_columnIndexOfAction)
          val _tmpAppPackage: String?
          if (_stmt.isNull(_columnIndexOfAppPackage)) {
            _tmpAppPackage = null
          } else {
            _tmpAppPackage = _stmt.getText(_columnIndexOfAppPackage)
          }
          _result = ReminderLog(_tmpId,_tmpType,_tmpTimestamp,_tmpAction,_tmpAppPackage)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
