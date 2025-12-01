package gr.codelearn.showcase.airline.repository;

import gr.codelearn.showcase.airline.config.PostgresContainerConfig;
import gr.codelearn.showcase.airline.domain.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresContainerConfig.class)
class CustomerRepositoryTest {
	@Autowired
	private CustomerRepository customerRepository;

	@Test
	void saveAndGetByEmail() {
		// Arrange
		Customer customer = new Customer();
		customer.setFullName("John Doe");
		customer.setEmail("john@doe.com");

		// Act
		customerRepository.save(customer);
		var result = customerRepository.findByEmail("john@doe.com");

		// Assert
		assertThat(result).isPresent();
		assertThat(result.get().getFullName()).isEqualTo("John Doe");

	}

	@Test
	void duplicateEmailFails() {
		// Arrange
		Customer c1 = new Customer();
		c1.setFullName("A");
		c1.setEmail("duplicate@test.com");
		customerRepository.save(c1);

		Customer c2 = new Customer();
		c2.setFullName("B");
		c2.setEmail("duplicate@test.com");

		// Act + Assert
		assertThrows(Exception.class, () -> customerRepository.saveAndFlush(c2));
	}

	@Test
	void findByEmailReturnsEmptyForUnknownEmail() {
		// Arrange
		// nothing

		// Act
		var result = customerRepository.findByEmail("missing@test.com");

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void emailLookupIsCaseSensitiveOrNotDependingOnDb() {
		// Arrange
		Customer c = new Customer();
		c.setFullName("Case Test");
		c.setEmail("case@test.com");
		customerRepository.save(c);

		// Act
		var lower = customerRepository.findByEmail("case@test.com");
		var upper = customerRepository.findByEmail("CASE@TEST.COM");

		// Assert
		assertThat(lower).isPresent();

		// Postgres default collation = case-sensitive
		assertThat(upper).isEmpty();
	}
}
