package gr.codelearn.showcase.airline.service;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.exception.BusinessException;
import gr.codelearn.showcase.airline.exception.NotFoundException;
import gr.codelearn.showcase.airline.repository.CustomerRepository;
import gr.codelearn.showcase.airline.repository.FlightRepository;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {
	private final FlightRepository flightRepo;
	private final CustomerRepository customerRepo;
	private final ReservationRepository reservationRepo;
	private final Clock clock;

	@Override
	public Reservation reserve(Long flightId, String customerEmail, SeatClass seatClass, String seatNumber) {
		var flight = flightRepo.findById(flightId).orElseThrow(() -> new NotFoundException("Flight not found"));

		if (flight.getDepartureAt().isBefore(ZonedDateTime.now(clock))) {
			throw new BusinessException("Cannot reserve a flight that has already departed.");
		}

		// Check if seat already reserved
		reservationRepo.findByFlightIdAndSeatNumber(flightId, seatNumber).ifPresent(r -> {
			throw new BusinessException("Seat already reserved.");
		});

		// Check flight capacity
		long confirmed = reservationRepo.countByFlightIdAndStatus(flightId, BookingStatus.CONFIRMED);
		if (confirmed >= flight.getCapacity()) {
			throw new BusinessException("Flight capacity reached.");
		}

		// Ensure customer exists
		Customer customer = customerRepo.findByEmail(customerEmail).orElseGet(
				() -> customerRepo.save(new Customer(null, customerEmail, customerEmail)));

		// Create new reservation
		Reservation res = new Reservation();
		res.setFlight(flight);
		res.setCustomer(customer);
		res.setSeatClass(seatClass);
		res.setSeatNumber(seatNumber);
		res.setStatus(BookingStatus.PENDING);
		res.setCreatedAt(ZonedDateTime.now(clock));

		return reservationRepo.save(res);
	}

	@Override
	public Reservation confirm(Long id) {
		Reservation r = reservationRepo.findById(id).orElseThrow(() -> new NotFoundException("Reservation not found."));

		if (r.getStatus() == BookingStatus.CANCELLED) {
			throw new BusinessException("Cannot confirm a cancelled reservation.");
		}

		r.setStatus(BookingStatus.CONFIRMED);
		return r;
	}

	@Override
	public void cancel(Long id) {
		Reservation r = reservationRepo.findById(id).orElseThrow(() -> new NotFoundException("Reservation not found."));

		r.setStatus(BookingStatus.CANCELLED);
	}

	@Override
	public Optional<Reservation> get(Long id) {
		return reservationRepo.findById(id);
	}
}
