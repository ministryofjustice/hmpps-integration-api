package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonableAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceAdjustment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceKeyDates
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionTransferRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Transactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.NomisOffenderVisitRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.NomisTransactionTransferResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAccounts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiBooking
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiImageDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiInmateDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiOffenceHistoryDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiOffenderSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiReasonableAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiReferenceCode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiSentenceSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiTransactionResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.visits.VisitBalances

@Component
class PrisonApiGateway(
  @Value("\${services.prison-api.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "API for Nomis DB used by DPS applications and other apis and services",
      developerPortalId = "DPS060",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/prison-api",
      apiDocUrl = "https://prison-api-dev.prison.service.justice.gov.uk/swagger-ui/index.html ",
      apiSpecUrl = "https://prison-api-dev.prison.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/prison-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getImageMetadataForPerson(id: String): Response<List<ImageMetadata>> {
    val result =
      webClient.requestList<PrisonApiImageDetail>(
        HttpMethod.GET,
        "api/images/offenders/$id",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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
    val result = webClient.request<ByteArray>(HttpMethod.GET, "/api/images/$id/data", authenticationHeader(), UpstreamApi.PRISON_API)

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
      webClient.requestList<PrisonApiAddress>(
        HttpMethod.GET,
        "/api/offenders/$id/addresses",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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
      webClient.requestList<PrisonApiOffenceHistoryDetail>(
        HttpMethod.GET,
        "/api/bookings/offenderNo/$id/offenceHistory",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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

  fun getSentencesForBooking(id: Int): Response<List<Sentence>> {
    val result =
      webClient.requestList<PrisonApiSentence>(
        HttpMethod.GET,
        "/api/offender-sentences/booking/$id/sentences-and-offences",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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

  fun getBookingIdsForPerson(id: String): Response<List<PrisonApiBooking>> {
    val result =
      webClient.requestList<PrisonApiBooking>(
        HttpMethod.GET,
        "/api/offender-sentences?offenderNo=$id",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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
      webClient.request<PrisonApiSentenceSummary>(
        HttpMethod.GET,
        "/api/offenders/$id/booking/latest/sentence-summary",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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
      webClient.request<PrisonApiOffenderSentence>(
        HttpMethod.GET,
        "/api/offenders/$id/sentences",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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
      webClient.request<PrisonApiInmateDetail>(
        HttpMethod.GET,
        "/api/offenders/$id",
        authenticationHeaderForCategories(),
        UpstreamApi.PRISON_API,
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
      webClient.request<PrisonApiReasonableAdjustments>(
        HttpMethod.GET,
        "/api/bookings/$booking/reasonable-adjustments$params",
        authenticationHeaderForCategories(),
        UpstreamApi.PRISON_API,
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

  fun getReferenceDomains(domain: String): Response<List<PrisonApiReferenceCode>> {
    val result =
      webClient.requestList<PrisonApiReferenceCode>(
        HttpMethod.GET,
        "/api/reference-domains/domains/$domain/codes",
        authenticationHeaderForCategories(),
        UpstreamApi.PRISON_API,
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
  ): Response<PrisonApiAccounts?> {
    val result =
      webClient.request<PrisonApiAccounts>(
        HttpMethod.GET,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/accounts",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
        badRequestAsError = true,
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

  fun getTransactionsForPerson(
    prisonId: String,
    nomisNumber: String,
    accountCode: String,
    fromDate: String,
    toDate: String,
  ): Response<Transactions?> {
    val result =
      webClient.request<Transactions>(
        HttpMethod.GET,
        "/api/transactions/prison/$prisonId/offenders/$nomisNumber/accounts/$accountCode?from_date=$fromDate&to_date=$toDate",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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

  fun getTransactionForPerson(
    prisonId: String,
    nomisNumber: String,
    clientUniqueRef: String,
  ): Response<Transaction?> {
    val result =
      webClient.request<Transaction>(
        HttpMethod.GET,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions/$clientUniqueRef",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
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

  fun postTransactionForPerson(
    prisonId: String,
    nomisNumber: String,
    transactionRequest: TransactionRequest,
  ): Response<PrisonApiTransactionResponse?> {
    val result =
      webClient.requestWithRetry<PrisonApiTransactionResponse>(
        HttpMethod.POST,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
        requestBody = transactionRequest.toApiConformingMap(),
        badRequestAsError = true,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }
      is WebClientWrapperResponse.Error,
      -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun postTransactionTransferForPerson(
    prisonId: String,
    nomisNumber: String,
    transactionTransferRequest: TransactionTransferRequest,
  ): Response<NomisTransactionTransferResponse?> {
    val result =
      webClient.requestWithRetry<NomisTransactionTransferResponse>(
        HttpMethod.POST,
        "/api/finance/prison/$prisonId/offenders/$nomisNumber/transfer-to-savings",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
        requestBody = transactionTransferRequest.toApiConformingMap(),
        badRequestAsError = true,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }
      is WebClientWrapperResponse.Error,
      -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getOffenderVisitRestrictions(offenderNumber: String): Response<List<PersonVisitRestriction>?> {
    val result =
      webClient.request<NomisOffenderVisitRestrictions>(
        HttpMethod.GET,
        "/api/offenders/$offenderNumber/offender-restrictions",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
        badRequestAsError = true,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.offenderRestrictions.map { it.toPersonVisitRestriction() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getVisitBalances(offenderNumber: String): Response<VisitBalances?> {
    val result =
      webClient.request<VisitBalances>(
        HttpMethod.GET,
        "/api/bookings/offenderNo/$offenderNumber/visit/balances",
        authenticationHeader(),
        UpstreamApi.PRISON_API,
        badRequestAsError = true,
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
