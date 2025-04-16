package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetImageMetadataForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<ImageMetadata>> {
    if (featureFlagConfig.usePrisonFilterImagesEndpoint) {
      val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
      if (personResponse.errors.isNotEmpty()) {
        return Response(data = emptyList(), errors = personResponse.errors)
      }

      val nomisNumber =
        personResponse.data?.nomisNumber ?: return Response(
          data = emptyList(),
          errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
        )
      return nomisGateway.getImageMetadataForPerson(nomisNumber)
    } else {
      val responseFromProbationOffenderSearch = nDeliusGateway.getPerson(hmppsId)
      val nomisNumber =
        responseFromProbationOffenderSearch.data?.identifiers?.nomisNumber ?: return Response(
          data = emptyList(),
          errors = responseFromProbationOffenderSearch.errors,
        )
      return nomisGateway.getImageMetadataForPerson(nomisNumber)
    }
  }
}
