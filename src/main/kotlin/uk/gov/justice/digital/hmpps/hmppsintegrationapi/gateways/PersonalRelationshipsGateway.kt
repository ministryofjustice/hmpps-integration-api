package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.LinkedPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PrisonerContactRestrictionsResponse

@Component
class PersonalRelationshipsGateway(
  @Value("http://localhost:4022") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)
  private val mapper: ObjectMapper = ObjectMapper()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getLinkedPrisoner(contactId: Long): Response<List<LinkedPrisoner>> {
    val result =
      webClient.request<List<LinkedPrisoner>>(
        HttpMethod.GET,
        "/contact/$contactId/linked-prisoners",
        authenticationHeader(),
        UpstreamApi.PERSONAL_RELATIONSHIPS,
      )
    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        return Response(
          data = mapToLinkedPrisoner(result),
        )
      }
      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getPrisonerContactRestrictions(prisonerContactId: Long): Response<PrisonerContactRestrictionsResponse?> {
    val result =
      webClient.request<PrisonerContactRestrictionsResponse>(
        HttpMethod.GET,
        "/prisoner-contact/$prisonerContactId/restriction",
        authenticationHeader(),
        UpstreamApi.PERSONAL_RELATIONSHIPS,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        return Response(
          data = result.data,
        )
      }
      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun mapToLinkedPrisoner(result: WebClientWrapper.WebClientWrapperResponse.Success<List<LinkedPrisoner>>): List<LinkedPrisoner> {
    val mappedResult: List<LinkedPrisoner> = mapper.convertValue(result.data, object : TypeReference<List<LinkedPrisoner>>() {})
    return mappedResult
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("PERSONAL-RELATIONSHIPS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
