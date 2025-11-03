package gr.codelearn.showcase.airline.api.resource.request;

import gr.codelearn.showcase.airline.domain.SeatClass;

public record CreateReservationResource(
		Long flightId,
		String customerEmail,
		SeatClass seatClass,
		String seatNumber
) {
}
