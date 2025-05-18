package com.movie.bookMyShow.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"book-movie"})
public class TestInfrastructureConfig {
    // Using mocks from TestConfig instead of real Redis
} 