package gr.codelearn.showcase.airline.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.codelearn.showcase.airline.api.resource.request.CreateReservationResource;
import gr.codelearn.showcase.airline.config.PostgresContainerConfig;
import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.repository.CustomerRepository;
import gr.codelearn.showcase.airline.repository.FlightRepository;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PostgresContainerConfig.class)
@ActiveProfiles("test")
class ReservationControllerIT {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FlightRepository flightRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private ReservationRepository reservationRepository;

	private Flight flight;
	private Customer customer;

	@BeforeEach
	void setup() {
		reservationRepository.deleteAll();
		flightRepository.deleteAll();
		customerRepository.deleteAll();

		flight = new Flight();
		flight.setOrigin("ATH");
		flight.setDestination("CDG");
		flight.setCapacity(150);
		flight.setDepartureAt(ZonedDateTime.now().plusHours(4));
		flight.setArrivalAt(ZonedDateTime.now().plusHours(6));
		flight = flightRepository.save(flight);

		customer = new Customer();
		customer.setFullName("Integration Tester");
		customer.setEmail("int@test.com");
		customer = customerRepository.save(customer);
	}

	@Test
	void reserveCreatesReservationSuccessfully() throws Exception {
		CreateReservationResource request = new CreateReservationResource(
				flight.getId(),
				customer.getEmail(),
				SeatClass.ECONOMY,
				"21A"
		);

		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.data.id").exists())
			   .andExpect(jsonPath("$.data.seatNumber").value("21A"))
			   .andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Test
	void reserveFailsForDuplicateSeat() throws Exception {
		reservationRepository.save(new Reservation(null, flight, customer, SeatClass.ECONOMY, "8C", BookingStatus.CONFIRMED));

		CreateReservationResource request = new CreateReservationResource(
				flight.getId(),
				customer.getEmail(),
				SeatClass.ECONOMY,
				"8C"
		);

		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
			   .andExpect(status().isNotAcceptable());
	}

	@Test
	void getReturnsReservationIfExists() throws Exception {
		Reservation r = new Reservation();
		r.setFlight(flight);
		r.setCustomer(customer);
		r.setSeatClass(SeatClass.BUSINESS);
		r.setSeatNumber("4B");
		r.setStatus(BookingStatus.CONFIRMED);
		r = reservationRepository.save(r);

		mockMvc.perform(get("/api/reservations/" + r.getId()))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.data.id").value(r.getId()))
			   .andExpect(jsonPath("$.data.seatNumber").value("4B"))
			   .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
	}

	@Test
	void getReturns404WhenMissing() throws Exception {
		mockMvc.perform(get("/api/reservations/999999"))
			   .andExpect(status().isNotFound());
	}

	@Test
	void cancelReturns204() throws Exception {
		Reservation r = new Reservation();
		r.setFlight(flight);
		r.setCustomer(customer);
		r.setSeatClass(SeatClass.ECONOMY);
		r.setSeatNumber("17F");
		r.setStatus(BookingStatus.PENDING);
		r = reservationRepository.save(r);

		mockMvc.perform(post("/api/reservations/" + r.getId()).header("action", "cancel"))
			   .andExpect(status().isNoContent());

		Reservation cancelled = reservationRepository.findById(r.getId()).orElseThrow();
		assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	void cannotCancelAfterDeparture() throws Exception {
		flight.setDepartureAt(ZonedDateTime.now().minusMinutes(5));
		flightRepository.save(flight);

		Reservation r = new Reservation();
		r.setFlight(flight);
		r.setCustomer(customer);
		r.setSeatClass(SeatClass.BUSINESS);
		r.setSeatNumber("19A");
		r.setStatus(BookingStatus.CONFIRMED);
		r = reservationRepository.save(r);

		mockMvc.perform(post("/api/reservations/" + r.getId()).header("action", "cancel"))
			   .andExpect(status().isNotAcceptable());
	}

	@Test
	void unsupportedActionReturns400() throws Exception {
		var r = reservationRepository.save(new Reservation(null, flight, customer, SeatClass.ECONOMY, "33C", BookingStatus.PENDING));
		mockMvc.perform(post("/api/reservations/" + r.getId()).header("action", "invalid"))
			   .andExpect(status().isNotFound());
	}
}
