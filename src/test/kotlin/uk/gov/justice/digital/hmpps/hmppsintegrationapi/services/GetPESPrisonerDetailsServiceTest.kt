package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonIntegrationpes.PESPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPESPrisonerDetailsService::class],
)
internal class GetPESPrisonerDetailsServiceTest(
  @MockBean val prisonerSearchGateway: PrisonerOffenderSearchGateway,
  private val getPESPrisonerDetailsService: GetPESPrisonerDetailsService,
) : DescribeSpec(
    {
      val hmppsId = "X123456"
      val expectedResult = PESPrisonerDetails(prisonerNumber = "X123456", "Molly", lastName = "Mob", prisonId = "LEI", prisonName = "HMP Leeds", cellLocation = "6-2-006")

      beforeEach {
        Mockito.reset(prisonerSearchGateway)

        whenever(prisonerSearchGateway.getPrisonOffender(hmppsId)).thenReturn(
          Response(
            data = POSPrisoner(prisonerNumber = hmppsId, firstName = "Molly", lastName = "Mob", prisonId = "LEI", prisonName = "HMP Leeds", cellLocation = "6-2-006"),
          ),
        )
      }

      it("Returns a prisoner name record according to the provided HMPPS ID") {
        val result = getPESPrisonerDetailsService.execute(hmppsId)

        result.shouldBe(Response(data = expectedResult))
      }
    },
  )
