package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

@Component
class ProbationOffenderSearchGateway(@Value("\${services.probation-offender-search.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway
  fun getPerson(id: String? = null): Response<Person?> {
    val queryField = if (isPncNumber(id)) {
      "pncNumber"
    } else {
      "crn"
    }

    val result = webClient.requestListWithErrorHandling<Offender>(
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
          data = person,
          errors = if (person == null) {
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

  fun getPersons(firstName: String?, surname: String?, searchWithinAliases: Boolean = false): Response<List<Person>> {
    val requestBody = mapOf("firstName" to firstName, "surname" to surname, "includeAliases" to searchWithinAliases)
      .filterValues { it != null }

    return Response(
      data = webClient.requestList<Offender>(HttpMethod.POST, "/search", authenticationHeader(), requestBody)
        .map { it.toPerson() },
    )
  }

  fun getAddressesForPerson(hmppsId: String): Response<List<Address>> {
    val queryField = if (isPncNumber(hmppsId)) {
      "pncNumber"
    } else {
      "crn"
    }

    val offender = webClient.requestList<Offender>(HttpMethod.POST, "/search", authenticationHeader(), mapOf(queryField to hmppsId))
    val addresses = offender.firstOrNull()?.contactDetails?.addresses.orEmpty()

    return if (addresses.isNullOrEmpty()) {
      Response(data = emptyList())
    } else {
      Response(data = addresses.map { it.toAddress() })
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
