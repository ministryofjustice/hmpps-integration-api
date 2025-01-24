package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class PostTransactionForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  @Suppress("ktlint:standard:property-naming")
  final val VALID_TRANSACTION_TYPE_CODES = listOf("CANT", "REFND", "PHONE", "MRPR", "MTDS", "DTDS", "CASHD", "RELA", "RELS")

  fun execute(
    prisonId: String,
    hmppsId: String,
    transactionRequest: TransactionRequest,
    filters: ConsumerFilters? = null,
  ): Response<TransactionCreateResponse?> {
    if (
      filters != null && !filters.matchesPrison(prisonId)
    ) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.FORBIDDEN, "Not found")),
      )
    }

    if (transactionRequest.type !in VALID_TRANSACTION_TYPE_CODES) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.BAD_REQUEST, "Invalid transaction type")),
      )
    }

    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse == null) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Nomis number not found")),
      )
    }

    val nomisNumber = personResponse.data?.nomisNumber

    if (nomisNumber == null) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    val response =
      nomisGateway.postTransactionForPerson(
        prisonId,
        nomisNumber,
        transactionRequest,
      )

    if (response.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = response.errors,
      )
    }

    val transactionCreateResponse = TransactionCreateResponse()
    if (!response.data.id.isNullOrBlank()) {
      transactionCreateResponse.transactionId = response.data.id
    }

    return Response(
      data = transactionCreateResponse,
      errors = emptyList(),
    )
  }
}
