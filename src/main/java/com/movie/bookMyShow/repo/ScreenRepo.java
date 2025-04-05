package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenRepo extends JpaRepository<Screen,Long> {
    boolean existsByScreenIdAndTheatre(Long screenId, Theatre theatre);
}
