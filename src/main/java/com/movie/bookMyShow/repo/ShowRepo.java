package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShowRepo extends JpaRepository<Show, Long> {
    @Query("SELECT COUNT(s) > 0 FROM Show s WHERE s.theatre = :theatre AND " +
            "s.screen = :screen AND " +
            "((:startTime BETWEEN s.startTime AND s.endTime) OR " +
            "(:endTime BETWEEN s.startTime AND s.endTime) OR " +
            "(s.startTime BETWEEN :startTime AND :endTime))")
    boolean existsOverlappingShow(@Param("theatre") Theatre theatre,
                                  @Param("screen") Screen screen,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

}
