package com.example.weather.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.weather.R;
import com.example.weather.model.WeatherItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ViewHolder> {
    private final List<WeatherItem> list;

    public DailyAdapter(List<WeatherItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherItem item = list.get(position);

        // Format date
        if (item.dt_txt != null && item.dt_txt.length() >= 10) {
            try {
                SimpleDateFormat sdfIn   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat sdfDay  = new SimpleDateFormat("EEE", Locale.getDefault());
                SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM", Locale.getDefault());
                Date date = sdfIn.parse(item.dt_txt);
                if (date != null) {
                    holder.day.setText(sdfDay.format(date));
                    holder.date.setText(sdfDate.format(date));
                }
            } catch (Exception e) {
                holder.day.setText(item.dt_txt.substring(0, 10));
                holder.date.setText("");
            }
        }

        // ✅ Only set views that exist in XML
        if (item.main != null) {
            holder.day_temp.setText(String.format(Locale.getDefault(),
                    "Day: %.0f°C", item.main.temp_max));
            holder.night_temp.setText(String.format(Locale.getDefault(),
                    "Night: %.0f°C", item.main.temp_min));
        }

        // ✅ Set icon based on condition — removed holder.condition.setText()
        if (item.weather != null && !item.weather.isEmpty()) {
            String condition = item.weather.get(0).main;

            switch (condition.toLowerCase()) {
                case "clear":
                    holder.icon.setImageResource(R.drawable.sunny);
                    break;
                case "clouds":
                    holder.icon.setImageResource(R.drawable.cloudy);
                    break;
                case "rain":
                case "drizzle":
                    holder.icon.setImageResource(R.drawable.rainy);
                    break;
                case "snow":
                    holder.icon.setImageResource(R.drawable.snowy);
                    break;
                case "thunderstorm":
                    holder.icon.setImageResource(R.drawable.storm);
                    break;
                default:
                    holder.icon.setImageResource(R.drawable.partly_cloudy);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView day, date, day_temp, night_temp;
        ImageView icon;

        ViewHolder(View view) {
            super(view);
            day        = view.findViewById(R.id.day);
            date       = view.findViewById(R.id.date);
            day_temp   = view.findViewById(R.id.day_temp);
            night_temp = view.findViewById(R.id.night_temp);
            icon       = view.findViewById(R.id.icon);
            // ✅ Removed temp and condition — not in XML
        }
    }
}