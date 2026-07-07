package uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import java.sql.Connection
import java.sql.DatabaseMetaData
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertTrue

@TestConfiguration
class MockDbConfig {
  @Bean
  @Primary
  fun mockDataSource(): DataSource {
    val mockDataSource = mock(DataSource::class.java)
    val mockConnection = mock(Connection::class.java)
    val mockMetaData = mock(DatabaseMetaData::class.java)

    `when`(mockDataSource.connection).thenReturn(mockConnection)
    `when`(mockConnection.metaData).thenReturn(mockMetaData)
    `when`(mockMetaData.databaseProductName).thenReturn("PostgreSQL")

    return mockDataSource
  }
}

@SpringBootTest(
  properties = [
    "DB_SERVER=dummy-server",
    "DB_NAME=dummy-db",
    "spring.flyway.enabled=false",
  ],
)
@Import(MockDbConfig::class)
class RolesValidationTest {
  @Autowired
  @Qualifier("requestMappingHandlerMapping")
  private lateinit var handlerMapping: RequestMappingHandlerMapping

  @Test
  fun `role endpoints should match actual application mappings`() {
    val validEndpoints: Set<String> =
      handlerMapping.handlerMethods.keys
        .flatMap { mappingInfo ->
          mappingInfo.pathPatternsCondition?.patternValues ?: emptySet()
        }.toSet() + setOf("/health/liveness", "/health/readiness", "/health", "/health/ping", "/info", "/swagger-ui/.*")

    roles.forEach { role ->
      val permissions = role.value.permissions ?: emptyList()

      permissions.forEach { endpoint ->
        assertTrue(
          validEndpoints.contains(endpoint),
          "Validation Failed: Role '${role.key}' contains an invalid endpoint '$endpoint'",
        )
      }
    }
  }
}
