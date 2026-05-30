package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetImageService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getById(
    id: Int,
    requestContext: RequestContext? = null,
  ): Response<ByteArray> = prisonApiGateway.getImageData(id, requestContext)

  fun execute(
    id: Int,
    hmppsId: String,
    requestContext: RequestContext?,
  ): Response<ByteArray?> {
    val personResponse = getPersonService.getNomisNumber(hmppsId, requestContext)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerImageDetails = prisonApiGateway.getImageMetadataForPerson(nomisNumber, requestContext)

    if (prisonerImageDetails.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerImageDetails.errors)
    }

    prisonerImageDetails.data.find { it.id.toInt() == id }
      ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerImageData = prisonApiGateway.getImageData(id, requestContext)

    if (prisonerImageData.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerImageData.errors)
    }

    return Response(data = prisonerImageData.data)
  }
}
