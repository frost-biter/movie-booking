package com.movie.bookMyShow.service;

import com.movie.bookMyShow.exception.CityAlreadyExistsException;
import com.movie.bookMyShow.exception.CityNotFoundException;
import com.movie.bookMyShow.model.City;
import com.movie.bookMyShow.repo.CityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityService {
    @Autowired
    private CityRepo cityRepo;
    public Long getIdByCity(String cityName) {
        Long cityId = cityRepo.findCityIdByName(cityName);
        if (cityId == null) {
            throw new CityNotFoundException("City not found: " + cityName);
        }
        return cityId;
    }

//    public ApiResponse addCity(City city) {
//        if (cityRepo.existsByCityName(city.getCityName())) {
//            return new ApiResponse(409, "City already Exists");
//        }
//        cityRepo.save(city);
//        return new ApiResponse(201, "City added successfully");
//    }
    public void addCity(City city) {
        if (cityRepo.existsByCityName(city.getCityName())) {
            throw new CityAlreadyExistsException("City already Exists");
        }
        cityRepo.save(city);
    }
}
