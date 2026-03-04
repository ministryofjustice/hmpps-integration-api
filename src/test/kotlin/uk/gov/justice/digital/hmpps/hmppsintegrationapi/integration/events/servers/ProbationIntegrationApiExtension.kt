package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.servers

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ProbationIntegrationApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  override fun afterAll(context: ExtensionContext): Unit = server.stop()

  override fun beforeAll(context: ExtensionContext): Unit = server.start()

  override fun beforeEach(context: ExtensionContext): Unit = server.resetRequests()

  companion object {
    val server = ProbationIntegrationApiMockServer()
  }
}
