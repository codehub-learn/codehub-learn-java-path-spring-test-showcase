package gr.codelearn.showcase.airline.service;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.exception.BusinessException;
import gr.codelearn.showcase.airline.exception.NotFoundException;
import gr.codelearn.showcase.airline.repository.CustomerRepository;
import gr.codelearn.showcase.airline.repository.FlightRepository;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReservationServiceMockitoTest {
	@Mock
	private FlightRepository flightRepo;
	@Mock
	private CustomerRepository customerRepo;
	@Mock
	private ReservationRepository reservationRepo;

	@InjectMocks
	private ReservationServiceImpl service;

	private final Clock fixedClock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);

	private Flight flight;

	private AutoCloseable closeable;

	@BeforeEach
	void setup() {
		closeable = MockitoAnnotations.openMocks(this);

		flight = new Flight();
		flight.setId(1L);
		flight.setOrigin("ATH");
		flight.setDestination("LHR");
		flight.setDepartureAt(ZonedDateTime.now(fixedClock).plusDays(2));
		flight.setArrivalAt(ZonedDateTime.now(fixedClock).plusDays(2).plusHours(4));
		flight.setCapacity(2);

		// Inject fixed clock manually since no Spring context
		service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	void reserveShouldCreateReservationSuccessfully() {
		Customer customer = new Customer(1L, "John Doe", "john@doe.com");

		when(flightRepo.findById(1L)).thenReturn(Optional.of(flight));
		when(customerRepo.findByEmail("john@doe.com")).thenReturn(Optional.of(customer));
		when(reservationRepo.countByFlightIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
		when(reservationRepo.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.empty());
		when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> {
			Reservation r = inv.getArgument(0);
			r.setId(10L);
			return r;
		});

		var res = service.reserve(1L, "john@doe.com", SeatClass.BUSINESS, "1A");

		assertNotNull(res.getId());
		assertEquals("1A", res.getSeatNumber());
		assertEquals(BookingStatus.PENDING, res.getStatus());
		verify(flightRepo, times(1)).findById(1L);
		verify(reservationRepo, times(1)).save(any());
	}

	@Test
	void reserveShouldThrowWhenSeatAlreadyTaken() {
		when(flightRepo.findById(1L)).thenReturn(Optional.of(flight));
		when(reservationRepo.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.of(new Reservation()));

		assertThrows(BusinessException.class, () ->
				service.reserve(1L, "someone@else.com", SeatClass.ECONOMY, "1A"));

		verify(reservationRepo, never()).save(any());
	}

	@Test
	void reserveShouldThrowWhenFlightDoesNotExist() {
		when(flightRepo.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () ->
				service.reserve(99L, "none@user.com", SeatClass.ECONOMY, "10A"));

		verify(flightRepo, times(1)).findById(99L);
		verifyNoInteractions(customerRepo, reservationRepo);
	}

	@Test
	void confirmShouldChangeStatusToConfirmed() {
		Reservation pending = new Reservation();
		pending.setId(5L);
		pending.setStatus(BookingStatus.PENDING);

		when(reservationRepo.findById(5L)).thenReturn(Optional.of(pending));

		var result = service.confirm(5L);

		assertEquals(BookingStatus.CONFIRMED, result.getStatus());
		verify(reservationRepo).findById(5L);
	}

	@Test
	void confirmShouldThrowIfCancelled() {
		Reservation cancelled = new Reservation();
		cancelled.setId(9L);
		cancelled.setStatus(BookingStatus.CANCELLED);

		when(reservationRepo.findById(9L)).thenReturn(Optional.of(cancelled));

		assertThrows(BusinessException.class, () -> service.confirm(9L));
	}

	@Test
	void cancelShouldUpdateStatusToCancelled() {
		Reservation res = new Reservation();
		res.setId(20L);
		res.setStatus(BookingStatus.PENDING);

		when(reservationRepo.findById(20L)).thenReturn(Optional.of(res));

		service.cancel(20L);

		assertEquals(BookingStatus.CANCELLED, res.getStatus());
		verify(reservationRepo, times(1)).findById(20L);
	}

	@Test
	void cancelShouldThrowIfNotFound() {
		when(reservationRepo.findById(77L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.cancel(77L));
		verify(reservationRepo, times(1)).findById(77L);
	}
}
