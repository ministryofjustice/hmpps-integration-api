package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.v2

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

@Service
class PersonRecordService(
  private val corePersonRecordGateway: CorePersonRecordGateway,
) {
  fun personRecord(
    hmppsId: String,
    requestContext: RequestContext,
  ): Response<CorePersonRecord> {
    val type = identifyHmppsId(hmppsId)
    if (type == IdentifierType.UNKNOWN) {
      throw ValidationException("Not a valid HMPPS Id: $hmppsId")
    }
    val response = corePersonRecordGateway.corePersonRecordFor(type, hmppsId, requestContext)
    return Response(data = response)
  }

  fun identifyHmppsId(input: String): IdentifierType {
    val nomsPattern = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    val crnPattern = Regex("^[A-Z]{1,2}\\d{6}$")

    return when {
      nomsPattern.matches(input) -> IdentifierType.NOMS
      crnPattern.matches(input) -> IdentifierType.CRN
      else -> IdentifierType.UNKNOWN
    }
  }
}
