package gr.codelearn.showcase.airline.testutil;

import gr.codelearn.showcase.airline.domain.BookingStatus;
import gr.codelearn.showcase.airline.domain.Customer;
import gr.codelearn.showcase.airline.domain.Flight;
import gr.codelearn.showcase.airline.domain.Reservation;
import gr.codelearn.showcase.airline.repository.CustomerRepository;
import gr.codelearn.showcase.airline.repository.FlightRepository;
import gr.codelearn.showcase.airline.repository.ReservationRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class FakeRepositories {

	// ---------- Fake Flight Repository ----------
	public static class FakeFlightRepository implements FlightRepository {
		private final Map<Long, Flight> flights = new HashMap<>();
		private final AtomicLong idGen = new AtomicLong(1);

		@Override
		public <S extends Flight> S save(S entity) {
			if (entity.getId() == null) {
				entity.setId(idGen.getAndIncrement());
			}
			flights.put(entity.getId(), entity);
			return entity;
		}

		@Override
		public <S extends Flight> S saveAndFlush(S entity) {
			return save(entity);
		}

		@Override
		public <S extends Flight> List<S> saveAllAndFlush(Iterable<S> entities) {
			List<S> list = new ArrayList<>();
			for (S e : entities) {
				list.add(save(e));
			}
			return list;
		}

		@Override
		public void flush() { /* no-op */ }

		@Override
		public void deleteAllInBatch() {
			flights.clear();
		}

		@Override
		public Flight getOne(final Long aLong) {
			return null;
		}

		@Override
		public Flight getById(final Long aLong) {
			return null;
		}

		@Override
		public Flight getReferenceById(final Long aLong) {
			return findById(aLong).orElseThrow(() -> new NoSuchElementException("Reservation not found with id " + aLong));
		}

		@Override
		public <S extends Flight> Optional<S> findOne(final Example<S> example) {
			return Optional.empty();
		}

		@Override
		public <S extends Flight> List<S> findAll(final Example<S> example) {
			return List.of();
		}

		@Override
		public <S extends Flight> List<S> findAll(final Example<S> example, final Sort sort) {
			return List.of();
		}

		@Override
		public <S extends Flight> Page<S> findAll(final Example<S> example, final Pageable pageable) {
			return null;
		}

		@Override
		public <S extends Flight> long count(final Example<S> example) {
			return 0;
		}

		@Override
		public <S extends Flight> boolean exists(final Example<S> example) {
			return false;
		}

		@Override
		public <S extends Flight, R> R findBy(final Example<S> example,
											  final Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<Flight> entities) {
			entities.forEach(e -> flights.remove(e.getId()));
		}

		@Override
		public void deleteAllByIdInBatch(Iterable<Long> ids) {
			ids.forEach(flights::remove);
		}

		@Override
		public Optional<Flight> findById(Long id) {
			return Optional.ofNullable(flights.get(id));
		}

		@Override
		public List<Flight> findByOriginAndDestinationAndDepartureAtBetween(
				String origin, String destination, java.time.ZonedDateTime from, java.time.ZonedDateTime to) {
			return flights.values().stream()
						  .filter(f -> f.getOrigin().equals(origin)
									   && f.getDestination().equals(destination)
									   && !f.getDepartureAt().isBefore(from)
									   && !f.getDepartureAt().isAfter(to))
						  .toList();
		}

		@Override
		public <S extends Flight> List<S> saveAll(final Iterable<S> entities) {
			return List.of();
		}

		@Override
		public List<Flight> findAll() {
			return new ArrayList<>(flights.values());
		}

		@Override
		public List<Flight> findAllById(final Iterable<Long> longs) {
			return List.of();
		}

		@Override
		public void deleteById(Long id) {
			flights.remove(id);
		}

		@Override
		public void delete(Flight entity) {
			flights.remove(entity.getId());
		}

		@Override
		public void deleteAllById(final Iterable<? extends Long> longs) {

		}

		@Override
		public void deleteAll(final Iterable<? extends Flight> entities) {

		}

		@Override
		public void deleteAll() {

		}

		@Override
		public boolean existsById(Long id) {
			return flights.containsKey(id);
		}

		@Override
		public long count() {
			return flights.size();
		}

		@Override
		public List<Flight> findAll(final Sort sort) {
			return new ArrayList<>(flights.values());
		}

		@Override
		public Page<Flight> findAll(final Pageable pageable) {
			return null;
		}
	}

	// ---------- Fake Customer Repository ----------
	public static class FakeCustomerRepository implements CustomerRepository {
		private final Map<String, Customer> customers = new HashMap<>();
		private final AtomicLong idGen = new AtomicLong(1);

		@Override
		public Optional<Customer> findByEmail(String email) {
			return Optional.ofNullable(customers.get(email));
		}

		@Override
		public <S extends Customer> S save(S entity) {
			if (entity.getId() == null) {
				entity.setId(idGen.getAndIncrement());
			}
			customers.put(entity.getEmail(), entity);
			return entity;
		}

		@Override
		public <S extends Customer> S saveAndFlush(S entity) {
			return save(entity);
		}

		@Override
		public <S extends Customer> List<S> saveAllAndFlush(Iterable<S> entities) {
			List<S> list = new ArrayList<>();
			for (S e : entities) {
				list.add(save(e));
			}
			return list;
		}

		@Override
		public void flush() { /* no-op */ }

		@Override
		public void deleteAllInBatch() {
			customers.clear();
		}

		@Override
		public Customer getOne(final Long aLong) {
			return null;
		}

		@Override
		public Customer getById(final Long aLong) {
			return null;
		}

		@Override
		public Customer getReferenceById(final Long aLong) {
			return findById(aLong).orElseThrow(() -> new NoSuchElementException("Reservation not found with id " + aLong));
		}

		@Override
		public <S extends Customer> Optional<S> findOne(final Example<S> example) {
			return Optional.empty();
		}

		@Override
		public <S extends Customer> List<S> findAll(final Example<S> example) {
			return List.of();
		}

		@Override
		public <S extends Customer> List<S> findAll(final Example<S> example, final Sort sort) {
			return List.of();
		}

		@Override
		public <S extends Customer> Page<S> findAll(final Example<S> example, final Pageable pageable) {
			return null;
		}

		@Override
		public <S extends Customer> long count(final Example<S> example) {
			return 0;
		}

		@Override
		public <S extends Customer> boolean exists(final Example<S> example) {
			return false;
		}

		@Override
		public <S extends Customer, R> R findBy(final Example<S> example,
												final Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<Customer> entities) {
			entities.forEach(e -> customers.remove(e.getEmail()));
		}

		@Override
		public void deleteAllByIdInBatch(Iterable<Long> ids) {
			customers.values().removeIf(c -> {
				for (Long id : ids) {
					if (Objects.equals(c.getId(), id)) {
						return true;
					}
				}
				return false;
			});
		}

		@Override
		public <S extends Customer> List<S> saveAll(final Iterable<S> entities) {
			return List.of();
		}

		@Override
		public List<Customer> findAll() {
			return new ArrayList<>(customers.values());
		}

		@Override
		public List<Customer> findAllById(final Iterable<Long> longs) {
			return List.of();
		}

		@Override
		public Optional<Customer> findById(Long id) {
			return customers.values().stream().filter(c -> Objects.equals(c.getId(), id)).findFirst();
		}

		@Override
		public void delete(Customer entity) {
			customers.remove(entity.getEmail());
		}

		@Override
		public void deleteAllById(final Iterable<? extends Long> longs) {

		}

		@Override
		public void deleteAll(final Iterable<? extends Customer> entities) {

		}

		@Override
		public void deleteAll() {

		}

		@Override
		public void deleteById(Long id) {
			customers.values().removeIf(c -> Objects.equals(c.getId(), id));
		}

		@Override
		public boolean existsById(Long id) {
			return customers.values().stream().anyMatch(c -> Objects.equals(c.getId(), id));
		}

		@Override
		public long count() {
			return customers.size();
		}

		@Override
		public List<Customer> findAll(final Sort sort) {
			return List.of();
		}

		@Override
		public Page<Customer> findAll(final Pageable pageable) {
			return null;
		}
	}

	// ---------- Fake Reservation Repository ----------
	public static class FakeReservationRepository implements ReservationRepository {
		private final Map<Long, Reservation> reservations = new HashMap<>();
		private final AtomicLong idGen = new AtomicLong(1);

		@Override
		public long countByFlightIdAndStatus(Long flightId, BookingStatus status) {
			return reservations.values().stream()
							   .filter(r -> Objects.equals(r.getFlight().getId(), flightId)
											&& r.getStatus() == status)
							   .count();
		}

		@Override
		public Optional<Reservation> findByFlightIdAndSeatNumber(Long flightId, String seatNumber) {
			return reservations.values().stream()
							   .filter(r -> Objects.equals(r.getFlight().getId(), flightId)
											&& Objects.equals(r.getSeatNumber(), seatNumber))
							   .findFirst();
		}

		@Override
		public <S extends Reservation> S save(S entity) {
			if (entity.getId() == null) {
				entity.setId(idGen.getAndIncrement());
			}
			reservations.put(entity.getId(), entity);
			return entity;
		}

		@Override
		public <S extends Reservation> S saveAndFlush(S entity) {
			return save(entity);
		}

		@Override
		public <S extends Reservation> List<S> saveAllAndFlush(Iterable<S> entities) {
			List<S> list = new ArrayList<>();
			for (S e : entities) {
				list.add(save(e));
			}
			return list;
		}

		@Override
		public void flush() { /* no-op */ }

		@Override
		public void deleteAllInBatch() {
			reservations.clear();
		}

		@Override
		public Reservation getOne(final Long aLong) {
			return null;
		}

		@Override
		public Reservation getById(final Long aLong) {
			return null;
		}

		@Override
		public Reservation getReferenceById(final Long aLong) {
			return findById(aLong).orElseThrow(() -> new NoSuchElementException("Reservation not found with id " + aLong));
		}

		@Override
		public <S extends Reservation> Optional<S> findOne(final Example<S> example) {
			return Optional.empty();
		}

		@Override
		public <S extends Reservation> List<S> findAll(final Example<S> example) {
			return List.of();
		}

		@Override
		public <S extends Reservation> List<S> findAll(final Example<S> example, final Sort sort) {
			return List.of();
		}

		@Override
		public <S extends Reservation> Page<S> findAll(final Example<S> example, final Pageable pageable) {
			return null;
		}

		@Override
		public <S extends Reservation> long count(final Example<S> example) {
			return 0;
		}

		@Override
		public <S extends Reservation> boolean exists(final Example<S> example) {
			return false;
		}

		@Override
		public <S extends Reservation, R> R findBy(final Example<S> example,
												   final Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<Reservation> entities) {
			entities.forEach(e -> reservations.remove(e.getId()));
		}

		@Override
		public void deleteAllByIdInBatch(Iterable<Long> ids) {
			ids.forEach(reservations::remove);
		}

		@Override
		public Optional<Reservation> findById(Long id) {
			return Optional.ofNullable(reservations.get(id));
		}

		@Override
		public <S extends Reservation> List<S> saveAll(final Iterable<S> entities) {
			return List.of();
		}

		@Override
		public List<Reservation> findAll() {
			return new ArrayList<>(reservations.values());
		}

		@Override
		public List<Reservation> findAllById(final Iterable<Long> longs) {
			return List.of();
		}

		@Override
		public void deleteById(Long id) {
			reservations.remove(id);
		}

		@Override
		public void delete(Reservation entity) {
			reservations.remove(entity.getId());
		}

		@Override
		public void deleteAllById(final Iterable<? extends Long> longs) {

		}

		@Override
		public void deleteAll(final Iterable<? extends Reservation> entities) {

		}

		@Override
		public void deleteAll() {

		}

		@Override
		public boolean existsById(Long id) {
			return reservations.containsKey(id);
		}

		@Override
		public long count() {
			return reservations.size();
		}

		@Override
		public List<Reservation> findAll(final Sort sort) {
			return List.of();
		}

		@Override
		public Page<Reservation> findAll(final Pageable pageable) {
			return null;
		}
	}
}
