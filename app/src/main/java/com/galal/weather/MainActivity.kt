package com.galal.weather

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import com.galal.weather.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import mumayank.com.airlocationlibrary.AirLocation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 9bda4264b961f7ebb7a9e7c8f689a549
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), AirLocation.Callback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var airLocation:AirLocation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchWeatherApp("Egypt, EG")
        searchCity()
        getLocation()
    }

    private fun getLocation() {
        airLocation = AirLocation(this,this,false,0,"")
        airLocation.start()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherApp(query)
                }else{
                    Toast.makeText(this@MainActivity,"Wrong country",Toast.LENGTH_SHORT).show()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherApp(cityName: String) {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
            .getWeatherDate(cityName,
                "9bda4264b961f7ebb7a9e7c8f689a549",
                "metric").enqueue(object :Callback<weatherApp>{
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<weatherApp>, response: Response<weatherApp>) {
                val responseBody = response.body()
                    val temp = responseBody?.main?.temp.toString()
                        val hum = responseBody?.main?.humidity
                        val seaLevel = responseBody?.main?.pressure
                        val maxTemp = responseBody?.main?.temp_max
                        val maxMini = responseBody?.main?.temp_min
                        val windSpeed = responseBody?.wind?.speed
                        val sunRise = responseBody?.sys?.sunrise?.toLong()
                        val sunset = responseBody?.sys?.sunset?.toLong()
                        val condition = responseBody?.weather?.firstOrNull()?.main?: "unknown"

                        binding.weather.text = condition
                        binding.maxTemp.text = "Max Temp: $maxTemp °C"
                        binding.miniTemp.text = "Min Temp: $maxMini °C"
                        binding.humidity.text = "$hum %"
                        binding.windSpeed.text = "$windSpeed m/s"
                        binding.sunrise.text = time(sunRise!!)
                        binding.sunsetT.text = time(sunset!!)
                        binding.sea.text = "$seaLevel hpa"
                        binding.condition.text = condition
                        binding.temp.text = "$temp°C"
                        binding.day.text = dayName()
                        binding.date.text = date()
                        binding.cityName.text = cityName

                    changeImageWeather(condition)


            }

            override fun onFailure(call: Call<weatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity,"error",Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun changeImageWeather(conditions : String) {
        when(conditions){
            "Clouds","Mist","Foggy", "Overcast","Partly Clouds" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear", "Sunny", "Clear Sky" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Heavy Rain","Showers","Moderate Rain","Drizzle","Light Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Heavy Snow","Moderate Snow","Blizzard","Light Snow" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    @SuppressLint("SimpleDateFormat")
    private fun date(): String {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return  simpleDateFormat.format((Date()))
    }

    private fun time(timesTemp: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return  simpleDateFormat.format((Date(timesTemp*1000)))
    }

    fun dayName():String{
        val simpleDateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return  simpleDateFormat.format((Date()))
    }

    override fun onSuccess(locations: ArrayList<Location>) {
        locations[0].accuracy
        val lat = locations[0].latitude
        val long = locations[0].longitude
        val g = Geocoder(this)
        val address = g.getFromLocation(lat,long,1)!!
        address[0].locality.toString()
        binding.cityName.text = address.toString()
    }
    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
        Snackbar.make(binding.sea,"check your permission",Snackbar.LENGTH_SHORT).show()
    }

}