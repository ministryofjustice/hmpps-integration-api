package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NumberOfChildren
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetNumberOfChildrenForPersonService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    requestContext: RequestContext? = null,
  ): Response<NumberOfChildren?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = null, requestContext = requestContext)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber ?: return Response(data = null, errors = personResponse.errors)

    val numberOfChildren = personalRelationshipsGateway.getNumberOfChildren(nomisNumber, requestContext)

    return Response(
      data = numberOfChildren.data?.toNumberOfChildren(),
      errors = numberOfChildren.errors,
    )
  }
}
