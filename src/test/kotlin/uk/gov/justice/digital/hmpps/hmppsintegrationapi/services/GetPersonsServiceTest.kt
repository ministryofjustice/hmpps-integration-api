package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonsService::class],
)
internal class GetPersonsServiceTest(
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockitoBean val deliusGateway: NDeliusGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val getPersonsService: GetPersonsService,
) : DescribeSpec({
    val firstName = personInProbationAndNomisPersona.firstName
    val lastName = personInProbationAndNomisPersona.lastName
    val pncNumber = "A1234AA"
    val dateOfBirth = personInProbationAndNomisPersona.dateOfBirth.toString()

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(deliusGateway)
      Mockito.reset(featureFlag)

      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(data = emptyList()))
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(data = emptyList()))
    }

    it("gets person(s) from Prisoner Offender Search") {
      getPersonsService.execute(firstName, lastName, null, dateOfBirth)

      verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth)
    }

    it("gets person(s) from Delius Gateway") {
      getPersonsService.execute(firstName, lastName, null, dateOfBirth)

      verify(deliusGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth)
    }

    it("defaults to not searching within aliases") {
      getPersonsService.execute(firstName, lastName, null, dateOfBirth)

      verify(deliusGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth, searchWithinAliases = false)
      verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth, searchWithinAliases = false)
    }

    it("allows searching within aliases") {
      whenever(
        deliusGateway.getPersons(firstName, lastName, null, dateOfBirth, searchWithinAliases = true),
      ).thenReturn(Response(data = emptyList()))
      whenever(
        prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth, searchWithinAliases = true),
      ).thenReturn(Response(data = emptyList()))

      getPersonsService.execute(firstName, lastName, null, dateOfBirth, true)

      verify(deliusGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth, true)
      verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth, true)
    }

    it("returns person(s)") {
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      val responseFromPrisonerOffenderSearch = Response(data = listOf(POSPrisoner(firstName = firstName, lastName = lastName, middleNames = "Gary", youthOffender = false)))

      whenever(
        deliusGateway.getPersons(firstName, lastName, null, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      whenever(
        prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth),
      ).thenReturn(responseFromPrisonerOffenderSearch)

      val result = getPersonsService.execute(firstName, lastName, null, dateOfBirth)
      val people = (responseFromPrisonerOffenderSearch.data.map { it.toPerson() } + responseFromProbationOffenderSearch.data)
      result.data.size.shouldBe(people.size)
      people
        .forEachIndexed { i, person: Person ->
          result.data[i].firstName.shouldBe(person.firstName)
          result.data[i].lastName.shouldBe(person.lastName)
          result.data[i].dateOfBirth.shouldBe(person.dateOfBirth)
        }
    }

    it("returns only probation person(s) if searched with a PNC") {
      val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
      val responseFromPrisonerOffenderSearch = Response(data = listOf(POSPrisoner(firstName = firstName, lastName = lastName, middleNames = "Gary", youthOffender = false)))

      whenever(
        deliusGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth),
      ).thenReturn(responseFromProbationOffenderSearch)
      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(responseFromPrisonerOffenderSearch)

      val response = getPersonsService.execute(firstName, lastName, pncNumber, dateOfBirth)

      response.data.shouldBe(responseFromProbationOffenderSearch.data)
      verify(prisonerOffenderSearchGateway, times(0)).getPersons(firstName, lastName, dateOfBirth, true)
    }

    it("returns an empty list when no person(s) are found") {
      whenever(deliusGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(emptyList()))
      whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(emptyList()))

      val response = getPersonsService.execute(firstName, lastName, null, dateOfBirth)
      response.data.shouldBe(emptyList())
    }
  })
