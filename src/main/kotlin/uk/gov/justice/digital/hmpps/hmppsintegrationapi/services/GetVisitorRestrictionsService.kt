package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.PRPrisonerContactRestrictions
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
  ): Response<PRPrisonerContactRestrictions?> {
    val personResponse = getPersonService.getPrisoner(hmppsId, filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(null, personResponse.errors)
    }
    val prisonId = personResponse.data?.prisonId

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PRPrisonerContactRestrictions>(prisonId, filters)
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

    val linkedPrisonerIds = linkedPrisoner.relationships?.map { it.prisonerContactId }

    val restrictionsResult = PRPrisonerContactRestrictions()
    if (linkedPrisonerIds != null) {
      for (prisonerContactId in linkedPrisonerIds) {
        val gatewayResult = personalRelationshipsGateway.getPrisonerContactRestrictions(prisonerContactId!!)
        if (gatewayResult.errors.isEmpty() && gatewayResult.data != null) {
          gatewayResult.data.prisonerContactRestrictions?.let { restrictionsResult.prisonerContactRestrictions?.addAll(it) }
          if (prisonerContactId == linkedPrisonerIds.first()) {
            restrictionsResult.contactGlobalRestrictions = gatewayResult.data.contactGlobalRestrictions?.first()
          }
        }

        // Continue to loop through ids and call gateway in the case the error is 404
        if (gatewayResult.errors.isNotEmpty() && !gatewayResult.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
          return Response(null, gatewayResult.errors)
        }
      }
    }

    return Response(data = restrictionsResult)
  }
}
