package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes.CNSearchNotesRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes.CNTypeSubType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.OCNCaseNote

@Component
class CaseNotesGateway(
  @Value("\${services.case-notes.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getCaseNotesForPerson(
    id: String,
    filter: CaseNoteFilter,
    caseNoteTypes: List<String>? = null,
  ): Response<OCNCaseNote?> {
    val requestBody =
      CNSearchNotesRequest(
        // date-time format enforced demands format RFC3339, ISO Offset isn't valid apparently
        occurredFrom = filter.startDate?.let { it.toString() + "Z" },
        occurredTo = filter.endDate?.let { it.toString() + "Z" },
        page = filter.page,
        size = filter.size,
        typeSubTypes = caseNoteTypes?.map { CNTypeSubType(it) },
      )

    val result =
      webClient.request<OCNCaseNote>(
        HttpMethod.POST,
        "/search/case-notes/$id",
        authenticationHeader(),
        requestBody = requestBody.toApiConformingMap(),
        upstreamApi = UpstreamApi.CASE_NOTES,
        forbiddenAsError = true,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("CaseNotes")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
