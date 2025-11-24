package gr.codelearn.showcase.airline.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainerConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	private static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
					.withDatabaseName("airline_test")
					.withUsername("postgres")
					.withPassword("postgres");

	static {
		POSTGRES.start();
	}

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		TestPropertyValues.of("spring.datasource.url=" + POSTGRES.getJdbcUrl(),
							  "spring.datasource.username=" + POSTGRES.getUsername(),
							  "spring.datasource.password=" + POSTGRES.getPassword(),
							  "spring.jpa.hibernate.ddl-auto=create-drop",
							  "spring.jpa.show-sql=false")
						  .applyTo(context.getEnvironment());
	}
}
