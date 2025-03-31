package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes.CNSearchNotesRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisPageCaseNote
import java.time.format.DateTimeFormatter

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
  ): Response<List<CaseNote>> {
    val requestBody =
      CNSearchNotesRequest(
        occurredFrom = filter.startDate?.format(DateTimeFormatter.ISO_DATE),
        occurredTo = filter.endDate?.format(DateTimeFormatter.ISO_DATE),
      )

    val result =
      webClient.request<NomisPageCaseNote>(
        HttpMethod.POST,
        "/search/case-notes/$id",
        authenticationHeader(),
        requestBody = requestBody.toApiConformingMap(),
        upstreamApi = UpstreamApi.CASE_NOTES,
        forbiddenAsError = true,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data.toCaseNotes())
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
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
