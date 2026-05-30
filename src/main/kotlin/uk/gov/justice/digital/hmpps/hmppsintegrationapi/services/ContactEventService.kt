package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class ContactEventService(
  @Autowired val deliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun getContactEvents(
    hmppsId: String,
    pageNo: Int,
    perPage: Int,
    requestContext: RequestContext? = null,
  ): Response<ContactEvents?> {
    val filters = requestContext?.filters
    val personResponse = getPersonService.execute(hmppsId, requestContext)
    val response =
      personResponse.data?.identifiers?.deliusCrn?.let {
        deliusGateway.getContactEventsForPerson(it, pageNo, perPage, ConsumerFilters.mappaCategories(filters), requestContext)
      } ?: throw EntityNotFoundException("NDelius CRN not found for $hmppsId")

    val contactEvents = response.data?.toPaginated(perPage, pageNo)
    return Response(
      data = contactEvents,
      errors = response.errors,
    )
  }

  fun getContactEvent(
    hmppsId: String,
    contactEventId: Long,
    requestContext: RequestContext?,
  ): Response<ContactEvent?> {
    val filters = requestContext?.filters
    val personResponse = getPersonService.execute(hmppsId, requestContext)
    val response =
      personResponse.data?.identifiers?.deliusCrn?.let {
        deliusGateway.getContactEventForPerson(it, contactEventId, ConsumerFilters.mappaCategories(filters), requestContext)
      } ?: throw EntityNotFoundException("NDelius CRN not found for $hmppsId with id $contactEventId")

    return Response(
      data = response.data?.toContactEvent(),
      errors = response.errors,
    )
  }
}
