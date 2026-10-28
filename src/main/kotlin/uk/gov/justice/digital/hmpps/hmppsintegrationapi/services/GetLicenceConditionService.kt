package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.CHECK_NDELIUS_WHEN_NO_CVL_LICENCES
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CreateAndVaryLicenceGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetLicenceConditionService(
  private val createAndVaryLicenceGateway: CreateAndVaryLicenceGateway,
  private val getPersonService: GetPersonService,
  private val nDeliusGateway: NDeliusGateway,
  private val featureFlagConfig: FeatureFlagConfig,
) {
  fun execute(hmppsId: String): Response<PersonLicences> {
    val personResponse = getPersonService.execute(hmppsId)
    val crn = personResponse.data?.identifiers?.deliusCrn

    var licences: Response<List<Licence>> = Response(data = emptyList())
    var personLicences = PersonLicences(hmppsId)
    if (crn != null) {
      licences = createAndVaryLicenceGateway.getLicenceSummaries(id = crn)
      licences.data.forEach { summary ->
        summary.conditions = summary.id.toIntOrNull()?.let { licenceId ->
          createAndVaryLicenceGateway.getLicenceConditions(licenceId).data
        } ?: emptyList()
      }

      // This is fix to check whether NDelius licences exist when no licences are found in CVL
      if (featureFlagConfig.isEnabled(CHECK_NDELIUS_WHEN_NO_CVL_LICENCES) && licences.data.isEmpty()) {
        val ndeliusResponse = nDeliusGateway.getLicenceConditionsForPerson(crn)
        if (ndeliusResponse.errors.isNotEmpty()) {
          return Response(PersonLicences(hmppsId, crn, emptyList()), ndeliusResponse.errors)
        }
        licences = Response(data = ndeliusResponse.data?.toLicences(hmppsId) ?: emptyList())
      }

      personLicences = PersonLicences(hmppsId, licences.data.firstOrNull()?.offenderNumber, licences.data)
    }

    return Response(
      data = personLicences,
      errors = personResponse.errors + licences.errors,
    )
  }
}
