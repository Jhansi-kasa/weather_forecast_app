package com.example.weather;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.adapter.CityAdapter;
import com.example.weather.api.RetrofitClient;
import com.example.weather.api.WeatherApi;
import com.example.weather.model.Cities;
import com.example.weather.model.CityEntity;
import com.example.weather.model.WeatherResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class search_city extends AppCompatActivity {

    RecyclerView recyclerView;
    CityAdapter adapter;
    List<Cities> list = new ArrayList<>();
    AppDatabase db;
    EditText etSearch;
    MaterialButton btnAdd;
    String currentUsername;

    static final String API_KEY = "57a275716f575222c2319c74740480c0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "default_user");

        etSearch = findViewById(R.id.etSearch);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerCities);
        db = AppDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CityAdapter(list, city -> {
            Intent intent = new Intent(search_city.this, InfoActivity.class);
            intent.putExtra("city", city.name);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadCities();

        btnAdd.setOnClickListener(v -> {
            String cityName = etSearch.getText().toString().trim();
            if (cityName.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Network Error: Please check your internet connection.", Toast.LENGTH_LONG).show();
                return;
            }
            
            fetchAndAddCity(cityName);
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void fetchAndAddCity(String cityName) {
        WeatherApi api = RetrofitClient.getClient().create(WeatherApi.class);
        api.getForecastByCity(cityName, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final String finalName = response.body().city.name;
                    
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // Strict duplicate check
                        List<CityEntity> existing = db.cityDao().getCitiesByUser(currentUsername);
                        boolean alreadyExists = false;
                        for (CityEntity c : existing) {
                            if (c.name.equalsIgnoreCase(finalName)) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (!alreadyExists) {
                            db.cityDao().insert(new CityEntity(finalName, currentUsername));
                            runOnUiThread(() -> {
                                etSearch.setText("");
                                loadCities();
                                Toast.makeText(search_city.this, finalName + " added successfully!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(search_city.this, finalName + " is already in your list", Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    Toast.makeText(search_city.this, "City not found. Please try another name.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                Toast.makeText(search_city.this, "Unable to reach server. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCities() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CityEntity> dbCities = db.cityDao().getCitiesByUser(currentUsername);
            List<Cities> tempList = new ArrayList<>();

            for (CityEntity c : dbCities) {
                tempList.add(new Cities(c.name, "Refreshing...", 0, 0, 0, 0));
            }

            runOnUiThread(() -> {
                list.clear();
                list.addAll(tempList);
                adapter.notifyDataSetChanged();
                
                for (int i = 0; i < list.size(); i++) {
                    updateCityTemp(i);
                }
            });
        });
    }

    private void updateCityTemp(int position) {
        if (position >= list.size()) return;
        Cities city = list.get(position);
        WeatherApi api = RetrofitClient.getClient().create(WeatherApi.class);
        api.getForecastByCity(city.name, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().list.isEmpty()) {
                    WeatherResponse data = response.body();
                    city.desc = data.list.get(0).weather.get(0).description;
                    city.temp = (int) Math.round(data.list.get(0).main.temp);
                    city.feelsLike = (int) Math.round(data.list.get(0).main.feels_like);
                    city.high = (int) Math.round(data.list.get(0).main.temp_max);
                    city.low = (int) Math.round(data.list.get(0).main.temp_min);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                Log.e("search_city", "Error updating " + city.name, t);
            }
        });
    }
}
