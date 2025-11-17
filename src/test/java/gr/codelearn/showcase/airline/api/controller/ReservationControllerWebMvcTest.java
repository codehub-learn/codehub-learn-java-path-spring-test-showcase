package gr.codelearn.showcase.airline.api.controller;

import gr.codelearn.showcase.airline.api.resource.mapper.ReservationMapper;
import gr.codelearn.showcase.airline.api.resource.response.ReservationResource;
import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.domain.SeatClass;
import gr.codelearn.showcase.airline.service.ReservationService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(ReservationControllerWebMvcTest.MockBeans.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationControllerWebMvcTest {

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

	@Order(1)
	@Test
	void reserveReturns200WithApiResponse() throws Exception {
		Reservation domain = new Reservation();
		domain.setId(10L);

		ReservationResource resource = new ReservationResource(
				10L,
				1L,
				"john@doe.com",
				"3A",
				SeatClass.BUSINESS,
				BookingStatus.PENDING
		);

		when(reservationService.reserve(1L, "john@doe.com", SeatClass.BUSINESS, "3A"))
				.thenReturn(domain);

		when(reservationMapper.toResource(domain)).thenReturn(resource);

		mockMvc.perform(post("/api/reservations")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										 {
										   "flightId": 1,
										   "customerEmail": "john@doe.com",
										   "seatClass": "BUSINESS",
										   "seatNumber": "3A"
										 }
										 """))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.data.id").value(10))
			   .andExpect(jsonPath("$.data.flightId").value(1))
			   .andExpect(jsonPath("$.data.customerEmail").value("john@doe.com"))
			   .andExpect(jsonPath("$.data.seatNumber").value("3A"))
			   .andExpect(jsonPath("$.data.seatClass").value("BUSINESS"))
			   .andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Order(2)
	@Test
	void confirmReturns200WithUpdatedResource() throws Exception {
		Reservation domain = new Reservation();
		domain.setId(10L);

		ReservationResource mapped = new ReservationResource(
				10L,
				1L,
				"john@doe.com",
				"3A",
				SeatClass.BUSINESS,
				BookingStatus.CONFIRMED
		);

		when(reservationService.confirm(10L)).thenReturn(domain);
		when(reservationMapper.toResource(domain)).thenReturn(mapped);

		mockMvc.perform(post("/api/reservations/10").header("action", "confirm"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.data.id").value(10))
			   .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
	}

	@Order(4)
	@Test
	void cancelReturns204() throws Exception {
		doNothing().when(reservationService).cancel(10L);

		mockMvc.perform(post("/api/reservations/10").header("action", "cancel"))
			   .andExpect(status().isNoContent());
	}

	@Order(3)
	@Test
	void getReturns200WithResource() throws Exception {
		Reservation domain = new Reservation();
		domain.setId(5L);

		ReservationResource resource = new ReservationResource(
				5L,
				1L,
				"alice@example.com",
				"4C",
				SeatClass.ECONOMY,
				BookingStatus.CONFIRMED
		);

		when(reservationService.get(5L)).thenReturn(java.util.Optional.of(domain));
		when(reservationMapper.toResource(domain)).thenReturn(resource);

		mockMvc.perform(get("/api/reservations/5"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.data.id").value(5))
			   .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
			   .andExpect(jsonPath("$.data.seatNumber").value("4C"));
	}

	@Order(5)
	@Test
	void getReturns404WhenMissing() throws Exception {
		when(reservationService.get(999L)).thenReturn(java.util.Optional.empty());

		mockMvc.perform(get("/api/reservations/999"))
			   .andExpect(status().isNotFound());
	}
}
