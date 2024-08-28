package com.example.weatherapp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class WeatherWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        fetchWeatherData()
        return Result.success()
    }

    private fun fetchWeatherData() {
        val cityName = "thimphu"
        val  apiKey ="442da14930cad8ea2d0302fc260bc92f"
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$apiKey"

        val queue = Volley.newRequestQueue(applicationContext)
        val stringReq = StringRequest(Request.Method.GET, weatherUrl, { response ->
            val obj = JSONObject(response)
            val main = obj.getJSONObject("main")
            val temperature = main.getString("temp")
            val city = obj.getString("name")

            // Here, you could store the data in a database, send a notification, or update a widget.
            Log.d("WeatherWorker", "$temperature°C in $city")
        },
            {
                Log.e("WeatherWorker", "Failed to fetch weather data.")
            })
        queue.add(stringReq)
    }
}
