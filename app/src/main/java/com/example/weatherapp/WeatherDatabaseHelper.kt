package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WeatherDatabaseHelper(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "weather.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "weather_data"
        const val COLUMN_ID = "id"
        const val COLUMN_CITY = "city"
        const val COLUMN_TEMP = "temp"
        const val COLUMN_WIND_SPEED = "wind_speed"
        const val COLUMN_WEATHER = "weather"
        const val COLUMN_TIME = "time"


    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CITY TEXT, " +
                "$COLUMN_TEMP TEXT, " +
                "$COLUMN_WIND_SPEED TEXT, " +
                "$COLUMN_WEATHER TEXT, " +
                "$COLUMN_TIME TEXT)")
        if (db != null) {
            db.execSQL(createTable)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        }
        onCreate(db)
    }
    fun insertWeatherData(city: String, temperature: String, windSpeed: String, weatherDescription: String, time: String){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CITY, city)
            put(COLUMN_TEMP, temperature)
            put(COLUMN_WIND_SPEED, windSpeed)
            put(COLUMN_WEATHER, weatherDescription)
            put(COLUMN_TIME, time)
        }
        db.insert(TABLE_NAME,null,values)
        db.close()
    }
    fun deleteOldestWeatherData(){
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID IN (SELECT $COLUMN_ID FROM $TABLE_NAME ORDER BY $COLUMN_ID ASC LIMIT 1)")
        db.close()
    }
}