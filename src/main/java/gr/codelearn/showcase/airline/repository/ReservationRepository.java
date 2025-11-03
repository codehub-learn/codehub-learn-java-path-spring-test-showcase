package gr.codelearn.showcase.airline.repository;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	long countByFlightIdAndStatus(Long flightId, BookingStatus status);

	Optional<Reservation> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}
