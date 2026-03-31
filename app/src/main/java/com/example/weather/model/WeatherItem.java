package com.example.weather.model;
import java.util.List;
public class WeatherItem {
    public Main main;
    public List<Weather> weather;
    public Wind wind;
    public String dt_txt;
    public long dt;
    public int visibility;
}
