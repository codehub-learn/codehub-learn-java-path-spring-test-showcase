package gr.codelearn.showcase.airline.service;

import gr.codelearn.showcase.airline.config.PostgresContainerConfig;
import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.exception.BusinessException;
import gr.codelearn.showcase.airline.repository.CustomerRepository;
import gr.codelearn.showcase.airline.repository.FlightRepository;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresContainerConfig.class)
class ReservationServiceIntegrationTest {
	@Autowired
	private ReservationService reservationService;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private FlightRepository flightRepository;

	@Autowired
	private CustomerRepository customerRepository;

	private Flight flight;
	private Customer customer;

	@BeforeEach
	void setUp() {
		reservationRepository.deleteAll();
		flightRepository.deleteAll();
		customerRepository.deleteAll();

		flight = new Flight();
		flight.setOrigin("ATH");
		flight.setDestination("FRA");
		flight.setCapacity(200);
		flight.setDepartureAt(ZonedDateTime.now().plusHours(4));
		flight.setArrivalAt(ZonedDateTime.now().plusHours(6));
		flight = flightRepository.save(flight);

		customer = new Customer();
		customer.setFullName("Test User");
		customer.setEmail("test@example.com");
		customer = customerRepository.save(customer);
	}

	@Test
	void reserveCreatesPendingReservationForEconomy() {
		// Act
		Reservation r = reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "10A");

		// Assert
		assertNotNull(r.getId());
		assertEquals(SeatClass.ECONOMY, r.getSeatClass());
		assertEquals(BookingStatus.PENDING, r.getStatus());
	}

	@Test
	void reserveAutoConfirmsBusinessClass() {
		// Act
		Reservation r = reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.BUSINESS, "2A");

		// Assert
		assertEquals(BookingStatus.CONFIRMED, r.getStatus());
	}

	@Test
	void reserveFailsIfSeatAlreadyTaken() {
		// Arrange
		reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "10B");

		// Act + Assert
		assertThrows(BusinessException.class,
					 () -> reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "10B"));
	}

	@Test
	void confirmReservationChangesStatusToConfirmed() {
		// Arrange
		Reservation r = reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "11A");

		assertEquals(BookingStatus.PENDING, r.getStatus());

		// Act
		Reservation confirmed = reservationService.confirm(r.getId());

		// Assert
		assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
	}

	@Test
	void confirmFailsIfAlreadyConfirmed() {
		// Arrange
		Reservation r = reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "1A");
		assertEquals(BookingStatus.PENDING, r.getStatus());
		reservationService.confirm(r.getId());

		// Act + Assert
		assertThrows(BusinessException.class, () -> reservationService.confirm(r.getId()));
	}

	@Test
	void cancelSetsStatusToCancelled() {
		// Arrange
		Reservation r = reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "12C");

		// Act
		reservationService.cancel(r.getId());

		// Assert
		Reservation cancelled = reservationRepository.findById(r.getId()).orElseThrow();
		assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
	}

	@Test
	void cannotReserveAfterDeparture() {
		// Arrange
		flight.setDepartureAt(ZonedDateTime.now().minusMinutes(10)); // departed flight
		flight = flightRepository.save(flight);

		// Act + Assert
		assertThrows(BusinessException.class,
					 () -> reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "13A"));
	}

	@Test
	void getReturnsEmptyForUnknownId() {
		assertTrue(reservationService.get(99999L).isEmpty());
	}

	@Test
	void getReturnsReservation() {
		Reservation r = reservationService.reserve(flight.getId(), customer.getEmail(), SeatClass.ECONOMY, "15D");

		assertThat(reservationService.get(r.getId())).isPresent();
	}
}
