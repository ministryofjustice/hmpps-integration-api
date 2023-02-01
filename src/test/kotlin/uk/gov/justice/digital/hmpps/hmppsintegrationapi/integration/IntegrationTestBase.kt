package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonerOffenderSearchApiMockServer

@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  companion object {
    private val nomisApiMockServer = NomisApiMockServer()
    private val prisonerOffenderSearchApiMockServer = PrisonerOffenderSearchApiMockServer()
    private val hmppsAuthMockServer = HmppsAuthMockServer()

    @BeforeAll
    @JvmStatic
    fun startMockServers() {
      nomisApiMockServer.start()
      hmppsAuthMockServer.start()
      prisonerOffenderSearchApiMockServer.start();

      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret")
    }

    @AfterAll
    @JvmStatic
    fun stopMockServers() {
      nomisApiMockServer.stop()
      hmppsAuthMockServer.stop()
      prisonerOffenderSearchApiMockServer.stop()
    }
  }
}
