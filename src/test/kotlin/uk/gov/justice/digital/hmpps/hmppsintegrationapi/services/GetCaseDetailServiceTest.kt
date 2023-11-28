package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseDetail

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCaseDetailForCtrlOService::class],
)
internal class GetCaseDetailServiceTest(
  @MockBean val nDeliusGateway: NDeliusGateway,
  private val getCaseDetailForCtrlOService: GetCaseDetailForCtrlOService,
) : DescribeSpec(
  {
    val hmppsId = "X123456"
    val eventNumber = 1234
    val caseDetail = CaseDetail(nomsId = "ABC123")

    beforeEach {
      Mockito.reset(nDeliusGateway)

      whenever(nDeliusGateway.getCaseDetailForPerson(hmppsId, eventNumber)).thenReturn(
        Response(
          data = caseDetail,
        ),
      )
    }

    it("Returns a list of supervisions for a probationer according to the provided Delius CRN") {
      val result = getCaseDetailForCtrlOService.execute(hmppsId, eventNumber)

      result.shouldBe(Response(data = caseDetail))
    }
  },
)
