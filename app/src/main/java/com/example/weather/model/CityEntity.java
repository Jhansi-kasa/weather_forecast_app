package com.example.weather.model;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "cities")
public class CityEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String username;
    public CityEntity(String name, String username) {
        this.name = name;
        this.username = username;
    }
}
