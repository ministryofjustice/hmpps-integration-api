package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.gateways

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.gateways.ProbationIntegrationApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.servers.ProbationIntegrationApiExtension

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationIntegrationApiGateway::class],
)
class ProbationIntegrationApiGatewayTest {
  @Autowired
  lateinit var probationIntegrationApiGateway: ProbationIntegrationApiGateway

  @BeforeEach
  fun setup() {
    ProbationIntegrationApiExtension.server.start()
    ProbationIntegrationApiExtension.server.stubGetPersonIdentifier("mockNomis", "mockCrn")
  }

  @AfterEach
  fun tearDown() {
    ProbationIntegrationApiExtension.server.stop()
  }

  @Test
  fun `Return null if person identifier is not found`() {
    val result = probationIntegrationApiGateway.getPersonIdentifier("otherNomis")

    result.shouldBeNull()
  }

  @Test
  fun `Return person identifier`() {
    val result = probationIntegrationApiGateway.getPersonIdentifier("mockNomis")

    result.shouldNotBeNull()
    result.crn.shouldBe("mockCrn")
  }
}
