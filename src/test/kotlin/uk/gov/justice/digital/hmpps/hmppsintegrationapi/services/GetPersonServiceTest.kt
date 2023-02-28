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

  beforeEach {
    Mockito.reset(nomisGateway)
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)
  }

  describe("by search criteria") {
    val firstName = "Bruce"
    val lastName = "Wayne"

    it("returns person(s) from Prisoner Offender Search") {
      getPersonService.execute(firstName, lastName)

      verify(prisonerOffenderSearchGateway, VerificationModeFactory.times(1)).getPrisoners(firstName, lastName)
    }

    it("returns person(s) from Probation Offender Search") {
      getPersonService.execute(firstName, lastName)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPersons(firstName, lastName)
    }

    it("returns person(s)") {
      val personsFromPrisonerOffenderSearch = listOf(
        Person("Bruce", "Wayne", middleName = "Gary")
      )
      val personsFromProbationOffenderSearch = listOf(
        Person("Bruce", "Wayne", middleName = "John")
      )

      whenever(prisonerOffenderSearchGateway.getPrisoners(firstName, lastName)).thenReturn(personsFromPrisonerOffenderSearch)
      whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(personsFromProbationOffenderSearch)

      val expectedResult = listOf(
        Person(firstName, lastName, middleName = "Gary"),
        Person(firstName, lastName, middleName = "John")
      )

      val result = getPersonService.execute(firstName, lastName)
      result.shouldBe(expectedResult)
    }

    it("returns an empty list when no person(s) are found") {
      whenever(prisonerOffenderSearchGateway.getPrisoners(firstName, lastName)).thenReturn(emptyList())
      whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(emptyList())

      val result = getPersonService.execute(firstName, lastName)
      result.shouldBe(emptyList())
    }
  }

  describe("by id") {
    val id = "abc123"

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

    it("returns null when a person isn't found in any APIs") {
      whenever(nomisGateway.getPerson(id)).thenReturn(null)
      whenever(prisonerOffenderSearchGateway.getPerson(id)).thenReturn(null)
      whenever(probationOffenderSearchGateway.getPerson(id)).thenReturn(null)

      val result = getPersonService.execute(id)

      result.shouldBeNull()
    }
  }
})
