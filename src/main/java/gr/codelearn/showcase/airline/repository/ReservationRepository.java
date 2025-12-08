package gr.codelearn.showcase.airline.repository;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	long countByFlightIdAndStatus(Long flightId, BookingStatus status);

	Optional<Reservation> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

	@EntityGraph(attributePaths = {"flight", "customer"})
	@Query("SELECT r FROM Reservation r WHERE r.id = :id")
	Optional<Reservation> getFullReservation(Long id);
}
