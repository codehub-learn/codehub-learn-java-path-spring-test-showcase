package gr.codelearn.showcase.airline.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "reservations",
	   uniqueConstraints = @UniqueConstraint(name = "uk_flight_seat", columnNames = {"flight_id", "seat_number"}))
public class Reservation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_id")
	private Flight flight;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@Enumerated(EnumType.STRING)
	@Column(name = "seat_class", nullable = false)
	private SeatClass seatClass;

	@Column(name = "seat_number", nullable = false)
	private String seatNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status = BookingStatus.PENDING;

	@Version
	private long version;

	@Column(name = "created_at", nullable = false)
	private ZonedDateTime createdAt = ZonedDateTime.now();
}
