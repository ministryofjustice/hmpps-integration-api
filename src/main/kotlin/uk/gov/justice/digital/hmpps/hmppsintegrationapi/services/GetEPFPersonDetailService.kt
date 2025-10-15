package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.EPF_GATEWAY_DISABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationIntegrationEPFGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetEPFPersonDetailService(
  @Autowired val probationIntegrationEPFGateway: ProbationIntegrationEPFGateway,
  @Autowired val deliusGateway: NDeliusGateway,
  @Autowired val featureFlag: FeatureFlagConfig,
) {
  fun execute(
    hmppsId: String,
    eventNumber: Int,
  ): Response<CaseDetail?> {
    val caseDetail =
      if (featureFlag.isEnabled(EPF_GATEWAY_DISABLED)) {
        deliusGateway.getEpfCaseDetailForPerson(hmppsId, eventNumber)
      } else {
        probationIntegrationEPFGateway.getCaseDetailForPerson(hmppsId, eventNumber)
      }

    return Response(
      data = caseDetail.data,
      errors = caseDetail.errors,
    )
  }
}
