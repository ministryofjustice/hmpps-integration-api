package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials

// A method of authenticating via basic authentication
interface IAuthGateway {
  fun getClientToken(): String
}
