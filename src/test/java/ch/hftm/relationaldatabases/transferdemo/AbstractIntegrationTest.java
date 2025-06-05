package ch.hftm.relationaldatabases.transferdemo;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
@Testcontainers
public abstract class AbstractIntegrationTest {
  private static final String POSTGRES_VERSION = "postgres:17";
  public static PostgreSQLContainer<?> postgreSQLContainer;

  static {
    postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_VERSION))
      .withDatabaseName("transferdemo")
      .withUsername("transferdemo")
      .withPassword("transferdemo");
    postgreSQLContainer.start();
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
        configurableApplicationContext,
        "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
        "spring.datasource.username=" + postgreSQLContainer.getUsername(),
        "spring.datasource.password=" + postgreSQLContainer.getPassword()
      );
    }
  }
}
