package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisAccounts

class GetBalancesForPersonServiceTestpackage uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetOffencesForPersonService::class],
)
internal class GetBalancesForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getBalancesForPersonService: GetBalancesForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"
    val prisonId = "ABC"

val personFromPrisonOffenderSearch =
      Person(
        firstName = "Chandler",
        lastName = "ProbationBing",
        identifiers = Identifiers(nomisNumber = prisonerNumber),
      )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(nomisGateway)

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
        Response(
          data = personFromPrisonOffenderSearch,
        ),
      )

      whenever(nomisGateway.getAccountsForPerson(prisonId, prisonerNumber)).thenReturn(
        Response(
          data =
            NomisAccounts(spends = 100, savings = 100, cash = 100)
        ),
      )

    }


    it("Returns a persons account balances given a hmppsId") {
      whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
        Response(
          data = personFromPrisonOffenderSearch,
        ),
      )

      val result = getBalancesForPersonService.execute(hmppsId)

      result.shouldBe(
        Response(data = listOf(prisonOffence1, prisonOffence2, prisonOffence3, probationOffence1, probationOffence2, probationOffence3)),
      )
    }
//    it("gets a person using a Hmpps ID") {
//      getOffencesForPersonService.execute(hmppsId)
//
//      verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
//    }
//
//    it("gets offences from NOMIS using a prisoner number") {
//      getOffencesForPersonService.execute(hmppsId)
//
//      verify(nomisGateway, VerificationModeFactory.times(1)).getOffencesForPerson(prisonerNumber)
//    }
//
//    it("gets offences from nDelius using a CRN") {
//      getOffencesForPersonService.execute(hmppsId)
//
//      verify(nDeliusGateway, VerificationModeFactory.times(1)).getOffencesForPerson(nDeliusCRN)
//    }
//
//    it("combines and returns offences from Nomis and nDelius") {
//      val response = getOffencesForPersonService.execute(hmppsId)
//
//      response.data.shouldBe(
//        listOf(
//          prisonOffence1,
//          prisonOffence2,
//          prisonOffence3,
//          probationOffence1,
//          probationOffence2,
//          probationOffence3,
//        ),
//      )
//    }
//
//    describe("when an upstream API returns an error when looking up a person from a Hmpps ID") {
//      beforeEach {
//        whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(
//          Response(
//            data = null,
//            errors =
//              listOf(
//                UpstreamApiError(
//                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
//                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                ),
//                UpstreamApiError(
//                  causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
//                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//                ),
//              ),
//          ),
//        )
//      }
//
//      it("records upstream API errors") {
//        val response = getOffencesForPersonService.execute(hmppsId)
//        response.errors.shouldHaveSize(2)
//      }
//
//      it("does not get offences from Nomis") {
//        getOffencesForPersonService.execute(hmppsId)
//        verify(nomisGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = prisonerNumber)
//      }
//
//      it("does not get offences from nDelius") {
//        getOffencesForPersonService.execute(hmppsId)
//        verify(nDeliusGateway, VerificationModeFactory.times(0)).getOffencesForPerson(id = nDeliusCRN)
//      }
//    }
//
//    it("records errors when it cannot find offences for a person") {
//      whenever(nDeliusGateway.getOffencesForPerson(id = nDeliusCRN)).thenReturn(
//        Response(
//          data = emptyList(),
//          errors =
//            listOf(
//              UpstreamApiError(
//                causedBy = UpstreamApi.NDELIUS,
//                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//              ),
//            ),
//        ),
//      )
//
//      whenever(nomisGateway.getOffencesForPerson(id = prisonerNumber)).thenReturn(
//        Response(
//          data = emptyList(),
//          errors =
//            listOf(
//              UpstreamApiError(
//                causedBy = UpstreamApi.NOMIS,
//                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
//              ),
//            ),
//        ),
//      )
//
//      val response = getOffencesForPersonService.execute(hmppsId)
//      response.errors.shouldHaveSize(2)
//    }
//  },
//)
{
}
