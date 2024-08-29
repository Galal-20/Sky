package com.galal.weather.UI

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.galal.weather.R
import com.galal.weather.Repository.WeatherRepository
import com.galal.weather.ViewModel.WeatherViewModel
import com.galal.weather.ViewModel.WeatherViewModelFactory
import com.galal.weather.databinding.ActivityMainBinding
import com.galal.weather.Model.weatherApp
import com.google.android.material.snackbar.Snackbar
import mumayank.com.airlocationlibrary.AirLocation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), AirLocation.Callback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var airLocation: AirLocation


    private val weatherViewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(WeatherRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        getLocation()
        searchCity()
    }

    private fun setupObservers() {
        weatherViewModel.weatherData.observe(this, Observer { result ->
            result.fold(
                onSuccess = { data ->
                    updateUI(data)
                },
                onFailure = {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            )
        })
    }

    private fun updateUI(weather: weatherApp) {
        binding.weather.text = weather.weather.firstOrNull()?.main ?: "Unknown"
        binding.temp.text = "${weather.main.temp}°C"
        binding.maxTemp.text = "Max Temp: ${weather.main.temp_max} °C"
        binding.miniTemp.text = "Min Temp: ${weather.main.temp_min} °C"
        binding.humidity.text = "${weather.main.humidity} %"
        binding.windSpeed.text = "${weather.wind.speed} m/s"
        binding.sunrise.text = time(weather.sys.sunrise.toLong())
        binding.sunsetT.text = time(weather.sys.sunset.toLong())
        binding.sea.text = "${weather.main.pressure} hpa"
        binding.condition.text = weather.weather.firstOrNull()?.main ?: "Unknown"
        binding.day.text = dayName()
        binding.date.text = date()
        binding.cityName.text = weather.name

        changeImageWeather(weather.weather.firstOrNull()?.main ?: "unknown")
    }

    private fun changeImageWeather(conditions: String) {
        when (conditions) {
            "Clouds", "Mist", "Foggy", "Overcast", "Partly Clouds" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear", "Sunny", "Clear Sky" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Heavy Rain", "Showers", "Moderate Rain", "Drizzle", "Light Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Heavy Snow", "Moderate Snow", "Blizzard", "Light Snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    @SuppressLint("SimpleDateFormat")
    private fun date(): String {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    private fun time(timesTemp: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return simpleDateFormat.format(Date(timesTemp * 1000))
    }

    fun dayName(): String {
        val simpleDateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    private fun getLocation() {
        airLocation = AirLocation(this, this, false, 0, "")
        airLocation.start()
    }

    private fun searchCity() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    weatherViewModel.fetchWeather(query)
                } else {
                    Toast.makeText(this@MainActivity, "Wrong country", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })
    }

    /*override fun onSuccess(locations: ArrayList<Location>) {
        locations[0].accuracy
        val lat = locations[0].latitude
        val long = locations[0].longitude
        val g = Geocoder(this)
        val address = g.getFromLocation(lat, long, 1)!!
        if (address.isNotEmpty()) {
            val cityName = address[0].getAddressLine(0)
            if (cityName != null) {
                weatherViewModel.fetchWeather(cityName)
            } else {
                binding.cityName.text = "Unknown City"
            }
        } else {
            binding.cityName.text = "Unknown Location"
        }
    }*/

    override fun onSuccess(locations: ArrayList<Location>) {
        locations[0].accuracy
        val lat = locations[0].latitude
        val long = locations[0].longitude
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addressList = geocoder.getFromLocation(lat, long, 1)
            if (!addressList.isNullOrEmpty()) {
                val cityName = addressList[0].countryName ?: "Unknown City"
                weatherViewModel.fetchWeather(cityName)
            } else {
                binding.cityName.text = "Unknown Location"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "Unable to get location", Snackbar.LENGTH_LONG).show()
        }
    }


    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
        Snackbar.make(binding.sea, "Check your permission", Snackbar.LENGTH_SHORT).show()
    }
}


