    package com.example.weatherapp

    import android.annotation.SuppressLint
    import android.content.pm.PackageManager
    import android.location.Location
    import android.os.Bundle
    import android.widget.Button
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.app.ActivityCompat
    import com.android.volley.Request
    import com.android.volley.toolbox.StringRequest
    import com.android.volley.toolbox.Volley
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import org.json.JSONObject
    import android.Manifest
    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.graphics.Rect
    import android.view.View
    import android.widget.EditText
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView

    import com.google.android.material.floatingactionbutton.FloatingActionButton
    import androidx.work.Data
    import androidx.work.PeriodicWorkRequestBuilder
    import androidx.work.WorkManager
    import java.util.concurrent.TimeUnit

    class MainActivity : AppCompatActivity() {
        var api_keys = "442da14930cad8ea2d0302fc260bc92f"

        private lateinit var cityEditView : EditText
        private lateinit var weatherButton: FloatingActionButton
        private lateinit var locationButton: FloatingActionButton
        private lateinit var cityTextView: TextView
        private lateinit var temperatureTextView: TextView
        private lateinit var weatherTextView: TextView
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var weather_url: String

        private val LOCATION_PERMISSION_REQUEST_CODE = 1

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_default)

            cityEditView = findViewById(R.id.CityEditView)

            cityTextView = findViewById(R.id.CityTextView)

            weatherButton = findViewById(R.id.weatherButton)

            locationButton = findViewById(R.id.locationButton)

            temperatureTextView = findViewById(R.id.temperatureTextView)

            weatherTextView = findViewById(R.id.weatherTextView)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Schedule the WeatherUpdateWorker to run periodically every 15 minutes
            val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(this).enqueue(weatherWorkRequest)

            // For the recyclerview

            weatherButton.setOnClickListener {
                val cityName = cityEditView.text.toString().trim()
                if(cityName.isNotEmpty()){
                    getWeatherData(cityName)
                }
                else{
                    Toast.makeText(this,"Enter the required city",Toast.LENGTH_SHORT).show()
                }
            }
            locationButton.setOnClickListener {
                checkForPermission()
            }
        }


        private fun checkForPermission() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                // Permissions are already granted, obtain the location
                obtainLocation()
            }
        }
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, obtain the location
                    obtainLocation()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun obtainLocation() {
            // get the last location

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // get the latitude and longitude
                    // and create the http URL
                    if (location != null) {
                        weather_url = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=${api_keys}"
                    }
                    getWeatherData()

                    // this function will
                    // fetch data from URL

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show()
                }
        }

        private fun getWeatherData() {
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(this)
            val url: String = weather_url
            // Request a string response
            // from the provided URL.
            val stringReq = StringRequest(
                Request.Method.GET, url, { response ->
                    val obj = JSONObject(response)

                    // Getting additional weather data
                    val main: JSONObject = obj.getJSONObject("main")
                    val temperature = main.getString("temp")
                    val weatherDescription =
                        obj.getJSONArray("weather").getJSONObject(0).getString("description")
                    val city = obj.getString("name")

                    // Display the additional information
                    cityTextView.text = "$city".trimIndent()
                    temperatureTextView.text = "$temperature°C".trimIndent()
                    weatherTextView.text = "$weatherDescription".trimIndent()
                    switchLayout(weatherDescription, temperature, city, isLocation = true)
                },
                {
                    weatherTextView.text = "Failed to retrieve weather data"
                    temperatureTextView.text = "Failed to retrieve temp data"
                    cityTextView.text = "Failed to retrieve city data"

                })
            queue.add(stringReq)
        }


        private fun getWeatherData(cityName:String = "",isLocation: Boolean = false) {
            val weather_url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$api_keys"
            val queue = Volley.newRequestQueue(this)
            val url: String = weather_url

            val stringReq = StringRequest(
                Request.Method.GET, url, { response ->
                    val obj = JSONObject(response)

                    // Getting additional weather data
                    val main: JSONObject = obj.getJSONObject("main")
                    val temperature = main.getString("temp")
                    val weatherDescription =
                        obj.getJSONArray("weather").getJSONObject(0).getString("description")
                    val city = obj.getString("name")

                    // Display the additional information
                    cityTextView.text = "$city".trimIndent()
                    temperatureTextView.text = "$temperature°C".trimIndent()
                    weatherTextView.text = "$weatherDescription".trimIndent()
                    switchLayout(weatherDescription, temperature, city,isLocation)
                },
                {
                    weatherTextView.text = "Failed to retrieve weather data"
                    temperatureTextView.text = "Failed to retrieve temp data"
                    cityTextView.text = "Failed to retrieve city data"

                })
            queue.add(stringReq)
        }

        private fun switchLayout(weatherDescription: String, temperature: String, city: String,isLocation: Boolean) {
            when {
                weatherDescription.contains("clear") || weatherDescription.contains("sunny") ->{
                    setContentView(R.layout.activity_main)
                }

                weatherDescription.contains("rain") || weatherDescription.contains("shower") ->{
                    setContentView(
                        R.layout.activity_main_rainy
                    )


                }

                weatherDescription.contains("cloud") || weatherDescription.contains("overcast") -> {

                    setContentView(
                        R.layout.activity_main_cloudy

                    )
                }

                else -> {
                    setContentView(R.layout.acitivity_main_unknown)
                }
            }
            if(isLocation == true){
                setupRecyclerView()

            }

            cityTextView = findViewById(R.id.CityTextView)
            weatherTextView = findViewById(R.id.weatherTextView)
            temperatureTextView = findViewById(R.id.temperatureTextView)

            // Set the UI elements again with the updated data
            cityTextView.text = city
            temperatureTextView.text = "$temperature °C"
            weatherTextView.text = weatherDescription

            resetButton()

        }
        private fun resetButton(){
            weatherButton = findViewById(R.id.weatherButton)
            weatherButton.setOnClickListener{
                setContentView(R.layout.activity_main_default)

                cityEditView = findViewById(R.id.CityEditView)
                cityTextView = findViewById(R.id.CityTextView)
                temperatureTextView = findViewById(R.id.temperatureTextView)
                weatherTextView = findViewById(R.id.weatherTextView)
                weatherButton = findViewById(R.id.weatherButton)
                locationButton = findViewById(R.id.locationButton)

                weatherButton.setOnClickListener {
                    val cityName = cityEditView.text.toString().trim()
                    if (cityName.isNotEmpty()) {
                        getWeatherData(cityName)
                    } else {
                        Toast.makeText(this, "Enter the required city", Toast.LENGTH_SHORT).show()
                    }
                }
                locationButton.setOnClickListener {
                    checkForPermission()

                }
            }

        }

        @SuppressLint("Range")
        private fun getWeatherDataFromDatabase(): List<WeatherData> {
            val dbHelper = WeatherDatabaseHelper(this)
            val weatherDataList = mutableListOf<WeatherData>()

            val cursor = dbHelper.readableDatabase.query(
                WeatherDatabaseHelper.TABLE_NAME,
                null, null, null, null, null, "${WeatherDatabaseHelper.COLUMN_ID} DESC"
            )

            while (cursor.moveToNext()) {
                val city = cursor.getString(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_CITY))
                val temperature = cursor.getString(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_TEMP))
                val windSpeed = cursor.getString(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_WIND_SPEED))
                val weatherDescription = cursor.getString(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_WEATHER))
                val time = cursor.getString(cursor.getColumnIndex(WeatherDatabaseHelper.COLUMN_TIME))

                weatherDataList.add(WeatherData(weatherDescription, temperature, windSpeed, time))
            }
            cursor.close()
            return weatherDataList
        }
        private fun setupRecyclerView() {
            val recyclerView: RecyclerView = findViewById(R.id.recycler_view_weather)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = WeatherAdapter(getWeatherDataFromDatabase())
        }

    }
