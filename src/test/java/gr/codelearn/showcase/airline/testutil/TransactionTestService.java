package gr.codelearn.showcase.airline.testutil;

import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionTestService {
	private final ReservationRepository reservationRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createReservationAndFail(Reservation reservation) {
		reservationRepository.saveAndFlush(reservation);
		throw new RuntimeException("force rollback");
	}
}
