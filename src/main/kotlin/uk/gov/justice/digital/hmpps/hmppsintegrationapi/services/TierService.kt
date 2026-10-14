package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.TierGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier.TierV3DetailsDto

@Service
class TierService(
  private val tierGateway: TierGateway,
  private val personService: GetPersonService,
) {
  fun getTierDetails(
    hmppsId: String,
    requestContext: RequestContext,
  ): Response<TierV3DetailsDto?> {
    val crn =
      when (identifyHmppsId(hmppsId)) {
        GetPersonService.IdentifierType.UNKNOWN -> throw ValidationException("Invalid HMPPS identifier: $hmppsId")
        GetPersonService.IdentifierType.CRN -> hmppsId
        else -> {
          val response = personService.convert(hmppsId, GetPersonService.IdentifierType.CRN)
          response.data ?: return Response(null, response.errors)
        }
      }
    return tierGateway.getTierDetails(crn, requestContext)
  }
}
