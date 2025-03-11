package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetNameForPersonService(
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PersonName?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val personName = PersonName(firstName = personResponse.data?.firstName, lastName = personResponse.data?.lastName)

    return Response(
      data = personName,
      errors = personResponse.errors,
    )
  }
}
