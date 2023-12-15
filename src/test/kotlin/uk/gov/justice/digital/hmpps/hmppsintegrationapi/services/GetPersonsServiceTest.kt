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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

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

    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(Response(data = emptyList()))
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(Response(data = emptyList()))
  }

  it("gets person(s) from Prisoner Offender Search") {
    getPersonsService.execute(firstName, lastName)

    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName)
  }

  it("gets person(s) from Probation Offender Search") {
    getPersonsService.execute(firstName, lastName)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName)
  }

  it("defaults to not searching within aliases") {
    getPersonsService.execute(firstName, lastName)

    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, searchWithinAliases = false)
    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, searchWithinAliases = false)
  }

  it("allows searching within aliases") {
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, searchWithinAliases = true)).thenReturn(Response(data = emptyList()))
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, searchWithinAliases = true)).thenReturn(Response(data = emptyList()))

    getPersonsService.execute(firstName, lastName, searchWithinAliases = true)

    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, searchWithinAliases = true)
    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, searchWithinAliases = true)
  }

  it("returns person(s)") {
    val responseFromPrisonerOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "Gary")))
    val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))

    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(responseFromPrisonerOffenderSearch)
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(responseFromProbationOffenderSearch)

    val response = getPersonsService.execute(firstName, lastName)

    response.data.shouldBe(responseFromPrisonerOffenderSearch.data + responseFromProbationOffenderSearch.data)
  }

  it("returns an empty list when no person(s) are found") {
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(Response(emptyList()))
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName)).thenReturn(Response(emptyList()))

    val response = getPersonsService.execute(firstName, lastName)

    response.data.shouldBe(emptyList())
  }
},)
