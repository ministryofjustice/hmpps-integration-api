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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
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
    val result = webClient.requestListWithErrorHandling<ImageDetail>(HttpMethod.GET, "api/images/offenders/$id", authenticationHeader(), UpstreamApi.NOMIS)

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toImageMetadata() })
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
    val result = webClient.requestWithErrorHandling<ByteArray>(HttpMethod.GET, "/api/images/$id/data", authenticationHeader(), UpstreamApi.NOMIS)

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
    val result = webClient.requestListWithErrorHandling<AddressFromNomis>(
      HttpMethod.GET,
      "/api/offenders/$id/addresses",
      authenticationHeader(),
      UpstreamApi.NOMIS,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toAddress() })
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
    val result = webClient.requestListWithErrorHandling<OffenceHistoryDetail>(
      HttpMethod.GET,
      "/api/bookings/offenderNo/$id/offenceHistory",
      authenticationHeader(),
      UpstreamApi.NOMIS,
    )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toOffence() })
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
    val result = webClient.requestListWithErrorHandling<AlertFromNomis>(
      HttpMethod.GET,
      "/api/offenders/$id/alerts/v2",
      authenticationHeader(),
      UpstreamApi.NOMIS,
    )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toAlert() })
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
    val result = webClient.requestListWithErrorHandling<SentenceFromNomis>(
      HttpMethod.GET,
      "/api/offender-sentences/booking/$id/sentences-and-offences",
      authenticationHeader(),
      UpstreamApi.NOMIS,
    )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toSentence() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getBookingIdsForPerson(id: String): Response<List<Booking>> {
    val result = webClient.requestListWithErrorHandling<Booking>(
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
    val result = webClient.requestWithErrorHandling<SentenceSummary>(
      HttpMethod.GET,
      "/api/offenders/$id/booking/latest/sentence-summary",
      authenticationHeader(),
      UpstreamApi.NOMIS,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.latestPrisonTerm.sentenceAdjustments.toSentenceAdjustment())
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
    val result = webClient.requestWithErrorHandling<OffenderSentence>(
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

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
