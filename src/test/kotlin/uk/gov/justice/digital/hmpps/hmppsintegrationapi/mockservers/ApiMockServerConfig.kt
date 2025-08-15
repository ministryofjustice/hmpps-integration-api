package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

data class ApiMockServerConfig(
  val port: Int,
  val configPath: String? = null,
  val overrideBindType: Boolean = false,
)
