package com.example.weather.model;
public class Cities {
    public String name, desc;
    public int temp, feelsLike, high, low;
    public Cities(String name, String desc, int temp, int feelsLike, int high, int low) {
        this.name = name;
        this.desc = desc;
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.high = high;
        this.low = low;
    }
}
