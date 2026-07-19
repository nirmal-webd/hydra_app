package com.hydra.app.`data`.room

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HydraDatabase_Impl : HydraDatabase() {
  private val _waterLogDao: Lazy<WaterLogDao> = lazy {
    WaterLogDao_Impl(this)
  }

  private val _reminderLogDao: Lazy<ReminderLogDao> = lazy {
    ReminderLogDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "ec85701147b1e022e41ddf1670acb9d4", "5215511814137c1a849a25d1064b89c5") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `water_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount_ml` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `source` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reminder_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `action` TEXT NOT NULL, `app_package` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reminder_water_log` (`reminder_id` INTEGER NOT NULL, `water_log_id` INTEGER NOT NULL, PRIMARY KEY(`reminder_id`, `water_log_id`), FOREIGN KEY(`reminder_id`) REFERENCES `reminder_log`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`water_log_id`) REFERENCES `water_log`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reminder_water_log_water_log_id` ON `reminder_water_log` (`water_log_id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ec85701147b1e022e41ddf1670acb9d4')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `water_log`")
        connection.execSQL("DROP TABLE IF EXISTS `reminder_log`")
        connection.execSQL("DROP TABLE IF EXISTS `reminder_water_log`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsWaterLog: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWaterLog.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWaterLog.put("amount_ml", TableInfo.Column("amount_ml", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWaterLog.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWaterLog.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWaterLog: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWaterLog: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWaterLog: TableInfo = TableInfo("water_log", _columnsWaterLog,
            _foreignKeysWaterLog, _indicesWaterLog)
        val _existingWaterLog: TableInfo = read(connection, "water_log")
        if (!_infoWaterLog.equals(_existingWaterLog)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |water_log(com.hydra.app.model.WaterLog).
              | Expected:
              |""".trimMargin() + _infoWaterLog + """
              |
              | Found:
              |""".trimMargin() + _existingWaterLog)
        }
        val _columnsReminderLog: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReminderLog.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderLog.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderLog.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderLog.put("action", TableInfo.Column("action", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderLog.put("app_package", TableInfo.Column("app_package", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReminderLog: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesReminderLog: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoReminderLog: TableInfo = TableInfo("reminder_log", _columnsReminderLog,
            _foreignKeysReminderLog, _indicesReminderLog)
        val _existingReminderLog: TableInfo = read(connection, "reminder_log")
        if (!_infoReminderLog.equals(_existingReminderLog)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reminder_log(com.hydra.app.model.ReminderLog).
              | Expected:
              |""".trimMargin() + _infoReminderLog + """
              |
              | Found:
              |""".trimMargin() + _existingReminderLog)
        }
        val _columnsReminderWaterLog: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReminderWaterLog.put("reminder_id", TableInfo.Column("reminder_id", "INTEGER", true,
            1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderWaterLog.put("water_log_id", TableInfo.Column("water_log_id", "INTEGER",
            true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReminderWaterLog: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysReminderWaterLog.add(TableInfo.ForeignKey("reminder_log", "CASCADE",
            "NO ACTION", listOf("reminder_id"), listOf("id")))
        _foreignKeysReminderWaterLog.add(TableInfo.ForeignKey("water_log", "CASCADE", "NO ACTION",
            listOf("water_log_id"), listOf("id")))
        val _indicesReminderWaterLog: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesReminderWaterLog.add(TableInfo.Index("index_reminder_water_log_water_log_id", false,
            listOf("water_log_id"), listOf("ASC")))
        val _infoReminderWaterLog: TableInfo = TableInfo("reminder_water_log",
            _columnsReminderWaterLog, _foreignKeysReminderWaterLog, _indicesReminderWaterLog)
        val _existingReminderWaterLog: TableInfo = read(connection, "reminder_water_log")
        if (!_infoReminderWaterLog.equals(_existingReminderWaterLog)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reminder_water_log(com.hydra.app.model.ReminderWaterLog).
              | Expected:
              |""".trimMargin() + _infoReminderWaterLog + """
              |
              | Found:
              |""".trimMargin() + _existingReminderWaterLog)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "water_log", "reminder_log",
        "reminder_water_log")
  }

  public override fun clearAllTables() {
    super.performClear(true, "water_log", "reminder_log", "reminder_water_log")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(WaterLogDao::class, WaterLogDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ReminderLogDao::class, ReminderLogDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun waterLogDao(): WaterLogDao = _waterLogDao.value

  public override fun reminderLogDao(): ReminderLogDao = _reminderLogDao.value
}
