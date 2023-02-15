package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Offender

@Component
class NomisGateway(@Value("\${services.prison-api.base-url}") baseUrl: String) {
  private val webClient: WebClient = WebClient.builder().baseUrl(baseUrl).build()
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.getClientToken("NOMIS")

    return try {
      webClient
        .get()
        .uri("/api/offenders/$id")
        .header("Authorization", "Bearer $token")
        .retrieve()
        .bodyToFlux(Offender::class.java)
        .map { offender -> offender.toPerson() }
        .blockFirst()
    } catch (exception: WebClientResponseException.BadRequest) {
      log.error("${exception.message} - ${Json.parseToJsonElement(exception.responseBodyAsString).jsonObject["developerMessage"]}")
      null
    } catch (exception: WebClientResponseException.NotFound) {
      null
    }
  }

  fun getImageMetadataForPerson(id: String): List<ImageMetadata> {
    return listOf()
  }
}
