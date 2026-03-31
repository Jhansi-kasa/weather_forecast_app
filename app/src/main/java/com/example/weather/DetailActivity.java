package com.example.weather;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.api.RetrofitClient;
import com.example.weather.api.WeatherApi;
import com.example.weather.model.*;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    LineChart chartHourly;
    TextView txtHumidity, txtPressure, txtWind, txtSunrise, txtSunset, txtFeelsLike, txtUV, txtVisibility;
    SunArcView sunArcView;

    static final String API_KEY = "57a275716f575222c2319c74740480c0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        chartHourly    = findViewById(R.id.chartHourly);
        txtHumidity    = findViewById(R.id.txtHumidity);
        txtPressure    = findViewById(R.id.txtPressure);
        txtWind        = findViewById(R.id.txtWind);
        txtSunrise     = findViewById(R.id.txtSunrise);
        txtSunset      = findViewById(R.id.txtSunset);
        txtFeelsLike   = findViewById(R.id.txtFeelsLike);
        txtUV          = findViewById(R.id.txtUV);
        txtVisibility  = findViewById(R.id.txtVisibility);
        sunArcView     = findViewById(R.id.sunArcView);

        String city = getIntent().getStringExtra("city");
        double lat  = getIntent().getDoubleExtra("lat", Double.NaN);
        double lon  = getIntent().getDoubleExtra("lon", Double.NaN);

        WeatherApi api = RetrofitClient.getClient().create(WeatherApi.class);
        Call<WeatherResponse> call;

        if (city != null && !city.isEmpty()) {
            call = api.getForecastByCity(city, API_KEY, "metric");
        } else if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
            call = api.getForecastByCoords(lat, lon, API_KEY, "metric");
        } else {
            finish();
            return;
        }

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayData(response.body());
                } else {
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                finish();
            }
        });
    }

    private void displayData(WeatherResponse data) {
        if (data.list == null || data.list.isEmpty()) return;

        List<WeatherItem> hourly = data.list.subList(0, Math.min(8, data.list.size()));
        WeatherItem current = data.list.get(0);

        if (current.main != null) {
            txtHumidity.setText(String.format(Locale.getDefault(), "%d%%", current.main.humidity));
            txtPressure.setText(String.format(Locale.getDefault(), "%d hPa", current.main.pressure));
            txtFeelsLike.setText(String.format(Locale.getDefault(), "%.0f°C", current.main.feels_like));
        }

        if (current.wind != null) {
            txtWind.setText(String.format(Locale.getDefault(), "%.1f m/s", current.wind.speed));
        }

        txtVisibility.setText(String.format(Locale.getDefault(), "%.1f km", current.visibility / 1000f));
        txtUV.setText("Low"); 

        if (data.city != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            txtSunrise.setText(sdf.format(new Date(data.city.sunrise * 1000L)));
            txtSunset.setText(sdf.format(new Date(data.city.sunset * 1000L)));
            sunArcView.setTimes(data.city.sunrise, data.city.sunset);
        }

        setupChart(hourly);
    }

    private void setupChart(List<WeatherItem> hourly) {
        List<Entry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < hourly.size(); i++) {
            WeatherItem item = hourly.get(i);
            if (item.main == null) continue;
            entries.add(new Entry(i, (float) item.main.temp));
            String timeLabel = item.dt_txt != null && item.dt_txt.length() >= 16
                    ? item.dt_txt.substring(11, 16) : (i * 3) + ":00";
            labels.add(timeLabel);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        
        // Fix: Line color to Blue for visibility on white background
        int colorMain = Color.parseColor("#1976D2");
        dataSet.setColor(colorMain);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleColor(colorMain);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(colorMain);
        dataSet.setFillAlpha(30);

        LineData lineData = new LineData(dataSet);
        chartHourly.setData(lineData);
        
        chartHourly.getDescription().setEnabled(false);
        chartHourly.getLegend().setEnabled(false);
        chartHourly.getAxisRight().setEnabled(false);
        chartHourly.getAxisLeft().setTextColor(Color.GRAY);
        chartHourly.getAxisLeft().setGridColor(Color.LTGRAY);
        
        XAxis xAxis = chartHourly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return idx >= 0 && idx < labels.size() ? labels.get(idx) : "";
            }
        });

        chartHourly.setBackgroundColor(Color.WHITE);
        chartHourly.animateX(1000);
        chartHourly.invalidate();
    }
}
