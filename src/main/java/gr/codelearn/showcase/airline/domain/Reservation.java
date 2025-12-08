package gr.codelearn.showcase.airline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "reservations",
	   uniqueConstraints = @UniqueConstraint(name = "uk_flight_seat", columnNames = {"flight_id", "seat_number"}))
public class Reservation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_flight"))
	private Flight flight;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_customer"))
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

	public Reservation(final Long id, final Flight flight, final Customer customer, final SeatClass seatClass, final String seatNumber,
					   final BookingStatus status) {
		this.id = id;
		this.flight = flight;
		this.customer = customer;
		this.seatClass = seatClass;
		this.seatNumber = seatNumber;
		this.status = status;
	}
}
