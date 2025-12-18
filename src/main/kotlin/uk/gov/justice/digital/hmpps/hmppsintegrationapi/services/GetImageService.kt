package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetImageService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getById(id: Int): Response<ByteArray> = prisonApiGateway.getImageData(id)

  fun execute(
    id: Int,
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<ByteArray?> {
    val personResponse = getPersonService.getNomisNumberWithFiltering(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerImageDetails = prisonApiGateway.getImageMetadataForPerson(nomisNumber)

    if (prisonerImageDetails.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerImageDetails.errors)
    }

    prisonerImageDetails.data.find { it.id.toInt() == id }
      ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerImageData = prisonApiGateway.getImageData(id)

    if (prisonerImageData.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerImageData.errors)
    }

    return Response(data = prisonerImageData.data)
  }
}
