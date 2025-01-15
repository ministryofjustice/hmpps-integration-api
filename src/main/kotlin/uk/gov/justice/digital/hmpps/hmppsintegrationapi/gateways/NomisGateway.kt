package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonableAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceKeyDates
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAlert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisBooking
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisImageDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisInmateDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisOffenceHistoryDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisOffenderSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisReasonableAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisReferenceCode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisSentenceSummary

@Component
class NomisGateway(
  @Value("\${services.prison-api.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getImageMetadataForPerson(id: String): Response<List<ImageMetadata>> {
    val result =
      webClient.requestList<NomisImageDetail>(
        HttpMethod.GET,
        "api/images/offenders/$id",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toImageMetadata() }.sortedByDescending { it.captureDateTime })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getImageData(id: Int): Response<ByteArray> {
    val result = webClient.request<ByteArray>(HttpMethod.GET, "/api/images/$id/data", authenticationHeader(), UpstreamApi.NOMIS)

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = byteArrayOf(),
          errors = result.errors,
        )
      }
    }
  }

  fun getAddressesForPerson(id: String): Response<List<Address>> {
    val result =
      webClient.requestList<NomisAddress>(
        HttpMethod.GET,
        "/api/offenders/$id/addresses",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toAddress() }.sortedByDescending { it.startDate })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getOffencesForPerson(id: String): Response<List<Offence>> {
    val result =
      webClient.requestList<NomisOffenceHistoryDetail>(
        HttpMethod.GET,
        "/api/bookings/offenderNo/$id/offenceHistory",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toOffence() }.sortedByDescending { it.startDate })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getAlertsForPerson(id: String): Response<List<Alert>> {
    val result =
      webClient.requestList<NomisAlert>(
        HttpMethod.GET,
        "/api/offenders/$id/alerts/v2",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toAlert() }.sortedByDescending { it.dateCreated })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getSentencesForBooking(id: Int): Response<List<Sentence>> {
    val result =
      webClient.requestList<NomisSentence>(
        HttpMethod.GET,
        "/api/offender-sentences/booking/$id/sentences-and-offences",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toSentence() }.sortedByDescending { it.dateOfSentencing })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getBookingIdsForPerson(id: String): Response<List<NomisBooking>> {
    val result =
      webClient.requestList<NomisBooking>(
        HttpMethod.GET,
        "/api/offender-sentences?offenderNo=$id",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getLatestSentenceAdjustmentsForPerson(id: String): Response<SentenceAdjustment?> {
    val result =
      webClient.request<NomisSentenceSummary>(
        HttpMethod.GET,
        "/api/offenders/$id/booking/latest/sentence-summary",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data =
            result.data.latestPrisonTerm.sentenceAdjustments
              .toSentenceAdjustment(),
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

  fun getLatestSentenceKeyDatesForPerson(id: String): Response<SentenceKeyDates?> {
    val result =
      webClient.request<NomisOffenderSentence>(
        HttpMethod.GET,
        "/api/offenders/$id/sentences",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.sentenceDetail.toSentenceKeyDates())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getRiskCategoriesForPerson(id: String): Response<RiskCategory?> {
    val result =
      webClient.request<NomisInmateDetail>(
        HttpMethod.GET,
        "/api/offenders/$id",
        authenticationHeaderForCategories(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.toRiskCategory())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = RiskCategory(),
          errors = result.errors,
        )
      }
    }
  }

  fun getReasonableAdjustments(booking: String): Response<List<ReasonableAdjustment>> {
    val treatmentCodes = getReferenceDomains("HEALTH_TREAT").data
    val codes = treatmentCodes.map { "type=${it.code}" }.toList()
    val params = codes.joinToString(separator = "&", prefix = "?")
    val result =
      webClient.request<NomisReasonableAdjustments>(
        HttpMethod.GET,
        "/api/bookings/$booking/reasonable-adjustments$params",
        authenticationHeaderForCategories(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.reasonableAdjustments.map { it.toReasonableAdjustment() })
      }
      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getReferenceDomains(domain: String): Response<List<NomisReferenceCode>> {
    val result =
      webClient.requestList<NomisReferenceCode>(
        HttpMethod.GET,
        "/api/reference-domains/domains/$domain/codes",
        authenticationHeaderForCategories(),
        UpstreamApi.NOMIS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }
      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getAccountsForPerson(
    prisonId: String,
    nomisNumber: String?,
  ): Response<NomisAccounts?> {
    val result =
      webClient.request<NomisAccounts>(
        HttpMethod.GET,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/accounts",
        authenticationHeader(),
        UpstreamApi.NOMIS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
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
    val token = hmppsAuthGateway.getClientToken("NOMIS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  private fun authenticationHeaderForCategories(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS")
    val version = "1.0"

    return mapOf(
      "Authorization" to "Bearer $token",
      "version" to version,
    )
  }
}
