package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

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
  val pncNumber = "2003/13116M"

  beforeEach {
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)

    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null)).thenReturn(Response(data = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, null, false)).thenReturn(Response(data = emptyList()))
  }

  it("gets person(s) from Prisoner Offender Search") {
    getPersonsService.execute(firstName, lastName, null)

    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName)
  }

  it("gets person(s) from Probation Offender Search") {
    getPersonsService.execute(firstName, lastName, null)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null)
  }

  it("defaults to not searching within aliases") {
    getPersonsService.execute(firstName, lastName, null)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, searchWithinAliases = false)
    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, searchWithinAliases = false)
  }

  it("allows searching within aliases") {
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null, searchWithinAliases = true)).thenReturn(Response(data = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, null, searchWithinAliases = true)).thenReturn(Response(data = emptyList()))

    getPersonsService.execute(firstName, lastName, null, true)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, true)
    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, searchWithinAliases = true)
  }

  it("allows prisonerOffenderSearchGateway to search with a hmppsId if a pncNumber is passed in") {
    val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John", identifiers = Identifiers(deliusCrn = "A1234AA"))))

    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, pncNumber)).thenReturn(responseFromProbationOffenderSearch)
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, "A1234AA")).thenReturn(Response(data = emptyList()))

    getPersonsService.execute(firstName, lastName, pncNumber)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, pncNumber)
    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, "A1234AA")
  }

  it("allows prisonerOffenderSearchGateway to not search with a hmppsId if a pncNumber is not passed in") {
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null)).thenReturn(Response(data = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, null)).thenReturn(Response(data = emptyList()))

    getPersonsService.execute(firstName, lastName, null)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null)
    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null)
  }

  it("returns person(s)") {
    val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
    val responseFromPrisonerOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "Gary")))

    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, pncNumber)).thenReturn(responseFromProbationOffenderSearch)
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, null)).thenReturn(responseFromPrisonerOffenderSearch)

    val response = getPersonsService.execute(firstName, lastName, pncNumber)

    response.data.shouldBe(responseFromPrisonerOffenderSearch.data + responseFromProbationOffenderSearch.data)
  }

  it("returns an empty list when no person(s) are found") {
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null)).thenReturn(Response(emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(Response(emptyList()))

    val response = getPersonsService.execute(firstName, lastName, null)

    response.data.shouldBe(emptyList())
  }
},)
