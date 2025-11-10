package gr.codelearn.showcase.airline.service;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.exception.NotFoundException;
import gr.codelearn.showcase.airline.repository.CustomerRepository;
import gr.codelearn.showcase.airline.repository.FlightRepository;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceAdvancedMockitoTest {
	@Mock
	private FlightRepository flightRepo;
	@Mock
	private CustomerRepository customerRepo;
	@Mock
	private ReservationRepository reservationRepo;

	@Spy
	private Clock clock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);

	@InjectMocks
	private ReservationServiceImpl service;

	private Flight flight;

	@BeforeEach
	void setup() {
		flight = new Flight();
		flight.setId(1L);
		flight.setOrigin("ATH");
		flight.setDestination("LHR");
		flight.setDepartureAt(ZonedDateTime.now(clock).plusDays(2));
		flight.setArrivalAt(ZonedDateTime.now(clock).plusDays(2).plusHours(4));
		flight.setCapacity(2);
	}

	@Test
	void argumentCaptorShouldCaptureReservationValues() {
		Customer customer = new Customer(1L, "John Doe", "john@doe.com");

		when(flightRepo.findById(1L)).thenReturn(Optional.of(flight));
		when(customerRepo.findByEmail("john@doe.com")).thenReturn(Optional.of(customer));
		when(reservationRepo.countByFlightIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
		when(reservationRepo.findByFlightIdAndSeatNumber(1L, "1A")).thenReturn(Optional.empty());
		when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

		service.reserve(1L, "john@doe.com", SeatClass.BUSINESS, "1A");

		verify(reservationRepo).save(captor.capture());
		Reservation captured = captor.getValue();

		assertEquals("1A", captured.getSeatNumber());
		assertEquals(SeatClass.BUSINESS, captured.getSeatClass());
		assertEquals(BookingStatus.PENDING, captured.getStatus());
		assertEquals("john@doe.com", captured.getCustomer().getEmail());
	}

	@Test
	void spyClockShouldBeInvoked() {
		when(flightRepo.findById(1L)).thenReturn(Optional.of(flight));
		when(customerRepo.findByEmail(anyString())).thenReturn(Optional.of(new Customer(1L, "Spy User", "spy@user.com")));
		when(reservationRepo.findByFlightIdAndSeatNumber(anyLong(), anyString())).thenReturn(Optional.empty());
		when(reservationRepo.countByFlightIdAndStatus(anyLong(), any())).thenReturn(0L);
		when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

		service.reserve(1L, "spy@user.com", SeatClass.ECONOMY, "4C");

		verify(clock, atLeastOnce()).instant();
		verify(clock, atLeastOnce()).getZone();
	}

	@Test
	void strictVerificationEnsuresNoExtraInteractions() {
		when(flightRepo.findById(1L)).thenReturn(Optional.of(flight));
		when(customerRepo.findByEmail(anyString())).thenReturn(Optional.of(new Customer(1L, "Strict User", "strict@user.com")));
		when(reservationRepo.findByFlightIdAndSeatNumber(anyLong(), anyString())).thenReturn(Optional.empty());
		when(reservationRepo.countByFlightIdAndStatus(anyLong(), any())).thenReturn(0L);
		when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

		service.reserve(1L, "strict@user.com", SeatClass.ECONOMY, "2B");

		verify(flightRepo).findById(1L);
		verify(customerRepo).findByEmail("strict@user.com");
		verify(reservationRepo).findByFlightIdAndSeatNumber(1L, "2B");
		verify(reservationRepo).countByFlightIdAndStatus(1L, BookingStatus.CONFIRMED);
		verify(reservationRepo).save(any(Reservation.class));
		verifyNoMoreInteractions(flightRepo, customerRepo, reservationRepo);
	}

	@Test
	void verifyNoInteractionsWhenFlightNotFound() {
		// Given: the flight repository returns empty
		when(flightRepo.findById(999L)).thenReturn(Optional.empty());

		// When & Then: reservation attempt throws NotFoundException
		assertThrows(NotFoundException.class, () ->
				service.reserve(999L, "ghost@flight.com", SeatClass.ECONOMY, "9Z"));

		// Verify: flightRepo was queried once, but others were never touched
		verify(flightRepo).findById(999L);
		verifyNoInteractions(customerRepo, reservationRepo);
	}

	@Test
	void confirmShouldUpdateReservationStatusWithSpyVerification() {
		Reservation pending = spy(new Reservation());
		pending.setId(50L);
		pending.setStatus(BookingStatus.PENDING);

		when(reservationRepo.findById(50L)).thenReturn(Optional.of(pending));

		service.confirm(50L);

		verify(pending).setStatus(BookingStatus.CONFIRMED);
		assertEquals(BookingStatus.CONFIRMED, pending.getStatus());
	}

	@Test
	void cancelShouldInvokeCorrectRepositoryMethods() {
		Reservation reservation = new Reservation();
		reservation.setId(99L);
		reservation.setStatus(BookingStatus.PENDING);

		when(reservationRepo.findById(99L)).thenReturn(Optional.of(reservation));

		service.cancel(99L);

		verify(reservationRepo, times(1)).findById(99L);
		assertEquals(BookingStatus.CANCELLED, reservation.getStatus());
	}

	@Test
	void verifyOrderEnsuresCorrectSequence() {
		Reservation reservation = new Reservation();
		reservation.setId(7L);
		reservation.setStatus(BookingStatus.PENDING);
		when(reservationRepo.findById(7L)).thenReturn(Optional.of(reservation));

		service.cancel(7L);

		InOrder order = inOrder(reservationRepo);
		order.verify(reservationRepo).findById(7L);
		order.verifyNoMoreInteractions();
	}

	@Test
	void verifyCorrectOrderAcrossMultipleRepositories() {
		// Given
		Flight flight = new Flight();
		flight.setId(1L);
		flight.setOrigin("ATH");
		flight.setDestination("LHR");
		flight.setCapacity(10);
		flight.setDepartureAt(ZonedDateTime.now(clock).plusDays(1));
		flight.setArrivalAt(ZonedDateTime.now(clock).plusDays(1).plusHours(3));

		Customer customer = new Customer(1L, "Order User", "order@demo.com");

		when(flightRepo.findById(1L)).thenReturn(Optional.of(flight));
		when(customerRepo.findByEmail("order@demo.com")).thenReturn(Optional.of(customer));
		when(reservationRepo.findByFlightIdAndSeatNumber(1L, "2C")).thenReturn(Optional.empty());
		when(reservationRepo.countByFlightIdAndStatus(1L, BookingStatus.CONFIRMED)).thenReturn(0L);
		when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

		// When
		service.reserve(1L, "order@demo.com", SeatClass.ECONOMY, "2C");

		// Then: verify sequence across multiple mocks
		InOrder order = inOrder(flightRepo, customerRepo, reservationRepo);

		order.verify(flightRepo).findById(1L);
		order.verify(customerRepo).findByEmail("order@demo.com");
		order.verify(reservationRepo).findByFlightIdAndSeatNumber(1L, "2C");
		order.verify(reservationRepo).countByFlightIdAndStatus(1L, BookingStatus.CONFIRMED);
		order.verify(reservationRepo).save(any(Reservation.class));
		order.verifyNoMoreInteractions();
	}

}
