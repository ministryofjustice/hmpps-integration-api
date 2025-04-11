package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier.Companion.CRN_REGEX
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResponseResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetHmppsIdService(
  private val getPersonService: GetPersonService,
  private val probationSearch: ProbationOffenderSearchGateway,
) : CrnSupplier {
  fun execute(
    nomisNumber: String,
    filters: ConsumerFilters? = null,
  ): Response<HmppsId?> {
    val (person, personErrors) = getPersonService.getPersonWithPrisonFilter(nomisNumber, filters)
    if (personErrors.isNotEmpty()) {
      return Response(
        data = null,
        errors = personErrors,
      )
    }

    val hmppsId =
      person?.hmppsId ?: person?.identifiers?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    return Response(data = HmppsId(hmppsId))
  }

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> =
    getPersonService.getNomisNumber(hmppsId = hmppsId).toResult().let {
      when (it) {
        is ResponseResult.Success -> Response(data = NomisNumber(nomisNumber = it.data.nomisNumber))
        is ResponseResult.Failure -> Response(data = null, errors = it.errors)
      }
    }

  override fun getCrn(hmppsId: String): String? =
    if (hmppsId.matches(CRN_REGEX)) {
      hmppsId
    } else {
      probationSearch
        .getPerson(hmppsId)
        .data
        ?.identifiers
        ?.deliusCrn
    }
}
