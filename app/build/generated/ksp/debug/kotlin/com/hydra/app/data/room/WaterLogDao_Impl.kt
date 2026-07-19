package com.hydra.app.`data`.room

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.hydra.app.model.WaterLog
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WaterLogDao_Impl(
  __db: RoomDatabase,
) : WaterLogDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWaterLog: EntityInsertAdapter<WaterLog>

  private val __deleteAdapterOfWaterLog: EntityDeleteOrUpdateAdapter<WaterLog>
  init {
    this.__db = __db
    this.__insertAdapterOfWaterLog = object : EntityInsertAdapter<WaterLog>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `water_log` (`id`,`amount_ml`,`timestamp`,`source`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WaterLog) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.amountMl.toLong())
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.source)
      }
    }
    this.__deleteAdapterOfWaterLog = object : EntityDeleteOrUpdateAdapter<WaterLog>() {
      protected override fun createQuery(): String = "DELETE FROM `water_log` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WaterLog) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(waterLog: WaterLog): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfWaterLog.insertAndReturnId(_connection, waterLog)
    _result
  }

  public override suspend fun delete(waterLog: WaterLog): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfWaterLog.handle(_connection, waterLog)
  }

  public override fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterLog>> {
    val _sql: String =
        "SELECT * FROM water_log WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("water_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startOfDay)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endOfDay)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMl: Int = getColumnIndexOrThrow(_stmt, "amount_ml")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _result: MutableList<WaterLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: WaterLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMl: Int
          _tmpAmountMl = _stmt.getLong(_columnIndexOfAmountMl).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          _item = WaterLog(_tmpId,_tmpAmountMl,_tmpTimestamp,_tmpSource)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalForDay(startOfDay: Long, endOfDay: Long): Flow<Int> {
    val _sql: String =
        "SELECT COALESCE(SUM(amount_ml), 0) FROM water_log WHERE timestamp >= ? AND timestamp < ?"
    return createFlow(__db, false, arrayOf("water_log")) { _connection ->
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

  public override fun getAllLogs(): Flow<List<WaterLog>> {
    val _sql: String = "SELECT * FROM water_log ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("water_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMl: Int = getColumnIndexOrThrow(_stmt, "amount_ml")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _result: MutableList<WaterLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: WaterLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMl: Int
          _tmpAmountMl = _stmt.getLong(_columnIndexOfAmountMl).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          _item = WaterLog(_tmpId,_tmpAmountMl,_tmpTimestamp,_tmpSource)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllLogsSnapshot(): List<WaterLog> {
    val _sql: String = "SELECT * FROM water_log ORDER BY timestamp DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMl: Int = getColumnIndexOrThrow(_stmt, "amount_ml")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _result: MutableList<WaterLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: WaterLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMl: Int
          _tmpAmountMl = _stmt.getLong(_columnIndexOfAmountMl).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          _item = WaterLog(_tmpId,_tmpAmountMl,_tmpTimestamp,_tmpSource)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getLogsSince(startMs: Long): Flow<List<WaterLog>> {
    val _sql: String = "SELECT * FROM water_log WHERE timestamp >= ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("water_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startMs)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMl: Int = getColumnIndexOrThrow(_stmt, "amount_ml")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _result: MutableList<WaterLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: WaterLog
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMl: Int
          _tmpAmountMl = _stmt.getLong(_columnIndexOfAmountMl).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          _item = WaterLog(_tmpId,_tmpAmountMl,_tmpTimestamp,_tmpSource)
          _result.add(_item)
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
