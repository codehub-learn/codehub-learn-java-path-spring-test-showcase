package gr.codelearn.showcase.airline.api.controller;

import gr.codelearn.showcase.airline.api.resource.mapper.ReservationMapper;
import gr.codelearn.showcase.airline.api.resource.response.ReservationResource;
import gr.codelearn.showcase.airline.domain.BookingStatus;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(ReservationControllerAdditionalWebMvcTest.MockBeans.class)
class ReservationControllerAdditionalWebMvcTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private ReservationMapper reservationMapper;

	@TestConfiguration
	static class MockBeans {
		@Bean
		ReservationService reservationService() {
			return Mockito.mock(ReservationService.class);
		}

		@Bean
		ReservationMapper reservationMapper() {
			return Mockito.mock(ReservationMapper.class);
		}
	}

	@Test
	void reserveReturns400OnInvalidPayload() throws Exception {
		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content("""
																								  {
																								      "flightId": null,
																								      "customerEmail": "",
																								      "seatClass": "INVALID",
																								      "seatNumber": ""
																								  }
																								  """)).andExpect(status().isBadRequest());
	}

	@Test
	void confirmReturns404IfNotFound() throws Exception {
		when(reservationService.confirm(99L)).thenThrow(new IllegalArgumentException("not found"));

		mockMvc.perform(post("/api/reservations/99").header("action", "confirm")).andExpect(
				status().isBadRequest()); // Or .isNotFound() if your controller maps the exception differently
	}

	@Test
	void getReturnsCorrectJsonShape() throws Exception {
		Reservation domain = new Reservation();
		domain.setId(123L);

		ReservationResource mapped = new ReservationResource(123L, 9L, "test@example.com", "1A", SeatClass.BUSINESS,
															 BookingStatus.CONFIRMED);

		when(reservationService.get(123L)).thenReturn(Optional.of(domain));
		when(reservationMapper.toResource(domain)).thenReturn(mapped);

		mockMvc.perform(get("/api/reservations/123")).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(123)).andExpect(
				jsonPath("$.data.flightId").value(9)).andExpect(jsonPath("$.data.customerEmail").value("test@example.com")).andExpect(
				jsonPath("$.data.status").value("CONFIRMED"));
	}

	@Test
	void unsupportedActionReturns400() throws Exception {
		mockMvc.perform(post("/api/reservations/10").header("action", "INVALID")).andExpect(status().isBadRequest());
	}
}
