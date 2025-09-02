package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier.Companion.CRN_REGEX
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetHmppsIdService(
  private val getPersonService: GetPersonService,
  private val deliusGateway: NDeliusGateway,
) : CrnSupplier {
  fun execute(
    nomisNumber: String,
    filters: RoleFilters? = null,
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
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    return Response(data = HmppsId(hmppsId))
  }

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> {
    val nomisResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    return Response(
      data = NomisNumber(nomisNumber = nomisResponse.data?.nomisNumber),
      errors = nomisResponse.errors,
    )
  }

  override fun getCrn(hmppsId: String): String? =
    if (hmppsId.matches(CRN_REGEX)) {
      hmppsId
    } else {
      deliusGateway
        .getPerson(hmppsId)
        .data
        ?.identifiers
        ?.deliusCrn
    }
}
