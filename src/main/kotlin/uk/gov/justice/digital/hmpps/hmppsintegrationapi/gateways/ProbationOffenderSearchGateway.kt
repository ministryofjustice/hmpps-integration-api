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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

@Component
class ProbationOffenderSearchGateway(@Value("\${services.probation-offender-search.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(pncId: String): Person? {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    return try {
      val offenders = webClient.requestList<Offender>(
        HttpMethod.POST,
        "/search",
        token,
        mapOf("pncNumber" to pncId, "valid" to true)
      )

      if (offenders.isNotEmpty()) offenders.first().toPerson() else null
    } catch (exception: WebClientResponseException.BadRequest) {
      log.error("${exception.message} - ${Json.parseToJsonElement(exception.responseBodyAsString).jsonObject["developerMessage"]}")
      null
    }
  }

  fun getPersons(firstName: String?, surname: String?): List<Person> {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    val requestBody = mapOf("firstName" to firstName, "surname" to surname, "valid" to true)
      .filterValues { it != null }

    return webClient.requestList<Offender>(HttpMethod.POST, "/search", token, requestBody)
      .map { it.toPerson() }
  }

  fun getAddressesForPerson(id: String): List<Address>? {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    val requestBody = mapOf("nomsNumber" to id, "valid" to true)

    val offender = webClient.requestList<Offender>(HttpMethod.POST, "/search", token, requestBody)

    if (offender.isEmpty()) return null

    return if (offender.first().contactDetails.addresses.isNotEmpty())
      offender.first().contactDetails.addresses.map { it.toAddress() }
    else
      listOf()
  }
}
