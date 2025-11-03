package gr.codelearn.showcase.airline.service;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.exception.BusinessException;
import gr.codelearn.showcase.airline.exception.NotFoundException;
import gr.codelearn.showcase.airline.testutil.FakeRepositories.FakeCustomerRepository;
import gr.codelearn.showcase.airline.testutil.FakeRepositories.FakeFlightRepository;
import gr.codelearn.showcase.airline.testutil.FakeRepositories.FakeReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationRulesTest {
	private final Clock fixedClock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);

	private Flight createFlight() {
		Flight flight = new Flight();
		flight.setId(1L);
		flight.setOrigin("ATH");
		flight.setDestination("LHR");
		flight.setDepartureAt(ZonedDateTime.now(fixedClock).plusDays(2));
		flight.setArrivalAt(ZonedDateTime.now(fixedClock).plusDays(2).plusHours(4));
		flight.setCapacity(2);
		return flight;
	}

	@Test
	void cannotReservePastFlight() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flight.setDepartureAt(ZonedDateTime.now(fixedClock).minusDays(1));
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		assertThrows(BusinessException.class, () ->
				service.reserve(flight.getId(), "a@b.com", SeatClass.ECONOMY, "1A"));
	}

	@Test
	void canReserveSeatSuccessfully() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);
		var result = service.reserve(flight.getId(), "a@b.com", SeatClass.ECONOMY, "1A");

		assertNotNull(result.getId());
		assertEquals(BookingStatus.PENDING, result.getStatus());
		assertEquals("1A", result.getSeatNumber());
		assertNotNull(result.getCreatedAt());
	}

	@Test
	void seatAlreadyReservedThrowsException() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);
		service.reserve(flight.getId(), "a@b.com", SeatClass.ECONOMY, "1A");

		assertThrows(BusinessException.class, () ->
				service.reserve(flight.getId(), "b@c.com", SeatClass.ECONOMY, "1A"));
	}

	@Test
	void cannotReserveIfFlightIsFull() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flight.setCapacity(1);
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		service.reserve(flight.getId(), "first@user.com", SeatClass.BUSINESS, "1A");
		service.confirm(flight.getId());

		assertThrows(BusinessException.class, () ->
				service.reserve(flight.getId(), "second@user.com", SeatClass.BUSINESS, "1B"));
	}

	@Test
	void customerAutoCreatedIfNotExists() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		assertTrue(customerRepo.findAll().isEmpty());

		service.reserve(flight.getId(), "new@user.com", SeatClass.ECONOMY, "2A");

		assertEquals(1, customerRepo.findAll().size(), "Customer should be auto-created");
	}

	@Test
	void confirmReservationUpdatesStatus() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);
		var res = service.reserve(flight.getId(), "test@user.com", SeatClass.ECONOMY, "2A");

		assertEquals(BookingStatus.PENDING, res.getStatus());

		var confirmed = service.confirm(res.getId());
		assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
	}

	@Test
	void cancelReservationUpdatesStatus() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);
		var res = service.reserve(flight.getId(), "test@user.com", SeatClass.ECONOMY, "2A");

		service.cancel(res.getId());

		var found = reservationRepo.findById(res.getId()).orElseThrow();
		assertEquals(BookingStatus.CANCELLED, found.getStatus());
	}

	@Test
	void getReservationReturnsExisting() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);
		var created = service.reserve(flight.getId(), "exists@user.com", SeatClass.ECONOMY, "3A");

		Optional<Reservation> fetched = service.get(created.getId());

		assertTrue(fetched.isPresent());
		assertEquals(created.getId(), fetched.get().getId());
		assertEquals(created.getCustomer().getEmail(), fetched.get().getCustomer().getEmail());
	}

	@Test
	void confirmThenCancelShouldPersistCorrectly() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();

		Flight flight = createFlight();
		flightRepo.save(flight);

		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		var r = service.reserve(flight.getId(), "lifecycle@user.com", SeatClass.BUSINESS, "5A");
		assertEquals(BookingStatus.PENDING, r.getStatus());

		service.confirm(r.getId());
		assertEquals(BookingStatus.CONFIRMED, reservationRepo.findById(r.getId()).orElseThrow().getStatus());

		service.cancel(r.getId());
		assertEquals(BookingStatus.CANCELLED, reservationRepo.findById(r.getId()).orElseThrow().getStatus());
	}

	@Test
	void reserveThrowsWhenFlightDoesNotExist() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();
		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		assertThrows(NotFoundException.class, () ->
				service.reserve(999L, "no@flight.com", SeatClass.ECONOMY, "10A"));
	}

	@Test
	void confirmThrowsWhenReservationDoesNotExist() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();
		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		assertThrows(NotFoundException.class, () -> service.confirm(999L));
	}

	@Test
	void cancelThrowsWhenReservationDoesNotExist() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();
		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		assertThrows(NotFoundException.class, () -> service.cancel(999L));
	}

	@Test
	void getReturnsEmptyWhenReservationDoesNotExist() {
		var flightRepo = new FakeFlightRepository();
		var customerRepo = new FakeCustomerRepository();
		var reservationRepo = new FakeReservationRepository();
		var service = new ReservationServiceImpl(flightRepo, customerRepo, reservationRepo, fixedClock);

		Optional<Reservation> result = service.get(999L);
		assertTrue(result.isEmpty());
	}
}
