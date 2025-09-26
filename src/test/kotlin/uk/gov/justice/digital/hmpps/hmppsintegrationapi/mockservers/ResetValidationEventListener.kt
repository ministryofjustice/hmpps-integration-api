package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ServeEventListener
import com.github.tomakehurst.wiremock.stubbing.ServeEvent

/**
 * ResetValidationEventListener. Resets validations before each mockserver request.
 * Required to verify specs after retry scenarios
 */
class ResetValidationEventListener(
  private val openApiValidationListener: OpenApiValidationListener,
) : ServeEventListener {
  override fun getName(): String = "ResetValidationEventListener"

  override fun beforeMatch(
    serveEvent: ServeEvent,
    parameters: Parameters,
  ) {
    openApiValidationListener.reset()
  }
}
