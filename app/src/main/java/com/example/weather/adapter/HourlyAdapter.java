package com.example.weather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weather.R;
import com.example.weather.model.WeatherItem;

import java.util.List;
import java.util.Locale;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
    private final List<WeatherItem> list;

    public HourlyAdapter(List<WeatherItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherItem item = list.get(position);

        // Show HH:mm from "2024-03-22 09:00:00"
        String timeStr = item.dt_txt != null && item.dt_txt.length() >= 16
                ? item.dt_txt.substring(11, 16) : "";
        holder.time.setText(timeStr);
        holder.temp.setText(String.format(Locale.getDefault(), "%.0f°C", item.main.temp));

        if (item.weather != null && !item.weather.isEmpty()) {
            String iconUrl = "https://openweathermap.org/img/wn/"
                    + item.weather.get(0).icon + ".png";
            Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.icon);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time, temp;
        ImageView icon;

        ViewHolder(View view) {
            super(view);
            time = view.findViewById(R.id.time);
            temp = view.findViewById(R.id.temp);
            icon = view.findViewById(R.id.icon);
        }
    }
}
