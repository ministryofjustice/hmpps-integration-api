package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_STUBBED_CONTACT_EVENTS_DATA
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class ContactEventService(
  @Autowired val deliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
) {
  fun getContactEvents(
    hmppsId: String,
    pageNo: Int,
    perPage: Int,
  ): Response<ContactEvents?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val response =
      personResponse.data?.identifiers?.deliusCrn?.let {
        if (featureFlagConfig.isEnabled(USE_STUBBED_CONTACT_EVENTS_DATA)) {
          deliusGateway.getStubbedContactEventsForPerson(it, pageNo, perPage)
        } else {
          deliusGateway.getContactEventsForPerson(it, pageNo, perPage)
        }
      } ?: throw EntityNotFoundException("Contact Events not found for $hmppsId")

    val contactEvents = response.data?.toPaginated()
    return Response(
      data = contactEvents,
      errors = response.errors,
    )
  }

  fun getContactEvent(
    hmppsId: String,
    contactEventId: Long,
  ): Response<ContactEvent?> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val response =
      personResponse.data?.identifiers?.deliusCrn?.let {
        if (featureFlagConfig.isEnabled(USE_STUBBED_CONTACT_EVENTS_DATA)) {
          deliusGateway.getStubbedContactEventForPerson(it, contactEventId)
        } else {
          deliusGateway.getContactEventForPerson(it, contactEventId)
        }
      } ?: throw EntityNotFoundException("Contact Event not found for $hmppsId with id $contactEventId")

    return Response(
      data = response.data?.toContactEvent(),
      errors = response.errors,
    )
  }
}
