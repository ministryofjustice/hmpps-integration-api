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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
internal class GetPersonServiceTest(
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  private val getPersonService: GetPersonService,
) : DescribeSpec({
  val pncId = "2003/13116M"

  beforeEach {
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)

    whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
      Response(data = listOf(Person(firstName = "Qui-gon", lastName = "Jin", prisonerId = "A1234AA"))),
    )
    whenever(probationOffenderSearchGateway.getPerson(pncId = pncId)).thenReturn(
      Response(data = Person(firstName = "Qui-gon", lastName = "Jin", prisonerId = "A1234AA")),
    )
  }

  it("retrieves a person from Prisoner Offender Search") {
    getPersonService.execute(pncId)

    verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(pncId = pncId)
  }

  it("retrieves a person from Probation Offender Search") {
    getPersonService.execute(pncId)

    verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(pncId)
  }

  it("returns a person") {
    val personFromPrisonerOffenderSearch = Person("Sally", "Sob")
    val personFromProbationOffenderSearch = Person("Molly", "Mob")

    whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(
      Response(data = listOf(personFromPrisonerOffenderSearch)),
    )
    whenever(probationOffenderSearchGateway.getPerson(pncId)).thenReturn(
      Response(personFromProbationOffenderSearch),
    )

    val result = getPersonService.execute(pncId)

    val expectedResult = mapOf(
      "prisonerOffenderSearch" to Person("Sally", "Sob"),
      "probationOffenderSearch" to Person("Molly", "Mob"),
    )

    result.data.shouldBe(expectedResult)
  }

  it("returns null when a person isn't found in any APIs") {
    whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(Response(data = emptyList()))
    whenever(probationOffenderSearchGateway.getPerson(pncId = pncId)).thenReturn(Response(data = null))

    val result = getPersonService.execute(pncId)
    val expectedResult = mapOf(
      "prisonerOffenderSearch" to null,
      "probationOffenderSearch" to null,
    )

    result.data.shouldBe(expectedResult)
  }

  it("returns no results from prisoner offender search, but one from probation offender search") {
    val personFromProbationOffenderSearch = Person("Molly", "Mob")

    whenever(prisonerOffenderSearchGateway.getPersons(pncId = pncId)).thenReturn(Response(data = emptyList()))
    whenever(probationOffenderSearchGateway.getPerson(pncId)).thenReturn(Response(personFromProbationOffenderSearch))

    val expectedResult = mapOf(
      "prisonerOffenderSearch" to null,
      "probationOffenderSearch" to Person("Molly", "Mob"),
    )

    val result = getPersonService.execute(pncId)

    result.data.shouldBe(expectedResult)
  }
},)
