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
  val dateOfBirth = "2004-04-19"

  beforeEach {
    Mockito.reset(prisonerOffenderSearchGateway)
    Mockito.reset(probationOffenderSearchGateway)

    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(data = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(data = emptyList()))
  }

  it("gets person(s) from Prisoner Offender Search") {
    getPersonsService.execute(firstName, lastName, null, dateOfBirth)

    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth)
  }

  it("gets person(s) from Probation Offender Search") {
    getPersonsService.execute(firstName, lastName, null, dateOfBirth)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth)
  }

  it("defaults to not searching within aliases") {
    getPersonsService.execute(firstName, lastName, null, dateOfBirth)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth, searchWithinAliases = false)
    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth, searchWithinAliases = false)
  }

  it("allows searching within aliases") {
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null, dateOfBirth, searchWithinAliases = true)).thenReturn(Response(data = emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth, searchWithinAliases = true)).thenReturn(Response(data = emptyList()))

    getPersonsService.execute(firstName, lastName, null, dateOfBirth, true)

    verify(probationOffenderSearchGateway, times(1)).getPersons(firstName, lastName, null, dateOfBirth, true)
    verify(prisonerOffenderSearchGateway, times(1)).getPersons(firstName, lastName, dateOfBirth, true)
  }

  it("returns person(s)") {
    val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
    val responseFromPrisonerOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "Gary")))

    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(responseFromProbationOffenderSearch)
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(responseFromPrisonerOffenderSearch)

    val response = getPersonsService.execute(firstName, lastName, null, dateOfBirth)

    response.data.shouldBe(responseFromPrisonerOffenderSearch.data + responseFromProbationOffenderSearch.data)
  }

  it("returns only probation person(s) if searched with a PNC") {
    val responseFromProbationOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "John")))
    val responseFromPrisonerOffenderSearch = Response(data = listOf(Person(firstName, lastName, middleName = "Gary")))

    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, pncNumber, dateOfBirth)).thenReturn(responseFromProbationOffenderSearch)
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(responseFromPrisonerOffenderSearch)

    val response = getPersonsService.execute(firstName, lastName, pncNumber, dateOfBirth)

    response.data.shouldBe(responseFromProbationOffenderSearch.data)
    verify(prisonerOffenderSearchGateway, times(0)).getPersons(firstName, lastName, dateOfBirth, true)
  }

  it("returns an empty list when no person(s) are found") {
    whenever(probationOffenderSearchGateway.getPersons(firstName, lastName, null, dateOfBirth)).thenReturn(Response(emptyList()))
    whenever(prisonerOffenderSearchGateway.getPersons(firstName, lastName, dateOfBirth)).thenReturn(Response(emptyList()))

    val response = getPersonsService.execute(firstName, lastName, null, dateOfBirth)
    response.data.shouldBe(emptyList())
  }
},)
