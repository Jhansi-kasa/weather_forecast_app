package com.example.weather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;
import com.example.weather.model.Cities;

import java.util.List;
import java.util.Locale;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private final List<Cities> cities;
    private final OnCityClickListener listener;

    // Listener interface
    public interface OnCityClickListener {
        void onCityClick(Cities city);
    }

    public CityAdapter(List<Cities> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        Cities city = cities.get(position);

        holder.txtCity.setText(city.name);
        holder.txtDesc.setText(city.desc);
        holder.txtTemp.setText(String.format(Locale.getDefault(), "%d°C", city.temp));
        holder.txtFeelsLike.setText(String.format(Locale.getDefault(), "Feels: %d°C", city.feelsLike));
        holder.txtHighLow.setText(String.format(Locale.getDefault(), "H: %d° L: %d°", city.high, city.low));

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities == null ? 0 : cities.size();
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCity, txtDesc, txtTemp, txtFeelsLike, txtHighLow;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCity = itemView.findViewById(R.id.txtCity);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtTemp = itemView.findViewById(R.id.txtTemp);
            txtFeelsLike = itemView.findViewById(R.id.txtFeelsLike);
            txtHighLow = itemView.findViewById(R.id.txtHighLow);
        }
    }
}