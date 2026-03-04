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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.gateways.PrisonerSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.servers.PrisonerSearchMockServer

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerSearchGateway::class],
)
class PrisonerSearchGatewayTest {
  @Autowired
  lateinit var prisonerSearchGateway: PrisonerSearchGateway

  val mockServer = PrisonerSearchMockServer()

  @BeforeEach
  fun setup() {
    mockServer.start()
    mockServer.stubGetPrisoner("mockNomis")
  }

  @AfterEach
  fun tearDown() {
    mockServer.stop()
  }

  @Test
  fun `Return null if prisoner is not found`() {
    val result = prisonerSearchGateway.getPrisoner("otherNomis")

    result.shouldBeNull()
  }

  @Test
  fun `Return prisoner`() {
    val result = prisonerSearchGateway.getPrisoner("mockNomis")

    result.shouldNotBeNull()
    result.prisonId.shouldBe("MDI")
  }
}
