package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetImageService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getById(id: Int): Response<ByteArray> = nomisGateway.getImageData(id)

  fun execute(
    id: Int,
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<ByteArray?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerImageDetails = nomisGateway.getImageMetadataForPerson(nomisNumber)

    if (prisonerImageDetails.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerImageDetails.errors)
    }

    prisonerImageDetails.data.find { it.id.toInt() == id }
      ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerImageData = nomisGateway.getImageData(id)

    if (prisonerImageData.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerImageData.errors)
    }

    return Response(data = prisonerImageData.data)
  }
}
