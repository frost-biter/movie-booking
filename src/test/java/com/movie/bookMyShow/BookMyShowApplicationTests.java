package com.movie.bookMyShow;

import com.movie.bookMyShow.config.TestInfrastructureConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestInfrastructureConfig.class)
class BookMyShowApplicationTests {

	@Test
	void contextLoads() {
	}

}
