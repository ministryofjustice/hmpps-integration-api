package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CreateAndVaryLicenceGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetLicenceConditionService(
  @Autowired val createAndVaryLicenceGateway: CreateAndVaryLicenceGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<Licence>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val crn = personResponse.data?.identifiers?.deliusCrn

    var licences: Response<List<Licence>> = Response(data = emptyList())

    if (crn != null) {
      licences = createAndVaryLicenceGateway.getLicenceSummaries(id = crn)
      licences.data.forEach {
        val conditions = createAndVaryLicenceGateway.getLicenceConditions(it.id)
        it.conditions = conditions.data
      }
    }

    return Response(
      data = licences.data,
      errors = personResponse.errors + licences.errors,
    )
  }
}
