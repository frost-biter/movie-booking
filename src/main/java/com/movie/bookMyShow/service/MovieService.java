package com.movie.bookMyShow.service;

import com.movie.bookMyShow.dto.ApiResponse;
import com.movie.bookMyShow.model.Movie;
import com.movie.bookMyShow.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    @Autowired
    private MovieRepo movieRepo;
    public ApiResponse addMovie(Movie movie) {

        if (movieRepo.existsByMovieName(movie.getMovieName())) {
            return new ApiResponse(409, "Movie already Exists");
        }
        movieRepo.save(movie);
        return new ApiResponse(201, "Movie added successfully");
    }
    public List<Movie> getMovies(Integer cityId) {
        return movieRepo.findMoviesByCityId(cityId);
    }
}
