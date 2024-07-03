package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetHmppsIdService(
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<HmppsId?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)

    return Response(
      data = HmppsId(hmppsId = personResponse.data?.hmppsId),
      errors = personResponse.errors,
    )
  }
}
