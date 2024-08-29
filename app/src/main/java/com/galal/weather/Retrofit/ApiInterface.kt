package com.galal.weather.Retrofit

import com.galal.weather.Model.weatherApp
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    suspend fun getWeatherDate(
        @Query("q") city: String,
        @Query("appId") appId: String,
        @Query("units") units: String
    ): Response<weatherApp>
}
