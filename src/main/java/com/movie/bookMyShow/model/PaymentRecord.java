package com.movie.bookMyShow.model;

import com.movie.bookMyShow.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_records")
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String holdId;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime attemptTime;

    @Column
    private LocalDateTime completionTime;

    @Column
    private String transactionId;

    @Column
    private Boolean isReverted;

    @Column
    private LocalDateTime revertedTime;
} 