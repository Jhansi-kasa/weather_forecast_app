package com.example.weather.api;

import com.example.weather.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    // Search by city name
    @GET("forecast")
    Call<WeatherResponse> getForecastByCity(
            @Query("q")     String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Search by coordinates (GPS)
    @GET("forecast")
    Call<WeatherResponse> getForecastByCoords(
            @Query("lat")   double lat,
            @Query("lon")   double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
