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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Identifiers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonService::class],
)
internal class GetPersonServiceTest(
  @MockBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @MockBean val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  private val getPersonService: GetPersonService,
) : DescribeSpec({
    val hmppsId = "2003/13116M"

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
      Mockito.reset(probationOffenderSearchGateway)

      whenever(prisonerOffenderSearchGateway.getPersons("Qui-gon", "Jin", "1966-10-25")).thenReturn(
        Response(data = listOf(Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = "A1234AA")))),
      )
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(
        Response(data = Person(firstName = "Qui-gon", lastName = "Jin", identifiers = Identifiers(nomisNumber = "A1234AA"))),
      )
      whenever(prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = "A1234AA")).thenReturn(
        Response(data = POSPrisoner(firstName = "Sam", lastName = "Mills")),
      )
    }

    it("gets a person from Probation Offender Search") {
      getPersonService.execute(hmppsId)

      verify(probationOffenderSearchGateway, VerificationModeFactory.times(1)).getPerson(hmppsId)
    }

    it("returns a person") {
      val personFromProbationOffenderSearch = Person("Molly", "Mob")

      whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(
        Response(personFromProbationOffenderSearch),
      )

      val result = getPersonService.execute(hmppsId)

      val expectedResult = Person("Molly", "Mob")

      result.data.shouldBe(expectedResult)
    }

    it("returns null when a person isn't found in probation offender search") {
      whenever(probationOffenderSearchGateway.getPerson(id = hmppsId)).thenReturn(Response(data = null))

      val result = getPersonService.execute(hmppsId)
      val expectedResult = null

      result.data.shouldBe(expectedResult)
    }

    it("returns a person with both probation and prison data when prison data exists") {
      val personFromProbationOffenderSearch = Person("Paula", "First", identifiers = Identifiers(nomisNumber = "A1234AA"))
      val personFromPrisonOffenderSearch = POSPrisoner("Sam", "Mills")

      whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(
        Response(data = personFromProbationOffenderSearch),
      )
      whenever(prisonerOffenderSearchGateway.getPrisonOffender("A1234AA")).thenReturn(
        Response(data = personFromPrisonOffenderSearch),
      )

      val result = getPersonService.getCombinedDataForPerson(hmppsId)
      val expectedResult = result.data

      result.data.shouldBe(expectedResult)
      result.errors shouldBe emptyList()
    }

    it("returns errors when unable to retrieve prison data and data when probation data is available") {
      val personFromProbationOffenderSearch = Person("Paula", "First", identifiers = Identifiers(nomisNumber = "A1234AA"))

      whenever(probationOffenderSearchGateway.getPerson(hmppsId)).thenReturn(Response(data = personFromProbationOffenderSearch))
      whenever(prisonerOffenderSearchGateway.getPrisonOffender("A1234AA")).thenReturn(Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "MockError"))))

      val result = getPersonService.getCombinedDataForPerson(hmppsId)
      result.data shouldBe
        mapOf(
          "prisonerOffenderSearch" to null,
          "probationOffenderSearch" to personFromProbationOffenderSearch,
        )
      result.errors.first().causedBy.shouldBe(UpstreamApi.PRISONER_OFFENDER_SEARCH)
      result.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      result.errors.first().description.shouldBe("MockError")
    }
  })
