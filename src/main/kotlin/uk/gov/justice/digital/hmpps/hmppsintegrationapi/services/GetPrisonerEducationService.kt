package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerEducation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonerEducationService(
  @Autowired val plpGateway: PLPGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
      hmppsId: String,
      filters: ConsumerFilters? = null,
  ): Response<PrisonerEducation?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Prisoner not found")))

    val response = plpGateway.getPrisonerEducation(nomisNumber)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }

    return Response(
      data = response.data?.toPrisonerEducation(),
    )
  }
}
