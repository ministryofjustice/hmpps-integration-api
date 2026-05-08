package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetCsraForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonApiGateway: PrisonApiGateway,
) {
  fun getCsraAssessments(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<PrisonApiAssessmentSummary>?> {
    val personResponse = getPersonService.getNomisNumber(hmppsId, filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val csraResponse = prisonApiGateway.getCsraAssessmentsForPerson(nomisNumber)
    if (csraResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = csraResponse.errors)
    }

    return Response(
      data = csraResponse.data,
    )
  }
}
