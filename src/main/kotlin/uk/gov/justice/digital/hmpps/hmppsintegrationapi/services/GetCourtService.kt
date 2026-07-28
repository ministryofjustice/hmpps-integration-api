package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CourtRegisterGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetCourtService(
  @Autowired val courtRegisterGateway: CourtRegisterGateway,
) {
  fun getCourt(
    courtId: String,
    requestContext: RequestContext?,
  ): Response<Court?> = courtRegisterGateway.getCourt(courtId, requestContext)
}
