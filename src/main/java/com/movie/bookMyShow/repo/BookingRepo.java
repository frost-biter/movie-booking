package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, String> {
    Optional<Booking> findByHoldId(String holdId);
}
