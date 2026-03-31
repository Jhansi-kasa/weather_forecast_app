package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.DailyAdapter;
import com.example.weather.api.RetrofitClient;
import com.example.weather.api.WeatherApi;
import com.example.weather.model.WeatherItem;
import com.example.weather.model.WeatherResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends AppCompatActivity {

    private TextView txtCity, txtDate, txtTemp, txtCondition, txtFeelsLike, txtHighLow;
    private ImageView imgWeather;
    private RecyclerView recyclerDaily;
    private MaterialButton btnDetails;
    private ImageButton btnSearchCity;

    private WeatherResponse weatherData;
    private static final String API_KEY = "57a275716f575222c2319c74740480c0"; // Replace with your OpenWeatherMap key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Bind views
        txtCity = findViewById(R.id.txtCity);
        txtDate = findViewById(R.id.txtDate);
        txtTemp = findViewById(R.id.txtTemp);
        txtCondition = findViewById(R.id.txtCondition);
        txtFeelsLike = findViewById(R.id.txtFeelsLike);
        txtHighLow = findViewById(R.id.txtHighLow);
        imgWeather = findViewById(R.id.imgWeather);
        recyclerDaily = findViewById(R.id.recyclerDaily);
        btnDetails = findViewById(R.id.btnDetails);
        btnSearchCity = findViewById(R.id.btnSearchCity);

        // Safety check: ensure layout has all required views
        if (txtCity == null || txtTemp == null || recyclerDaily == null) {
            throw new IllegalStateException("activity_info.xml is missing required views!");
        }

        btnSearchCity.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, search_city.class);
            startActivity(intent);
        });

        // Disable details button until data loads
        btnDetails.setEnabled(false);
        btnDetails.setAlpha(0.5f);

        btnDetails.setOnClickListener(v -> {
            String cityName = txtCity.getText().toString().trim();
            if (cityName.isEmpty() || cityName.equals("Unknown")) {
                Toast.makeText(this, "Please wait for data to load", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(InfoActivity.this, DetailActivity.class);
            String city = getIntent().getStringExtra("city");
            double lat = getIntent().getDoubleExtra("lat", Double.NaN);
            double lon = getIntent().getDoubleExtra("lon", Double.NaN);

            if (city != null && !city.isEmpty()) {
                intent.putExtra("city", city);
            } else {
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
            }
            startActivity(intent);
        });

        // Retrofit API call
        WeatherApi api = RetrofitClient.getClient().create(WeatherApi.class);
        String city = getIntent().getStringExtra("city");
        double lat = getIntent().getDoubleExtra("lat", Double.NaN);
        double lon = getIntent().getDoubleExtra("lon", Double.NaN);

        Call<WeatherResponse> call;
        if (city != null && !city.isEmpty()) {
            call = api.getForecastByCity(city, API_KEY, "metric");
        } else if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
            call = api.getForecastByCoords(lat, lon, API_KEY, "metric");
        } else {
            Toast.makeText(this, "No location data provided", Toast.LENGTH_LONG).show();
            return;
        }

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherData = response.body();
                    displayWeather();

                    btnDetails.setEnabled(true);
                    btnDetails.setAlpha(1.0f);
                } else {
                    Toast.makeText(InfoActivity.this, "Failed to get weather data", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                Toast.makeText(InfoActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayWeather() {
        if (weatherData == null || weatherData.list == null || weatherData.list.isEmpty()) {
            Toast.makeText(this, "No forecast data available", Toast.LENGTH_SHORT).show();
            return;
        }

        WeatherItem current = weatherData.list.get(0);
        if (current.main == null) return;

        txtCity.setText(weatherData.city != null ? weatherData.city.name : "Unknown");

        String formattedDate = new SimpleDateFormat("dd MMMM, EEE", Locale.getDefault())
                .format(new Date(current.dt * 1000L));
        txtDate.setText(formattedDate);

        txtTemp.setText(String.format(Locale.getDefault(), "%.0f°C", current.main.temp));

        txtCondition.setText(
                current.weather != null && !current.weather.isEmpty()
                        ? capitalizeWords(current.weather.get(0).description)
                        : "--"
        );

        txtFeelsLike.setText(String.format(Locale.getDefault(), "Feels like %.0f°C", current.main.feels_like));

        double high = current.main.temp_max;
        double low = current.main.temp_min;

        String currentDate = (current.dt_txt != null) ? current.dt_txt.substring(0, 10) : "";
        if (!currentDate.isEmpty()) {
            for (WeatherItem item : weatherData.list) {
                if (item.dt_txt != null && item.dt_txt.startsWith(currentDate) && item.main != null) {
                    high = Math.max(high, item.main.temp_max);
                    low = Math.min(low, item.main.temp_min);
                }
            }
        }

        txtHighLow.setText(String.format(Locale.getDefault(), "H: %.0f° L: %.0f°", high, low));

        if (current.weather != null && !current.weather.isEmpty()) {
            imgWeather.setImageResource(getCustomWeatherIcon(current.weather.get(0).main));
        }

        List<WeatherItem> daily = getFiveDayForecast(weatherData.list);
        recyclerDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerDaily.setAdapter(new DailyAdapter(daily));
    }

    private List<WeatherItem> getFiveDayForecast(List<WeatherItem> list) {
        List<WeatherItem> daily = new ArrayList<>();
        Set<String> seenDates = new HashSet<>();

        for (WeatherItem item : list) {
            if (item.dt_txt != null) {
                String date = item.dt_txt.substring(0, 10);
                if (!seenDates.contains(date) && item.dt_txt.contains("12:00:00")) {
                    seenDates.add(date);
                    daily.add(item);
                    if (daily.size() == 5) break;
                }
            }
        }

        if (daily.size() < 5) {
            seenDates.clear();
            daily.clear();
            for (WeatherItem item : list) {
                if (item.dt_txt != null) {
                    String date = item.dt_txt.substring(0, 10);
                    if (!seenDates.contains(date)) {
                        seenDates.add(date);
                        daily.add(item);
                        if (daily.size() == 5) break;
                    }
                }
            }
        }

        return daily;
    }

    private int getCustomWeatherIcon(String condition) {
        if (condition == null) return R.drawable.cloudy;

        switch (condition.toLowerCase()) {
            case "clear":
                return R.drawable.sunny;
            case "clouds":
                return R.drawable.partly_cloudy;
            case "rain":
            case "drizzle":
                return R.drawable.rainy;
            case "thunderstorm":
                return R.drawable.storm;
            case "snow":
                return R.drawable.snowy;
            case "mist":
            case "fog":
            case "haze":
                return R.drawable.cloudy;
            default:
                return R.drawable.partly_cloudy;
        }
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return "";

        String[] words = text.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            }
        }

        return builder.toString().trim();
    }
}