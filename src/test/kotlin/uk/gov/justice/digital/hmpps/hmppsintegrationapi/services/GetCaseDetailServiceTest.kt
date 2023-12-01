package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationIntegrationEPFGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCaseDetailService::class],
)
internal class GetCaseDetailServiceTest(
  @MockBean val probationIntegrationEPFGateway: ProbationIntegrationEPFGateway,
  private val getCaseDetailService: GetCaseDetailService,
) : DescribeSpec(
  {
    val hmppsId = "X123456"
    val eventNumber = 1234
    val caseDetail = CaseDetail(nomsId = "ABC123")

    beforeEach {
      Mockito.reset(probationIntegrationEPFGateway)

      whenever(probationIntegrationEPFGateway.getCaseDetailForPerson(hmppsId, eventNumber)).thenReturn(
        Response(
          data = caseDetail,
        ),
      )
    }

    it("Returns a list of supervisions for a probationer according to the provided Delius CRN") {
      val result = getCaseDetailService.execute(hmppsId, eventNumber)

      result.shouldBe(Response(data = caseDetail))
    }
  },
)
