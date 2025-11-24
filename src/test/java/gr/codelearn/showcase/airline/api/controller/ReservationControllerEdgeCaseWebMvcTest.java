package gr.codelearn.showcase.airline.api.controller;

import gr.codelearn.showcase.airline.api.resource.mapper.ReservationMapper;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(ReservationControllerEdgeCaseWebMvcTest.MockBeans.class)
class ReservationControllerEdgeCaseWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ReservationService reservationService;
	@Autowired
	private ReservationMapper reservationMapper;

	@TestConfiguration
	static class MockBeans {
		@Bean
		ReservationService service() {
			return Mockito.mock(ReservationService.class);
		}

		@Bean
		ReservationMapper mapper() {
			return Mockito.mock(ReservationMapper.class);
		}
	}

	@Test
	void reserveFailsOnMissingBody() throws Exception {
		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON))
			   .andExpect(status().isBadRequest());
	}

	@Test
	void reserveFailsOnMalformedJson() throws Exception {
		mockMvc.perform(post("/api/reservations")
								.contentType(MediaType.APPLICATION_JSON)
								.content("{ invalid json"))
			   .andExpect(status().isBadRequest());
	}

	@Test
	void reserveFailsOnInvalidEnum() throws Exception {
		mockMvc.perform(post("/api/reservations")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										     {
										       "flightId": 1,
										       "customerEmail": "a@b.com",
										       "seatClass": "INVALID_CLASS",
										       "seatNumber": "2B"
										     }
										 """))
			   .andExpect(status().isBadRequest());
	}

	@Test
	void actionHeaderIsRequired() throws Exception {
		mockMvc.perform(post("/api/reservations/10"))
			   .andExpect(status().isNotFound());
	}

	@Test
	void reserveFailsWithWrongContentType() throws Exception {
		mockMvc.perform(post("/api/reservations")
								.contentType(MediaType.TEXT_PLAIN)
								.content("flightId=1"))
			   .andExpect(status().isUnsupportedMediaType());
	}

	@Test
	void getFailsOnNegativeId() throws Exception {
		// normally you would add @Validated + @Min(1) to controller parameter
		mockMvc.perform(get("/api/reservations/-1"))
			   .andExpect(status().isBadRequest());
	}

	@Test
	void reserveFailsOnBusinessError() throws Exception {
		when(reservationService.reserve(1L, "a@b.com", SeatClass.ECONOMY, "1A")).thenThrow(new IllegalStateException("Seat already " +
																													 "taken"));

		mockMvc.perform(post("/api/reservations")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										     {
										       "flightId": 1,
										       "customerEmail": "a@b.com",
										       "seatClass": "ECONOMY",
										       "seatNumber": "1A"
										     }
										 """))
			   .andExpect(status().isConflict());
	}

	@Test
	void mapperFailureReturnsServerError() throws Exception {
		Reservation domain = new Reservation();
		domain.setId(5L);

		when(reservationService.get(5L)).thenReturn(Optional.of(domain));
		when(reservationMapper.toResource(domain)).thenThrow(new RuntimeException("Mapper exploded"));

		mockMvc.perform(get("/api/reservations/5"))
			   .andExpect(status().isInternalServerError());
	}

	@Test
	void confirmMissingReservation() throws Exception {
		when(reservationService.confirm(100L)).thenThrow(new RuntimeException("not found"));

		mockMvc.perform(post("/api/reservations/100").header("action", "confirm"))
			   .andExpect(status().isBadRequest());
	}

	@Test
	void cancelIsIdempotent() throws Exception {
		doNothing().when(reservationService).cancel(10L);

		// first call
		mockMvc.perform(post("/api/reservations/10").header("action", "cancel"))
			   .andExpect(status().isNoContent());

		// simulate another call
		mockMvc.perform(post("/api/reservations/10").header("action", "cancel"))
			   .andExpect(status().isNoContent());  // idempotent
	}
}
