package dev.test.social_login;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SocialLoginApplicationTests {

	@Test
	void contextLoads() {
		// Test to ensure the Spring application context loads successfully
	}

	@Test
	void exampleTest() {
		// Example simple test
		int sum = 3 + 2;
		assertEquals(6, sum);
	}
}