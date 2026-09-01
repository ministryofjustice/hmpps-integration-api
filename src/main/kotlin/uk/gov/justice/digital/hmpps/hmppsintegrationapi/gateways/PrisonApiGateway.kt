package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.MovementItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.NomisOffenderVisitRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.NomisTransactionTransferResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAccounts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiBooking
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiImageDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiInmateDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiMovements
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiOffenceHistoryDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiOffenderSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPrisonTimeline
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiReasonableAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiReferenceCode
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiScheduledEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiSentenceSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiTransactionResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.visits.VisitBalances
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class PrisonApiGateway(
  @Value("\${services.prison-api.base-url}") val baseUrl: String,
  val featureFlag: FeatureFlagConfig,
  val prisonApiRestClient: RestApiClient,
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

  fun getImageMetadataForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<ImageMetadata>> {
    if (useRestApiClient(requestContext)) {
      return getImageMetadataForPerson2(id, requestContext)
    }

    val result =
      webClient.requestList<PrisonApiImageDetail>(
        HttpMethod.GET,
        "api/images/offenders/$id",
        authenticationHeader(requestContext),
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

  fun getImageMetadataForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<ImageMetadata>> {
    val result =
      prisonApiRestClient.getList(
        "/api/images/offenders/$id",
        PrisonApiImageDetail::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.map { it.toImageMetadata() }.sortedByDescending { it.captureDateTime })
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getImageData(
    id: Int,
    requestContext: RequestContext? = null,
  ): Response<ByteArray> {
    if (useRestApiClient(requestContext)) {
      return getImageData2(id, requestContext)
    }

    val result = webClient.request<ByteArray>(HttpMethod.GET, "/api/images/$id/data", authenticationHeader(requestContext), UpstreamApi.PRISON_API)

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

  fun getImageData2(
    id: Int,
    requestContext: RequestContext? = null,
  ): Response<ByteArray> {
    val result =
      prisonApiRestClient.get(
        "/api/images/$id/data",
        ByteArray::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, byteArrayOf())
    }
  }

  fun getAddressesForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<Address>> {
    if (useRestApiClient(requestContext)) {
      return getAddressesForPerson2(id, requestContext)
    }

    val result =
      webClient.requestList<PrisonApiAddress>(
        HttpMethod.GET,
        "/api/offenders/$id/addresses",
        authenticationHeader(requestContext),
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

  fun getAddressesForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<Address>> {
    val result =
      prisonApiRestClient.getList(
        "/api/offenders/$id/addresses",
        PrisonApiAddress::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.map { it.toAddress() }.sortedByDescending { it.startDate })
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getOffencesForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<Offence>> {
    if (useRestApiClient(requestContext)) {
      return getOffencesForPerson2(id, requestContext)
    }

    val result =
      webClient.requestList<PrisonApiOffenceHistoryDetail>(
        HttpMethod.GET,
        "/api/bookings/offenderNo/$id/offenceHistory",
        authenticationHeader(requestContext),
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

  fun getOffencesForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<Offence>> {
    val result =
      prisonApiRestClient.getList(
        "/api/bookings/offenderNo/$id/offenceHistory",
        PrisonApiOffenceHistoryDetail::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.map { it.toOffence() }.sortedByDescending { it.startDate })
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getSentencesForBooking(
    id: Int,
    context: RequestContext? = null,
  ): Response<List<Sentence>> {
    if (useRestApiClient(context)) {
      return getSentencesForBooking2(id, context)
    }

    val result =
      webClient.requestList<PrisonApiSentence>(
        HttpMethod.GET,
        "/api/offender-sentences/booking/$id/sentences-and-offences",
        authenticationHeader(context),
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

  fun getSentencesForBooking2(
    id: Int,
    context: RequestContext? = null,
  ): Response<List<Sentence>> {
    val result =
      prisonApiRestClient.getList(
        "/api/offender-sentences/booking/$id/sentences-and-offences",
        PrisonApiSentence::class,
        authenticationHeader(context),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.map { it.toSentence() }.sortedByDescending { it.dateOfSentencing })
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getBookingIdsForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<PrisonApiBooking>> {
    if (useRestApiClient(requestContext)) {
      return getBookingIdsForPerson2(id, requestContext)
    }

    val result =
      webClient.requestList<PrisonApiBooking>(
        HttpMethod.GET,
        "/api/offender-sentences?offenderNo=$id",
        authenticationHeader(requestContext),
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

  fun getBookingIdsForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<List<PrisonApiBooking>> {
    val result =
      prisonApiRestClient.getList(
        "/api/offender-sentences?offenderNo=$id",
        PrisonApiBooking::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.toList())
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getLatestSentenceAdjustmentsForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<SentenceAdjustment?> {
    if (useRestApiClient(requestContext)) {
      return getLatestSentenceAdjustmentsForPerson2(id, requestContext)
    }

    val result =
      webClient.request<PrisonApiSentenceSummary>(
        HttpMethod.GET,
        "/api/offenders/$id/booking/latest/sentence-summary",
        authenticationHeader(requestContext),
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

  fun getLatestSentenceAdjustmentsForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<SentenceAdjustment?> {
    val result =
      prisonApiRestClient.get(
        "/api/offenders/$id/booking/latest/sentence-summary",
        PrisonApiSentenceSummary::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(
        data =
          result.data!!
            .latestPrisonTerm.sentenceAdjustments
            .toSentenceAdjustment(),
      )
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getLatestSentenceKeyDatesForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<SentenceKeyDates?> {
    if (useRestApiClient(requestContext)) {
      return getLatestSentenceKeyDatesForPerson2(id, requestContext)
    }

    val result =
      webClient.request<PrisonApiOffenderSentence>(
        HttpMethod.GET,
        "/api/offenders/$id/sentences",
        authenticationHeader(requestContext),
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

  fun getLatestSentenceKeyDatesForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<SentenceKeyDates?> {
    val result =
      prisonApiRestClient.get(
        "/api/offenders/$id/sentences",
        PrisonApiOffenderSentence::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data?.sentenceDetail?.toSentenceKeyDates())
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getRiskCategoriesForPerson(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<RiskCategory?> {
    if (useRestApiClient(requestContext)) {
      return getRiskCategoriesForPerson2(id, requestContext)
    }

    val result =
      webClient.request<PrisonApiInmateDetail>(
        HttpMethod.GET,
        "/api/offenders/$id",
        authenticationHeaderForCategories(requestContext),
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

  fun getRiskCategoriesForPerson2(
    id: String,
    requestContext: RequestContext? = null,
  ): Response<RiskCategory?> {
    val result =
      prisonApiRestClient.get(
        "/api/offenders/$id",
        PrisonApiInmateDetail::class,
        authenticationHeaderForCategories(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data?.toRiskCategory())
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, RiskCategory())
    }
  }

  fun getReasonableAdjustments(
    booking: String,
    requestContext: RequestContext? = null,
  ): Response<List<ReasonableAdjustment>> {
    if (useRestApiClient(requestContext)) {
      return getReasonableAdjustments2(booking, requestContext)
    }

    val treatmentCodes = getReferenceDomains("HEALTH_TREAT", requestContext).data
    val codes = treatmentCodes.map { "type=${URLEncoder.encode(it.code, StandardCharsets.UTF_8)}" }.toList()
    val params = codes.joinToString(separator = "&", prefix = "?")
    val result =
      webClient.request<PrisonApiReasonableAdjustments>(
        HttpMethod.GET,
        "/api/bookings/$booking/reasonable-adjustments$params",
        authenticationHeaderForCategories(requestContext),
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

  fun getReasonableAdjustments2(
    booking: String,
    requestContext: RequestContext? = null,
  ): Response<List<ReasonableAdjustment>> {
    val treatmentCodes = getReferenceDomains("HEALTH_TREAT", requestContext).data
    val codes = treatmentCodes.map { "type=${URLEncoder.encode(it.code, StandardCharsets.UTF_8)}" }.toList()
    val params = codes.joinToString(separator = "&", prefix = "?")
    val result =
      prisonApiRestClient.get(
        "/api/bookings/$booking/reasonable-adjustments$params",
        PrisonApiReasonableAdjustments::class,
        authenticationHeaderForCategories(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.reasonableAdjustments.map { it.toReasonableAdjustment() })
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getReferenceDomains(
    domain: String,
    requestContext: RequestContext? = null,
  ): Response<List<PrisonApiReferenceCode>> {
    if (useRestApiClient(requestContext)) {
      return getReferenceDomains2(domain, requestContext)
    }

    val result =
      webClient.requestList<PrisonApiReferenceCode>(
        HttpMethod.GET,
        "/api/reference-domains/domains/$domain/codes",
        authenticationHeaderForCategories(requestContext),
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

  fun getReferenceDomains2(
    domain: String,
    requestContext: RequestContext? = null,
  ): Response<List<PrisonApiReferenceCode>> {
    val result =
      prisonApiRestClient.getList(
        "/api/reference-domains/domains/$domain/codes",
        PrisonApiReferenceCode::class,
        authenticationHeaderForCategories(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.toList())
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getAccountsForPerson(
    prisonId: String,
    nomisNumber: String?,
    requestContext: RequestContext? = null,
  ): Response<PrisonApiAccounts?> {
    if (useRestApiClient(requestContext)) {
      return getAccountsForPerson2(prisonId, nomisNumber, requestContext)
    }

    val result =
      webClient.request<PrisonApiAccounts>(
        HttpMethod.GET,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/accounts",
        authenticationHeader(requestContext),
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

  fun getAccountsForPerson2(
    prisonId: String,
    nomisNumber: String?,
    requestContext: RequestContext? = null,
  ): Response<PrisonApiAccounts?> {
    val result =
      prisonApiRestClient.get(
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/accounts",
        PrisonApiAccounts::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getTransactionsForPerson(
    prisonId: String,
    nomisNumber: String,
    accountCode: String,
    fromDate: String,
    toDate: String,
    requestContext: RequestContext? = null,
  ): Response<Transactions?> {
    if (useRestApiClient(requestContext)) {
      return getTransactionsForPerson2(prisonId, nomisNumber, accountCode, fromDate, toDate, requestContext)
    }

    val result =
      webClient.request<Transactions>(
        HttpMethod.GET,
        "/api/transactions/prison/$prisonId/offenders/$nomisNumber/accounts/$accountCode?from_date=$fromDate&to_date=$toDate",
        authenticationHeader(requestContext),
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

  fun getTransactionsForPerson2(
    prisonId: String,
    nomisNumber: String,
    accountCode: String,
    fromDate: String,
    toDate: String,
    requestContext: RequestContext? = null,
  ): Response<Transactions?> {
    val result =
      prisonApiRestClient.get(
        "/api/transactions/prison/$prisonId/offenders/$nomisNumber/accounts/$accountCode?from_date=$fromDate&to_date=$toDate",
        Transactions::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getTransactionForPerson(
    prisonId: String,
    nomisNumber: String,
    clientUniqueRef: String,
    requestContext: RequestContext? = null,
  ): Response<Transaction?> {
    if (useRestApiClient(requestContext)) {
      return getTransactionForPerson2(prisonId, nomisNumber, clientUniqueRef, requestContext)
    }

    val result =
      webClient.request<Transaction>(
        HttpMethod.GET,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions/$clientUniqueRef",
        authenticationHeader(requestContext),
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

  fun getTransactionForPerson2(
    prisonId: String,
    nomisNumber: String,
    clientUniqueRef: String,
    requestContext: RequestContext? = null,
  ): Response<Transaction?> {
    val result =
      prisonApiRestClient.get(
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions/$clientUniqueRef",
        Transaction::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun postTransactionForPerson(
    prisonId: String,
    nomisNumber: String,
    transactionRequest: TransactionRequest,
    requestContext: RequestContext? = null,
  ): Response<PrisonApiTransactionResponse?> {
    if (useRestApiClient(requestContext)) {
      return postTransactionForPerson2(prisonId, nomisNumber, transactionRequest, requestContext)
    }

    val result =
      webClient.requestWithRetry<PrisonApiTransactionResponse>(
        HttpMethod.POST,
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions",
        authenticationHeader(requestContext),
        UpstreamApi.PRISON_API,
        requestBody = transactionRequest.toApiConformingMap(),
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

  fun postTransactionForPerson2(
    prisonId: String,
    nomisNumber: String,
    transactionRequest: TransactionRequest,
    requestContext: RequestContext? = null,
  ): Response<PrisonApiTransactionResponse?> {
    val result =
      prisonApiRestClient.post(
        "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions",
        transactionRequest.toApiConformingMap(),
        PrisonApiTransactionResponse::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun postTransactionTransferForPerson(
    prisonId: String,
    nomisNumber: String,
    transactionTransferRequest: TransactionTransferRequest,
    requestContext: RequestContext? = null,
  ): Response<NomisTransactionTransferResponse?> {
    if (useRestApiClient(requestContext)) {
      return postTransactionTransferForPerson2(prisonId, nomisNumber, transactionTransferRequest, requestContext)
    }

    val result =
      webClient.requestWithRetry<NomisTransactionTransferResponse>(
        HttpMethod.POST,
        "/api/finance/prison/$prisonId/offenders/$nomisNumber/transfer-to-savings",
        authenticationHeader(requestContext),
        UpstreamApi.PRISON_API,
        requestBody = transactionTransferRequest.toApiConformingMap(),
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

  fun postTransactionTransferForPerson2(
    prisonId: String,
    nomisNumber: String,
    transactionTransferRequest: TransactionTransferRequest,
    requestContext: RequestContext? = null,
  ): Response<NomisTransactionTransferResponse?> {
    val result =
      prisonApiRestClient.post(
        "/api/finance/prison/$prisonId/offenders/$nomisNumber/transfer-to-savings",
        transactionTransferRequest.toApiConformingMap(),
        NomisTransactionTransferResponse::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getOffenderVisitRestrictions(
    offenderNumber: String,
    requestContext: RequestContext? = null,
  ): Response<List<PersonVisitRestriction>?> {
    if (useRestApiClient(requestContext)) {
      return getOffenderVisitRestrictions2(offenderNumber, requestContext)
    }

    val result =
      webClient.request<NomisOffenderVisitRestrictions>(
        HttpMethod.GET,
        "/api/offenders/$offenderNumber/offender-restrictions",
        authenticationHeader(requestContext),
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

  fun getOffenderVisitRestrictions2(
    offenderNumber: String,
    requestContext: RequestContext? = null,
  ): Response<List<PersonVisitRestriction>?> {
    val result =
      prisonApiRestClient.get(
        "/api/offenders/$offenderNumber/offender-restrictions",
        NomisOffenderVisitRestrictions::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data?.offenderRestrictions?.map { it.toPersonVisitRestriction() })
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getVisitBalances(
    offenderNumber: String,
    requestContext: RequestContext? = null,
  ): Response<VisitBalances?> {
    if (useRestApiClient(requestContext)) {
      return getVisitBalances2(offenderNumber, requestContext)
    }

    val result =
      webClient.request<VisitBalances>(
        HttpMethod.GET,
        "/api/bookings/offenderNo/$offenderNumber/visit/balances",
        authenticationHeader(requestContext),
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

  fun getVisitBalances2(
    offenderNumber: String,
    requestContext: RequestContext? = null,
  ): Response<VisitBalances?> {
    val result =
      prisonApiRestClient.get(
        "/api/bookings/offenderNo/$offenderNumber/visit/balances",
        VisitBalances::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getCsraAssessmentsForPerson(
    nomisNumber: String,
    requestContext: RequestContext? = null,
  ): Response<List<PrisonApiAssessmentSummary>> {
    if (useRestApiClient(requestContext)) {
      return getCsraAssessmentsForPerson2(nomisNumber, requestContext)
    }

    val result =
      webClient.requestList<PrisonApiAssessmentSummary>(
        HttpMethod.GET,
        "/api/offender-assessments/csra/$nomisNumber",
        authenticationHeader(requestContext),
        UpstreamApi.PRISON_API,
        badRequestAsError = true,
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

  fun getCsraAssessmentsForPerson2(
    nomisNumber: String,
    requestContext: RequestContext? = null,
  ): Response<List<PrisonApiAssessmentSummary>> {
    val result =
      prisonApiRestClient.getList(
        "/api/offender-assessments/csra/$nomisNumber",
        PrisonApiAssessmentSummary::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.toList())
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  fun getPrisonTimelineForPerson(
    nomisNumber: String,
    requestContext: RequestContext?,
  ): Response<PrisonApiPrisonTimeline?> {
    if (useRestApiClient(requestContext)) {
      return getPrisonTimelineForPerson2(nomisNumber, requestContext)
    }

    val result =
      webClient.request<PrisonApiPrisonTimeline>(
        HttpMethod.GET,
        "/api/offenders/$nomisNumber/prison-timeline",
        authenticationHeader(requestContext),
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

  fun getPrisonTimelineForPerson2(
    nomisNumber: String,
    requestContext: RequestContext? = null,
  ): Response<PrisonApiPrisonTimeline?> {
    val result =
      prisonApiRestClient.get(
        "/api/offenders/$nomisNumber/prison-timeline",
        PrisonApiPrisonTimeline::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getMovementsForPerson(
    nomisNumber: String,
    requestContext: RequestContext?,
  ): Response<PrisonApiMovements?> {
    val result =
      prisonApiRestClient.getList(
        "/api/movements/offender/$nomisNumber?movementTypes=TRN&movementTypes=CRT&allBookings=true",
        MovementItem::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty() && result.data != null) {
      Response(data = PrisonApiMovements(movements = result.data))
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, null)
    }
  }

  fun getScheduledMovements(
    nomisNumber: String,
    requestContext: RequestContext,
  ): Response<List<PrisonApiScheduledEvents>> {
    val result =
      prisonApiRestClient.getList(
        "/api/offenders/$nomisNumber/scheduled-events",
        PrisonApiScheduledEvents::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!.toList())
    } else {
      Response.error(UpstreamApi.PRISON_API, result.errors, emptyList())
    }
  }

  private fun authenticationHeader(requestContext: RequestContext? = null): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS", requestContext)

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  private fun authenticationHeaderForCategories(requestContext: RequestContext? = null): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NOMIS", requestContext)
    val version = "1.0"

    return mapOf(
      "Authorization" to "Bearer $token",
      "version" to version,
    )
  }

  internal fun useRestApiClient(requestContext: RequestContext?): Boolean = requestContext?.featureFlags?.isEnabled(FeatureFlagConfig.RESTAPICLIENT_FOR_PRISON_API_GATEWAY) ?: featureFlag.isEnabled(FeatureFlagConfig.RESTAPICLIENT_FOR_PRISON_API_GATEWAY)
}
