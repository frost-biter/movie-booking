package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.dto.TheatreDTO;
import com.movie.bookMyShow.exception.ResourceNotFoundException;
import com.movie.bookMyShow.model.City;
import com.movie.bookMyShow.model.Theatre;
import com.movie.bookMyShow.repo.CityRepo;
import com.movie.bookMyShow.repo.TheatreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheatreService {
    @Autowired
    private TheatreRepo theatreRepo;

    @Autowired
    private CityRepo cityRepo;

    public List<TheatreDTO> getAllTheatres() {
        return theatreRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TheatreDTO getTheatreById(Long theatreId) {
        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + theatreId));
        return convertToDTO(theatre);
    }

    public List<TheatreDTO> getTheatresByCity(String cityName) {
        City city = cityRepo.findByCityName(cityName)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityName));
        return theatreRepo.findByCity(city).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApiResponse createTheatre(TheatreDTO theatreDTO) {
        if (theatreRepo.existsByTheatreName(theatreDTO.getTheatreName())) {
            return new ApiResponse(HttpStatus.CONFLICT.value(), "Theatre already exists");
        }

        Theatre theatre = new Theatre();
        theatre.setTheatreName(theatreDTO.getTheatreName());
        theatre.setAddress(theatreDTO.getAddress());
        
        City city = cityRepo.findByCityName(theatreDTO.getCity())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + theatreDTO.getCity()));
        theatre.setCity(city);

        theatreRepo.save(theatre);
        return new ApiResponse(HttpStatus.CREATED.value(), "Theatre created successfully");
    }

    @Transactional
    public ApiResponse updateTheatre(Long theatreId, TheatreDTO theatreDTO) {
        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + theatreId));

        if (!theatre.getTheatreName().equals(theatreDTO.getTheatreName()) &&
            theatreRepo.existsByTheatreName(theatreDTO.getTheatreName())) {
            return new ApiResponse(HttpStatus.CONFLICT.value(), "Theatre name already exists");
        }

        theatre.setTheatreName(theatreDTO.getTheatreName());
        theatre.setAddress(theatreDTO.getAddress());
        
        City city = cityRepo.findByCityName(theatreDTO.getCity())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + theatreDTO.getCity()));
        theatre.setCity(city);

        theatreRepo.save(theatre);
        return new ApiResponse(HttpStatus.OK.value(), "Theatre updated successfully");
    }

    @Transactional
    public ApiResponse deleteTheatre(Long theatreId) {
        if (!theatreRepo.existsById(theatreId)) {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Theatre not found");
        }
        theatreRepo.deleteById(theatreId);
        return new ApiResponse(HttpStatus.OK.value(), "Theatre deleted successfully");
    }

    private TheatreDTO convertToDTO(Theatre theatre) {
        TheatreDTO dto = new TheatreDTO();
        dto.setTheatreId(theatre.getTheatreId());
        dto.setTheatreName(theatre.getTheatreName());
        dto.setAddress(theatre.getAddress());
        dto.setCity(theatre.getCity().getCityName());
        return dto;
    }

    public ApiResponse addTheatre(Theatre theatre) {
        if (theatreRepo.existsByTheatreName(theatre.getTheatreName())) {
            return new ApiResponse(409, "Theatre already Exists");
        }
        theatreRepo.save(theatre);
        return new ApiResponse(201, "Theatre added successfully");
    }
}
