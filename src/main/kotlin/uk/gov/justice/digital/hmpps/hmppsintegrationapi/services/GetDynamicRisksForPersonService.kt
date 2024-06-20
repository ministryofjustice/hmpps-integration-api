package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetDynamicRisksForPersonService(
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<DynamicRisk>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val deliusCrn = personResponse.data?.identifiers?.deliusCrn

    var nDeliusDynamicRisks: Response<List<DynamicRisk>> = Response(data = emptyList())

    if (deliusCrn != null) {
      val allNDeliusDynamicRisks = nDeliusGateway.getDynamicRisksForPerson(deliusCrn)
      val filteredNDeliusDynamicRisks =
        allNDeliusDynamicRisks.data.filter {
          it.code in
            listOf(
              "RCCO", "RCPR", "REG22", "RVLN", "ALT8", "STRG", "AVIS", "ALT1", "WEAP",
              "RHRH", "RLRH", "RMRH", "RVHR", "RCHD", "REG15", "REG16", "REG17",
              "ALT4", "AVS2", "ALT7", "ALSH",
            )
        }
      nDeliusDynamicRisks = Response(data = filteredNDeliusDynamicRisks, errors = allNDeliusDynamicRisks.errors)
    }

    return Response(
      data = nDeliusDynamicRisks.data,
      errors = personResponse.errors + nDeliusDynamicRisks.errors,
    )
  }
}
