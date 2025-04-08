package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ShowSeatRepo extends JpaRepository<ShowSeat, Long>{
    static List<ShowSeat> findForUpdate(Long showId, List<Long> seatIds) {
        return null;
    }

    List<ShowSeat> findByShowAndStatus(Show show, SeatStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show = :show AND ss.seat IN :seats")
    List<ShowSeat> findLockedSeats(@Param("show") Show show, @Param("seats") List<Seat> seats);

    Optional<ShowSeat> findByShowAndSeat(Show show, Seat seat);

    List<Seat> findAvailableSeatsByShowId(Long showId);
}
