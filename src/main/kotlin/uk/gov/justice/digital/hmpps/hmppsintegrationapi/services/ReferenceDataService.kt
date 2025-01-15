package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import ReferenceData
import ReferenceDataItem
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.getOrError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisReferenceCode

@Service
class ReferenceDataService(
  @Value("\${services.ndelius.base-url}") deliusBaseUrl: String,
  @Value("\${services.prison-api.base-url}") prisonBaseUrl: String,
  private val hmppsAuthGateway: HmppsAuthGateway,
) {
  private val deliusWebClient = WebClientWrapper(deliusBaseUrl)
  private val prisonApiWebclient = WebClientWrapper(prisonBaseUrl)

  fun referenceData(): Response<ReferenceData?> {
    val probationReferenceData =
      deliusWebClient
        .request<ReferenceData>(
          HttpMethod.GET,
          "/reference-data",
          authHeader(),
          UpstreamApi.NDELIUS,
        ).getOrError { (errors) -> return Response(null, errors = listOf(errors)) }
        .probationReferenceData

    val prisonReferenceData =
      NomisReferenceDataType.entries
        .flatMap {
          val rd = prisonReferenceData(it.name)
          if (rd.errors.isNotEmpty()) {
            return Response(data = null, errors = rd.errors)
          }
          rd.data!!
        }.groupByTo(LinkedHashMap(), { NomisReferenceDataType.valueOf(it.domain!!).category }, { ReferenceDataItem(it.code!!, it.description!!) })

    return Response(data = ReferenceData(prisonReferenceData, probationReferenceData))
  }

  private fun prisonReferenceData(domain: String): Response<List<NomisReferenceCode>?> {
    val prisonReferenceData =
      prisonApiWebclient
        .requestList<NomisReferenceCode>(
          HttpMethod.GET,
          "/api/reference-domains/domains/$domain",
          prisonAuthHeader(),
          UpstreamApi.NOMIS,
        ).getOrError { (errors) -> return Response(null, errors = listOf(errors)) }
    return Response(data = prisonReferenceData)
  }

  private fun authHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("nDelius")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  private fun prisonAuthHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS")
    val version = "1.0"

    return mapOf(
      "Authorization" to "Bearer $token",
      "version" to version,
      "Page-Limit" to Int.MAX_VALUE.toString(),
    )
  }
}

enum class NomisReferenceDataType(
  val category: String,
) {
  PHONE_USAGE("PHONE_TYPE"),
  ALERT("ALERT_TYPE"),
  ETHNICITY("ETHNICITY"),
  SEX("GENDER"),
}
