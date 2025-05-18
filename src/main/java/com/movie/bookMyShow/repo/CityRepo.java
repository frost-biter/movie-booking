package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepo extends JpaRepository<City, Integer> {
    @Query("select cityId from City c where c.cityName = :cityName")
    Long findCityIdByName(@Param("cityName") String cityName);

    boolean existsByCityName(String cityName);

    Optional<City> findByCityName(String cityName);
}
