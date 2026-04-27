package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.GetPrisonIdService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService

@Service
class DomainEventIdentitiesResolver(
  @Autowired val probationIntegrationApiGateway: NDeliusGateway,
  @Autowired val getPrisonIdService: GetPrisonIdService,
  private val personService: GetPersonService,
  private val featureFlagService: FeatureFlagConfig,
) {
  /**
   * The hmpps id is an id that the end client will use in ongoing processing.
   * In the future when we have a core person record it will be that id
   * for now the id will default to the crn but if there is no crn it will be the noms number.
   * The end client that receives the messages must treat it as a hmpps_id and NOT a crn/noms number.
   * A look-up service exist to decode the hmpps_id into a crn or noms number.
   */
  fun getHmppsId(hmppsEvent: HmppsDomainEvent): String? {
    val crn: String? = hmppsEvent.personReference?.findCrnIdentifier()
    if (crn != null) {
      probationIntegrationApiGateway.getPersonExists(crn).let {
        if (it.existsInDelius) {
          return crn
        }
        throw EntityNotFoundException("Person with crn $crn not found")
      }
    }

    val nomsNumber = getNomisNumber(hmppsEvent)

    return nomsNumber?.let { noms ->
      probationIntegrationApiGateway.getPersonIdentifier(noms)?.crn ?: noms
    }
  }

  fun getPrisonId(hmppsEvent: HmppsDomainEvent): String? {
    val prisonId =
      hmppsEvent.prisonId
        ?: hmppsEvent.additionalInformation?.prisonId
    if (prisonId != null) {
      return prisonId
    }

    val nomsNumber = getNomisNumber(hmppsEvent)
    if (nomsNumber != null) {
      return getPrisonIdService.execute(nomsNumber)
    }

    val locationKey = hmppsEvent.additionalInformation?.key
    if (locationKey != null) {
      val regex = Regex("^[A-Z]*((?=-)|$)")
      val match = regex.find(locationKey)
      if (match != null) {
        return match.groups.first()?.value
      }
    }
    return null
  }

  /**
   * Gets the supervision status of the person for the hmppsId
   *
   * @param hmppsId
   * @return The supervision status (one of PRISONS, PROBATION, NONE or UNKNOWN)
   */
  fun getSupervisionStatus(hmppsId: String?): String? = personService.getSupervisionStatus(hmppsId).name

  private fun getNomisNumber(hmppsEvent: HmppsDomainEvent): String? {
    val nomsNumber =
      hmppsEvent.personReference?.findNomsIdentifier()
        ?: hmppsEvent.additionalInformation?.nomsNumber
        ?: hmppsEvent.additionalInformation?.prisonerId
        ?: hmppsEvent.additionalInformation?.prisonerNumber
        ?: hmppsEvent.prisonerId

    return nomsNumber
  }
}
