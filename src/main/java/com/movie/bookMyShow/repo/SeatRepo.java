package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SeatRepo extends JpaRepository<Seat, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 🔒 Lock seats to prevent simultaneous bookings
    @Query("SELECT s FROM Seat s WHERE s.seatId IN :seatIds")
    List<Seat> findAllById(@Param("seatIds") List<Long> seatIds);

    boolean existsByScreen_ScreenIdAndRowAndSeatNo(Long screenId, Character row, Long seatNo);
}
