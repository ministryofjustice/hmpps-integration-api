package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext

// A method of authenticating via basic authentication
interface IAuthGateway {
  fun getClientToken(
    service: String,
    requestContext: RequestContext? = null,
  ): String
}
