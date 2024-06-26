package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonIntegrationpes.PESPrisonerDetails

@Service
class GetPESPrisonerDetailsService(
  @Autowired val prisonerSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<PESPrisonerDetails?> {
    val prisonResponse = prisonerSearchGateway.getPrisonOffender(hmppsId)

    return Response(
      data = prisonResponse.data?.toPESPrisonerDetails(),
      errors = prisonResponse.errors,
    )
  }
}
