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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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

	@Test
	void saveFailsWhenOriginIsNull() {
		// Arrange
		Flight flight = new Flight();
		flight.setDestination("LHR");
		flight.setCapacity(180);
		flight.setDepartureAt(ZonedDateTime.now().plusDays(1));
		flight.setArrivalAt(ZonedDateTime.now().plusDays(1).plusHours(3));

		// Act + Assert
		assertThatThrownBy(() -> flightRepository.saveAndFlush(flight)).isInstanceOf(
				Exception.class); // usually DataIntegrityViolationException
	}

	@Test
	void findAllWithPaginationWorks() {
		// Arrange
		for (int i = 0; i < 5; i++) {
			Flight f = new Flight();
			f.setOrigin("ATH");
			f.setDestination("PAR");
			f.setCapacity(100);
			f.setDepartureAt(ZonedDateTime.now().plusDays(1));
			f.setArrivalAt(ZonedDateTime.now().plusDays(1).plusHours(3));
			flightRepository.save(f);
		}

		// Act
		var page = flightRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 2));

		// Assert
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getTotalElements()).isEqualTo(5);
	}

	@Test
	void sortingByDestinationWorks() {
		// Arrange
		Flight f1 = new Flight();
		f1.setOrigin("ATH");
		f1.setDestination("AAA");
		f1.setCapacity(100);
		f1.setDepartureAt(ZonedDateTime.now());
		f1.setArrivalAt(ZonedDateTime.now().plusHours(3));

		Flight f2 = new Flight();
		f2.setOrigin("ATH");
		f2.setDestination("CCC");
		f2.setCapacity(100);
		f2.setDepartureAt(ZonedDateTime.now());
		f2.setArrivalAt(ZonedDateTime.now().plusHours(3));

		Flight f3 = new Flight();
		f3.setOrigin("ATH");
		f3.setDestination("BBB");
		f3.setCapacity(100);
		f3.setDepartureAt(ZonedDateTime.now());
		f3.setArrivalAt(ZonedDateTime.now().plusHours(3));

		flightRepository.save(f1);
		flightRepository.save(f2);
		flightRepository.save(f3);

		var sort = org.springframework.data.domain.Sort.by("destination");

		// Act
		var result = flightRepository.findAll(sort);

		// Assert
		assertThat(result.get(0).getDestination()).isEqualTo("AAA");
		assertThat(result.get(1).getDestination()).isEqualTo("BBB");
		assertThat(result.get(2).getDestination()).isEqualTo("CCC");
	}

	@Test
	void updatingFlightWorks() {
		// Arrange
		Flight flight = new Flight();
		flight.setOrigin("ATH");
		flight.setDestination("LHR");
		flight.setCapacity(100);
		flight.setDepartureAt(ZonedDateTime.now());
		flight.setArrivalAt(ZonedDateTime.now().plusHours(3));

		var saved = flightRepository.save(flight);

		// Act
		saved.setCapacity(150);
		flightRepository.save(saved);

		var updated = flightRepository.findById(saved.getId()).orElseThrow();

		// Assert
		assertThat(updated.getCapacity()).isEqualTo(150);
	}

}
