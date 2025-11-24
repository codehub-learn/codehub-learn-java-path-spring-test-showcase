package gr.codelearn.showcase.airline.repository;

import gr.codelearn.showcase.airline.config.PostgresContainerConfig;
import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
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
class ReservationRepositoryTest {
	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private FlightRepository flightRepository;
	@Autowired
	private CustomerRepository customerRepository;

	@Test
	void saveReservationAndLoadGraph() {
		// Arrange
		Flight flight = new Flight();
		flight.setOrigin("ATH");
		flight.setDestination("FRA");
		flight.setCapacity(100);
		flight.setDepartureAt(ZonedDateTime.now().plusDays(2));
		flight.setArrivalAt(ZonedDateTime.now().plusDays(2).plusHours(2));
		flightRepository.save(flight);

		Customer customer = new Customer();
		customer.setFullName("Alice Wonderland");
		customer.setEmail("alice@wonderland.com");
		customerRepository.save(customer);

		Reservation reservation = new Reservation();
		reservation.setCustomer(customer);
		reservation.setFlight(flight);
		reservation.setSeatClass(SeatClass.BUSINESS);
		reservation.setSeatNumber("2A");
		reservation.setStatus(BookingStatus.PENDING);

		// Act
		Reservation saved = reservationRepository.save(reservation);
		Reservation found = reservationRepository.findById(saved.getId()).orElseThrow();

		// Assert
		assertThat(found.getId()).isNotNull();
		assertThat(found.getCustomer().getEmail()).isEqualTo("alice@wonderland.com");
		assertThat(found.getFlight().getOrigin()).isEqualTo("ATH");
		assertThat(found.getSeatClass()).isEqualTo(SeatClass.BUSINESS);
		assertThat(found.getSeatNumber()).isEqualTo("2A");
	}

	@Test
	void countByFlightIdAndStatusWorks() {
		// Arrange: create flight
		Flight flight = new Flight();
		flight.setOrigin("ATH");
		flight.setDestination("PAR");
		flight.setCapacity(50);
		flight.setDepartureAt(ZonedDateTime.now().plusDays(1));
		flight.setArrivalAt(ZonedDateTime.now().plusDays(1).plusHours(4));
		flightRepository.save(flight);

		// Arrange: create customer
		Customer customer = new Customer();
		customer.setFullName("Bob Menendez");
		customer.setEmail("bob@example.com");
		customerRepository.save(customer);

		// Arrange: create reservation
		Reservation reservation = new Reservation();
		reservation.setFlight(flight);
		reservation.setCustomer(customer);
		reservation.setSeatClass(SeatClass.ECONOMY);
		reservation.setSeatNumber("4D");
		reservation.setStatus(BookingStatus.CONFIRMED);
		reservationRepository.save(reservation);

		// Act
		long count = reservationRepository.countByFlightIdAndStatus(flight.getId(), BookingStatus.CONFIRMED
																   );
		// Assert
		assertThat(count).isEqualTo(1);
	}
}
