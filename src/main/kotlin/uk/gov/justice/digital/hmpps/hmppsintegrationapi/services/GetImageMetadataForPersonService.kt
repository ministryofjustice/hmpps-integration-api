package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetImageMetadataForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<ImageMetadata>> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = emptyList(), errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = emptyList(),
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )
    return prisonApiGateway.getImageMetadataForPerson(nomisNumber)
  }
}
