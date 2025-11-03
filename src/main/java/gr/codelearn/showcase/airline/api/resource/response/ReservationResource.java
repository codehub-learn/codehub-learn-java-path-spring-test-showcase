package gr.codelearn.showcase.airline.api.resource.response;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.SeatClass;

public record ReservationResource(
		Long id,
		Long flightId,
		String customerEmail,
		String seatNumber,
		SeatClass seatClass,
		BookingStatus status
) {
}
