package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.REPLACE_PROBATION_SEARCH
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetPersonsService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  private val deliusGateway: NDeliusGateway,
  private val featureFlag: FeatureFlagConfig,
) {
  fun execute(
    firstName: String?,
    lastName: String?,
    pncNumber: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<Person>> {
    val responseFromProbationOffenderSearch =
      if (featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)) {
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)
      } else {
        probationOffenderSearchGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth, searchWithinAliases)
      }

    if (responseFromProbationOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromProbationOffenderSearch.errors)
    }

    if (pncNumber.isNullOrBlank()) {
      val responseFromPrisonerOffenderSearch =
        prisonerOffenderSearchGateway.getPersons(
          firstName,
          lastName,
          dateOfBirth,
          searchWithinAliases,
        )
      return Response(data = responseFromPrisonerOffenderSearch.data.map { it.toPerson() } + responseFromProbationOffenderSearch.data)
    }

    return Response(data = responseFromProbationOffenderSearch.data)
  }
}
