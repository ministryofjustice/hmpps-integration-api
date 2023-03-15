package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
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
  val pncId = "2003/13116M"

  beforeEach {
    Mockito.reset(nomisGateway)
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)
  }

  it("retrieves a person from NOMIS") {
    getPersonService.execute(pncId)

    verify(nomisGateway, VerificationModeFactory.times(1)).getPerson(pncId)
  }

  it("retrieves a person from Prisoner Offender Search") {
    getPersonService.execute(pncId)

    verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(pncId)
  }

  it("retrieves a person from Probation Offender Search") {
    getPersonService.execute(pncId)

    verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(pncId)
  }

  it("returns a person") {
    val personFromNomis = Person("Billy", "Bob")
    val personFromPrisonerOffenderSearch = Person("Sally", "Sob")
    val personFromProbationOffenderSearch = Person("Molly", "Mob")

    whenever(nomisGateway.getPerson(pncId)).thenReturn(personFromNomis)
    whenever(prisonerOffenderSearchGateway.getPerson(pncId)).thenReturn(personFromPrisonerOffenderSearch)
    whenever(probationOffenderSearchGateway.getPerson(pncId)).thenReturn(personFromProbationOffenderSearch)

    val result = getPersonService.execute(pncId)

    val expectedResult = mapOf(
      "nomis" to Person("Billy", "Bob"),
      "prisonerOffenderSearch" to Person("Sally", "Sob"),
      "probationOffenderSearch" to Person("Molly", "Mob")
    )

    result.shouldBe(expectedResult)
  }

  it("returns null when a person isn't found in any APIs") {
    whenever(nomisGateway.getPerson(pncId)).thenReturn(null)
    whenever(prisonerOffenderSearchGateway.getPerson(pncId)).thenReturn(null)
    whenever(probationOffenderSearchGateway.getPerson(pncId)).thenReturn(null)

    val result = getPersonService.execute(pncId)

    result.shouldBeNull()
  }
})
