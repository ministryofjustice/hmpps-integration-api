package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import java.io.File

class FilterViolationIntegrationTest : IntegrationTestBase() {
  private final val nomsPath = "/v1/persons/$nomsId"
  private final val addressPath = "$nomsPath/addresses"

  @Test
  fun `prisons only - throws a filter violation exception when UNKNOWN`() {
    val posPrisoner = objectMapper.readValue(File("$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json").readText(), POSPrisoner::class.java)
    val posPrisonerUnknownStatus = posPrisoner.copy(status = null)
    whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsId)).thenReturn(Response(data = posPrisonerUnknownStatus))
    callApiWithCN(addressPath, "supervision-status-prison-only")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `prisons only - the call to the upstream returns a 404 so no check can be performed`() {
    whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsId)).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))))
    callApiWithCN(addressPath, "supervision-status-prison-only")
      .andExpect(status().isNotFound)
  }
}
