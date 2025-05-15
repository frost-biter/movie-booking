package com.movie.bookMyShow.repo;

import com.movie.bookMyShow.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRecordRepo extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findByHoldId(String holdId);
    List<PaymentRecord> findByPhoneNumber(String phoneNumber);
    List<PaymentRecord> findByShowId(Long showId);
    List<PaymentRecord> findByStatus(String status);
} 