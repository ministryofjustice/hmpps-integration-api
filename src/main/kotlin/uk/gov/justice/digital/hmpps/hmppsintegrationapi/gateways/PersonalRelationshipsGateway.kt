package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRDetailedContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRLinkedPrisoners
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPaginatedPrisonerContacts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPrisonerContactRestrictions

@Component
class PersonalRelationshipsGateway(
  @Value("\${services.personal-relationships.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)
  private val mapper: ObjectMapper = ObjectMapper()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getLinkedPrisoner(contactId: Long): Response<PRLinkedPrisoners?> {
    val result =
      webClient.request<PRLinkedPrisoners>(
        HttpMethod.GET,
        "/contact/$contactId/linked-prisoners",
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

  fun getPrisonerContactRestrictions(prisonerContactId: Long): Response<PRPrisonerContactRestrictions?> {
    val result =
      webClient.request<PRPrisonerContactRestrictions>(
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

  fun getContactByContactId(contactId: Long): Response<PRDetailedContact?> {
    val result =
      webClient.request<PRDetailedContact>(
        HttpMethod.GET,
        "/contact/$contactId",
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

  fun getContacts(
    prisonerId: String,
    page: Int,
    size: Int,
  ): Response<PRPaginatedPrisonerContacts?> {
    val result =
      webClient.request<PRPaginatedPrisonerContacts?>(
        HttpMethod.GET,
        "/prisoner/$prisonerId/contact?page=${page - 1}&size=$size",
        authenticationHeader(),
        UpstreamApi.PERSONAL_RELATIONSHIPS,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data =
            result.data,
        )
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("PERSONAL-RELATIONSHIPS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
