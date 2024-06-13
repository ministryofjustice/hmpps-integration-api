package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.ContactDetailsWithAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

@Component
class ProbationOffenderSearchGateway(
  @Value("\${services.probation-offender-search.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getOffender(id: String? = null): Response<Offender?> {
    val queryField =
      if (isPncNumber(id)) {
        "pncNumber"
      } else {
        "crn"
      }

    val result =
      webClient.requestList<Offender>(
        HttpMethod.POST,
        "/search",
        authenticationHeader(),
        UpstreamApi.PROBATION_OFFENDER_SEARCH,
        mapOf(queryField to id),
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val persons = result.data
        val person = persons.firstOrNull()?.toPerson()

        Response(
          data = persons.firstOrNull(),
          errors =
            if (person == null) {
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              )
            } else {
              emptyList()
            },
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

  fun getPerson(id: String? = null): Response<Person?> {
    val offender = getOffender(id)
    return Response(data = offender.data?.toPerson(), errors = offender.errors)
  }

  fun getPersons(
    firstName: String?,
    surname: String?,
    pncNumber: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<Person>> {
    val requestBody =
      mapOf(
        "firstName" to firstName,
        "surname" to surname,
        "pncNumber" to pncNumber,
        "dateOfBirth" to dateOfBirth,
        "includeAliases" to searchWithinAliases,
      )
        .filterValues { it != null }

    val result =
      webClient.requestList<Offender>(
        HttpMethod.POST,
        "/search",
        authenticationHeader(),
        UpstreamApi.PROBATION_OFFENDER_SEARCH,
        requestBody,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = result.data.map { it.toPerson() }.sortedBy { it.dateOfBirth },
        )
      }
      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getAddressesForPerson(hmppsId: String): Response<List<Address>> {
    val result =
      webClient.requestList<ContactDetailsWithAddress>(
        HttpMethod.POST,
        "/search",
        authenticationHeader(),
        UpstreamApi.PROBATION_OFFENDER_SEARCH,
        mapOf("crn" to hmppsId),
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        return Response(result.data.firstOrNull()?.contactDetails?.addresses.orEmpty().map { it.toAddress() })
      }
      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  private fun isPncNumber(id: String?): Boolean {
    return id?.matches(Regex("^[0-9]+/[0-9A-Za-z]+$")) == true
  }
}
