package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

interface AccessFor {
  fun getAccessForCrn(crn: String): CaseAccess?
}

@Service
class GetCaseAccess(
  private val deliusApi: NDeliusGateway,
) : AccessFor {
  override fun getAccessForCrn(crn: String): CaseAccess? = deliusApi.getCaseAccess(crn).data
}
