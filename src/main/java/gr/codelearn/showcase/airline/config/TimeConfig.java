package gr.codelearn.showcase.airline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {
	/*
	 * Problem -> Solution
	 * Each service manually creates or receives a new clock -> One Spring bean provides a shared, injectable clock
	 * Hard to override during testing -> Mock/Spy a single bean
	 * Inconsistent “now” across services -> All time calculations share the same reference
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
