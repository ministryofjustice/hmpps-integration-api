package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NumberOfChildren
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetNumberOfChildrenForPersonService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: RoleFilters? = null,
  ): Response<NumberOfChildren?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber ?: return Response(data = null, errors = personResponse.errors)

    val numberOfChildren = personalRelationshipsGateway.getNumberOfChildren(nomisNumber)

    return Response(
      data = numberOfChildren.data?.toNumberOfChildren(),
      errors = numberOfChildren.errors,
    )
  }
}
