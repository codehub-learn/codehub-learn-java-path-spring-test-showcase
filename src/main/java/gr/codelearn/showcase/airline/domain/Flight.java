package gr.codelearn.showcase.airline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flights")
public class Flight {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 3)
	private String origin;

	@Column(nullable = false, length = 3)
	private String destination;

	@Column(name = "departure_at", nullable = false)
	private ZonedDateTime departureAt;

	@Column(name = "arrival_at", nullable = false)
	private ZonedDateTime arrivalAt;

	@Column(nullable = false)
	private int capacity;
}
