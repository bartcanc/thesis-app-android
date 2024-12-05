package com.example.thesisapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class SensorDataDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "sensor_data.db"
        private const val DATABASE_VERSION = 3 // Zwiększ wersję bazy, aby wymusić migrację

        const val TABLE_NAME = "sensor_data"
        const val COLUMN_ID = "id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_RAW_DATA = "raw_data" // Dodana kolumna na surowe dane
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER,
                $COLUMN_RAW_DATA BLOB
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            // Jeśli migrujemy z wersji 2 do 3, dodajemy kolumnę `raw_data`
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_RAW_DATA BLOB")
        }
    }

    fun insertRawSensorData(timestamp: Long, rawData: ByteArray): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_RAW_DATA, rawData) // Zapisuje dane jako BLOB
        }
        return db.insert(TABLE_NAME, null, values)
    }

}
