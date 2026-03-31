package com.example.weather;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.weather.model.CityEntity;
import java.util.List;
@Dao
public interface CityDao {
    @Insert
    void insert(CityEntity city);
    @Query("SELECT * FROM cities")
    List<CityEntity> getAllCities();
    @Query("SELECT * FROM cities WHERE username = :username")
    List<CityEntity> getCitiesByUser(String username);
    @Delete
    void delete(CityEntity city);
    @Query("DELETE FROM cities")
    void deleteAll();
}
