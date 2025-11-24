package gr.codelearn.showcase.airline.repository;

import gr.codelearn.showcase.airline.config.PostgresContainerConfig;
import gr.codelearn.showcase.airline.domain.Flight;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresContainerConfig.class)
class FlightRepositoryTest {
	// @DataJpaTest does NOT support constructor injection of beans. It only supports:
	// @Autowired field injection
	// @Autowired setter injection
	@Autowired
	private FlightRepository flightRepository;

	@Test
	void saveAndFindFlight() {
		// Arrange
		Flight flight = new Flight();
		flight.setOrigin("ATH");
		flight.setDestination("LHR");
		flight.setCapacity(180);
		flight.setDepartureAt(ZonedDateTime.now().plusDays(1));
		flight.setArrivalAt(ZonedDateTime.now().plusDays(1).plusHours(3));

		// Act
		Flight saved = flightRepository.save(flight);
		Flight found = flightRepository.findById(saved.getId()).orElseThrow();

		// Assert
		assertThat(found.getId()).isNotNull();
		assertThat(found.getOrigin()).isEqualTo("ATH");
		assertThat(found.getDestination()).isEqualTo("LHR");

	}
}
