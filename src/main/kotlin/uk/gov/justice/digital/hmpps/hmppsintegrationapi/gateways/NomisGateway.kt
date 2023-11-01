package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceKeyDates
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Booking
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.ImageDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.OffenceHistoryDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Offender
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.OffenderSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Address as AddressFromNomis
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Alert as AlertFromNomis
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Sentence as SentenceFromNomis

@Component
class NomisGateway(@Value("\${services.prison-api.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    return try {
      webClient.request<Offender>(HttpMethod.GET, "/api/offenders/$id", authenticationHeader()).toPerson()
    } catch (exception: WebClientResponseException.BadRequest) {
      log.error("${exception.message} - ${Json.parseToJsonElement(exception.responseBodyAsString).jsonObject["developerMessage"]}")
      null
    } catch (exception: WebClientResponseException.NotFound) {
      null
    }
  }

  fun getImageMetadataForPerson(id: String): Response<List<ImageMetadata>> {
    return try {
      Response(
        data = webClient.requestList<ImageDetail>(HttpMethod.GET, "api/images/offenders/$id", authenticationHeader())
          .map { it.toImageMetadata() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getImageData(id: Int): Response<ByteArray> {
    return try {
      Response(data = webClient.request(HttpMethod.GET, "/api/images/$id/data", authenticationHeader()))
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = byteArrayOf(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getAddressesForPerson(id: String): Response<List<Address>> {
    return try {
      Response(
        data = webClient.requestList<AddressFromNomis>(
          HttpMethod.GET,
          "/api/offenders/$id/addresses",
          authenticationHeader(),
        ).map { it.toAddress() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getOffencesForPerson(id: String): Response<List<Offence>> {
    return try {
      Response(
        data = webClient.requestList<OffenceHistoryDetail>(
          HttpMethod.GET,
          "/api/bookings/offenderNo/$id/offenceHistory",
          authenticationHeader(),
        ).map { it.toOffence() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getAlertsForPerson(id: String): Response<List<Alert>> {
    return try {
      Response(
        data = webClient.requestList<AlertFromNomis>(
          HttpMethod.GET,
          "/api/offenders/$id/alerts/v2",
          authenticationHeader(),
        ).map { it.toAlert() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getSentencesForBooking(id: Int): Response<List<Sentence>> {
    return try {
      Response(
        data = webClient.requestList<SentenceFromNomis>(
          HttpMethod.GET,
          "/api/offender-sentences/booking/$id/sentences-and-offences",
          authenticationHeader(),
        ).map { it.toSentence() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getBookingIdsForPerson(id: String): Response<List<Booking>> {
    return try {
      Response(
        data = webClient.requestList<Booking>(
          HttpMethod.GET,
          "/api/offender-sentences?offenderNo=$id",
          authenticationHeader(),
        ),
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getSentenceAdjustmentsForPerson(id: String): Response<SentenceAdjustment?> {
    return try {
      Response(
        data = webClient.request<SentenceSummary>(
          HttpMethod.GET,
          "/api/offenders/$id/booking/latest/sentence-summary",
          authenticationHeader(),
        ).latestPrisonTerm.sentenceAdjustments.toSentenceAdjustment(),
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = null,
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getLatestSentenceKeyDatesForPerson(id: String): Response<SentenceKeyDates?> {
    return try {
      Response(
        data = webClient.request<OffenderSentence>(
          HttpMethod.GET,
          "/api/offenders/$id/sentences",
          authenticationHeader(),
        ).sentenceDetail.toSentenceKeyDates(),
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = null,
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NOMIS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
