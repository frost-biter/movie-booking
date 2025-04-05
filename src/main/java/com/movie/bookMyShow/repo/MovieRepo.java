package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepo extends JpaRepository<Movie, Long> {

    @Query("SELECT DISTINCT s.movie FROM Show s WHERE s.theatre.city.cityId = :cityId")
    List<Movie> findMoviesByCityId(@Param("cityId") Integer cityId);

    boolean existsByMovieName(String movieName);

    @Query("SELECT DISTINCT(m.movieName) FROM Movie m WHERE m.movieId = :movieId")
    String findMovieNameByMovieId(@Param("movieId") int movieId);
}
