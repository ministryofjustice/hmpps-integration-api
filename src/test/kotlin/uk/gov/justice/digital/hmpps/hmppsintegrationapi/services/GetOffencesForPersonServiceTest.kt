package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetOffencesForPersonService::class],
)
internal class GetOffencesForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getOffencesForPersonService: GetOffencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"

    beforeEach {
      Mockito.reset(nomisGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(data = listOf(Person(firstName = "Chandler", lastName = "Bing", prisonerId = prisonerNumber))),
      )

      whenever(nomisGateway.getOffencesForPerson(prisonerNumber)).thenReturn(
        Response(
          data = listOf(
            Offence(code = "RR12345", description = "First Offence", date = LocalDate.parse("2020-02-03")),
            Offence(code = "RR54321", description = "Second Offence", date = LocalDate.parse("2021-03-04")),
            Offence(code = "RR24680", description = "Third Offence", date = LocalDate.parse("2022-04-05")),
          ),
        ),
      )
    }

    it("retrieves prisoner ID from Prisoner Offender Search using a PNC ID") {
      getOffencesForPersonService.execute(pncId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
    }

    it("retrieves offences for a person from NOMIS using prisoner number") {
      getOffencesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getOffencesForPerson(prisonerNumber)
    }

    it("returns all offences for a person") {
      val response = getOffencesForPersonService.execute(pncId)

      response.data.shouldHaveSize(3)
    }

    it("returns an error when person cannot be found in NOMIS") {
      whenever(nomisGateway.getOffencesForPerson(prisonerNumber)).thenReturn(
        Response(
          data = emptyList(),
          errors = listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.NOMIS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        ),
      )

      val response = getOffencesForPersonService.execute(pncId)

      response.errors.shouldHaveSize(1)
    }
  },
)
