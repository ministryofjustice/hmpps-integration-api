package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactGlobalRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRestrictions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetVisitorRestrictionsService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    contactId: Long,
    filters: ConsumerFilters?,
  ): Response<PrisonerContactRestrictions?> {
    val personResponse = getPersonService.getPrisoner(hmppsId, filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(null, personResponse.errors)
    }
    val prisonId = personResponse.data?.prisonId

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerContactRestrictions>(prisonId, filters)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val (linkedPrisoners, linkedPrisonersErrors) = personalRelationshipsGateway.getLinkedPrisoner(contactId)

    if (linkedPrisonersErrors.isNotEmpty()) {
      return Response(null, linkedPrisonersErrors)
    }

    val linkedPrisoner = linkedPrisoners.firstOrNull { it.prisonerNumber == personResponse.data?.identifiers?.nomisNumber }

    if (linkedPrisoner == null) {
      return Response(null, listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Prisoner not found")))
    }

    val prisonerContactRestrictions = mutableListOf<PrisonerContactRestriction>()
    var contactGlobalRestrictions = listOf<ContactGlobalRestriction>()

    val prisonerContactIds = linkedPrisoner.relationships?.map { it.prisonerContactId }.orEmpty()
    for (prisonerContactId in prisonerContactIds) {
      val gatewayResult = personalRelationshipsGateway.getPrisonerContactRestrictions(prisonerContactId!!)
      if (gatewayResult.errors.isEmpty() && gatewayResult.data != null) {
        if (gatewayResult.data.prisonerContactRestrictions != null) {
          prisonerContactRestrictions.addAll(gatewayResult.data.prisonerContactRestrictions.map { it.toPrisonerContactRestriction() })
        }
        if (prisonerContactId == prisonerContactIds.first() && gatewayResult.data.contactGlobalRestrictions != null) {
          contactGlobalRestrictions = gatewayResult.data.contactGlobalRestrictions.map { it.toContactGlobalRestriction() }
        }
      }

      // Continue to loop through ids and call gateway in the case the error is 404
      if (gatewayResult.errors.isNotEmpty() && !gatewayResult.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
        return Response(null, gatewayResult.errors)
      }
    }

    val restrictionsResult =
      PrisonerContactRestrictions(
        prisonerContactRestrictions = prisonerContactRestrictions,
        contactGlobalRestrictions = contactGlobalRestrictions,
      )

    return Response(data = restrictionsResult)
  }
}
