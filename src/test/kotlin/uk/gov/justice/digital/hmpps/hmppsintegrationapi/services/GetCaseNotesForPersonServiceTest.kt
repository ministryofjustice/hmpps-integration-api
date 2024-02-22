package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCaseNotesForPersonService::class],
)
class GetCaseNotesForPersonServiceTest(
  @MockBean val caseNotesGateway: CaseNotesGateway,
  @MockBean val getPersonService: GetPersonService,
  private val getCaseNoteForPersonService: GetCaseNotesForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val person = Person(firstName = "Julianna", lastName = "Blake", identifiers = Identifiers(nomisNumber = nomisNumber))
    val caseNotes =
      listOf(
        CaseNote(
          caseNoteId = "12345ABC",
        ),
      )

    beforeEach {
      Mockito.reset(getPersonService)
      Mockito.reset(caseNotesGateway)

      whenever(getPersonService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
      whenever(caseNotesGateway.getCaseNotesForPerson(id = nomisNumber, CaseNoteFilter(hmppsId = hmppsId))).thenReturn(Response(caseNotes))
    }

    it("performs a search according to hmpps Id") {
      getCaseNoteForPersonService.execute(CaseNoteFilter(hmppsId = hmppsId))
      verify(getPersonService, VerificationModeFactory.times(1)).execute(hmppsId = hmppsId)
    }

    it("should return case notes from gateway") {
      val result = getCaseNoteForPersonService.execute(CaseNoteFilter(hmppsId = hmppsId))
      result.data.size.shouldBe(1)
      result.data.first().caseNoteId.shouldBe("12345ABC")
      result.errors.count().shouldBe(0)
    }
  },
)
