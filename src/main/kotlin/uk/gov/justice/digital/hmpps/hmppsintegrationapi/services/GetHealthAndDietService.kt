package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HealthAndMedicationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CateringInstruction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Diet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HealthAndDiet
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
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
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }
    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = HealthAndDiet(),
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val smokerResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
    if (smokerResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = smokerResponse.errors)
    }

    val dietResponse = healthAndMedicationGateway.getHealthAndMedicationData(nomisNumber)
    if (dietResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      return Response(
        data =
          HealthAndDiet(
            diet =
              Diet(
                foodAllergies = emptyList(),
                medicalDietaryRequirements = emptyList(),
                personalisedDietaryRequirements = emptyList(),
                cateringInstructions = CateringInstruction(value = null),
              ),
            smoking = smokerResponse.data?.smoker,
          ),
      )
    } else if (dietResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = dietResponse.errors)
    }

    return Response(
      data =
        HealthAndDiet(
          diet = dietResponse.data?.dietAndAllergy?.toDiet(),
          smoking = smokerResponse.data?.smoker,
        ),
    )
  }
}
