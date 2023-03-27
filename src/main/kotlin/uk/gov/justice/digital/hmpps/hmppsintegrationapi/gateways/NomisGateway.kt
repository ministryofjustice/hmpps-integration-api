package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.ImageDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Offender
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Address as AddressFromNomis

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

  fun getImageMetadataForPerson(id: String): List<ImageMetadata> {
    return try {
      webClient.requestList<ImageDetail>(HttpMethod.GET, "api/images/offenders/$id", authenticationHeader())
        .map { it.toImageMetadata() }
    } catch (exception: WebClientResponseException.NotFound) {
      throw EntityNotFoundException("Could not find person with id: $id")
    }
  }

  fun getImageData(id: Int): ByteArray {
    return try {
      webClient.request<ByteArray>(HttpMethod.GET, "/api/images/$id/data", authenticationHeader())
    } catch (exception: WebClientResponseException.NotFound) {
      throw EntityNotFoundException("Could not find image with id: $id")
    }
  }

  fun getAddressesForPerson(id: String): List<Address>? {
    return try {
      webClient.requestList<AddressFromNomis>(HttpMethod.GET, "/api/offenders/$id/addresses", authenticationHeader())
        .map { it.toAddress() }
    } catch (exception: WebClientResponseException.NotFound) {
      null
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
