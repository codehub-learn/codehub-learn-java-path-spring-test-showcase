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
}
