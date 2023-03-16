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

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonsService::class],
)
internal class GetPersonsServiceTest(
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  private val getPersonsService: GetPersonsService,
) : DescribeSpec({
  val firstName = "Bruce"
  val lastName = "Wayne"

  beforeEach {
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)
  }

  it("returns person(s) from Prisoner Offender Search") {
    getPersonsService.execute(firstName, lastName)

    verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(firstName, lastName)
  }

  it("returns person(s) from Probation Offender Search") {
    getPersonsService.execute(firstName, lastName)

    verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(firstName, lastName)
  }

  it("returns person(s)") {
    val personsFromPrisonerOffenderSearch = listOf(Person(firstName, lastName, middleName = "Gary"))
    val personsFromProbationOffenderSearch = listOf(Person(firstName, lastName, middleName = "John"))

    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(personsFromPrisonerOffenderSearch)
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(personsFromProbationOffenderSearch)

    val result = getPersonsService.execute(firstName, lastName)

    result.shouldBe(personsFromPrisonerOffenderSearch + personsFromProbationOffenderSearch)
  }

  it("returns an empty list when no person(s) are found") {
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(emptyList())
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(emptyList())

    val result = getPersonsService.execute(firstName, lastName)
    result.shouldBe(emptyList())
  }
},)
