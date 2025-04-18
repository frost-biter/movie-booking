package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Screen;
import com.movie.bookMyShow.model.ShowSeat;
import com.movie.bookMyShow.model.Show;
import com.movie.bookMyShow.model.Seat;
import com.movie.bookMyShow.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public interface ShowSeatRepo extends JpaRepository<ShowSeat, Long>{

    @Query("""
    SELECT s FROM Seat s
    WHERE s.screen.screenId = :screenId
    AND s.seatId NOT IN (
        SELECT ss.seat.seatId FROM ShowSeat ss
        WHERE ss.show.showId = :showId
        AND ss.status IN (com.movie.bookMyShow.enums.SeatStatus.BOOKED, com.movie.bookMyShow.enums.SeatStatus.HELD)
    )
    """)
    List<Seat> findAvailableSeats(@Param("showId") Long showId, @Param("screenId") Long screenId);


    @Query("""
        SELECT s FROM Seat s
        WHERE s.screen.screenId = :screenId
          AND s.seatId IN :seatIds
          AND s.seatId NOT IN (
              SELECT ss.seat.seatId FROM ShowSeat ss
              WHERE ss.show.showId = :showId
                AND ss.status IN (com.movie.bookMyShow.enums.SeatStatus.BOOKED, com.movie.bookMyShow.enums.SeatStatus.HELD)
          )
    """)
    List<Seat> findAvailableSelectedSeats(@Param("showId") Long showId,
                                          @Param("screenId") Long screenId,
                                          @Param("seatIds") List<Long> seatIds);

    @Query("SELECT COUNT(ss) > 0 FROM ShowSeat ss WHERE ss.show.showId = :showId AND ss.seat.seatId IN :seatIds AND ss.status = :status")
    boolean existsByShowIdAndSeatIdInAndStatus(@Param("showId") Long showId, 
                                              @Param("seatIds") List<Long> seatIds, 
                                              @Param("status") SeatStatus status);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.showId = :showId AND ss.seat.seatId IN :seatIds")
    List<ShowSeat> findByShowIdAndSeatIdIn(@Param("showId") Long showId, 
                                          @Param("seatIds") List<Long> seatIds);
}
