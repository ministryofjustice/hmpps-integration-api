package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Supervision

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetEventsForCtrlOService::class],
)
internal class GetEventsForCtrlOServiceTest(
  @MockBean val nDeliusGateway: NDeliusGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getEventsForCtrlOService: GetEventsForCtrlOService,
) : DescribeSpec(
  {
    val nDeliusCRN = "X123456"
    val personFromProbationOffenderSearch = Person(
      firstName = "Chandler",
      lastName = "ProbationBing",
      identifiers = Identifiers(deliusCrn = nDeliusCRN),
    )

    val supervisions1 = Supervision(active = false, custodial = false)
    val supervisions2 = Supervision(active = true, custodial = false)

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nDeliusGateway)

      whenever(getPersonService.execute(hmppsId = nDeliusCRN)).thenReturn(
        Response(
          data = personFromProbationOffenderSearch,
        ),
      )

      whenever(nDeliusGateway.getSupervisionsForPerson(nDeliusCRN)).thenReturn(
        Response(
          data = listOf(
            supervisions1,
            supervisions2,
          ),
        ),
      )
    }

    it("Returns a list of supervisions for a probationer according to the provided Delius CRN") {
      whenever(getPersonService.execute(hmppsId = nDeliusCRN)).thenReturn(
        Response(
          data = personFromProbationOffenderSearch,
        ),
      )

      val result = getEventsForCtrlOService.execute(nDeliusCRN)

      result.shouldBe(Response(data = listOf(supervisions1, supervisions2)))
    }
  },
)
