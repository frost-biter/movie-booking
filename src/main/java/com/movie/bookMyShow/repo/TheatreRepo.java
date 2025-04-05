package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepo extends JpaRepository<Theatre,Long> {
    @Query("SELECT t FROM Theatre t JOIN t.showList s WHERE t.city.cityId = :cityId AND s.movie.movieId = :movieId")
    List<Theatre> findByCityAndMovie(@Param("cityId") Integer cityId, @Param("movieId") Integer movieId);

    boolean existsByTheatreName(String theatreName);

//    List<Theatre> findByCity_CityIdAndShowList_Movie_MovieId(Integer cityId, Integer movieId);



}
