package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PrisonerContactRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

class GetVisitorRestrictionsService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    contactId: Long,
    filters: ConsumerFilters?,
  ): Response<List<PrisonerContactRestrictions>?> {
    val personResponse = getPersonService.getPrisoner(hmppsId, filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(emptyList(), personResponse.errors)
    }
    val prisonId = personResponse.data?.prisonId

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<PrisonerContactRestrictions>>(prisonId, filters)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val (linkedPrisoners, linkedPrisonersErrors) = personalRelationshipsGateway.getLinkedPrisoner(contactId)

    if (linkedPrisonersErrors.isNotEmpty()) {
      return Response(emptyList(), linkedPrisonersErrors)
    }

    // revise
    var linkedPrisonerIds = linkedPrisoners.map { it.relationships?.map { it.prisonerContactId } }.flatMap { it ?: emptyList() }

    var restrictionsResult = mutableListOf<PrisonerContactRestrictions>()
    // maybe revise with coroutine depending on webclient behavior
    for (prisonerContactId in linkedPrisonerIds) {
      val gatewayResult = personalRelationshipsGateway.getPrisonerContactRestrictions(prisonerContactId!!)
      if (gatewayResult.errors.isEmpty() && gatewayResult.data != null) {
        restrictionsResult.add(gatewayResult.data)
      }
      // continue to call gateway in the case the error is 404
      if (gatewayResult.errors.isNotEmpty() && !gatewayResult.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
        return Response(emptyList(), gatewayResult.errors)
      }
    }
    // A. return 404 not found if none of prisonerContactIds find data
    // B. find data for some prisonerContactIds, what do we do about the ones we don't find? send 404 or ignore and only send data we have
    // C. what to do when we get back a non 404 error, error out immediately or keep making calls
    return Response(data = restrictionsResult)
  }
}
