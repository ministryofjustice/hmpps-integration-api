package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HealthAndMedicationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HealthAndDiet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetHealthAndDietService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val healthAndMedicationGateway: HealthAndMedicationGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<HealthAndDiet?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val smokerResponse = prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)
    val dietResponse = healthAndMedicationGateway.getHealthAndMedicationData(hmppsId)

    val healthAndDiet =
      HealthAndDiet(
        diet = dietResponse.data?.dietAndAllergy?.toDiet(),
        smoking = smokerResponse.data?.smoker,
      )

    return Response(data = healthAndDiet, errors = smokerResponse.errors + dietResponse.errors)
  }
}
