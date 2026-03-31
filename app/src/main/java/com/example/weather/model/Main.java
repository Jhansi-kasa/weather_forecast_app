package com.example.weather.model;

import com.google.gson.annotations.SerializedName;

public class Main {
    public double temp;
    @SerializedName("feels_like")
    public double feels_like;
    public int humidity;
    public int pressure;
    @SerializedName("temp_min")
    public double temp_min;
    @SerializedName("temp_max")
    public double temp_max;
}
