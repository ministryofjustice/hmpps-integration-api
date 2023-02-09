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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class]
)
internal class GetPersonServiceTest(
  @MockBean val nomisGateway: NomisGateway,
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  private val getPersonService: GetPersonService
) : DescribeSpec({
  val id = "abc123"

  beforeEach {
    Mockito.reset(nomisGateway)
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)
  }

  it("retrieves a person from NOMIS") {
    getPersonService.execute(id)

    verify(nomisGateway, VerificationModeFactory.times(1)).getPerson(id)
  }

  it("retrieves a person from Prisoner Offender Search") {
    getPersonService.execute(id)

    verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(id)
  }

  it("retrieves a person from Probation Offender Search") {
    getPersonService.execute(id)

    verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(id)
  }

  it("returns a person") {
    val personFromNomis = Person("Billy", "Bob")
    val personFromPrisonerOffenderSearch = Person("Sally", "Sob")
    val personFromProbationOffenderSearch = Person("Molly", "Mob")

    whenever(nomisGateway.getPerson(id)).thenReturn(personFromNomis)
    whenever(prisonerOffenderSearchGateway.getPerson(id)).thenReturn(personFromPrisonerOffenderSearch)
    whenever(probationOffenderSearchGateway.getPerson(id)).thenReturn(personFromProbationOffenderSearch)

    val result = getPersonService.execute(id)

    val expectedResult = mapOf(
      "nomis" to Person("Billy", "Bob"),
      "prisonerOffenderSearch" to Person("Sally", "Sob"),
      "probationOffenderSearch" to Person("Molly", "Mob")
    )

    result.shouldBe(expectedResult)
  }
})
