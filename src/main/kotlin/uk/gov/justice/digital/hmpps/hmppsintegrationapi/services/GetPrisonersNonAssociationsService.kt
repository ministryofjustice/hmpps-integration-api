package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NonAssociationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociation

@Service
class GetPrisonersNonAssociationsService(
  @Autowired val nonAssociationsGateway: NonAssociationsGateway,
) {
  fun execute(
    prisonerNumber: String,
    includeOpen: String? = "true",
    includeClosed: String? = "false",
  ): Response<List<NonAssociation>> {
    val responseFromNonAssociationsGateway = nonAssociationsGateway.getNonAssociationsForPerson(prisonerNumber, includeOpen, includeClosed)

    if (responseFromNonAssociationsGateway.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromNonAssociationsGateway.errors)
    }

    return Response(data = responseFromNonAssociationsGateway.data)
  }
}
