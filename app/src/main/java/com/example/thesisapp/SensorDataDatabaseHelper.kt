package com.example.thesisapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SensorDataDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "sensor_data.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NAME = "sensor_data"
        const val COLUMN_ID = "id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_IR = "ir"
        const val COLUMN_RED = "red"
        const val COLUMN_ACC_X = "acc_x"
        const val COLUMN_ACC_Y = "acc_y"
        const val COLUMN_ACC_Z = "acc_z"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER,
                $COLUMN_IR REAL,
                $COLUMN_RED REAL,
                $COLUMN_ACC_X REAL,
                $COLUMN_ACC_Y REAL,
                $COLUMN_ACC_Z REAL
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_TIMESTAMP INTEGER")
        }
    }

    fun insertSensorData(timestamp: Long, ir: Long, red: Long, accX: Double, accY: Double, accZ: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_IR, ir)
            put(COLUMN_RED, red)
            put(COLUMN_ACC_X, accX)
            put(COLUMN_ACC_Y, accY)
            put(COLUMN_ACC_Z, accZ)
        }
        return db.insert(TABLE_NAME, null, values)
    }
}
