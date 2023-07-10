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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetSentencesForPersonService::class],
)
internal class GetSentencesForPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getSentencesForPersonService: GetSentencesForPersonService,
) : DescribeSpec(
  {
    val pncId = "1234/56789B"
    val prisonerNumber = "Z99999ZZ"

    beforeEach {
      Mockito.reset(nomisGateway)

      whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
        Response(
          data = listOf(
            Person(
              firstName = "Chandler",
              lastName = "Bing",
              identifiers = Identifiers(nomisNumber = prisonerNumber),
            ),
          ),
        ),
      )

      whenever(nomisGateway.getSentencesForPerson(prisonerNumber)).thenReturn(
        Response(
          data = listOf(
            Sentence(
              startDate = LocalDate.parse("2020-03-03"),
            ),
            Sentence(
              startDate = LocalDate.parse("2016-01-01"),
            ),
          ),
        ),
      )
    }

    it("retrieves prisoner ID from Prisoner Offender Search using a PNC ID") {
      getSentencesForPersonService.execute(pncId)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
    }

    it("retrieves sentences for a person from NOMIS using prisoner number") {
      getSentencesForPersonService.execute(pncId)

      verify(nomisGateway, VerificationModeFactory.times(1)).getSentencesForPerson(prisonerNumber)
    }

    it("returns all sentences for a person") {
      val response = getSentencesForPersonService.execute(pncId)!!

      response.data.shouldHaveSize(2)
    }

    it("returns an error when person cannot be found in NOMIS") {
      whenever(nomisGateway.getSentencesForPerson(prisonerNumber)).thenReturn(
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

      val response = getSentencesForPersonService.execute(pncId)!!

      response.errors.shouldHaveSize(1)
    }
  },
)
