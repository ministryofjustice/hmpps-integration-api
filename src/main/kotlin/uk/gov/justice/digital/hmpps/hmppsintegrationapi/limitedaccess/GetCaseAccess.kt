package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

interface AccessFor {
  fun getAccessFor(hmppsId: String): CaseAccess?
}

@Service
class GetCaseAccess(
  private val crnSupplier: CrnSupplier,
  private val deliusApi: NDeliusGateway,
) : AccessFor {
  override fun getAccessFor(hmppsId: String): CaseAccess? = crnSupplier.getCrn(hmppsId)?.let { deliusApi.getCaseAccess(it).data }
}
