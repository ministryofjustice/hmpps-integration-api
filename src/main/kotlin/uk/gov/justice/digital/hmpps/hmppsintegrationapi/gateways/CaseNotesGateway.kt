package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisPageCaseNote

@Component
class CaseNotesGateway(
  @Value("\${services.case-notes.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getCaseNotesForPerson(id: String): Response<PageCaseNote> {
    val result =
      webClient.request<NomisPageCaseNote>(
        HttpMethod.GET,
        "/case-notes/$id",
        authenticationHeader(),
        UpstreamApi.CASE_NOTES,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data.toPageCaseNote())
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = PageCaseNote(null),
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
