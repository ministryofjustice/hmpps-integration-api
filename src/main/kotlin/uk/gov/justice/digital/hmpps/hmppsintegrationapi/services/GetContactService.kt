package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PersonalRelationshipsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DetailedContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetContactService(
  @Autowired val personalRelationshipsGateway: PersonalRelationshipsGateway,
) {
  fun execute(contactId: String): Response<DetailedContact?> {
    val validatedContactId: Long
    try {
      validatedContactId = contactId.toLong()
    } catch (e: NumberFormatException) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PERSONAL_RELATIONSHIPS, UpstreamApiError.Type.BAD_REQUEST)))
    }

    val response = personalRelationshipsGateway.getContactByContactId(validatedContactId)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }
    if (response.data == null) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.PERSONAL_RELATIONSHIPS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
      )
    }

    return Response(
      data = response.data.toDetailedContact(),
    )
  }
}
