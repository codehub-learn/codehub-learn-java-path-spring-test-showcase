package gr.codelearn.showcase.airline.repository;

import gr.codelearn.showcase.airline.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
	List<Flight> findByOriginAndDestinationAndDepartureAtBetween(
			String origin, String destination, ZonedDateTime from, ZonedDateTime to);
}
