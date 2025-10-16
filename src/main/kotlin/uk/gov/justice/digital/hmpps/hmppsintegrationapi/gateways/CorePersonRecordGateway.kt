package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Component
class CorePersonRecordGateway(
  @Value("\${services.core-person-record.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("CORE_PERSON_RECORD")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  /**
   * This function showld always return a core person record
   * Any exceptions should be thrown at this point and caught at the HmppsIntegrationApiExceptionHandler
   * There is no point passing these exceptions all the way up to a controller just to be thrown and caught by the HmppsIntegrationApiExceptionHandler
   */
  fun corePersonRecordFor(
    cprType: String, // prison or probation
    hmppsId: String,
  ): CorePersonRecord {
    val uri = "/person/$cprType/$hmppsId"
    val result =
      webClient.request<CorePersonRecord>(
        HttpMethod.GET,
        uri,
        authenticationHeader(),
        UpstreamApi.CORE_PERSON_RECORD,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        return result.data
      }
      is WebClientWrapperResponse.Error -> {
        when (result.errors.map { it.type }.firstOrNull()) {
          UpstreamApiError.Type.BAD_REQUEST -> throw ValidationException("Invalid request to core person record $uri.")
          UpstreamApiError.Type.ENTITY_NOT_FOUND -> throw EntityNotFoundException("Could not find core person record at $uri")
          else -> throw RuntimeException("Error retrieving core person record from $uri with error ${result.errors.map { it.type }.joinToString(",")}")
        }
      }
    }
  }
}
