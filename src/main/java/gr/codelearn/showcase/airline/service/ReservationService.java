package gr.codelearn.showcase.airline.service;

import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;

import java.util.Optional;

public interface ReservationService {
	Reservation reserve(Long flightId, String customerEmail, SeatClass seatClass, String seatNumber);

	Reservation confirm(Long reservationId);

	void cancel(Long reservationId);

	Optional<Reservation> get(Long reservationId);
}
