package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
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

  fun getCaseNotesForPerson(id: String, filter: CaseNoteFilter): Response<List<CaseNote>> {
    val params = getParamFilter(filter)
    val result =
      webClient.request<NomisPageCaseNote>(
        HttpMethod.GET,
        "/case-notes/$id?$params",
        authenticationHeader(),
        UpstreamApi.CASE_NOTES,
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

  private fun getParamFilter(filter: CaseNoteFilter): String {
    val paramFilterMap = mutableMapOf<String, String>().apply {
      filter.locationId?.let { this["locationId"] = filter.locationId }
      filter.startDate?.let { this["startDate"] = filter.startDate.format(DateTimeFormatter.ISO_DATE) }
      filter.endDate?.let { this["endDate"] = filter.endDate.format(DateTimeFormatter.ISO_DATE) }
    }
    return paramFilterMap.entries.joinToString(separator = "&") { (key, value) -> "$key=$value" }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("CaseNotes")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
