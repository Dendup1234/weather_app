package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject

class WeatherUpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseHelper: WeatherDatabaseHelper

    override fun doWork(): Result {
        Log.d("WeatherUpdateWorker", "Worker started")
        // Initialize the fusedLocationClient and databaseHelper
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        databaseHelper = WeatherDatabaseHelper(applicationContext)
        checkForLocationPermissionAndFetchWeather()
        return Result.success()
    }
    private var lastTemperature: Float? = null

    private fun fetchWeatherUpdate(location: Location) {
        val api_key = "442da14930cad8ea2d0302fc260bc92f"
        val weatherUrl =
            "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=${api_key}"

        val queue = Volley.newRequestQueue(applicationContext)
        val stringRequest = StringRequest(Request.Method.GET, weatherUrl, { response ->
            val obj = JSONObject(response)
            val main: JSONObject = obj.getJSONObject("main")
            val temperature = main.getString("temp").toFloat()
            val city = obj.getString("name")
            val wind_speed = obj.getJSONObject("wind").getString("speed")
            val weatherDescription = obj.getJSONArray("weather").getJSONObject(0).getString("description")

            val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

            // Check for significant temperature change before sending notification
            if (shouldSendNotification(temperature)) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.sendNotification(city, temperature.toString(), weatherDescription)
            }

            // Storing the weather data in SQLite database
            storeWeatherData(city, temperature.toString(), wind_speed, weatherDescription, time)

        }, { error ->
            Log.d("WeatherUpdateWorker", "Error fetching weather: ${error.message}")
        })

        queue.add(stringRequest)
    }

    private fun shouldSendNotification(currentTemperature: Float): Boolean {
        val threshold = 2.0f // Set your threshold for temperature change
        val shouldNotify = lastTemperature == null || kotlin.math.abs(currentTemperature - lastTemperature!!) >= threshold
        lastTemperature = currentTemperature
        return shouldNotify
    }

    private fun storeWeatherData(city: String, temperature: String, windSpeed: String, weatherDescription: String, time: String) {
        // Insert new weather data
        databaseHelper.insertWeatherData(city, temperature, windSpeed, weatherDescription, time)

        // Delete oldest record if more than 4 records exist
        val cursor = databaseHelper.readableDatabase.rawQuery("SELECT COUNT(*) FROM ${WeatherDatabaseHelper.TABLE_NAME}", null)
        if (cursor.moveToFirst() && cursor.getInt(0) > 4) {
            databaseHelper.deleteOldestWeatherData()
        }
        cursor.close()
    }

    private fun checkForLocationPermissionAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                applicationContext,
                "Location permission not granted",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        obtainLocation()
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    fetchWeatherUpdate(location)
                }
            }
            .addOnFailureListener {
                Log.d("WeatherUpdateWorker", "Failed to get location")
            }
    }
}
