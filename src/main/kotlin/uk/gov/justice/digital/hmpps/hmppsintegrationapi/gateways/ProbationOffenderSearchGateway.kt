package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

@Component
class ProbationOffenderSearchGateway(@Value("\${services.probation-offender-search.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(pncId: String): Response<Person?> {
    return try {
      val offenders = webClient.requestList<Offender>(
        HttpMethod.POST,
        "/search",
        authenticationHeader(),
        mapOf("pncNumber" to pncId, "valid" to true),
      )

      if (offenders.isEmpty()) {
        Response(
          data = null,
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        )
      } else {
        Response(data = offenders.first().toPerson())
      }
    } catch (exception: WebClientResponseException.BadRequest) {
      Response(
        data = null,
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
            type = UpstreamApiError.Type.BAD_REQUEST,
          ),
        ),
      )
    }
  }

  fun getPersons(firstName: String?, surname: String?): Response<List<Person>> {
    val requestBody = mapOf("firstName" to firstName, "surname" to surname, "valid" to true)
      .filterValues { it != null }

    return Response(
      data = webClient.requestList<Offender>(HttpMethod.POST, "/search", authenticationHeader(), requestBody)
        .map { it.toPerson() },
    )
  }

  fun getAddressesForPerson(pncId: String): Response<List<Address>> {
    val requestBody = mapOf("pncNumber" to pncId, "valid" to true)

    val offender = webClient.requestList<Offender>(HttpMethod.POST, "/search", authenticationHeader(), requestBody)

    if (offender.isEmpty()) {
      return Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }

    return if (offender.first().contactDetails == null) {
      Response(data = emptyList())
    } else if (offender.first().contactDetails!!.addresses == null) {
      Response(data = emptyList())
    } else if (offender.first().contactDetails!!.addresses!!.isNotEmpty()) {
      Response(data = offender.first().contactDetails!!.addresses!!.map { it.toAddress() })
    } else {
      Response(data = emptyList())
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
