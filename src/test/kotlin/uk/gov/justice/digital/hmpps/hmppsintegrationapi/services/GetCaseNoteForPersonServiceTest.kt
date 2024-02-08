package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCaseNoteForPersonService::class],
)
class GetCaseNoteForPersonServiceTest(
  @MockBean val mockCaseNotesGateway: CaseNotesGateway,
  @MockBean val mockGetPersonService: GetPersonService,
  private val getCaseNoteForPersonService: GetCaseNoteForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val person = Person(firstName = "Julianna", lastName = "Blake", identifiers = Identifiers(nomisNumber = nomisNumber))
    val caseNotes =
      PageCaseNote(
        caseNotes =
        listOf(
          CaseNote(
            caseNoteId = "12345ABC",
          ),
        ),
      )

    beforeEach {
      Mockito.reset(mockGetPersonService)
      Mockito.reset(mockCaseNotesGateway)

      whenever(mockGetPersonService.execute(hmppsId = hmppsId)).thenReturn(Response(person))
      whenever(mockCaseNotesGateway.getCaseNotesForPerson(id = nomisNumber)).thenReturn(Response(caseNotes))
    }
  },
)
