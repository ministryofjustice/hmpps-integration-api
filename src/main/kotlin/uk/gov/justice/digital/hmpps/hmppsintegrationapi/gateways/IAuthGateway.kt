package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

// A method of authenticating via basic authentication
interface IAuthGateway {
  fun getClientToken(service: String): String
}
