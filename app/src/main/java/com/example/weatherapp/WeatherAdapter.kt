package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(private val weatherlist : List<WeatherData>):
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>()
{
    class WeatherViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val conditionTextView: TextView = itemView.findViewById(R.id.text_weather_condition)
        val temperatureTextView: TextView = itemView.findViewById(R.id.text_temperature)
        val windSpeedTextView : TextView = itemView.findViewById(R.id.text_wind_speed)
        val timeTextView: TextView = itemView.findViewById(R.id.text_time)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather,parent,false)
        return WeatherViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weatherlist.size
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherData = weatherlist[position]
        holder.conditionTextView.text = weatherData.weather
        holder.temperatureTextView.text = "Temperature: ${weatherData.temperature}°C"
        holder.windSpeedTextView.text = "Wind Speed: ${weatherData.windspeed} m/s"
        holder.timeTextView.text = "Updated at: ${weatherData.time}"
    }
}