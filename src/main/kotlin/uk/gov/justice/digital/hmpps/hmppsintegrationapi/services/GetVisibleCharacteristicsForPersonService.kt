package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisibleCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetVisibleCharacteristicsForPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<VisibleCharacteristics?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber ?: return Response(data = null, errors = personResponse.errors)

    val visibleCharacteristics = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)

    return Response(
      data = visibleCharacteristics.data?.toVisibleCharacteristics(),
      errors = visibleCharacteristics.errors,
    )
  }
}
